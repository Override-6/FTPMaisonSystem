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

package fr.linkit.core.connection.packet.traffic

import fr.linkit.api.connection.packet.traffic.{InjectionContainer, PacketInjection}
import fr.linkit.api.connection.packet.{DedicatedPacketCoordinates, Packet}
import fr.linkit.api.local.system.AppLogger
import fr.linkit.core.local.concurrency.pool.BusyWorkerPool.currentTasksId

import scala.collection.mutable

class DirectInjectionContainer extends InjectionContainer {

    private val processingInjections = new mutable.LinkedHashMap[(Int, String), DirectInjection]

    override def makeInjection(packet: Packet, coordinates: DedicatedPacketCoordinates): PacketInjection = this.synchronized {
        val number = packet.number
        AppLogger.debug(s"${currentTasksId} <> $number -> CREATING INJECTION FOR PACKET $packet WITH COORDINATES $coordinates")
        val id     = coordinates.injectableID
        val sender = coordinates.senderID

        val injection = processingInjections.get((id, sender)) match {
            case Some(value) =>
                AppLogger.debug(s"${currentTasksId} <> $number -> INJECTION ALREADY EXISTS, ADDING PACKET.")
                value
            case None        =>
                AppLogger.debug(s"${currentTasksId} <> $number -> INJECTION DOES NOT EXISTS, CREATING IT.")
                new DirectInjection(coordinates)
        }
        processingInjections.put((id, sender), injection)

        injection.inject(packet)
        injection
    }

    override def isProcessing(injection: PacketInjection): Boolean = {
        val coords = injection.coordinates
        val id     = coords.injectableID
        val sender = coords.senderID
        processingInjections.contains((id, sender))
    }

    def removeInjection(injection: PacketInjection): Unit = {
        val coords = injection.coordinates
        val id     = coords.injectableID
        val sender = coords.senderID
        processingInjections.remove((id, sender))
    }

}

