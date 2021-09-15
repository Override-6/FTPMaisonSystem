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

package fr.linkit.engine.connection.packet.persistence.context.script

import fr.linkit.api.local.script.ScriptContext
import fr.linkit.engine.connection.packet.persistence.context.script.ScriptPersistenceConfigHandler.ScriptPackage
import fr.linkit.engine.local.script.SimpleScriptHandler
import fr.linkit.engine.local.script.SimpleScriptHandler.ScriptName

class ScriptPersistenceConfigHandler extends SimpleScriptHandler[ScriptConfig] {

    override protected val className   : String = ScriptName
    override protected val classPackage: String = ScriptPackage

    override def newScriptContext(scriptSourceCode: String, scriptName: String, additionalArguments: Map[String, Class[_]], scriptClassLoader: ClassLoader): ScriptContext = {
        new ScriptConfigContext(scriptSourceCode, scriptName, classOf[ScriptConfig], additionalArguments, scriptClassLoader)
    }
}

object ScriptPersistenceConfigHandler {

    final val ScriptPackage = "gen.scala.persistence.config.script"
    final val ScriptName    = "ScalaPersistenceConfigScript_"
}
