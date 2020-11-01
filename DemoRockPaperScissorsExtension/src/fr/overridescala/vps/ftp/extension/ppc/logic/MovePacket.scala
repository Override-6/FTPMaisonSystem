package fr.overridescala.vps.ftp.`extension`.ppc.logic

import fr.overridescala.vps.ftp.api.packet.ext.PacketFactory
import fr.overridescala.vps.ftp.api.packet.ext.PacketUtils.{cutEnd, cutString}
import fr.overridescala.vps.ftp.api.packet.{Packet, PacketChannel}

case class MovePacket private(override val senderID: String,
                              override val targetID: String,
                              override val channelID: Int,
                              move: MoveType) extends Packet {


}

object MovePacket {
    def apply(move: MoveType)(implicit channel: PacketChannel): MovePacket =
        new MovePacket(
            channel.ownerIdentifier,
            channel.connectedIdentifier,
            channel.channelID,
            move
        )

    object Factory extends PacketFactory[MovePacket] {

        private val TYPE_FLAG = "[RPS]".getBytes
        private val MOVE_FLAG = "<move>".getBytes

        override def decompose(implicit packet: MovePacket): Array[Byte] = {
            val channelID = packet.channelID.toString.getBytes
            val move = packet.move.name().getBytes
            TYPE_FLAG ++ channelID ++
                    MOVE_FLAG ++ move
        }

        override def canTransform(implicit bytes: Array[Byte]): Boolean = bytes.containsSlice(TYPE_FLAG)

        override def build(senderID: String, targetID: String)(implicit bytes: Array[Byte]): MovePacket = {
            val channelID = cutString(TYPE_FLAG, MOVE_FLAG).toInt
            val moveName = new String(cutEnd(MOVE_FLAG))
            new MovePacket(senderID, targetID, channelID, MoveType.valueOf(moveName))
        }
    }
}
