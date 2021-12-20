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

package fr.linkit.engine.gnom.cache.sync.tree.node

import fr.linkit.api.gnom.cache.sync.contract.SynchronizedStructureContract
import fr.linkit.api.gnom.cache.sync.invokation.InvocationChoreographer
import fr.linkit.api.gnom.cache.sync.invokation.local.Chip
import fr.linkit.api.gnom.cache.sync.invokation.remote.Puppeteer
import fr.linkit.api.gnom.cache.sync.tree.{NoSuchSyncNodeException, SyncNode, SyncObjectReference, SynchronizedObjectTree}
import fr.linkit.api.gnom.cache.sync.{CanNotSynchronizeException, SynchronizedObject}
import fr.linkit.api.gnom.packet.channel.request.Submitter
import fr.linkit.api.gnom.reference.presence.NetworkObjectPresence
import fr.linkit.engine.gnom.cache.sync.RMIExceptionString
import fr.linkit.engine.gnom.cache.sync.invokation.remote.InvocationPacket
import fr.linkit.engine.gnom.packet.UnexpectedPacketException
import fr.linkit.engine.gnom.packet.fundamental.RefPacket
import org.jetbrains.annotations.Nullable

import java.util.concurrent.ThreadLocalRandom
import scala.collection.mutable
import scala.util.control.NonFatal
import scala.util.{Failure, Success, Try}

class ObjectSyncNode[A <: AnyRef](@Nullable override val parent: SyncNode[_],
                                  data: ObjectNodeData[A]) extends TrafficInterestedSyncNode[A] {

    override  val reference         : SyncObjectReference              = data.reference
    override  val tree              : SynchronizedObjectTree[_]        = data.tree
    override  val puppeteer         : Puppeteer[A]                     = data.puppeteer
    override  val contract          : SynchronizedStructureContract[A] = data.contract
    override  val chip              : Chip[A]                          = data.chip
    override  val synchronizedObject: A with SynchronizedObject[A]     = data.synchronizedObject
    override  val id                : Int                              = reference.nodePath.last
    /**
     * The identifier of the engine that posted this object.
     */
    override  val ownerID           : String                           = puppeteer.ownerID
    /**
     * This map contains all the synchronized object of the parent object
     * including method return values and parameters and class fields
     * */
    protected val childs                                               = new mutable.HashMap[Int, ObjectSyncNode[_]]
    private   val currentIdentifier : String                           = data.currentIdentifier
    /**
     * This set stores every engine where this object is synchronized.
     * */
    override  val objectPresence    : NetworkObjectPresence            = data.presence
    private   val origin                                               = data.origin

    synchronizedObject.initialize(this)

    def addChild(node: ObjectSyncNode[_]): Unit = {
        if (node.parent ne this)
            throw new CanNotSynchronizeException("Attempted to add a child to this node that does not define this node as its parent.")
        if (childs.contains(node.id))
            throw new IllegalStateException(s"A Synchronized Object Node already exists at ${puppeteer.nodeLocation.nodePath.mkString("/") + s"/$id"}")
        childs.put(node.id, node)
    }

    def getChild[B <: AnyRef](id: Int): Option[ObjectSyncNode[B]] = (childs.get(id): Any) match {
        case None        => None
        case Some(value) => value match {
            case node: ObjectSyncNode[B] => Some(node)
            case _                       => None
        }
    }

    @Nullable
    def getMatchingSyncNode(nonSyncObject: AnyRef): SyncNode[_ <: AnyRef] = InvocationChoreographer.forceLocalInvocation {
        if (origin != null && nonSyncObject == origin)
            return this

        for (child <- childs.values) {
            val found = child.getMatchingSyncNode(nonSyncObject)
            if (found != null)
                return found
        }
        null
    }

    override def handlePacket(packet: InvocationPacket, senderID: String, response: Submitter[Unit]): Unit = {
        if (!(packet.path sameElements treePath)) {
            val packetPath = packet.path
            if (!packetPath.startsWith(treePath))
                throw UnexpectedPacketException(s"Received invocation packet that does not target this node or this node's children ${packetPath.mkString("/")}.")

            tree.findNode[AnyRef](packetPath.drop(treePath.length))
                    .fold[Unit](throw new NoSuchSyncNodeException(s"Received packet that aims for an unknown puppet children node (${packetPath.mkString("/")})")) {
                        case node: TrafficInterestedSyncNode[_] => node.handlePacket(packet, senderID, response)
                        case _                                  =>
                    }
        }
        makeMemberInvocation(packet, senderID, response)
    }

    private def makeMemberInvocation(packet: InvocationPacket, senderID: String, response: Submitter[Unit]): Unit = {
        Try(chip.callMethod(packet.methodID, packet.params, senderID)) match {
            case Success(value)     => handleInvocationResult(value.asInstanceOf[AnyRef], packet, response)
            case Failure(exception) => exception match {
                case NonFatal(e) =>
                    e.printStackTrace()
                    if (packet.expectedEngineIDReturn == currentIdentifier)
                        response.addPacket(RMIExceptionString(e.toString)).submit()
                case o           => throw o
            }
        }
    }

    private def handleInvocationResult(initialResult: AnyRef, packet: InvocationPacket, response: Submitter[Unit]): Unit = {
        var result   = initialResult
        val behavior = contract.behavior
        if (packet.expectedEngineIDReturn == currentIdentifier) {
            val methodBehavior      = behavior.getMethodBehavior(packet.methodID).get
            val returnValueBehavior = methodBehavior.returnValueBehavior
            if (result != null && returnValueBehavior.isActivated && !result.isInstanceOf[SynchronizedObject[_]]) {
                val id = ThreadLocalRandom.current().nextInt()
                //TODO modifier for RMI return value
                //val modifier = returnValueBehavior.modifier
                //result = modifier.toRemote(result, invocation)
                result = tree.insertObject(this, id, result, ownerID).synchronizedObject
            }
            response
                    .addPacket(RefPacket[Any](result))
                    .submit()
        }
    }

}
