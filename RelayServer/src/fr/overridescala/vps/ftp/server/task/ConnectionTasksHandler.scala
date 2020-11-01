package fr.overridescala.vps.ftp.server.task

import java.io.BufferedOutputStream
import java.net.Socket

import fr.overridescala.vps.ftp.api.Relay
import fr.overridescala.vps.ftp.api.exceptions.{TaskException, TaskOperationException}
import fr.overridescala.vps.ftp.api.packet.ext.fundamental.{ErrorPacket, TaskInitPacket}
import fr.overridescala.vps.ftp.api.task.{TaskCompleterHandler, TaskExecutor, TasksHandler}
import fr.overridescala.vps.ftp.server.RelayServer

/**
 * handle tasks between a RelayPoint.
 * @param identifier the connected RelayPoint identifier.
 * @param server the server instance
 * @param socket the writer in which tasks will write packets
 * */
class ConnectionTasksHandler(override val identifier: String,
                             private val server: RelayServer,
                             private val socket: Socket) extends TasksHandler {

    private val packetManager = server.packetManager
    private val out = new BufferedOutputStream(socket.getOutputStream)
    private var tasksThread = new ConnectionTasksThread(identifier)
    tasksThread.start()

    override val tasksCompleterHandler: TaskCompleterHandler = server.taskCompleterHandler

    /**
     * Handles the packet.
     * @param packet packet to handle
     *
     * @throws TaskException if the handling went wrong
     * */
    override def handlePacket(packet: TaskInitPacket): Unit = {
        try {
            tasksCompleterHandler.handleCompleter(packet, this)
        } catch {
            case e: TaskException =>
                val packet = new ErrorPacket(-1,
                    server.identifier,
                    identifier,
                    ErrorPacket.ABORT_TASK,
                    e.getMessage)
                Console.err.println(e.getMessage)
                out.write(packetManager.toBytes(packet))
                out.flush()
        }
    }

    /**
     * Registers a task
     * @param executor the task to execute
     * @param taskIdentifier the task identifier
     * @param ownFreeWill true if the task was created by the user, false if the task comes from other Relay
     * */
    override def registerTask(executor: TaskExecutor, taskIdentifier: Int, targetID: String, senderID: String, ownFreeWill: Boolean): Unit = {
        val linkedRelayID = if (ownFreeWill) targetID else senderID
        if (linkedRelayID == server.identifier)
            throw new TaskOperationException("can't start a task from server to server !")
        tasksThread.addTicket(new TaskTicket(executor, taskIdentifier, linkedRelayID, server, ownFreeWill))
    }

    /**
     * closes the current client tasks thread
     * */
    override def close(): Unit = {
        tasksThread.close()
        out.close()
    }

    /**
     * Suddenly stop a task execution and execute his successor.
     * */
    override def skipCurrent(): Unit = {
        //Restarting the thread causes the current task to be skipped
        //And wait or execute the task that come after it
        val lastThread = tasksThread
        tasksThread = tasksThread.copy()
        lastThread.close()
        tasksThread.start()
    }

}