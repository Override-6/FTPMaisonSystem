/*
 * Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR FILE HEADERS.
 *
 * This code is free software; you can only use it for personal uses, studies or documentation.
 * You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 * ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 * For any professional use, please contact me at overridelinkit@gmail.com.
 *
 * Please contact overridelinkit@gmail.com if you need additional information or have any
 * questions.
 */

package fr.linkit.engine.gnom.persistence.serializor.read

import fr.linkit.api.gnom.cache.sync.SynchronizedObject
import fr.linkit.api.gnom.cache.sync.generation.SyncClassCenter
import fr.linkit.api.gnom.persistence.PersistenceBundle
import fr.linkit.api.gnom.persistence.context.ControlBox
import fr.linkit.api.gnom.persistence.obj.{InstanceObject, PoolObject, ReferencedNetworkObject}
import fr.linkit.api.gnom.reference.NetworkObjectReference
import fr.linkit.engine.gnom.persistence.UnexpectedObjectException
import fr.linkit.engine.gnom.persistence.context.SimpleControlBox
import fr.linkit.engine.gnom.persistence.obj.ObjectSelector
import fr.linkit.engine.gnom.persistence.serializor.ConstantProtocol._
import fr.linkit.engine.gnom.persistence.serializor.{ArrayPersistence, ClassNotMappedException}
import fr.linkit.engine.internal.mapping.ClassMappings

import java.lang.invoke.SerializedLambda
import java.lang.reflect.{Array => RArray}
import java.nio.ByteBuffer
import scala.annotation.switch
import scala.reflect.ClassTag

class ObjectReader(bundle: PersistenceBundle, center: SyncClassCenter) {

    final         val buff: ByteBuffer                   = bundle.buff
    private final val selector                           = new ObjectSelector(bundle)
    private final val config                             = bundle.config
    private       val (widePacket: Boolean, sizes, pool) = readPoolStructure()
    private var isInit                                   = false
    val controlBox: ControlBox = new SimpleControlBox()

    def readAndInit(): Unit = {
        if (isInit)
            throw new IllegalStateException("This reader is already initialised.")
        isInit = true
        var i: Byte = 0
        //println(s"Read chunks : ${buff.array().mkString(", ")}")
        while (i < ChunkCount) {
            val size = sizes(i)
            if (size > 0) {
                //println(s"Read chunk. pos of ${i} = ${buff.position()}")
                readNextChunk(size, i)
                //println(s"End Read chunk. end pos of ${i} = ${buff.position()}")
            }
            i = (i + 1).toByte
        }
    }

    def getPool: DeserializerObjectPool = pool

    @inline
    def readNextRef: Int = {
        if (widePacket) buff.getInt() else buff.getChar()
    }

    private def readNextChunk(size: Int, flag: Byte): Unit = {
        if (flag >= Int && flag <= Char) {
            val chunk   = pool.getChunkFromFlag[Any](flag)
            val content = ArrayPersistence.readPrimitiveArray(size, flag, this)
            System.arraycopy(content, 0, chunk.array, 0, RArray.getLength(content))
            return
        }

        @inline
        def collectAndUpdateChunk[T: ClassTag](@inline action: => T): Unit = {
            var i     = 0
            val chunk = pool.getChunkFromFlag[Any](flag)
            chunk.resetPos()
            while (i < size) {
                //println(s"reading item (type: $flag, pos: ${buff.position()})")
                val item: T = action
                //println(s"Item read ! (type: $flag, pos: ${buff.position()})")
                chunk.add(item)
                i += 1
            }
        }

        (flag: @switch) match {
            case Class     => collectAndUpdateChunk[Class[_]](readClass())
            case SyncClass => collectAndUpdateChunk[Class[AnyRef with SynchronizedObject[AnyRef]]](center.getSyncClass(readClass())) //would compile the class if it's Sync version does not exists on this engine
            case Enum      => collectAndUpdateChunk[Enum[_]](readEnum())
            case String    => collectAndUpdateChunk[String](readString())
            case Array     => collectAndUpdateChunk[PoolObject[_ <: AnyRef]](ArrayPersistence.readArray(this))
            case Object    => collectAndUpdateChunk[NotInstantiatedObject[_]](readObject())
            case Lambda    => collectAndUpdateChunk[NotInstantiatedLambdaObject](readLambdaObject())
            case RNO       => collectAndUpdateChunk[ReferencedNetworkObject](readContextObject())
        }
    }

    private def readContextObject(): ReferencedNetworkObject = {
        val poolLoc = readNextRef
        new ReferencedNetworkObject {
            override      val locationIdx: Int                    = poolLoc
            override lazy val location   : NetworkObjectReference = pool
                    .getChunkFromFlag[InstanceObject[AnyRef]](Object)
                    .get(poolLoc)
                    .value match {
                case l: NetworkObjectReference => l
                case o                         => throw new UnexpectedObjectException(s"Received object '$o' which seems to be used as a network reference location, but does not extends NetworkReferenceLocation.")
            }

            override lazy val value: AnyRef = {
                val loc = location
                selector.findObject(loc).getOrElse {
                    throw new NoSuchElementException(s"Could not find network object referenced at $loc.")
                }
            }
        }
    }

    private def readClass(): Class[_] = {
        val code  = buff.getInt
        val clazz = ClassMappings.getClass(code)
        if (clazz == null)
            throw new ClassNotMappedException(s"No class is bound to code $code")
        clazz
    }

    private def readEnum[T <: Enum[T]](): Enum[T] = {
        val tpe  = pool.getType(readNextRef)
        val name = readString()
        java.lang.Enum.valueOf[T](tpe.asInstanceOf[Class[T]], name)
    }

    private def readPoolStructure(): (Boolean, Array[Int], DeserializerObjectPool) = {
        val widePacket = buff.get() == 1
        val sizes      = new Array[Int](ChunkCount)
        var i          = 0
        while (i < ChunkCount) {
            sizes(i) = readNextRef
            i += 1
        }
        (widePacket, sizes, new DeserializerObjectPool(sizes))
    }

    private def readLambdaObject(): NotInstantiatedLambdaObject = {
        val nio = readObject().asInstanceOf[NotInstantiatedObject[SerializedLambda]]
        new NotInstantiatedLambdaObject(nio)
    }

    private def readObject(): NotInstantiatedObject[AnyRef] = {
        val classRef    = readNextRef
        val clazz       = pool.getType(classRef)
        val profile     = config.getProfile[AnyRef](clazz)
        val contentSize = buff.getInt
        val content     = readObjectContent(contentSize)
        new NotInstantiatedObject[AnyRef](profile, clazz, content, controlBox, selector, pool)
    }

    private def readObjectContent(length: Int): Array[Int] = {
        var i       = 0
        val content = new Array[Int](length)
        while (i < length) {
            content(i) = readNextRef
            i += 1
        }
        content
    }

    private def readString(): String = {
        val size  = buff.getInt()
        val array = new Array[Byte](size)
        buff.get(array)
        new String(array)
    }
}
