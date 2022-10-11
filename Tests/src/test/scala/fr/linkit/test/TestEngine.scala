
package fr.linkit.test

import fr.linkit.api.application.config.ApplicationConfiguration
import fr.linkit.api.gnom.network.Network
import fr.linkit.api.internal.system.security.ApplicationSecurityManager
import fr.linkit.client.config.ClientConnectionConfigBuilder
import fr.linkit.mock.LinkitApplicationMock
import fr.linkit.server.config.ServerConnectionConfigBuilder

import java.net.InetSocketAddress

object TestEngine {

    var application      : LinkitApplicationMock = _
    var clientSideNetwork: Network               = _
    var serverSideNetwork: Network               = _

    launchMockingNetwork()

    def launchMockingNetwork(): Unit = {
        if (application != null) //already loaded
            return
        application = LinkitApplicationMock.launch(new ApplicationConfiguration {
            override val logfilename    : Option[String]             = None
            override val securityManager: ApplicationSecurityManager = ApplicationSecurityManager.None
            override val resourceFolder : String                     = {
                val envValue = System.getenv("LINKIT_HOME")
                if (envValue == null)
                    throw new NoSuchElementException("LinkitHome property not set. Please set a path in LinkitHome property to start testing.")
                envValue
            }
        })

        serverSideNetwork = application.openServerConnection(new ServerConnectionConfigBuilder {
            override val identifier: String = "Server"
            override val port      : Int    = 49490
        }).network

        clientSideNetwork = application.openClientConnection(new ClientConnectionConfigBuilder {
            override val identifier   : String            = "Client"
            override val remoteAddress: InetSocketAddress = new InetSocketAddress(49490)
        }).network
        Thread.sleep(1000)
    }

}