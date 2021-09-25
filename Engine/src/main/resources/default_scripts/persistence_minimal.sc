import fr.linkit.api.connection.packet.traffic.PacketTraffic
import fr.linkit.api.local.ApplicationContext
import fr.linkit.engine.connection.network.{NetworkDataBundle, NetworkDataTrunk}
import fr.linkit.engine.connection.packet.fundamental.EmptyPacket
import fr.linkit.engine.connection.packet.persistence.context.PersistenceConfigBuilder

//Start Of Context
val builder: PersistenceConfigBuilder = null
val app    : ApplicationContext       = null
val traffic: PacketTraffic            = null

import builder._

//ENd Of Context

val connection = traffic.connection
putContextReference(0, None)
putContextReference(1, EmptyPacket)
putContextReference(2, app)
putContextReference(3, traffic)
putContextReference(4, connection)
setTConverter[NetworkDataTrunk, NetworkDataBundle](_.toBundle)(NetworkDataTrunk.fromData)
//setTConverter[File, String](_.getAbsolutePath)(new File(_))
//setTConverter[Date, Long](_.getTime)(new Date(_))
//setTConverter[Timestamp, Long](_.getTime)(new Timestamp(_))