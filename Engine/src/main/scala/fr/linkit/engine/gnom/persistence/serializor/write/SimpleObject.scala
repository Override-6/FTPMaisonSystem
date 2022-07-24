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

package fr.linkit.engine.gnom.persistence.serializor.write

import fr.linkit.api.gnom.cache.sync.contract.description.SyncClassDef
import fr.linkit.api.gnom.persistence.context.TypeProfile
import fr.linkit.api.gnom.persistence.obj.ProfilePoolObject
import fr.linkit.engine.internal.utils.JavaUtils

class SimpleObject(override val value: AnyRef,
                   val valueClassRef: Either[Class[_], SyncClassDef],
                   val decomposed: Array[Any],
                   override val profile: TypeProfile[_]) extends ProfilePoolObject[AnyRef] {

    override def hashCode(): Int = identity

    override val identity: Int = System.identityHashCode(value)

    override def equals(obj: Any): Boolean = JavaUtils.sameInstance(obj, value)

    override def toString: String = value.toString
}