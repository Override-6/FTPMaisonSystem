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

package fr.linkit.engine.connection.packet.persistence.v3.serialisation.node

import fr.linkit.api.connection.packet.persistence.v3.serialisation.SerialOutputStream
import fr.linkit.api.connection.packet.persistence.v3.serialisation.node.SerializerNode
import fr.linkit.engine.connection.packet.persistence.v3.serialisation.node.NullInstanceNode._

class NullInstanceNode(isOption: Boolean) extends SerializerNode {

    override def writeBytes(out: SerialOutputStream): Unit = {
        out.put(NullFlag)
        if (isOption)
            out.put(NoneFlag)
    }

}

object NullInstanceNode {

    val NullFlag: Byte = -74
    val NoneFlag: Byte = -73
}
