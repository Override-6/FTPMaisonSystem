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

package fr.linkit.engine.connection.packet.persistence.v3.deserialisation.node

import fr.linkit.api.connection.packet.persistence.v3.deserialisation.DeserializationInputStream
import fr.linkit.api.connection.packet.persistence.v3.deserialisation.node.ObjectDeserializerNode
import fr.linkit.engine.local.utils.JavaUtils

class SimpleObjectDeserializerNode(deserializeAction: DeserializationInputStream => Any) extends ObjectDeserializerNode {

    protected override var ref: Any = _

    def setReference(ref: Any): Unit = {
        if (this.ref != null)
            throw new IllegalStateException("Reference is already set.")
        listeners.foreach(listener => listener(ref))
        this.ref = ref
    }

    override def deserialize(in: DeserializationInputStream): Any = {
        val returned = deserializeAction(in)
        if (ref == null)
            setReference(returned)
        returned
    }

}

object SimpleObjectDeserializerNode {

    def apply(ref: Any)(deserialize: DeserializationInputStream => Any): SimpleObjectDeserializerNode = {
        val node = new SimpleObjectDeserializerNode(deserialize)
        node.setReference(ref)
        node
    }

}