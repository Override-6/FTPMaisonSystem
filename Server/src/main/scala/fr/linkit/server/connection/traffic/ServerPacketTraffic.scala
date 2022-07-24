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

package fr.linkit.server.connection.traffic

import fr.linkit.api.application.ApplicationContext
import fr.linkit.api.gnom.packet.traffic.PacketWriter
import fr.linkit.api.gnom.persistence.context.PersistenceConfig
import fr.linkit.api.gnom.referencing.traffic.ObjectManagementChannel
import fr.linkit.engine.gnom.packet.traffic.channel.DefaultObjectManagementChannel
import fr.linkit.engine.gnom.packet.traffic.{AbstractPacketTraffic, ChannelScopes, WriterInfo}
import fr.linkit.server.connection.ServerConnection

import java.net.URL

class ServerPacketTraffic(override val connection: ServerConnection,
                          defaultPersistenceConfigScript: Option[URL]) extends AbstractPacketTraffic(connection.currentIdentifier, defaultPersistenceConfigScript) {

    override def application: ApplicationContext = connection.getApp

    override val currentIdentifier: String = connection.currentIdentifier
    override val serverIdentifier : String = currentIdentifier

    override def newWriter(path: Array[Int], persistenceConfig: PersistenceConfig): PacketWriter = {
        new ServerPacketWriter(connection, WriterInfo(this, persistenceConfig, path, () => connection.network))
    }
}