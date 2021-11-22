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

package fr.linkit.api.gnom.network

import fr.linkit.api.gnom.cache.SharedCacheManager
import fr.linkit.api.gnom.network.statics.StaticAccess
import fr.linkit.api.internal.system.Versions

import java.sql.Timestamp

trait Engine extends /*NetworkObject[EngineReference] with*/ Updatable {

    val identifier: String

    val cache: SharedCacheManager

    val versions: Versions

    val connectionDate: Timestamp

    val network: Network

    val staticAccess: StaticAccess

    def isServer: Boolean = network.serverEngine eq this

    def getConnectionState: ExternalConnectionState

    def toString: String
    /*
    * connection.accessor
    * */
}
