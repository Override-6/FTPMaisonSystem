/*
 *  Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can only use it for personal uses, studies or documentation.
 *  You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 *  ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 *
 *  Please contact maximebatista18@gmail.com if you need additional information or have any
 *  questions.
 */

package fr.linkit.engine.connection.packet.persistence.v3.serialisation

import fr.linkit.api.connection.packet.persistence.v3.PersistenceContext
import fr.linkit.api.connection.packet.persistence.v3.serialisation.node.SerializerNode
import fr.linkit.api.connection.packet.persistence.v3.serialisation.{SerialisationOutputStream, SerialisationProgression}
import fr.linkit.engine.connection.packet.persistence.v3.helper.ArrayPersistence
import fr.linkit.engine.connection.packet.persistence.v3.serialisation.SerializerNodeFlags._
import fr.linkit.engine.local.utils.NumberSerializer

import java.nio.ByteBuffer

class DefaultSerialisationOutputStream(override val buff: ByteBuffer,
                                       progress: SerialisationProgression,
                                       context: PersistenceContext) extends SerialisationOutputStream {

    override def writeObject(obj: Any): SerializerNode = progress.checkNode(obj) { out =>
        context.getSerializationNode(obj, out, progress)
    }

    override def writeClass(clazz: Class[_]): Unit = buff.putInt(clazz.getName.hashCode)

    override def writePrimitive(anyVal: AnyVal): SerializerNode = progress.checkNode(anyVal) { out => {
        val (bytes, flag) = anyVal match {
            case i: Int     => (NumberSerializer.serializeNumber(i, true), IntFlag)
            case b: Byte    => (NumberSerializer.serializeNumber(b, true), ByteFlag)
            case s: Short   => (NumberSerializer.serializeNumber(s, true), ShortFlag)
            case l: Long    => (NumberSerializer.serializeNumber(l, true), LongFlag)
            case d: Double  => (NumberSerializer.serializeNumber(java.lang.Double.doubleToLongBits(d), true), DoubleFlag)
            case f: Float   => (NumberSerializer.serializeNumber(java.lang.Float.floatToIntBits(f), true), FloatFlag)
            case b: Boolean => (Array[Byte](1) :+ (if (b) 1 else 0).toByte, BooleanFlag)
            case c: Char    => (NumberSerializer.serializeNumber(c.toInt, true), CharFlag)
        }
        out.put(flag).put(bytes)
    }
    }

    override def writeString(str: String): SerializerNode = progress.checkNode(str) { out =>
        out.write(StringFlag +: str.getBytes())
    }

    override def writeArray(array: Array[Any]): SerializerNode = progress.checkNode(array) { out =>
        ArrayPersistence.serialize(array, out, progress, context)
    }

    override def writeEnum(enum: Enum[_]): SerializerNode = progress.checkNode(`enum`) { out =>
        val name     = enum.name()
        val enumType = NumberSerializer.serializeInt(enum.getClass.getName.hashCode)
        out.put(enumType).put(name.getBytes())
    }
}

