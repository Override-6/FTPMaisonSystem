/*
 * Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can only use it for personal uses, studies or documentation.
 * You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 * ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 *
 * Please contact maximebatista18@gmail.com if you need additional information or have any
 * questions.
 */

package fr.linkit.core.connection.network

import fr.linkit.core.connection.packet.traffic.channel.AsyncPacketChannel
import fr.linkit.core.local.utils.InactiveOutputStream
import org.jetbrains.annotations.Nullable

import java.io.PrintStream

//TODO -------------------------------------------------- MAINTAINED --------------------------------------------------
class SimpleRemoteConsole private(@Nullable channel: AsyncPacketChannel,
                                  //relay: Relay,
                                  owner: String,
                                  kind: String) extends PrintStream(InactiveOutputStream, true) {
/*
    override def write(b: Array[Byte]): Unit = {
        print(new String(b))
    }

    override def write(b: Int): Unit = {
        print(b)
    }

    override def write(buf: Array[Byte], off: Int, len: Int): Unit = {
        print(new String(buf.slice(off, off + len)))
    }

    override def print(obj: Any): Unit = {
        var str = String.valueOf(obj)

        if (obj.getClass.isArray)
            str = java.util.Arrays.deepToString(obj.asInstanceOf[Array[AnyRef]])

        if (channel != null)
            channel.send(TaggedObjectPacket(kind, String.valueOf(obj))) //send the print to the linked console

        val entity = relay.network.getEntity(owner).get
        val event = NetworkEvents.remotePrintSentEvent(entity, str)
        relay.eventNotifier.notifyEvent(relay.networkHooks, event)
    }

    override def print(x: Boolean): Unit = print(x: Any)

    override def print(c: Char): Unit = {
        print(c: Any)
    }

    override def print(s: Array[Char]): Unit = print(s: Any)

    //avoid newLine
    override def print(str: String): Unit = if (str != lineSeparator) print(str: Any)

    override def print(i: Int): Unit = print(i: Any)

    override def print(l: Long): Unit = print(l: Any)

    override def print(f: Float): Unit = print(f: Any)

    override def print(d: Double): Unit = print(d: Any)

    private val lineSeparator = AccessController.doPrivileged(new GetPropertyAction("line.separator"))
*/
}

object SimpleRemoteConsole {
/*
    def err(channel: AsyncPacketChannel, relay: Relay, owner: String): SimpleRemoteConsole = new SimpleRemoteConsole(channel, relay, owner, "err")

    def out(channel: AsyncPacketChannel, relay: Relay, owner: String): SimpleRemoteConsole = new SimpleRemoteConsole(channel, relay, owner, "out")

    val Mock: SimpleRemoteConsole = new SimpleRemoteConsole(null, null, null, "mock")
*/
}