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

package fr.linkit.api.application.resource.local

import fr.linkit.api.application.resource.ResourceListener
import fr.linkit.api.internal.system.delegate.DelegateFactory

import java.nio.file.Path

trait LocalFolder extends LocalResource with ResourceFolder {
    
    override def getEntry: ResourceEntry[LocalFolder]
    
    /**
     * Performs a non-recursive scan of all the content of this folder, excluding folders.
     * Each times the scan hits a resource that is not yet registered, the scanAction gets called.
     * scanAction may determine whether the hit resource must be registered or not, attached by
     * any representation kind, or destroyed...
     *
     * The implementation can perform default operations before or after invoking the scanAction.
     *
     * @param scanAction the action to perform on each new resource.
     * */
    def scanFiles(scanAction: String => Unit): Unit
    
    /**
     * Performs a non-recursive scan of all the content of this folder, excluding files.
     * Each times the scan hits a resource that is not yet registered, the scanAction gets called.
     * scanAction may determine whether the hit resource must be registered or not, attached by
     * any representation kind, or destroyed...
     *
     * The implementation can perform default operations before or after invoking the scanAction.
     *
     * @param scanAction the action to perform on each new resource.
     * */
    def scanFolders(scanAction: String => Unit): Unit
    
}

object LocalFolder extends ResourceFactory[LocalFolder] {
    
    private val factories = DelegateFactory.resourceFactories
    
    override def apply(adapter: Path, listener: ResourceListener, parent: Option[ResourceFolder]): LocalFolder = {
        factories.localFolderFactory(adapter, listener, parent)
    }
}