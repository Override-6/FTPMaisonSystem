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

package fr.linkit.core.connection.network.cache

import fr.linkit.api.connection.network.cache.{HandleableSharedCache, SharedCacheFactory, SharedCacheManager}
import fr.linkit.api.connection.packet.traffic.{ChannelScope, PacketSender, PacketSyncReceiver, PacketTraffic}
import fr.linkit.api.connection.packet.{DedicatedPacketCoordinates, Packet, PacketCoordinates}
import fr.linkit.core.connection.network.cache.SimpleSharedCacheManager.{MockCache, RequestSender}
import fr.linkit.core.connection.network.cache.map.SharedMap
import fr.linkit.core.connection.packet.fundamental.RefPacket.ArrayObjectPacket
import fr.linkit.core.connection.packet.fundamental.ValPacket.LongPacket
import fr.linkit.core.connection.packet.fundamental.WrappedPacket
import fr.linkit.core.connection.packet.traffic.channel.CommunicationPacketChannel

import java.util.NoSuchElementException
import scala.collection.mutable
import scala.reflect.{ClassTag, classTag}
import scala.util.control.NonFatal

//FIXME: Critical bug occurred when a lot of clients are connecting to the server,
// packets begin to shift, they are injected multiple times (maybe due to packet coordinates(id/senderID) ambiguity into the PacketInjections class)
// and this is a big problem for this class to initialise completely, which is a big problem for the network's initialisation,
// which is a big problem for the client relay's initialisation....

//TODO Use Array[Serializable] instead of Array[Any] for shared contents
//TODO replace Longs with Ints (be aware that, with the current serialization algorithm,
// primitives integers are all converted to Long, so it would cause cast problems until the algorithm is modified)
class SimpleSharedCacheManager(override val family: String,
                               override val ownerID: String,
                               traffic: PacketTraffic) extends SharedCacheManager {

    protected val communicator: RequestSender =
        traffic.getInjectable(11, ChannelScope.broadcast, new RequestSender(_))

    private val sharedObjects: map.SharedMap[Long, Serializable] = init()
    println(s"sharedObjects = $sharedObjects")

    override def post[A <: Serializable](key: Long, value: A): A = {
        sharedObjects.put(key, value)
        value
    }

    override def get[A <: Serializable](key: Long): Option[A] = sharedObjects.get(key).asInstanceOf[Option[A]]

    override def getOrWait[A <: Serializable](key: Long): A = sharedObjects.getOrWait(key).asInstanceOf[A]

    override def apply[A <: Serializable](key: Long): A = sharedObjects(key).asInstanceOf[A]

    override def get[A <: HandleableSharedCache : ClassTag](cacheID: Long, factory: SharedCacheFactory[A]): A = {
        LocalCacheHandler
            .findCache[A](cacheID)
            .getOrElse {
                println(s"OPENING CACHE $cacheID OF TYPE ${classTag[A].runtimeClass}")
                val baseContent = retrieveBaseContent(cacheID)
                println(s"CONTENT RECEIVED (${baseContent.mkString("Array(", ", ", ")")}) FOR CACHE $cacheID")
                val sharedCache = factory.createNew(this, cacheID, baseContent, communicator)
                LocalCacheHandler.register(cacheID, sharedCache)
                sharedCache
            }
    }

    override def getUpdated[A <: HandleableSharedCache : ClassTag](cacheID: Long, factory: SharedCacheFactory[A]): A = {
        get(cacheID, factory).update()
    }


    def forget(cacheID: Long): Unit = {
        LocalCacheHandler.unregister(cacheID)
    }

    override def update(): this.type = {
        LocalCacheHandler.updateAll()
        //sharedObjects will be updated by LocalCacheHandler.updateAll
        this
    }

    private def retrieveBaseContent(cacheID: Long): Array[Any] = {
        println(s"Sending request to server in order to retrieve content of cache number $cacheID")
        communicator.sendRequest(WrappedPacket(family, LongPacket(cacheID)), ownerID)
        println(s"request sent !")
        val content = communicator.nextResponse[ArrayObjectPacket].value //The request will return the cache content
        println(s"Content received ! (${content.mkString("Array(", ", ", ")")})")
        content.asInstanceOf[Array[Any]]
    }

    private def init(): SharedMap[Long, Serializable] = {
        if (this.sharedObjects != null)
            throw new IllegalStateException("This SharedCacheManager is already initialised !")

        initPacketHandling()

        val content = retrieveBaseContent(1)

        val cacheOwners = SharedMap[Long, Serializable].createNew(this, 1, content, communicator)
        LocalCacheHandler.register(1L, cacheOwners)
        cacheOwners
            .foreachKeys(LocalCacheHandler.registerMock) //mock all current caches that are registered on this family

        cacheOwners

    }

    private def initPacketHandling(): Unit = {
        communicator.addRequestListener((packet, coords) => {
            packet match {
                case WrappedPacket(tag, subPacket) =>
                    if (tag == family)
                        handlePacket(subPacket, coords)
            }
        })
    }

    private def handlePacket(packet: Packet, coords: DedicatedPacketCoordinates): Unit = {
        println(s"HANDLING PACKET $packet, $coords")

        packet match {
            //Normal packet
            case WrappedPacket(key, subPacket) =>
                LocalCacheHandler.injectPacket(key.toLong, subPacket, coords)

            case LongPacket(cacheID) =>
                val senderID: String = coords.senderID
                //println(s"RECEIVED CONTENT REQUEST FOR IDENTIFIER $cacheID REQUESTOR : $senderID")
                val content = LocalCacheHandler.getContentOrElseMock(cacheID)
                //println(s"Content = ${content.mkString("Array(", ", ", ")")}")
                communicator.sendResponse(ArrayObjectPacket(content), senderID)
        }
    }

    protected object LocalCacheHandler {

        private val localRegisteredCaches = mutable.Map.empty[Long, HandleableSharedCache]

        def updateAll(): Unit = {
            println(s"updating cache ($localRegisteredCaches)...")
            localRegisteredCaches
                .foreach(_._2.update())
            println(s"cache updated ! ($localRegisteredCaches)")
        }

        def register(identifier: Long, cache: HandleableSharedCache): Unit = {
            println(s"Registering $identifier into local cache.")
            localRegisteredCaches.put(identifier, cache)
            println(s"Local cache is now $localRegisteredCaches")
        }

        def unregister(identifier: Long): Unit = {
            println(s"Removing cache $identifier")
            localRegisteredCaches.remove(identifier)
            println(s"Cache is now $identifier")
        }

        def injectPacket(cacheID: Long, packet: Packet, coords: PacketCoordinates): Unit = try {
            localRegisteredCaches(cacheID).handlePacket(packet, coords)
        } catch {
            case _: NoSuchElementException =>
                println(s"Mocked $cacheID")
                registerMock(cacheID)
            case NonFatal(e) => e.printStackTrace(Console.out)
        }

        def registerMock(identifier: Long): Unit = {
            localRegisteredCaches.put(identifier, MockCache)
        }

        def getContent(cacheID: Long): Array[Any] = {
            localRegisteredCaches(cacheID).currentContent
        }

        def getContentOrElseMock(cacheID: Long): Array[Any] = {
            val opt = localRegisteredCaches.get(cacheID)
            if (opt.isEmpty) {
                registerMock(cacheID)
                return Array()
            }
            opt.get.currentContent
        }

        def isRegistered(cacheID: Long): Boolean = {
            localRegisteredCaches.contains(cacheID)
        }

        def findCache[A: ClassTag](cacheID: Long): Option[A] = {
            val opt = localRegisteredCaches.get(cacheID).asInstanceOf[Option[A]]
            if (opt.exists(_.isInstanceOf[MockCache.type]))
                return None

            if (opt.exists(!_.isInstanceOf[A])) {
                val requestedClass = classTag[A].runtimeClass
                val presentClass = opt.get.getClass
                throw new IllegalArgumentException(s"Attempted to open a cache of type '$cacheID' while a cache with the same id is already registered, but does not have the same type. ($presentClass vs $requestedClass)")
            }

            opt
        }

        override def toString: String = localRegisteredCaches.toString()

    }

    private def println(msg: String): Unit = {
        //Console.println(s"<$family, $ownerID> $msg")
    }

}

