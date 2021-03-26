/*
 * Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can only use it for personal uses, studies or documentation.
 * You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 * ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 *
 * Please contact maximebatista18@gmail.com if you need additional information or have any
 * questions.
 */

package fr.linkit.client.connection

import fr.linkit.api.connection.ConnectionException
import fr.linkit.api.connection.packet.serialization.PacketTranslator
import fr.linkit.api.connection.packet.traffic.PacketTraffic.SystemChannelID
import fr.linkit.api.connection.packet.traffic.{ChannelScope, PacketTraffic}
import fr.linkit.api.local.system.event.EventNotifier
import fr.linkit.client.ClientApplication
import fr.linkit.client.config.ClientConnectionConfiguration
import fr.linkit.client.network.ClientSideNetwork
import fr.linkit.core.connection.network.cache.SimpleSharedCacheManager
import fr.linkit.core.connection.packet.traffic.{DynamicSocket, SocketPacketTraffic}
import fr.linkit.core.local.concurrency.PacketReaderThread
import fr.linkit.core.local.system.event.DefaultEventNotifier
import fr.linkit.core.local.system.{ContextLogger, SystemPacketChannel}

case class ClientConnectionSession(socket: DynamicSocket,
                                   info: ClientConnectionSessionInfo,
                                   serverIdentifier: String) {

    val appContext       : ClientApplication             = info.appContext
    val configuration    : ClientConnectionConfiguration = info.configuration
    val readThread       : PacketReaderThread            = info.readThread
    val supportIdentifier: String                        = configuration.identifier
    val translator       : PacketTranslator              = configuration.translator
    val traffic          : PacketTraffic                 = new SocketPacketTraffic(socket, translator, supportIdentifier, serverIdentifier)
    val eventNotifier    : EventNotifier                 = new DefaultEventNotifier
    val systemChannel    : SystemPacketChannel           = traffic.getInjectable(SystemChannelID, ChannelScope.broadcast, SystemPacketChannel)

    private var sideNetwork: ClientSideNetwork = _

    def initNetwork(connection: ClientConnection): ClientSideNetwork = {
        if (sideNetwork != null)
            return sideNetwork

        if (connection.supportIdentifier != supportIdentifier || connection.boundIdentifier != serverIdentifier)
            throw new ConnectionException(connection, "Provided unexpected connection.")

        val globalCache = SimpleSharedCacheManager.get("Global Cache", serverIdentifier)(traffic)
        translator.updateCache(globalCache)
        ContextLogger.info(s"$supportIdentifier: Stage 2 completed : Main cache manager created.")
        sideNetwork = new ClientSideNetwork(connection, globalCache)
        sideNetwork
    }

    def network: ClientSideNetwork = sideNetwork


}
