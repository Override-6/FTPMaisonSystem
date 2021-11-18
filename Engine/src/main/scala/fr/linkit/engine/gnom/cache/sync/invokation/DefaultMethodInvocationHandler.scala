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

package fr.linkit.engine.gnom.cache.sync.invokation

import fr.linkit.api.gnom.cache.sync.contract.behavior.RMIRulesAgreement
import fr.linkit.api.gnom.cache.sync.invokation.local.CallableLocalMethodInvocation
import fr.linkit.api.gnom.cache.sync.invokation.remote.{DispatchableRemoteMethodInvocation, MethodInvocationHandler, Puppeteer}
import fr.linkit.api.internal.system.AppLogger

object DefaultMethodInvocationHandler extends MethodInvocationHandler {

    override def handleRMI[R](localInvocation: CallableLocalMethodInvocation[R]): R = {
        val syncNode   = localInvocation.objectNode
        val syncObject = syncNode.synchronizedObject
        val behavior   = localInvocation.methodBehavior
        val desc       = behavior.desc
        val puppeteer  = syncObject.getPuppeteer
        val args       = localInvocation.methodArguments
        val name       = desc.javaMethod.getName
        val methodID   = localInvocation.methodID
        val network    = puppeteer.network

        val currentIdentifier   = puppeteer.currentIdentifier
        val rootOwnerIdentifier = syncNode.tree.rootNode.ownerID

        val enableDebug = localInvocation.debug

        if (enableDebug) {
            val argsString = args.mkString("(", ", ", ")")
            val className  = desc.classDesc.clazz
            AppLogger.debug(s"$name: Performing rmi call for ${className.getSimpleName}.$name$argsString (id: $methodID)")
            AppLogger.debug(s"MethodBehavior = $behavior")
        }
        // From here we are sure that we want to perform a remote
        // method invocation. (An invocation to the current machine (invocation.callSuper()) can be added).
        var result     : Any = behavior.defaultReturnValue
        var localResult: Any = result
        val methodAgreement  = behavior.completeAgreement(new GenericRMIRulesAgreementBuilder(puppeteer.ownerID, currentIdentifier, rootOwnerIdentifier))
        val mayPerformRMI    = methodAgreement.mayPerformRemoteInvocation
        if (methodAgreement.mayCallSuper) {
            localResult = localInvocation.callSuper()
        }
        val remoteInvocation = new AbstractMethodInvocation[R](localInvocation) with DispatchableRemoteMethodInvocation[R] {
            override val agreement: RMIRulesAgreement = methodAgreement

            override def dispatchRMI(dispatcher: Puppeteer[AnyRef]#RMIDispatcher): Unit = {
                behavior.dispatch(dispatcher, network, syncObject.getBehaviorFactory, localInvocation)
            }
        }
        if (methodAgreement.getDesiredEngineReturn == currentIdentifier) {
            if (mayPerformRMI)
                puppeteer.sendInvoke(remoteInvocation)
            result = localResult
        } else if (mayPerformRMI) {
            result = puppeteer.sendInvokeAndWaitResult(remoteInvocation)
        }
        result.asInstanceOf[R]
    }

}
