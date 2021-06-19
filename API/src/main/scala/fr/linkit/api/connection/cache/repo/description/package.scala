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

package fr.linkit.api.connection.cache.repo

import scala.reflect.api
import scala.reflect.api.{TypeCreator, Universe}
import scala.reflect.runtime.universe._

package object description {

    def toTypeTag[A](tpe: Type, loader: ClassLoader): TypeTag[A] = {
        TypeTag[A](runtimeMirror(loader), new TypeCreator {
            override def apply[U <: Universe with Singleton](m: api.Mirror[U]): U#Type = {
                tpe.asInstanceOf[U#Type]
            }
        })
    }

    def toTypeTag[A](clazz: Class[_]): TypeTag[A] = toTypeTag({
        runtimeMirror(clazz.getClassLoader)
                .staticClass(clazz.getName)
                .toType
    }, clazz.getClassLoader)

}