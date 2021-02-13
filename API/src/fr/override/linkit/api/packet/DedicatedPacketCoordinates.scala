package fr.`override`.linkit.api.packet
import fr.`override`.linkit.api.packet.serialization.PacketSerializer

case class DedicatedPacketCoordinates(injectableID: Int, targetID: String, senderID: String) extends PacketCoordinates {

    override def toString: String = s"DedicatedPacketCoordinates(channelId: $injectableID, targetID: $targetID, senderID: $senderID)"

    def reversed(): DedicatedPacketCoordinates = DedicatedPacketCoordinates(injectableID, senderID, targetID)

    override def determineSerializer(cachedWhitelist: Array[String], raw: PacketSerializer, cached: PacketSerializer): PacketSerializer = {
        if (cachedWhitelist.contains(targetID)) cached else raw
    }
}