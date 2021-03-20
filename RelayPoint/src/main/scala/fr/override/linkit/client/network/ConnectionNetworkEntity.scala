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

package fr.`override`.linkit.client.network

import fr.`override`.linkit.api.connection.ConnectionContext
import fr.`override`.linkit.api.connection.network.ConnectionState
import fr.`override`.linkit.api.connection.network.cache.SharedCacheManager
import fr.`override`.linkit.core.connection.network.AbstractRemoteEntity
import fr.`override`.linkit.core.connection.network.cache.{AbstractSharedCacheManager, SharedInstance}
import fr.`override`.linkit.core.connection.packet.traffic.channel.CommunicationPacketChannel

class ConnectionNetworkEntity private(connection: ConnectionContext,
                                      identifier: String,
                                      cache: SharedCacheManager)
        extends AbstractRemoteEntity(identifier, cache) {

    def this(connection: ConnectionContext, identifier: String, communicator: CommunicationPacketChannel) = {
        this(connection, identifier, AbstractSharedCacheManager.get(identifier)(relay.traffic), communicator)
    }

    private val stateInstance = cache.get(3, SharedInstance[ConnectionState])

    override def getConnectionState: ConnectionState = stateInstance.get

    stateInstance.addListener(newState => {
        //val event = NetworkEvents.entityStateChange(this, newState, getConnectionState)
        //relay.eventNotifier.notifyEvent(relay.networkHooks, event)
    })

}
