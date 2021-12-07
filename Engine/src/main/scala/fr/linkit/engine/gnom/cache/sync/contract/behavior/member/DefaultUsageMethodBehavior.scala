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

package fr.linkit.engine.gnom.cache.sync.contract.behavior.member

import fr.linkit.api.gnom.cache.sync.contract.behavior.RMIRulesAgreement
import fr.linkit.api.gnom.cache.sync.contract.behavior.member.method.{ParameterBehavior, ReturnValueBehavior, UsageMethodBehavior}
import org.jetbrains.annotations.Nullable

case class DefaultUsageMethodBehavior(override val isActivated: Boolean,
                                      override val parameterBehaviors: Array[ParameterBehavior[Any]],
                                      @Nullable override val returnValueBehavior: ReturnValueBehavior[Any],
                                      override val isHidden: Boolean,
                                      override val forceLocalInnerInvocations: Boolean,
                                      @Nullable("when isActivated = false") agreement: RMIRulesAgreement) extends UsageMethodBehavior {

}

object DefaultUsageMethodBehavior {

    final val Disabled: DefaultUsageMethodBehavior = {
        DefaultUsageMethodBehavior(false, Array.empty, null, false, false, null)
    }

    def copy(other: DefaultUsageMethodBehavior): DefaultUsageMethodBehavior = {
        import other._
        DefaultUsageMethodBehavior(isActivated, parameterBehaviors, returnValueBehavior, isHidden, forceLocalInnerInvocations, agreement)
    }
}