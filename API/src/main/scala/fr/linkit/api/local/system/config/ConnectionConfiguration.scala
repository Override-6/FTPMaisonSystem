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

package fr.linkit.api.local.system.config

import fr.linkit.api.connection.packet.persistence.PacketTranslator
import fr.linkit.api.connection.packet.persistence.context.PersistenceConfig
import fr.linkit.api.connection.packet.traffic.PacketTraffic
import fr.linkit.api.local.ApplicationContext
import fr.linkit.api.local.system.security.BytesHasher

trait ConnectionConfiguration {

    val configName: String

    val identifier: String

    val hasher: BytesHasher

    val translatorFactory: (ApplicationContext) => PacketTranslator

    val defaultPersistenceConfig: PersistenceConfig

}
