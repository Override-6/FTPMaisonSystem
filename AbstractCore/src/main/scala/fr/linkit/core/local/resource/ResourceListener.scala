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

package fr.linkit.core.local.resource

import java.nio.file._
import java.util

import fr.linkit.api.local.resource.{ResourcesMaintainer, ResourcesMaintainerInformer}

import scala.collection.mutable

class ResourceListener(resourcePath: String) {

    private val rootPath              = Path.of(resourcePath)
    private val watcher: WatchService = FileSystems.getDefault.newWatchService()
    private val informers           = new mutable.HashMap[String, ResourcesMaintainerInformer]()

    @volatile private var alive = false

    def startWatchService(): Unit = {
        if (alive)
            throw new IllegalStateException("This Resource folder event listener is already alive !")
        alive = true
        new Thread(() => {
            while (alive) {
                val key    = watcher.take()
                val events = key.pollEvents().asInstanceOf[util.List[WatchEvent[Path]]]
                events.forEach(event => {
                    val path   = rootPath.resolve(event.context())

                    val folder = path.getParent
                    println(s"file updated ${path}")
                    println(s"in folder $folder")
                    informers.get(folder.toString).fold() { informer =>
                        informer.informLocalModification(path.getFileName.toString)
                    }
                })
                key.reset()
            }
        }, "Resources Maintainers Listener").start()
    }

    def close(): Unit = {
        alive = false
        watcher.close()
    }

    def putMaintainer(maintainer: ResourcesMaintainer with ResourcesMaintainerInformer): Unit = {

        val behaviorOptions = maintainer.getBehaviors
        if (behaviorOptions.isEmpty)
            return

        val location = maintainer.getResources.getLocation
        informers.put(location, maintainer)
        val path = Path.of(location)

        path.register(watcher, behaviorOptions.map(_.getWatchEventKind): _*)
    }

}
