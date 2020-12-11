package fr.overridescala.linkkit.client

import fr.overridescala.linkkit.api.Relay
import fr.overridescala.linkkit.api.system.security.{RelaySecurityException, RelaySecurityManager}

class RelayPointSecurityManager extends RelaySecurityManager {

    //FIXME, if two relay point uses a different security manager, and have different hash algorithm, this would cause the receiver to misread the packet then crash
    override def hashBytes(raw: Array[Byte]): Array[Byte] = raw //do not hash bytes

    override def deHashBytes(hashed: Array[Byte]): Array[Byte] = hashed // do not hash bytes

    /**
     * Proceeds to two checks : before any load, and the relay completely loaded and connected.
     *
     * @throws RelaySecurityException if the relay is invalid, this will cause relay to close automatically.
     * */
    override def checkRelay(relay: Relay): Unit = {
        val identifier = relay.identifier
        if (identifier == RelayPoint.ServerID || identifier == "unknown")
            throw RelaySecurityException(s"'$identifier' is a blacklisted identifier !")
        if (!identifier.matches("^\\w{0,16}$"))
            throw RelaySecurityException(s"'$identifier' does not match regex '^\\w{0,16}$$'")
    }
}