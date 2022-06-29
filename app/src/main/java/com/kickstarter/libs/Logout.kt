package com.kickstarter.libs

import com.facebook.login.LoginManager
import java.net.CookieManager

interface Logout {
    fun execute()
}
class LogoutImpl(private val cookieManager: CookieManager, private val currentUser: CurrentUserType) : Logout {
    override fun execute() {
        currentUser.logout()
        cookieManager.cookieStore.removeAll()
        LoginManager.getInstance().logOut()
    }
}
