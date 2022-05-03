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

package fr.linkit.engine.gnom.persistence.serializor.write

import fr.linkit.api.gnom.cache.sync.SynchronizedObject
import fr.linkit.api.gnom.cache.sync.contract.description.SyncClassDef
import fr.linkit.api.gnom.cache.sync.invocation.InvocationChoreographer
import fr.linkit.api.gnom.persistence.obj.{MirroringPoolObject, ReferencedPoolObject}
import fr.linkit.api.gnom.persistence.{Freezable, PersistenceBundle}
import fr.linkit.engine.gnom.network.DefaultEngine
import fr.linkit.engine.gnom.persistence.obj.PoolChunk
import fr.linkit.engine.gnom.persistence.serializor.ConstantProtocol._
import fr.linkit.engine.gnom.persistence.serializor.{ArrayPersistence, PacketPoolTooLongException}
import fr.linkit.engine.internal.mapping.ClassMappings

import java.nio.ByteBuffer
import scala.annotation.switch

class ObjectWriter(bundle: PersistenceBundle) extends Freezable {

    val buff: ByteBuffer = bundle.buff
    private final val boundClassMappings = bundle.network.findEngine(bundle.boundId).flatMap(_.asInstanceOf[DefaultEngine].classMappings).orNull
    private       val config             = bundle.config
    private var widePacket               = config.widePacket
    private       val pool               = new SerializerObjectPool(bundle)

    def addObjects(roots: Array[AnyRef]): Unit = {
        val pool = this.pool
        //Registering all objects, and contained objects in the constant pool.
        for (o <- roots) {
            pool.addObject(o)
        }
    }

    override def freeze(): Unit = pool.freeze()

    override def isFrozen: Boolean = pool.isFrozen

    /**
     * Will writes the chunks contained in the pool
     * This is a terminal action:
     */
    def writePool(): Unit = {
        if (pool.isFrozen)
            throw new IllegalStateException("Pool is frozen.")
        pool.freeze() //Ensure that the pool is no more susceptible to be updated.

        widePacket = widePacket || pool.size > java.lang.Short.MAX_VALUE

        //Informs the deserializer if we send a wide packet or not.
        //This means that for wide packets, pool references index are ints, and
        //for "non wide packets", references index are unsigned shorts
        buff.put((if (widePacket) 1 else 0): Byte)
        //Write the content
        writeChunks()
    }

    @inline
    private def writeChunks(): Unit = {
        //println(s"Write chunks : ${buff.array().mkString(", ")}")
        //let a hole for placing in chunk sizes

        val chunks    = pool.getChunks
        var totalSize = 0

        val announcedChunksPos = buff.position()
        buff.position(announcedChunksPos + 8)

        //Announcing what chunk has been used by the packet and writing their sizes in the buff
        var announcedChunksNumber: Long = 0x0000 //0b00000000
        var i                           = 0
        while (i < ChunkCount) {
            val chunk = chunks(i)
            val size  = chunk.size
            if (size > 0) {
                totalSize += size
                //Tag's announcement mark is appended to the announced chunks number
                announcedChunksNumber |= (1 << chunk.tag)

                putRef(size) //append the size of the chunk
            }
            i += 1
        }
        buff.putLong(announcedChunksPos, announcedChunksNumber)
        i = 0
        while (i < ChunkCount) {
            val chunk = chunks(i)
            val size  = chunk.size
            if (size > 0) {
                writeChunk(i.toByte, chunk)
            }
            i += 1
        }
        //just here to check if the total pool size is not larger than Char.MaxValue if (widePacket == false)
        //or if the total size is not larger than Int.MaxValue (if widePacket == true)
        //else throw PacketPoolTooLongException.
        if ((totalSize > scala.Char.MaxValue && !widePacket) || totalSize > scala.Int.MaxValue)
            throw new PacketPoolTooLongException(s"Packet total items size exceeded available size (total size: $totalSize, widePacket: $widePacket)")
    }

