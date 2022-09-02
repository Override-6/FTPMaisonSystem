/*
 * Copyright (c) 2021. Linkit and or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR FILE HEADERS.
 *
 * This code is free software; you can USE it as you want.
 * You can download this source code, and modify it ONLY FOR PRIVATE USE but you
 * ARE NOT ALLOWED to distribute your MODIFIED VERSION.
 * For any professional use, please contact me at overridelinkit@gmail.com.
 *
 * Please contact overridelinkit@gmail.com if you need additional information or have any
 * questions.
 */

package fr.linkit.engine.gnom.cache.sync.invokation.remote

import fr.linkit.api.gnom.cache.sync.invocation.InvocationFailedException
import fr.linkit.api.gnom.cache.sync.invocation.remote.{DispatchableRemoteMethodInvocation, Puppeteer}
import fr.linkit.api.gnom.cache.sync._
import fr.linkit.api.gnom.network.Network
import fr.linkit.api.gnom.packet.Packet
import fr.linkit.api.gnom.packet.channel.ChannelScope
import fr.linkit.api.gnom.packet.channel.request.{RequestPacketChannel, ResponseHolder}
import fr.linkit.api.gnom.packet.traffic.InjectableTrafficNode
import fr.linkit.api.internal.concurrency.Procrastinator
import fr.linkit.engine.gnom.cache.sync.RMIExceptionString
import fr.linkit.engine.gnom.packet.fundamental.RefPacket
import fr.linkit.engine.gnom.packet.traffic.ChannelScopes
import fr.linkit.engine.internal.util.JavaUtils
import org.jetbrains.annotations.Nullable

class ObjectPuppeteer[S <: AnyRef](channel: RequestPacketChannel,
                                   override val cache: ConnectedObjectCache[_],
                                   override val nodeReference: ConnectedObjectReference) extends Puppeteer[S] {
    
    override val network          : Network = cache.network
    private  val traffic                    = channel.traffic
    override val currentIdentifier: String  = traffic.currentIdentifier
    private  val writer                     = traffic.newWriter(channel.trafficPath)
    private  val channelNode                = traffic.findNode(channel.trafficPath).get match {
        case node: InjectableTrafficNode[_] => node
        case _                              => throw new IllegalArgumentException(s"Packet Channel ${channel.reference} is referring to a non Injectable Traffic Node.")
    }
    
    override def isCurrentEngineOwner: Boolean = ownerID == currentIdentifier
    
    private def isPerformant: Boolean = channelNode.preferPerformances()
    
    override def sendInvokeAndWaitResult[R](invocation: DispatchableRemoteMethodInvocation[R]): R = {
        val agreement = invocation.agreement
        if (!agreement.mayPerformRemoteInvocation)
            throw new IllegalAccessException("the agreement states that the method should not be called on a remote engine")
        val desiredEngineReturn = agreement.getAppointedEngineReturn
        
        if (desiredEngineReturn == currentIdentifier)
            throw new UnsupportedOperationException("invocation's desired engine return is this engine.")
        
        val methodId         = invocation.methodID
        val scope            = new AgreementScope(writer, network, agreement)
        var requestResult: R = JavaUtils.nl()
        var isResultSet      = false
        val dispatcher       = new ObjectRMIDispatcher(scope, methodId, desiredEngineReturn) {
            override protected def handleResponseHolder(holder: ResponseHolder): Unit = {
                holder
                        .nextResponse
                        .nextPacket[Packet] match {
                    case RMIExceptionString(exceptionString) =>
                        throw new InvocationFailedException(s"Remote Method Invocation for method with id $methodId on object $nodeReference, executed on engine '$desiredEngineReturn' failed :\n$exceptionString")
                    case p: RefPacket[R]                     =>
                        requestResult = p.value
                        isResultSet = true
                }
            }
        }
        invocation.dispatchRMI(dispatcher.asInstanceOf[Puppeteer[AnyRef]#RMIDispatcher])
        if (!isResultSet)
            throw new IllegalStateException("RMI dispatch has been processed asynchronously.")
        requestResult match {
            case r: R with AnyRef => r
            case null             => null.asInstanceOf[R]
        }
    }
    
    override def sendInvoke(invocation: DispatchableRemoteMethodInvocation[_]): Unit = {
        val agreement = invocation.agreement
        if (!agreement.mayPerformRemoteInvocation)
            throw new IllegalAccessException("agreement may not perform remote invocation")
        
        val methodId = invocation.methodID
        
        runLaterIfPerformantChannelElseRun {
            val scope      = new AgreementScope(writer, network, agreement)
            val dispatcher = new ObjectRMIDispatcher(scope, methodId, null)
            invocation.dispatchRMI(dispatcher.asInstanceOf[Puppeteer[AnyRef]#RMIDispatcher])
        }
    }
    
    class ObjectRMIDispatcher(scope: AgreementScope, methodID: Int, @Nullable returnEngine: String) extends RMIDispatcher {
        
        override def broadcast(args: Array[Any]): Unit = {
            handleResponseHolder(makeRequest(scope, args))
        }
        
        private def makeRequest(scope: ChannelScope, args: Array[Any]): ResponseHolder = {
            channel.makeRequest(scope)
                    .addPacket(InvocationPacket(nodeReference, methodID, args, returnEngine))
                    .submit()
        }
        
        protected def handleResponseHolder(holder: ResponseHolder): Unit = ()
        
        override def foreachEngines(action: String => Array[Any]): Unit = {
            scope.foreachAcceptedEngines(engineID => runLaterIfPerformantChannelElseRun {
                //return engine is processed at last, don't send a request to the current engine
                if (engineID != returnEngine && engineID != currentIdentifier)
                    makeRequest(ChannelScopes.include(engineID)(writer), action(engineID))
            })
            if (returnEngine != null && returnEngine != currentIdentifier)
                handleResponseHolder(makeRequest(ChannelScopes.include(returnEngine)(writer), action(returnEngine)))
        }
    }
    
    private val procrastinator: Procrastinator = network.connection
    
    private def runLaterIfPerformantChannelElseRun(action: => Unit): Unit = {
        if (isPerformant) procrastinator.runLater(action)
        else action
    }
    
}

object ObjectPuppeteer {
    
    def apply[S <: AnyRef](channel: RequestPacketChannel, cache: ConnectedObjectCache[_], nodeLocation: ConnectedObjectReference): ObjectPuppeteer[S] = new ObjectPuppeteer(channel, cache, nodeLocation)
}