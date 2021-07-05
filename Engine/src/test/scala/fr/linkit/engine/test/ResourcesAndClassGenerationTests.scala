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

package fr.linkit.engine.test

import fr.linkit.api.connection.cache.repo.description.{PuppetDescription, PuppeteerDescription}
import fr.linkit.api.connection.cache.repo.generation.GeneratedClassClassLoader
import fr.linkit.api.local.generation.TypeVariableTranslator
import fr.linkit.api.local.resource.external.ResourceFolder
import fr.linkit.api.local.system.config.ApplicationConfiguration
import fr.linkit.api.local.system.fsa.FileSystemAdapter
import fr.linkit.api.local.system.security.ApplicationSecurityManager
import fr.linkit.api.local.system.{AppLogger, Version}
import fr.linkit.engine.connection.cache.repo.SimplePuppeteer
import fr.linkit.engine.connection.cache.repo.generation.rectifier.ByteCodeRectifier
import fr.linkit.engine.connection.cache.repo.generation.{PuppetWrapperClassGenerator, WrapperInstantiator, WrappersClassResource}
import fr.linkit.engine.local.LinkitApplication
import fr.linkit.engine.local.generation.compilation.access.DefaultCompilerCenter
import fr.linkit.engine.local.resource.external.LocalResourceFolder._
import fr.linkit.engine.local.system.fsa.LocalFileSystemAdapters
import fr.linkit.engine.test.ScalaReflectionTests.TestClass
import fr.linkit.engine.test.classes.ScalaClass
import fr.linkit.engine.test.objects.PlayerObject
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation
import org.junit.jupiter.api.TestInstance.Lifecycle
import org.junit.jupiter.api._
import org.mockito.Mockito

import java.nio.file.Path
import java.util
import scala.collection.mutable.ListBuffer

@TestInstance(Lifecycle.PER_CLASS)
@TestMethodOrder(classOf[OrderAnnotation])
class ResourcesAndClassGenerationTests {

    private var resources: ResourceFolder    = _
    private val app      : LinkitApplication = Mockito.mock(classOf[LinkitApplication])

    @BeforeAll
    def init(): Unit = {
        val config      = new ApplicationConfiguration {
            override val pluginFolder   : Option[String]             = None
            override val resourceFolder : String                     = System.getenv("LinkitHome")
            override val fsAdapter      : FileSystemAdapter          = LocalFileSystemAdapters.Nio
            override val securityManager: ApplicationSecurityManager = null
        }
        val testVersion = Version("Tests", "0.0.0", false)
        System.setProperty("LinkitImplementationVersion", testVersion.toString)

        resources = LinkitApplication.prepareApplication(testVersion, config, Seq(getClass))
        Mockito.when(app.getAppResources).thenReturn(resources)
        LinkitApplication.setInstance(app)
        AppLogger.useVerbose = true
    }

    @Test
    @Order(-1)
    def genericParameterTests(): Unit = {
        val testMethod  = classOf[TestClass].getMethod("genericMethod2")
        val javaResult  = TypeVariableTranslator.toJavaDeclaration(testMethod.getTypeParameters)
        val scalaResult = TypeVariableTranslator.toScalaDeclaration(testMethod.getTypeParameters)
        println(s"Java result = ${javaResult}")
        println(s"Scala result = ${scalaResult}")
    }

    @Test
    @Order(0)
    def methodsSignatureTests(): Unit = {
        val clazz = classOf[TestClass]
        println(s"clazz = ${clazz}")
        val methods = clazz.getDeclaredMethods
        methods.foreach { method =>
            val args = method.getGenericParameterTypes
            println(s"args = ${args.mkString("Array(", ", ", ")")}")
        }
        println("qsd")
    }

    @Test
    @Order(2)
    def generateSimpleClass(): Unit = {
        val obj = forObject(new ScalaClass)
        println(s"obj = ${obj}")
        obj.testRMI()
    }

    @Test
    @Order(3)
    def generateComplexScalaClass(): Unit = {
        val buff = forObject(ListBuffer.empty[String]) += "sdqzd"
        println(s"buff = ${buff}")
    }

    @Test
    def classModificationTests(): Unit = {
        val classPath     = "C:\\Users\\maxim\\Desktop\\Dev\\Linkit\\Home\\CompilationCenter\\Classes"
        val loader        = new GeneratedClassClassLoader(Path.of(classPath), getClass.getClassLoader)
        val modifier      = new ByteCodeRectifier("gen.scala.collection.mutable.PuppetListBuffer", loader, classOf[ListBuffer[_]])
        val modifiedClass = modifier.rectifiedClass
        println(s"modifiedClass = ${modifiedClass}")
        println(s"modifiedClass.getSuperclass = ${modifiedClass.getSuperclass}")
        /*val path = Path.of("C:\\Users\\maxim\\Desktop\\BCScanning\\PuppetListBufferExtendingListBufferAndWithSuperCalls.class")
        val bytes = Files.readAllBytes(path)
        val result = ListBuffer.from(Files.readAllBytes(path))
        val fileBuff = ByteBuffer.wrap(bytes)

        if (fileBuff.order(ByteOrder.BIG_ENDIAN).getInt != 0xcafebabe) //Head
            throw new IllegalClassFormatException()

        val minor: Int = fileBuff.getChar
        val version: Int = fileBuff.getChar
        println(s"class file version: $version.$minor")
        val constantPoolLength: Int = fileBuff.getChar
        println(s"constantPoolLength = ${constantPoolLength}")

        println("end.")
        println(s"Next flag is : ${fileBuff.get}")*/
    }

    @Test
    @Order(3)
    def generateComplexJavaClass(): Unit = {
        forObject(new util.ArrayList[String]())
    }

    private def forObject(obj: Any): obj.type = {
        Assertions.assertNotNull(resources)
        val cl = obj.getClass.asInstanceOf[Class[obj.type]]

        val resource    = resources.getOrOpenThenRepresent[WrappersClassResource](LinkitApplication.getProperty("compilation.working_dir.classes"))
        val generator   = new PuppetWrapperClassGenerator(new DefaultCompilerCenter, resource)
        val puppetClass = generator.getClass[obj.type](cl)
        println(s"puppetClass = ${puppetClass}")
        val pup    = new SimplePuppeteer[obj.type](null, null, PuppeteerDescription("", 8, "", Array(1)), PuppetDescription[obj.type](cl))
        val puppet = WrapperInstantiator.instantiateFromOrigin[obj.type](puppetClass, obj)
        puppet.initPuppeteer(pup)
        //println(s"puppet = ${puppet}")
        puppet
    }

}

