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

package fr.linkit.engine.connection.cache.obj.generation.bp

import fr.linkit.api.connection.cache.obj.description.{MethodDescription, SyncObjectSuperclassDescription}
import fr.linkit.api.local.generation.compilation.access.CompilerType
import fr.linkit.engine.connection.cache.obj.generation.bp.ScalaBlueprintUtilities._
import fr.linkit.engine.local.generation.cbp.AbstractClassBlueprint
import fr.linkit.engine.local.generation.compilation.access.CommonCompilerTypes

import java.io.InputStream

class ScalaWrapperClassBlueprint(in: InputStream) extends AbstractClassBlueprint[SyncObjectSuperclassDescription[_]](in) {

    override val compilerType: CompilerType = CommonCompilerTypes.Scalac

    override val rootScope: RootValueScope = new RootValueScope {
        bindValue("WrappedClassSimpleName" ~> (_.clazz.getSimpleName))
        bindValue("WrappedClassName" ~> (_.clazz.getTypeName.replaceAll("\\$", ".")))
        bindValue("TParamsIn" ~> (getGenericParams(_, _.asType.toType.finalResultType)))
        bindValue("TParamsOut" ~> (getGenericParams(_, _.name)))
        bindValue("TParamsInBusted" ~> (getGenericParams(_, _ => "_")))
        bindValue("BustedConstructor" ~> getBustedConstructor)

        bindSubScope(new ScalaWrapperMethodBlueprint.ValueScope("INHERITED_METHODS", _, _), (desc, action: MethodDescription => Unit) => {
            desc.listMethods()
                    .toSeq
                    .distinctBy(_.methodId)
                    // .filterNot(m => m.symbol.isSetter || m.symbol.isGetter)
                    .foreach(action)
        })

    }

    private def getBustedConstructor(desc: SyncObjectSuperclassDescription[_]): String = {
        desc.classType
                .decls
                .find(dec => dec.isConstructor && (dec.isPublic || dec.isProtected))
                .fold("") {
                    _.asMethod
                            .paramLists
                            .map(_.map(_ => "nl").mkString("(", ",", ")"))
                            .mkString(",")
                }
    }

}
