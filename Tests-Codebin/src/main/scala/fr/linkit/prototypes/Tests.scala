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

package fr.linkit.prototypes

import fr.linkit.api.connection.packet.DedicatedPacketCoordinates
import fr.linkit.api.local.ApplicationContext
import fr.linkit.core.connection.packet.SimplePacketAttributes
import fr.linkit.core.connection.packet.fundamental.RefPacket.ObjectPacket
import fr.linkit.core.connection.packet.fundamental.WrappedPacket
import fr.linkit.core.local.mapping.ClassMapEngine
import fr.linkit.core.local.system.fsa.JDKFileSystemAdapters

object Tests {

    private val coords     = DedicatedPacketCoordinates(12, "s1", "TestServer1")
    private val packet     = WrappedPacket("Hey", WrappedPacket("World", ObjectPacket(KillerClass(KillerClass(KillerClass(null))))))
    private val attributes = SimplePacketAttributes.empty

    private val fsa = JDKFileSystemAdapters.Nio

    case class KillerClass(other: KillerClass) extends Serializable {

    }

    def main(args: Array[String]): Unit = {
        doMappings()
        println(System.getenv())
        println(System.getenv("LinkitHome"))

    }

    private def doMappings(): Unit = {
        ClassMapEngine.mapAllSourcesOfClasses(fsa, getClass, ClassMapEngine.getClass, Predef.getClass, classOf[ApplicationContext])
        ClassMapEngine.mapJDK(fsa)
    }

}