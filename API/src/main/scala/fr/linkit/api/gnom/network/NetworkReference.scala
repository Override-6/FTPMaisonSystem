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

package fr.linkit.api.gnom.network

import fr.linkit.api.gnom.reference.{NetworkObjectReference, SystemObjectReference}

class NetworkReference() extends NetworkObjectReference {

    override def toString: String = s"@network"

    override def hashCode(): Int = 33

    override def equals(obj: Any): Boolean = obj.isInstanceOf[NetworkReference]

}

object NetworkReference extends NetworkReference with SystemObjectReference