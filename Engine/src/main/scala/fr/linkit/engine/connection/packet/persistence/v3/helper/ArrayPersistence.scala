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

package fr.linkit.engine.connection.packet.persistence.v3.helper

import fr.linkit.api.connection.packet.persistence.v3.deserialisation.DeserializationInputStream
import fr.linkit.api.connection.packet.persistence.v3.deserialisation.node.ObjectDeserializerNode
import fr.linkit.api.connection.packet.persistence.v3.serialisation.SerialisationProgression
import fr.linkit.api.connection.packet.persistence.v3.serialisation.node.SerializerNode
import fr.linkit.engine.connection.packet.persistence.v3.ArraySign
import fr.linkit.engine.connection.packet.persistence.v3.deserialisation.node.RawObjectNode
import fr.linkit.engine.connection.packet.persistence.v3.serialisation.SerializerNodeFlags.ArrayFlag
import fr.linkit.engine.local.utils.NumberSerializer
import fr.linkit.engine.local.utils.NumberSerializer.deserializeFlaggedNumber
import fr.linkit.engine.local.utils.UnWrapper.unwrap

import java.lang
import java.lang.reflect.{Array => RArray}

object ArrayPersistence {

    val EmptyArrayFlag: Byte = -105

    def deserialize(in: DeserializationInputStream): ObjectDeserializerNode = {
        val buff       = in.buff
        val compType   = in.readClass()
        val arrayDepth = in.get + Byte.MaxValue
        if (buff.get(buff.position()) == EmptyArrayFlag) {
            buff.position(buff.position() + 1)
            val array = buildArray(compType, arrayDepth, 0)
            return RawObjectNode(array, in.context)
        }

        val signItemCount = deserializeFlaggedNumber[Int](buff)

        val sign   = ArraySign.in(signItemCount, in)
        val result = buildArray(compType, arrayDepth, signItemCount)
        sign.deserializeRef(result)(itemNodes => {
            var i = 0
            itemNodes.foreach(node => {
                putInArray(result, i, node.deserialize(in))
                i += 1
            })
        })
    }

    private def putInArray(array: Array[_], idx: Int, value: Any): Unit = {
        array.getClass.componentType() match {
            case Integer.TYPE      => RArray.setInt(array, idx, unwrap(value, _.intValue))
            case lang.Byte.TYPE    => RArray.setByte(array, idx, unwrap(value, _.byteValue))
            case lang.Short.TYPE   => RArray.setShort(array, idx, unwrap(value, _.shortValue))
            case lang.Long.TYPE    => RArray.setLong(array, idx, unwrap(value, _.longValue))
            case lang.Double.TYPE  => RArray.setDouble(array, idx, unwrap(value, _.doubleValue))
            case lang.Float.TYPE   => RArray.setFloat(array, idx, unwrap(value, _.floatValue))
            case lang.Boolean.TYPE => RArray.setBoolean(array, idx, unwrap(value, _.booleanValue))
            case Character.TYPE    => RArray.setChar(array, idx, unwrap(value, _.charValue))
            case _                 => RArray.set(array, idx, value)
        }
    }

    private def buildArray(compType: Class[_], arrayDepth: Int, arrayLength: Int): Array[_] = {
        var finalCompType = compType
        for (_ <- 0 until arrayDepth) {
            finalCompType = finalCompType.arrayType()
        }
        RArray.newInstance(finalCompType, arrayLength).asInstanceOf[Array[_]]
    }

    def serialize(array: Array[_], progress: SerialisationProgression): SerializerNode = {
        val (compType, depth) = getAbsoluteCompType(array)
        val arrayTypeBytes    = NumberSerializer.serializeInt(compType.getName.hashCode)
        val head              = Array(ArrayFlag) ++ arrayTypeBytes :+ depth
        if (array.isEmpty) {
            return out => out.put(head :+ EmptyArrayFlag)
        }
        val node = ArraySign.out(array, progress).getNode
        out => {
            out.write(head)
            out.write(NumberSerializer.serializeNumber(array.length))
            node.writeBytes(out)
        }
    }

    /**
     *
     * @param array the array to test
     * @return a tuple where the left index is the absolute component type of the array and the right index
     *         is the depth of the absolute component type in the array
     */
    private def getAbsoluteCompType(array: Array[_]): (Class[_], Byte) = {
        var i    : Byte     = Byte.MinValue
        var clazz: Class[_] = array.getClass
        while (clazz.isArray) {
            i = (i + 1).toByte
            clazz = clazz.componentType()
        }
        (clazz, i)
    }
}
