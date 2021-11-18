package fr.linkit.api.gnom.cache.sync.contract

import fr.linkit.api.gnom.cache.sync.contract.behavior.member.method.MethodBehavior
import fr.linkit.api.gnom.cache.sync.contract.description.MethodDescription
import fr.linkit.api.gnom.cache.sync.contract.modification.MethodCompModifier
import fr.linkit.api.gnom.cache.sync.invokation.remote.MethodInvocationHandler
import fr.linkit.api.internal.concurrency.Procrastinator
import org.jetbrains.annotations.Nullable

trait MethodContract {

              val description        : MethodDescription
              val behavior           : Option[MethodBehavior]
              val parameterContracts : Array[MethodParameterContract]
    @Nullable val procrastinator     : Procrastinator
    @Nullable val handler            : MethodInvocationHandler
    @Nullable val returnValueModifier: MethodCompModifier[AnyRef]

}
