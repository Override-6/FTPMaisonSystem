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

package fr.linkit.engine.gnom.cache.sync.contract.behavior

import fr.linkit.api.gnom.cache.sync.contract.StructureContract
import fr.linkit.api.gnom.cache.sync.contract.behavior.{ObjectContractFactory, ConnectedObjectContext}
import fr.linkit.api.gnom.cache.sync.contract.descriptor.ContractDescriptorData
import fr.linkit.api.gnom.cache.sync.contract.modification.ValueModifier

class SyncObjectContractFactory(override val data: ContractDescriptorData) extends ObjectContractFactory {


    override def getContract[A <: AnyRef](clazz: Class[A], context: ConnectedObjectContext): StructureContract[A] = {
        data.getNode[A](clazz).getContract(clazz, context: ConnectedObjectContext)
    }

    override def getInstanceModifier[A <: AnyRef](clazz: Class[A], limit: Class[_ >: A]): ValueModifier[A] = {
        data.getNode[A](clazz).getInstanceModifier(this, limit)
    }
}

object SyncObjectContractFactory {

    def apply(data: ContractDescriptorData): SyncObjectContractFactory = new SyncObjectContractFactory(data)
}