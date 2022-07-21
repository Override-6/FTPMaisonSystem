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

package fr.linkit.api.gnom.reference

import fr.linkit.api.gnom.reference.linker.ContextObjectLinker

/**
 *  Extend this trait to create a network object.
 *  The object's reference is sent instead of the object.
 *  learn more on the wiki: <a href="https://override-6.github.io/Linkit/docs/GNOM/Naming/What%20Is%20A%20Network%20Object">click here</a>
 *  @tparam R the type of reference of the network object
 * */
trait NetworkObject[+R <: NetworkObjectReference] {

    /**
     * The reference of this Network Object.
     * @see [[NetworkObjectReference]]
     * */
    def reference: R

}

object NetworkObject {
    implicit def unwrap[R <: NetworkObjectReference](o: NetworkObject[R]): R = o.reference
}
