package fr.overridescala.vps.ftp.api.packet.ext

import fr.overridescala.vps.ftp.api.exceptions.UnexpectedPacketException
import fr.overridescala.vps.ftp.api.packet.Packet
import fr.overridescala.vps.ftp.api.packet.ext.PacketManager.SIZE_SEPARATOR
import fr.overridescala.vps.ftp.api.packet.ext.fundamental.{DataPacket, ErrorPacket, TaskInitPacket}

import scala.collection.mutable


object PacketManager {
    private val SIZE_SEPARATOR = ":".getBytes
}

class PacketManager {

    type PT <: Packet

    private val factories = withFundamentals()

    def withFundamentals(): mutable.Map[Class[PT], PacketFactory[PT]] = {
        val factories = mutable.Map.empty[Class[PT], PacketFactory[PT]]

        def put[D <: Packet](clazz: Class[D], fact: PacketFactory[D]): Unit =
            factories.put(clazz.asInstanceOf[Class[PT]], fact.asInstanceOf[PacketFactory[PT]])

        put(classOf[DataPacket], DataPacket.Factory)
        put(classOf[TaskInitPacket], TaskInitPacket.Factory)
        put(classOf[ErrorPacket], ErrorPacket.Factory)
        factories
    }

    def registerIfAbsent[P <: Packet](packetClass: Class[P], packetFactory: PacketFactory[P]): Unit = {
        val factory = packetFactory.asInstanceOf[PacketFactory[PT]]
        val pcktClass = packetClass.asInstanceOf[Class[PT]]
        if (!factories.contains(pcktClass))
            factories.put(pcktClass, factory) 
    }

    def toPacket(bytes: Array[Byte]): Packet = {
        for (factory <- factories.values) {
            if (factory.canTransform(bytes))
                return factory.toPacket(bytes)
        }
        throw UnexpectedPacketException(s"could not find packet factory for ${new String(bytes)}")
    }

    def toBytes[D <: Packet](classOfP: Class[D], packet: D): Array[Byte] = {
        val bytes = factories(classOfP.asInstanceOf[Class[PT]])
                .asInstanceOf[PacketFactory[D]]
                .toBytes(packet)
        s"${bytes.length}".getBytes ++ SIZE_SEPARATOR ++ bytes
    }

    def toBytes[D <: Packet](packet: D): Array[Byte] = {
        try {
            val packetClass = packet.getClass.asInstanceOf[Class[D]]
            toBytes(packetClass, packet)
        }
    }

}
