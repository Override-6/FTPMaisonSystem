package fr.`override`.linkit.api.packet.traffic.dedicated

import java.util.concurrent.{BlockingQueue, LinkedBlockingQueue}

import fr.`override`.linkit.api.concurrency.{PacketWorkerThread, RelayWorkerThreadPool}
import fr.`override`.linkit.api.exception.UnexpectedPacketException
import fr.`override`.linkit.api.packet.traffic.PacketWriter
import fr.`override`.linkit.api.packet.traffic.dedicated.{AbstractPacketChannel, DedicatedPacketSender, DedicatedPacketSyncReceiver, PacketChannelFactory}
import fr.`override`.linkit.api.packet.{Packet, PacketCoordinates}
import fr.`override`.linkit.api.system.CloseReason


//TODO doc
class SyncPacketChannel protected(writer: PacketWriter,
                                  connectedID: String,
                                  providable: Boolean) extends AbstractPacketChannel(writer, connectedID)
        with DedicatedPacketSender with DedicatedPacketSyncReceiver {


    /**
     * this blocking queue stores the received packets until they are requested
     * */
    private val queue: BlockingQueue[Packet] = {
        if (!providable)
            new LinkedBlockingQueue[Packet]()
        else {
            RelayWorkerThreadPool
                    .ifCurrentOrElse(_.newProvidedQueue, new LinkedBlockingQueue[Packet]())
        }
    }

    /**
     * add a packet into the PacketChannel. the PacketChannel will stop waiting in [[PacketChannel#nextPacket]] if it where waiting for a packet
     *
     * @param packet the packet to add
     * @param coords the packet coordinates
     *
     * @throws IllegalArgumentException if the packet coordinates does not match with this channel coordinates
     * */
    override def injectPacket(packet: Packet, coords: PacketCoordinates): Unit = {
        if (coords.senderID != connectedID)
            throw new UnexpectedPacketException("Attempted to inject a packet that comes from a relay that is not bound to this channel")
        queue.add(packet)
    }

    override def sendPacket(packet: Packet): Unit = writer.writePacket(packet, connectedID)

    override def close(reason: CloseReason): Unit = {
        super.close(reason)
        queue.clear()
    }

    override def nextPacket(): Packet = {
        if (queue.isEmpty)
            PacketWorkerThread.checkNotCurrent()
        val packet = queue.take()
        //handler.notifyPacketUsed(packet, coordinates)
        packet
    }


    /**
     * @return true if this channel contains stored packets. In other words, return true if [[nextPacket]] would not wait
     * */
    override def haveMorePackets: Boolean =
        !queue.isEmpty

}


object SyncPacketChannel extends PacketChannelFactory[SyncPacketChannel] {

    override def createNew(writer: PacketWriter,
                           connectedID: String): SyncPacketChannel = {
        new SyncPacketChannel(writer, connectedID, false)
    }

    def providable: PacketChannelFactory[SyncPacketChannel] = new SyncPacketChannel(_, _, true)

}