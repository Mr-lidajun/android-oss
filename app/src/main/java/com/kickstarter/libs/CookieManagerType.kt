package com.kickstarter.libs

import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookieStore

interface CookieManagerType {
    fun cookieStore(): CookieStore
    fun handler(): CookieHandler
    fun manager(): CookieManager
}

class CookieManagerImpl : CookieManagerType, CookieManager() {
    override fun handler() = this
    override fun manager() = this
    override fun cookieStore(): CookieStore = this.cookieStore
}
