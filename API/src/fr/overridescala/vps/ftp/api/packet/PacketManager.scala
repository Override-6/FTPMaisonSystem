package fr.overridescala.vps.ftp.api.packet

import fr.overridescala.vps.ftp.api.`extension`.packet.PacketFactory
import fr.overridescala.vps.ftp.api.exceptions.UnexpectedPacketException
import fr.overridescala.vps.ftp.api.packet.PacketUtils.wrap
import fr.overridescala.vps.ftp.api.packet.fundamental._
import fr.overridescala.vps.ftp.api.system.SystemPacket
import fr.overridescala.vps.ftp.api.system.event.EventDispatcher.EventNotifier

import scala.collection.mutable


object PacketManager {
    val ChannelIDSeparator: Array[Byte] = "<channel>".getBytes
    val SenderSeparator: Array[Byte] = "<sender>".getBytes
    val TargetSeparator: Array[Byte] = "<target>".getBytes
}

class PacketManager(private[api] val notifier: EventNotifier) { //Notifier is accessible from api to reduce parameter number in (A)SyncPacketChannel

    import PacketManager._

    type PT <: Packet

    private val factories = withFundamentals()


    def registerIfAbsent[P <: Packet](packetClass: Class[P], packetFactory: PacketFactory[P]): Unit = {
        val factory = packetFactory.asInstanceOf[PacketFactory[PT]]
        val ptClass = packetClass.asInstanceOf[Class[PT]]
        if (!factories.contains(ptClass))
            factories.put(ptClass, factory)
        notifier.onPacketTypeRegistered(packetClass, packetFactory)
    }

    def toPacket(implicit bytes: Array[Byte]): Packet = {
        val channelIndex = bytes.indexOfSlice(ChannelIDSeparator)
        val senderIndex = bytes.indexOfSlice(SenderSeparator)
        val targetIndex = bytes.indexOfSlice(TargetSeparator)
        val channelID = new String(bytes.slice(0, channelIndex)).toInt
        val senderID = new String(bytes.slice(channelIndex + ChannelIDSeparator.length, senderIndex))
        val targetID = new String(bytes.slice(senderIndex + SenderSeparator.length, targetIndex))

        val endIndex = targetIndex + TargetSeparator.length
        val customPacketBytes = bytes.slice(endIndex, bytes.length)
        for (factory <- factories.values) {
            if (factory.canTransform(customPacketBytes))
                return factory.build(channelID, senderID, targetID)(customPacketBytes)
        }
        throw new UnexpectedPacketException(s"Could not find packet factory for ${new String(bytes)}")
    }

    def toBytes[D <: Packet](classOfP: Class[D], packet: D): Array[Byte] = {
        val packetBytes = factories(classOfP.asInstanceOf[Class[PT]])
                .asInstanceOf[PacketFactory[D]]
                .decompose(packet)
        val bytes = PacketUtils.redundantBytesOf(packet) ++ packetBytes
        wrap(bytes)
    }

    def toBytes[D <: Packet](packet: D): Array[Byte] = {
        val packetClass = packet.getClass.asInstanceOf[Class[D]]
        toBytes(packetClass, packet)
    }

    private def withFundamentals(): mutable.Map[Class[PT], PacketFactory[PT]] = {
        val factories = mutable.LinkedHashMap.empty[Class[PT], PacketFactory[PT]]

        def register[D <: Packet](clazz: Class[D], fact: PacketFactory[D]): Unit =
            factories.put(clazz.asInstanceOf[Class[PT]], fact.asInstanceOf[PacketFactory[PT]])

        register(classOf[DataPacket], DataPacket.Factory)
        register(classOf[EmptyPacket], EmptyPacket.Factory)
        register(classOf[TaskInitPacket], TaskInitPacket.Factory)
        register(classOf[ErrorPacket], ErrorPacket.Factory)
        register(classOf[SystemPacket], SystemPacket.Factory)
        factories
    }


}