package fr.linkit.engine.gnom.cache.sync.contract.description

import fr.linkit.api.gnom.cache.sync.SynchronizedObject
import fr.linkit.api.gnom.cache.sync.contract.description.{FieldDescription, MethodDescription}
import fr.linkit.api.gnom.persistence.context.{Deconstructible, Persist}
import fr.linkit.api.internal.system.AppLogger
import fr.linkit.engine.gnom.cache.sync.contract.description.SyncObjectDescription.SyntheticMod

import java.lang.reflect.{Executable, Method, Modifier}
import scala.collection.mutable
import scala.reflect.{ClassTag, classTag}

class SyncObjectDescription[A <: AnyRef] @Persist() private(clazz: Class[A]) extends AbstractSyncStructureDescription[A](clazz) with Deconstructible {

    def listMethods[L >: A](limit: Class[L]): Iterable[MethodDescription] = {
        listMethods().filter(m => limit.isAssignableFrom(m.javaMethod.getDeclaringClass))
    }

    def listFields[L >: A](limit: Class[L]): Iterable[FieldDescription] = {
        listFields().filter(f => limit.isAssignableFrom(f.javaField.getDeclaringClass))
    }

    override protected def applyNotFilter(e: Executable): Boolean = {
        isNotOverridable(e) || containsNotAccessibleElements(e) ||
            //FIXME Weird bug due to scala's Any and AnyRef stuff...
            (e.getName == "equals" && e.getParameterTypes.length == 1) ||
            //FIXME Bug occurred for objects that extends NetworkObject[A].
            // as SynchronizedObject trait also extends NetworkObject[SyncObjectReference],
            // a collision may occur as the generated method would be
            // syncClass#reference: A, which overrides SynchronizedObject#reference: SyncObjectReference (there is an incompatible type definition)
            // Maybe making the GNOLinkage able to support multiple references to an object would help, but certainly overkill
            (e.getName == "reference" && e.getParameterTypes.isEmpty)
    }

    override def deconstruct(): Array[Any] = Array(clazz)

    private def isNotOverridable(e: Executable): Boolean = {
        val mods = e.getModifiers
        import Modifier._
        isPrivate(mods) || (mods & SyntheticMod) != 0 /*is Synthetic*/ || isStatic(mods) || isFinal(mods) || isNative(mods)
    }

    private def containsNotAccessibleElements(e: Executable): Boolean = {
        def isNotAccessible(clazz: Class[_], tpe: String): Boolean = {
            if (clazz.isPrimitive)
                return false
            val mods = clazz.getModifiers
            import Modifier._
            val notAccessible = isPrivate(mods) || !(isProtected(mods) || isPublic(mods))
            if (notAccessible)
                AppLogger.warn(s"Could not handle method ${e} because $tpe '${clazz.getName}' is not accessible for the generated Sync implementation class of '${this.clazz}'")
            notAccessible
        }

        e match {
            case method: Method =>
                isNotAccessible(method.getReturnType, "return type") ||
                    method.getParameterTypes.exists(isNotAccessible(_, "parameter type"))
            case _              => false
        }
    }
}

object SyncObjectDescription {

    private val SyntheticMod = 0x00001000

    private val cache = mutable.HashMap.empty[Class[_], SyncObjectDescription[_]]

    def apply[A <: AnyRef](clazz: Class[_]): SyncObjectDescription[A] = cache.getOrElseUpdate(clazz, {
        if (classOf[SynchronizedObject[_]].isAssignableFrom(clazz))
            throw new IllegalArgumentException(s"Provided class already extends from SynchronizedObject ($clazz)")
        val AClass = clazz.asInstanceOf[Class[A]]
        new SyncObjectDescription[A](AClass)
    }).asInstanceOf[SyncObjectDescription[A]]


    implicit def fromTag[A <: AnyRef : ClassTag]: SyncObjectDescription[A] = apply[A](classTag[A].runtimeClass)

}