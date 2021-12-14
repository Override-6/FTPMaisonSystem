package fr.linkit.examples.ssc.server

import fr.linkit.examples.ssc.api.{CurrentUserAccount, CurrentUserWallet, UserWallet}

import scala.collection.mutable

class CurrentUserAccountImpl(override val name: String) extends CurrentUserAccount {

    private val wallets = mutable.HashMap.empty[String, CurrentUserWallet]

    override def openWallet(name: String, initialAmount: Int): CurrentUserWallet = {
        if (wallets.contains(name))
            throw new IllegalArgumentException(s"wallet '$name' already exists.")
        wallets.getOrElseUpdate(name, new CurrentUserWalletImpl(this, name))
    }

    override def findWallet(name: String): Option[CurrentUserWallet] = wallets.get(name)

    override def getWallets: Seq[UserWallet] = wallets.values.toSeq
}