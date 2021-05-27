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

package fr.linkit.engine.connection.cache.repo.generation

import java.nio.file.Path
import javax.tools.ToolProvider

object JavacCompilerAccess extends AbstractCompilerAccess {

    val JavaFileExtension = ".java"

    override def filePredicate(filePath: Path): Boolean = filePath.toString.endsWith(JavaFileExtension)

    override def compile(sourceFiles: Array[Path], destination: Path, classPaths: Seq[Path]): Int = {
        val javac                = ToolProvider.getSystemJavaCompiler
        val cpStrings            = classPaths.mkString(";")
        val options: Seq[String] = Seq[String]("-d", destination.toString, "-Xlint:none", "-classpath", cpStrings) ++ sourceFiles.map(_.toString)
        val code = javac.run(null, null, null, options: _*)
        code
    }
}
