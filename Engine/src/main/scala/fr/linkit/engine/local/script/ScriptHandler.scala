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

package fr.linkit.engine.local.script

import fr.linkit.engine.local.generation.compilation.resource.ClassFolderResource

import java.io.InputStream

trait ScriptHandler[S <: ScriptFile] {

    def scriptClassBlueprint: InputStream

    def findScript(name: String, classes: ClassFolderResource[ScriptFile]): Option[S]

    def newScript(clazz: Class[_ <: S]): S

    def newScriptContext(scriptSourceCode: String, scriptName: String, scriptClassLoader: ClassLoader): ScriptContext

}