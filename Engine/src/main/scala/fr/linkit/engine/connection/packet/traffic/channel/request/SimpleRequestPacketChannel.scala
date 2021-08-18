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

package fr.linkit.engine.connection.packet.traffic.channel.request

import fr.linkit.api.connection.packet.channel.ChannelScope.ScopeFactory
import fr.linkit.api.connection.packet.channel.request.{RequestPacketBundle, RequestPacketChannel, ResponseHolder, Submitter}
import fr.linkit.api.connection.packet.channel.{ChannelScope, PacketChannel}
import fr.linkit.api.connection.packet.traffic.PacketInjectableFactory
import fr.linkit.api.connection.packet.traffic.injection.PacketInjection
import fr.linkit.api.local.concurrency.WorkerPools
import fr.linkit.api.local.concurrency.WorkerPools.{currentTasksId, ensureCurrentIsWorker}
import fr.linkit.api.local.system.AppLogger
import fr.linkit.engine.connection.packet.traffic.ChannelScopes
import fr.linkit.engine.connection.packet.traffic.channel.AbstractPacketChannel
import fr.linkit.engine.local.utils.ConsumerContainer
import org.jetbrains.annotations.Nullable

import java.util.NoSuchElementException
import java.util.concurrent.LinkedBlockingQueue
import scala.collection.mutable

class SimpleRequestPacketChannel(@Nullable parent: PacketChannel, scope: ChannelScope) extends AbstractPacketChannel(parent, scope) with RequestPacketChannel {

    private val requestHolders         = mutable.LinkedHashMap.empty[Int, SimpleResponseHolder]
    private val requestConsumers       = ConsumerContainer[DefaultRequestChannelBundle]()
    @volatile private var requestCount = 0

    //debug only
    private val source = scope.traffic.currentIdentifier

    AppLogger.vDebug(s"Created $this, of parent $parent")

    override def handleInjection(injection: PacketInjection): Unit = {
        val coords = injection.coordinates
        injection.attachPin { (packet, attr) =>
            packet match {
                case request: RequestPacket =>
                    AppLogger.vDebug(this)
                    AppLogger.vDebug(s"${currentTasksId} <> $source: INJECTING REQUEST $request with attributes ${request.getAttributes}" + this)
                    request.setAttributes(attr)

                    val submitterScope = scope.shareWriter(ChannelScopes.include(coords.senderID))
                    val submitter      = new ResponseSubmitter(request.id, submitterScope)

                    requestConsumers.applyAllLater(DefaultRequestChannelBundle(this, request, coords, submitter))

                case response: ResponsePacket =>
                    AppLogger.vDebug(s"${currentTasksId} <> $source: INJECTING RESPONSE $response with attributes ${response.getAttributes}" + this)
                    response.setAttributes(attr)

                    val responseID = response.id
                    requestHolders.get(responseID) match {
                        case Some(request)                     => request.pushResponse(response)
                        case None if responseID > requestCount =>
                            throw new NoSuchElementException(s"(${Thread.currentThread().getName}) Response.id not found (${response.id}) ($requestHolders)")
                        case _                                 =>
                    }
            }
        }
    }

    override def addRequestListener(callback: RequestPacketBundle => Unit): Unit = {
        requestConsumers += callback
    }

    override def makeRequest(scopeFactory: ScopeFactory[_ <: ChannelScope]): Submitter[ResponseHolder] = {
        val writer = traffic.newWriter(identifier)
        makeRequest(scopeFactory(writer))
    }

    override def makeRequest(scope: ChannelScope): Submitter[ResponseHolder] = {
        if (scope.writer.path != identifier)
            throw new IllegalArgumentException("Scope is not set on the same injectable id of this packet channel.")
        val requestID = nextRequestID
        //TODO Make an adaptive queue that make non WorkerPool threads wait and worker pools change task when polling.
        val queue = WorkerPools.currentPool.map(_.newBusyQueue[AbstractSubmitterPacket]).getOrElse(new LinkedBlockingQueue[AbstractSubmitterPacket]())
        new RequestSubmitter(requestID, scope, queue, this)
    }

    private def nextRequestID: Int = {
        requestCount += 1
        requestCount
    }

    private[request] def addRequestHolder(holder: SimpleResponseHolder): Unit = {
        requestHolders.put(holder.id, holder)
    }

    private[request] def removeRequestHolder(holder: SimpleResponseHolder): Unit = {
        requestHolders -= holder.id
    }

}

object SimpleRequestPacketChannel extends PacketInjectableFactory[SimpleRequestPacketChannel] {

    override def createNew(@Nullable parent: PacketChannel, scope: ChannelScope): SimpleRequestPacketChannel = new SimpleRequestPacketChannel(parent, scope)
}
