/*
 *  Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 *  This code is free software; you can only use it for personal uses, studies or documentation.
 *  You can download this source code, and modify it ONLY FOR PERSONAL USE and you
 *  ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 *
 *  Please contact maximebatista18@gmail.com if you need additional information or have any
 *  questions.
 */

package fr.linkit.api.connection.cache.traffic

import fr.linkit.api.connection.cache.{CacheContent, SharedCacheManager}
import fr.linkit.api.connection.cache.traffic.handler.CacheHandler
import fr.linkit.api.connection.packet.channel.request.RequestPacketChannel

trait CachePacketChannel extends RequestPacketChannel {

    val manager: SharedCacheManager

    val cacheID: Int

    def setHandler(handler: CacheHandler): Unit

    def getCacheOfOwner: CacheContent

    def getHandler: Option[CacheHandler]

}