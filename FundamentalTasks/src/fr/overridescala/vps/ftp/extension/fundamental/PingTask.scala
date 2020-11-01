package fr.overridescala.vps.ftp.`extension`.fundamental

import fr.overridescala.vps.ftp.api.packet.ext.fundamental.EmptyPacket
import fr.overridescala.vps.ftp.api.task.{Task, TaskExecutor, TaskInitInfo}
import PingTask.TYPE

class PingTask(private val targetId: String) extends Task[Long](targetId) {

    override val initInfo: TaskInitInfo =
        TaskInitInfo.of(TYPE, targetId)

    override def execute(): Unit = {
        val t0 = System.currentTimeMillis()
        channel.sendPacket(EmptyPacket())
        channel.nextPacket()
        val t1 = System.currentTimeMillis()
        success(t1 - t0)
    }
}

object PingTask {
    val TYPE: String = "PING"

    class Completer extends TaskExecutor {
        override def execute(): Unit = {
            channel.nextPacket() //waiting ping packet
            val packet = EmptyPacket()
            channel.sendPacket(packet)
        }

    }

    def apply(targetID: String): PingTask =
        new PingTask(targetID)


}