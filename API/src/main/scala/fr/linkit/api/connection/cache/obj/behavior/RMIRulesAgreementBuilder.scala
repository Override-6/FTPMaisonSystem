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

package fr.linkit.api.connection.cache.obj.behavior

trait RMIRulesAgreementBuilder {

    def discard(target: String): this.type

    def accept(target: String): this.type

    def acceptOwner(): this.type

    def acceptAll(): this.type

    def discardAll(): this.type

    def acceptCurrent(): this.type

    def discardCurrent(): this.type

    def discardOwner(): this.type

    def setDesiredEngineReturn(target: String): this.type

    def setDesiredCurrentEngineReturn(): this.type

    def setDesiredOwnerEngineReturn(): this.type

    def ifCurrentIsOwner(action: this.type => this.type): this.type

    def ifCurrentIsNotOwner(action: this.type => this.type): this.type

}
