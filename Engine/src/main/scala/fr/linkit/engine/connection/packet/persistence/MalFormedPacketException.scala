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

package fr.linkit.engine.connection.packet.persistence

import java.nio.ByteBuffer

import fr.linkit.api.connection.packet.PacketException

case class MalFormedPacketException(bytes: Array[Byte], msg: String) extends PacketException(msg) {

    def this(buffer: ByteBuffer, msg: String) {
        this(buffer.array().slice(buffer.position(), buffer.limit()), msg)
    }

    override protected def appendMessage(sb: StringBuilder): Unit = {
        super.appendMessage(sb)
        sb.append(s"For sequence: ${new String(bytes)}")
    }

}
