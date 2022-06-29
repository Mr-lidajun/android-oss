package com.kickstarter.libs

import com.facebook.login.LoginManager
import java.net.CookieManager

interface LogoutDI {
    fun execute()
}
class Logout(private val cookieManager: CookieManager, private val currentUser: CurrentUserType): LogoutDI {
    override fun execute() {
        currentUser.logout()
        cookieManager.cookieStore.removeAll()
        LoginManager.getInstance().logOut()
    }
}