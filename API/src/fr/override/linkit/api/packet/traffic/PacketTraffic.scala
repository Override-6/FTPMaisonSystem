package fr.`override`.linkit.api.packet.traffic

import fr.`override`.linkit.api.packet.channel.{PacketChannel, PacketChannelFactory}
import fr.`override`.linkit.api.packet.collector.{PacketCollector, PacketCollectorFactory}
import fr.`override`.linkit.api.packet.{Packet, PacketCoordinates}
import fr.`override`.linkit.api.system.JustifiedCloseable


trait PacketTraffic extends PacketWriter with JustifiedCloseable {

    def register(injectable: PacketInjectable): Unit

    def createChannel[C <: PacketChannel](channelId: Int, targetID: String, factory: PacketChannelFactory[C]): C

    def createCollector[C <: PacketCollector](channelId: Int, factory: PacketCollectorFactory[C]): C

    def injectPacket(packet: Packet, coordinates: PacketCoordinates): Unit

    def isRegistered(identifier: Int): Boolean

    def checkThread(): Unit

}

object PacketTraffic {
    val SystemChannel = 1
    val RemoteConsoles = 2
}