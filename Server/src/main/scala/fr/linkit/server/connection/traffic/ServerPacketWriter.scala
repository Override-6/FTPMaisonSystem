/*
 * Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR FILE HEADERS.
 *
 * This code is free software; you can USE it as you want.
 * You can download this source code, and modify it ONLY FOR PRIVATE USE but you
 * ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 * For any professional use, please contact me at overridelinkit@gmail.com.
 *
 * Please contact overridelinkit@gmail.com if you need additional information or have any
 * questions.
 */

package fr.linkit.server.connection.traffic

import fr.linkit.api.application.connection.NoSuchConnectionException
import fr.linkit.api.gnom.packet.traffic.{PacketTraffic, PacketWriter}
import fr.linkit.api.gnom.packet.{DedicatedPacketCoordinates, Packet, PacketAttributes}
import fr.linkit.engine.gnom.packet.SimplePacketAttributes
import fr.linkit.engine.gnom.packet.traffic.WriterInfo
import fr.linkit.server.connection.ServerConnection

class ServerPacketWriter(serverConnection: ServerConnection,
                         info: WriterInfo) extends PacketWriter {
    
    override val path             : Array[Int]    = info.path
    override val traffic          : PacketTraffic = info.traffic
    override val serverIdentifier : String        = serverConnection.currentIdentifier
    override val currentIdentifier: String        = traffic.currentIdentifier
    override def writePacket(packet: Packet, targetIDs: Array[String]): Unit = {
        writePacket(packet, SimplePacketAttributes.empty, targetIDs)
    }
    
    override def writePacket(packet: Packet, attributes: PacketAttributes, targetIDs: Array[String]): Unit = {
        targetIDs.foreach(targetID => {
            /*
             * If the targetID is the same as the server's identifier, that means that we target ourself,
             * so the packet, as it can't be written to a socket that target the current server, will be directly
             * injected into the traffic.
             * */
            if (targetID == serverIdentifier) {
                val coords = DedicatedPacketCoordinates(path, targetID, serverIdentifier)
                traffic.processInjection(packet, attributes, coords)
                return
            }
            val opt = serverConnection.getConnection(targetID)
            if (opt.isDefined) {
                opt.get.sendPacket(packet, attributes, path)
            } else {
                throw NoSuchConnectionException(s"Attempted to send a packet to target '$targetID', but this conection is missing or not connected.")
            }
        })
    }
    
    override def writeBroadcastPacket(packet: Packet, discardedIDs: Array[String]): Unit = {
        writeBroadcastPacket(packet, SimplePacketAttributes.empty, discardedIDs)
    }
    
    override def writeBroadcastPacket(packet: Packet, attributes: PacketAttributes, discarded: Array[String]): Unit = {
        serverConnection.broadcastPacket(packet, attributes, currentIdentifier, path, info.persistenceConfig, discarded)
    }
    
}
