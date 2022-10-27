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

package fr.linkit.api.gnom.packet.traffic

import fr.linkit.api.gnom.network.EngineTag
import fr.linkit.api.gnom.packet.{Packet, PacketAttributes}

trait PacketWriter {

    val serverName       : String
    val currentEngineName: String
    val path             : Array[Int]
    val traffic          : PacketTraffic

    def writePackets(packet: Packet, targetIDs: Array[EngineTag], excludeTargets: Boolean): Unit

    def writePackets(packet: Packet, attributes: PacketAttributes, targetTags: Array[EngineTag], excludeTargets: Boolean): Unit


}
