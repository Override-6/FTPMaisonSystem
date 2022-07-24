/*
 * Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR FILE HEADERS.
 *
 * This code is free software; you can only use it for personal uses, studies or documentation.
 * You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 * ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 * For any professional use, please contact me at overridelinkit@gmail.com.
 *
 * Please contact overridelinkit@gmail.com if you need additional information or have any
 * questions.
 */

package fr.linkit.api.internal.system.security

import fr.linkit.api.application.connection.ConnectionContext
import fr.linkit.api.application.config.ConnectionConfiguration

trait ApplicationSecurityManager {

    @throws[ConnectionSecurityException]("If the security manager rejected the configuration.")
    def checkConnectionConfig(config: ConnectionConfiguration): Unit

    @throws[ConnectionSecurityException]("If the security manager rejected the initialisation.")
    def checkConnection(connection: ConnectionContext): Unit

}

object ApplicationSecurityManager {
    class Default extends ApplicationSecurityManager {

        override def checkConnection(connection: ConnectionContext): Unit = ()

        override def checkConnectionConfig(config: ConnectionConfiguration): Unit = ()
    }

    def none: ApplicationSecurityManager = new Default
}