object SimpleSharedCacheManager {

    private val caches = mutable.HashMap.empty[(String, PacketTraffic), SharedCacheManager]

    def get(family: String, ownerIdentifier: String)
           (implicit traffic: PacketTraffic): SharedCacheManager = {

        caches.get((family, traffic))
            .fold {
                //println(s"--> CREATING SHARED CACHE HANDLER <$family>")
                val cache = new SimpleSharedCacheManager(family, ownerIdentifier, traffic)
                //println(s"--> SHARED CACHE HANDLER CREATED <$family>")
                caches.put((family, traffic), cache)
                cache: SharedCacheManager
            }(cache => {
                //println(s"--> UPDATING CACHE <$family> INSTEAD OF CREATING IT.")
                cache.update()
                //println(s"--> UPDATED CACHE <$family> INSTEAD OF CREATING IT.")
                cache
            })
    }

    private[SimpleSharedCacheManager] class RequestSender(scope: ChannelScope) extends CommunicationPacketChannel(scope, true) with PacketSender with PacketSyncReceiver {
        override def send(packet: Packet): Unit = sendRequest(packet)

        override def sendTo(packet: Packet, targets: String*): Unit = sendRequest(packet, targets: _*)

        override def nextPacket[P <: Packet]: P = nextResponse

        override def haveMorePackets: Boolean = false
    }

    object MockCache extends AbstractSharedCache[Nothing](null, -1, null) {

        override val family: String = ""

        override var autoFlush: Boolean = false

        override def handlePacket(packet: Packet, coords: PacketCoordinates): Unit = ()

        override def currentContent: Array[Any] = Array()

        override def flush(): this.type = this

        override def modificationCount(): Int = -1

        override def update(): this.type = this

        override protected def setCurrentContent(content: Array[Nothing]): Unit = ()
    }

}