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

package fr.linkit.engine.gnom.cache.sync.contract.behavior

import fr.linkit.api.gnom.cache.sync.contract.SynchronizedStructureContract
import fr.linkit.api.gnom.cache.sync.contract.behavior.{AgreementContext, SynchronizedObjectContractFactory}
import fr.linkit.engine.gnom.cache.sync.contract.builder.ObjectBehaviorDescriptor
import fr.linkit.engine.internal.utils.ClassMap

class SyncObjectContractFactory(descriptions: Array[ObjectBehaviorDescriptor[_]]) extends SynchronizedObjectContractFactory {

    private val nodeMap = createNodes(descriptions)

    override def getObjectContract[A <: AnyRef](clazz: Class[_], context: AgreementContext): SynchronizedStructureContract[A] = {
        nodeMap.get(clazz).get.getContract(clazz, context: AgreementContext).asInstanceOf[SynchronizedStructureContract[A]]
    }

    private def createNodes(descriptors: Array[ObjectBehaviorDescriptor[_]]): ClassMap[BehaviorDescriptorNode[_]] = {
        descriptors
            .sortInPlace()((a, b) => {
                getClassHierarchicalDepth(a.targetClass) - getClassHierarchicalDepth(b.targetClass)
            })
        val relations        = new ClassMap[SyncObjectClassRelation[AnyRef]]()
        val objectDescriptor = descriptors.head
        if (objectDescriptor.targetClass != classOf[Object])
            throw new IllegalArgumentException("Descriptions must contain the java.lang.Object type behavior description.")

        val objectRelation = new SyncObjectClassRelation[AnyRef](cast(objectDescriptor), null)
        relations.put(objectDescriptor.targetClass, objectRelation)
        for (descriptor <- descriptors) {
            val clazz  = descriptor.targetClass
            val parent = relations.get(clazz).getOrElse(objectRelation) //should at least return the java.lang.Object behavior descriptor
            relations.put(clazz, cast(new SyncObjectClassRelation(cast(descriptor), cast(parent))))
        }
        for ((clazz, relation) <- relations) {
            val interfaces = clazz.getInterfaces
            for (interface <- interfaces) {
                val interfaceRelation = relations.get(interface).getOrElse(objectRelation) //should at least return the java.lang.Object behavior relation
                relation.addInterface(cast(interfaceRelation))
            }
        }
        val map = relations.map(pair => (pair._1, pair._2.toNode)).toMap
        new ClassMap[BehaviorDescriptorNode[_]](map)
    }

    private def cast[X](y: Any): X = y.asInstanceOf[X]

    private def getClassHierarchicalDepth(clazz: Class[_]): Int = {
        if (clazz == null)
            throw new NullPointerException("clazz is null")
        if (clazz eq classOf[Object])
            return 0
        var cl    = clazz.getSuperclass
        var depth = 1
        while (cl ne null) {
            cl = cl.getSuperclass
            depth += 1
        }
        depth
    }

}
