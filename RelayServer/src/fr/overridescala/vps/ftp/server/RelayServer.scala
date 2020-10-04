package fr.overridescala.vps.ftp.server

import java.net.ServerSocket
import java.nio.ByteBuffer
import java.nio.charset.Charset

import fr.overridescala.vps.ftp.api.Relay
import fr.overridescala.vps.ftp.api.task.{Task, TaskCompleterHandler}
import fr.overridescala.vps.ftp.api.utils.Constants
import fr.overridescala.vps.ftp.server.connection.ConnectionsManager
import fr.overridescala.vps.ftp.server.task.ServerTaskCompleterHandler

class RelayServer()
        extends Relay {


    private val serverSocket = new ServerSocket(Constants.PORT)
    private val completerHandler = new ServerTaskCompleterHandler(this)
    private val connectionsManager = new ConnectionsManager(this)

    private var open = false

    override val identifier: String = Constants.SERVER_ID

    override def scheduleTask[R](task: Task[R]): RelayTaskAction[R] = {
        ensureOpen()
        val targetIdentifier = task.targetID
        val tasksHandler = connectionsManager.getConnectionFromIdentifier(targetIdentifier).tasksHandler
        task.init(tasksHandler)
        RelayTaskAction[R](task)
    }

    override def getTaskCompleterHandler: TaskCompleterHandler = completerHandler

    override def start(): Unit = {
        println("ready !")
        println("current encoding is " + Charset.defaultCharset().name())
        println("listening on port " + Constants.PORT)

        open = true
        while (open) awaitClientConnection()
    }

    def awaitClientConnection(): Unit = {
        val clientSocket = serverSocket.accept()
        val address = clientSocket.getRemoteSocketAddress
        println(s"new connection : $address")
        connectionsManager.register(clientSocket)
    }

    override def close(): Unit = {
        println("closing server...")
        connectionsManager.close()
        serverSocket.close()
        open = false
        println("server disconnected !")
    }

    private def ensureOpen(): Unit = {
        if (!open)
            throw new UnsupportedOperationException("Relay Point have to be started !")
    }

    // default tasks
    Runtime.getRuntime.addShutdownHook(new Thread(() => close()))

}