package fr.`override`.linkit.client

import fr.`override`.linkit.api.concurrency.relayWorkerExecution
import fr.`override`.linkit.api.system.CloseReason
import fr.`override`.linkit.client.config.RelayPointBuilder

import java.net.InetSocketAddress
import java.nio.file.Paths
import java.util.Scanner

object Main {

    val PORT = 48484
    val SERVER_ADDRESS = new InetSocketAddress("192.168.1.19", PORT)
    val LOCALHOST = new InetSocketAddress("localhost", PORT)


    /**
     * @param args "--ide-run", used to determine if the application is running into IntelliJ or from an external context
     *             "--no-tasks", used to decide if the relay should not load any tasks from his RelayExtensions.
     * */
    def main(args: Array[String]): Unit = {
        val ideRun = args.contains("--ide-run")
        val loadExtensions = !(args.contains("--no-ext") || ideRun)

        print("say 'y' to connect to localhost : ")
        val scanner = new Scanner(System.in)
        val isLocalhost = scanner.nextLine().startsWith("y")
        val address = if (isLocalhost) LOCALHOST else SERVER_ADDRESS

        print("choose an identifier : ")
        val identifier = scanner.nextLine()

        launch(ideRun, loadExtensions, address, identifier)
    }

    def launch(ideRun: Boolean,
               loadExtensions: Boolean,
               address: InetSocketAddress,
               identifier0: String): Unit = {

        val relayPoint: RelayPoint = new RelayPointBuilder {
            enableExtensionsFolderLoad = loadExtensions
            extensionsFolder = getExtensionFolderPath

            override var serverAddress: InetSocketAddress = address
            override var identifier: String = identifier0
        }

        relayPoint runLater {
            println("Waiting RelayPoint to connecting...")
            val hooks = relayPoint.relayHooks
            hooks.connecting.await()
            println("connecting ! :D")
            println("Waiting to be ready...")
            hooks.ready.await()
            println("Dayum :0:0:0")
        } runLater {
            startRelay(relayPoint, ideRun)
        }
    }

    @relayWorkerExecution
    private def startRelay(relayPoint: RelayPoint, ideRun: Boolean): Unit = {
        relayPoint.start()

        Runtime.getRuntime.addShutdownHook(new Thread(() => relayPoint.runLater(relayPoint.close(CloseReason.INTERNAL))))

        if (ideRun && relayPoint.isOpen) {

            import fr.`override`.linkit.`extension`.controller.ControllerExtension
            import fr.`override`.linkit.`extension`.debug.DebugExtension
            import fr.`override`.linkit.`extension`.easysharing.EasySharing

            val loader = relayPoint.extensionLoader
            loader.loadExtensions(
                classOf[ControllerExtension],
                classOf[EasySharing],
                classOf[DebugExtension]
            )
        }
    }


    private def getExtensionFolderPath: String = {
        lazy val sourcePath = Paths.get(getClass.getProtectionDomain.getCodeSource.getLocation.toURI).getParent.toString
        System.getenv().get("COMPUTERNAME") match {
            case "PC_MATERIEL_NET" => "C:\\Users\\maxim\\Desktop\\Dev\\Linkit\\ClientSide\\RelayExtensions"
            //case "LORDI-N4SO7IERS" => "D:\\Users\\Maxime\\Desktop\\Dev\\Perso\\FileTransferer\\ClientSide\\RelayExtensions"
            case _ => sourcePath + "/RelayExtensions/"
        }
    }
}
