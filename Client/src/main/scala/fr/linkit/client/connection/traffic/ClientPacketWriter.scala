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

package fr.linkit.client.connection.traffic

import fr.linkit.api.gnom.packet._
import fr.linkit.api.gnom.packet.traffic.{PacketTraffic, PacketWriter}
import fr.linkit.api.gnom.persistence.ObjectTranslator
import fr.linkit.api.gnom.persistence.context.PersistenceConfig
import fr.linkit.engine.gnom.packet.SimplePacketAttributes
import fr.linkit.engine.gnom.packet.traffic.{DynamicSocket, WriterInfo}
import fr.linkit.engine.gnom.persistence.SimpleTransferInfo

class ClientPacketWriter(socket: DynamicSocket,
                         ordinal: OrdinalCounter,
                         translator: ObjectTranslator,
                         writerInfo: WriterInfo) extends PacketWriter {
    
    override     val traffic          : PacketTraffic     = writerInfo.traffic
    override     val serverIdentifier : String            = traffic.serverIdentifier
    override     val currentIdentifier: String            = traffic.currentIdentifier
    override     val path             : Array[Int]        = writerInfo.path
    private      val persistenceConfig: PersistenceConfig = writerInfo.persistenceConfig
    private lazy val network                              = writerInfo.network
    
    
    override def writePacket(packet: Packet, targetIDs: Array[String]): Unit = writePacket(packet, SimplePacketAttributes.empty, targetIDs)
    
    override def writePacket(packet: Packet, attributes: PacketAttributes, targetIDs: Array[String]): Unit = {
        if (targetIDs.length == 1) {
            val target    = targetIDs.head
            val dedicated = DedicatedPacketCoordinates(path, target, currentIdentifier)
            if (target == currentIdentifier) {
                traffic.processInjection(packet, attributes, dedicated)
                return
            }
            send(dedicated)(attributes, packet)
        } else {
            if (targetIDs.contains(currentIdentifier)) {
                val coords = DedicatedPacketCoordinates(path, serverIdentifier, currentIdentifier)
                traffic.processInjection(packet, attributes, coords)
            }
            
            for (target <- targetIDs) if (target != currentIdentifier) {
                val coords = DedicatedPacketCoordinates(path, target, currentIdentifier)
                send(coords)(attributes, packet)
            }
        }
    }
    
    override def writeBroadcastPacket(packet: Packet, attributes: PacketAttributes, discardedIDs: Array[String]): Unit = {
        if (!discardedIDs.contains(currentIdentifier))
            traffic.processInjection(packet, attributes, DedicatedPacketCoordinates(path, currentIdentifier, currentIdentifier))
        network.listEngines
                .filterNot(e => discardedIDs.contains(e.identifier))
                .foreach(engine => {
                    val coords = DedicatedPacketCoordinates(path, engine.identifier, currentIdentifier)
                    send(coords)(attributes, packet)
                })
    }
    
    
    private def send(coords: DedicatedPacketCoordinates)(attributes: PacketAttributes, packet: Packet): Unit = {
        val transferInfo = SimpleTransferInfo(coords, attributes, packet, persistenceConfig, network)
        val result = translator.translate(transferInfo)
        socket.write(result.buff(() => this.ordinal.next()))
    }
    
    override def writeBroadcastPacket(packet: Packet, discardedIDs: Array[String]): Unit = {
        writeBroadcastPacket(packet, SimplePacketAttributes.empty, discardedIDs)
    }
    
}