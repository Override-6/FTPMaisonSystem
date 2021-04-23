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

package fr.linkit.core.local.resource.entry

import fr.linkit.api.local.resource.external.{ExternalResource, ResourceEntry, ResourceFolder}
import fr.linkit.api.local.system.Versions
import fr.linkit.api.local.system.fsa.FileAdapter
import fr.linkit.core.local.resource.ResourceFolderMaintainer
import fr.linkit.core.local.system.StaticVersions
import org.jetbrains.annotations.Nullable

abstract class AbstractResource(@Nullable parent: ResourceFolder, adapter: FileAdapter) extends ExternalResource {

    override val name: String = adapter.getName

    protected val entry = new DefaultResourceEntry[this.type](this)

    private val lastModified = getMaintainer.getLastModified(name)

    protected def getMaintainer: ResourceFolderMaintainer

    def getEntry: ResourceEntry[this.type] = entry

    override def getLocation: String = {
        if (parent == null)
            return "/"
        parent.getLocation + '/' + name
    }

    override def getLastModified: Versions = lastModified

    override def getParent: ResourceFolder = parent

    override def getRoot: ResourceFolder = {
        var lastParent: ResourceFolder = getParent
        while (lastParent != null)
            lastParent = lastParent.getParent
        lastParent
    }

    override def getAdapter: FileAdapter = adapter

    override def getChecksum: Long

    override def getLastChecksum: Long = getMaintainer.getLastChecksum(name)

    override def markAsModifiedByCurrentApp(): Unit = {
        lastModified.setAll(StaticVersions.currentVersion)
        if (getParent != null)
            getParent.markAsModifiedByCurrentApp()
    }
}