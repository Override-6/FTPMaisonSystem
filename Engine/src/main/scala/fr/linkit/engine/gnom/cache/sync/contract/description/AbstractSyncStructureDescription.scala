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

package fr.linkit.engine.gnom.cache.sync.contract.description

import fr.linkit.api.gnom.cache.sync.contract.description._
import fr.linkit.engine.gnom.cache.sync.generation.sync.SyncObjectClassResource._

import java.lang.reflect._
import scala.collection.mutable.ListBuffer

abstract class AbstractSyncStructureDescription[A <: AnyRef](override val specs: SyncClassDef) extends SyncStructureDescription[A] {

    private val methodDescriptions: Map[Int, MethodDescription] = collectMethods()
    private val fieldDescriptions : Map[Int, FieldDescription]  = collectFields()

    //The generated class name
    override def classPackage: String = GeneratedClassesPackage + specs.mainClass.getPackageName

    override def className: String = specs.mainClass.getSimpleName + SyncSuffixName + s"_${specs.id}"

    override def parentLoader: ClassLoader = specs.mainClass.getClassLoader

    override def listMethods(): Iterable[MethodDescription] = {
        methodDescriptions.values
    }

    override def listFields(): Iterable[FieldDescription] = {
        fieldDescriptions.values
    }

    override def findMethodDescription(methodID: Int): Option[MethodDescription] = {
        methodDescriptions.get(methodID)
    }

    override def findMethodDescription(methodName: String, params: Seq[Class[_]]): Option[MethodDescription] = {
        methodDescriptions.values.find(m => m.javaMethod.getName == methodName && m.javaMethod.getParameterTypes.toSeq == params)
    }

    override def findFieldDescription(fieldName: String): Option[FieldDescription] = {
        fieldDescriptions.values.find(_.javaField.getName == fieldName)
    }

    override def findFieldDescription(fieldID: Int): Option[FieldDescription] = {
        fieldDescriptions.get(fieldID)
    }

    private def collectMethods(): Map[Int, MethodDescription] = {
        getFiltered
                .map(toMethodDesc)
                .map(desc => (desc.methodId, desc))
                .toMap
    }

    protected def toMethodDesc(method: Method): MethodDescription = new MethodDescription(method, this)

    protected def getAllMethods: Seq[Method] = {
        val buff = ListBuffer.empty[Method]

        def addAllMethods(clazz: Class[_]): Unit = {
            buff ++= clazz.getMethods
            if (clazz.isInterface)
                buff ++= classOf[Object].getDeclaredMethods
            var cl: Class[_] = clazz
            while (cl != null) {
                buff ++= cl.getDeclaredMethods.filter(m => Modifier.isProtected(m.getModifiers))
                cl = cl.getSuperclass
            }
        }

        addAllMethods(specs.mainClass)
        specs match {
            case multiple: SyncClassDefMultiple =>
                multiple.interfaces.foreach(addAllMethods)
            case _                              =>
        }
        buff.toSeq
    }

    protected def applyNotFilter(e: Executable): Boolean

    private def getFiltered: Iterable[Method] = {
        getAllMethods
                .distinctBy(m => (m.getName, m.getParameterTypes, m.getReturnType))
                .filterNot(applyNotFilter)
    }

    private def collectFields(): Map[Int, FieldDescription] = {
        val fields          = ListBuffer.empty[Field]
        var clazz: Class[_] = this.specs.mainClass
        while (clazz != null) {
            fields ++= clazz.getDeclaredFields
                    .filterNot(f => Modifier.isStatic(f.getModifiers))
                    .filter(setAccessible)
            clazz = clazz.getSuperclass
        }
        fields.map(FieldDescription(_, this))
                .map(desc => (desc.fieldId, desc))
                .toMap
    }

    private def setAccessible(f: Field): Boolean = {
        try {
            f.setAccessible(true)
            true
        } catch {
            case _: InaccessibleObjectException => false
        }
    }

}