    private def writeChunk(flag: Byte, poolChunk: PoolChunk[Any]): Unit = {
        val size = poolChunk.size
        //Write content
        if (flag >= Int && flag < Char) {
            ArrayPersistence.writePrimitiveArrayContent(this, poolChunk.array, flag, 0, size)
            return
        }

        @inline
        def foreach[T](@inline action: T => Unit): Unit = {
            val items = poolChunk.array.asInstanceOf[Array[T]]
            var i     = 0
            while (i < size) {
                val item = items(i)
                //println(s"Writing item $item (pos: ${buff.position()})")
                action(item)
                //println(s"Item Written! (pos: ${buff.position()})")
                i += 1
            }
        }

        (flag: @switch) match {
            case Class  => foreach[Class[_]](writeClass)
            case SyncDef => foreach[SyncClassDef](writeSyncClassDef)
            case String          => foreach[String](putString)
            case Enum              => foreach[Enum[_]](putEnum)
            case Array             => foreach[AnyRef](xs => ArrayPersistence.writeArray(this, xs))
            case Object            => foreach[SimpleObject](writeObject)
            case Lambda            => foreach[SimpleLambdaObject](writeLambdaObject)
            case RNO               => foreach[ReferencedPoolObject](obj => putRef(obj.referenceIdx))
            case Mirroring         => foreach[MirroringPoolObject](writeMirroringObject)
        }
    }

    def getPool: SerializerObjectPool = pool

    private def writeSyncClassDef(syncDef: SyncClassDef): Unit = {

    }

    private def writeMirroringObject(rpo: MirroringPoolObject): Unit = {
        writeClass(rpo.stubClass)
        putRef(rpo.referenceIdx)
    }

    private def writeClass(clazz: Class[_]): Unit = {
        val code = ClassMappings.codeOfClass(clazz)
        buff.putInt(code)
        if (boundClassMappings != null && !boundClassMappings.isClassCodeMapped(code)) InvocationChoreographer.ensinv {
            boundClassMappings.addClassToMap(clazz.getName)
        }
    }

    private def putEnum(enum: Enum[_]): Unit = {
        putTypeRef(enum.getClass, forceSyncClass = false)
        putString(enum.name())
    }

    private def putString(str: String): Unit = {
        buff.putInt(str.length).put(str.getBytes())
    }

    @inline
    private def writeLambdaObject(poolObj: SimpleLambdaObject): Unit = {
        val rep = poolObj.representation
        writeObject(rep.getClass, rep.isInstanceOf[SynchronizedObject[_]], poolObj.representationDecomposed)
    }

    @inline
    private def writeObject(poolObj: SimpleObject): Unit = {
        writeObject(poolObj.valueClass, poolObj.isSync, poolObj.decomposed)
    }

    private def writeObject(objectType: Class[_], isSyncClass: Boolean, decomposed: Array[Any]): Unit = {
        //writing object's class
        putTypeRef(objectType, isSyncClass)
        //writing object content
        ArrayPersistence.writeArrayContent(this, decomposed)
    }

    @inline
    def putRef(ref: Int): Unit = {
        if (ref < 0)
            throw new IndexOutOfBoundsException(s"Could not write negative reference index into buffer.")
        if (widePacket) buff.putInt(ref)
        else buff.putChar(ref.toChar)
    }

    @inline
    def putPoolRef(obj: Any): Unit = {
        val idx = pool.globalPosition(obj)
        putRef(idx)
    }

    def putTypeRef(tpe: Class[_], forceSyncClass: Boolean): Unit = {
        if (forceSyncClass) {
            putGeneratedTypeRef(tpe)
            return
        }
        val idx = pool.getChunkFromFlag(Class).indexOf(tpe)
        if (idx == -1) {
            putGeneratedTypeRef(tpe)
            return
        }
        putRef(idx)
    }

    private def putGeneratedTypeRef(clazz: Class[_]): Unit = {
        val size = pool.getChunkFromFlag(Class).size
        val idx  = pool.getChunkFromFlag(SyncDef).indexOf(clazz) + size
        putRef(idx)
    }

}
