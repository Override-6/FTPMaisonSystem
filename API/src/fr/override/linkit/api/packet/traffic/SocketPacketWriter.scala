package fr.`override`.linkit.api.packet.traffic

import fr.`override`.linkit.api.packet.{Packet, PacketCoordinates, PacketTranslator}

import scala.collection.mutable

class SocketPacketWriter(socket: DynamicSocket,
                         translator: PacketTranslator,
                         info: WriterInfo) extends PacketWriter {

    private val coordsCache = mutable.Map.empty[String, PacketCoordinates]

    override val traffic    : PacketTraffic = info.traffic
    override val relayID    : String        = traffic.relayID
    override val identifier : Int           = info.identifier

    override def writePacket(packet: Packet, targetID: String): Unit = {
        val transformedPacket = info.transform(packet)
        val coords = coordsCache.getOrElseUpdate(targetID, PacketCoordinates(identifier, targetID, relayID))
        socket.write(translator.fromPacketAndCoords(transformedPacket, coords))
    }

    override def writeBroadcastPacket(packet: Packet): Unit = writePacket(packet, "BROADCAST")
}
