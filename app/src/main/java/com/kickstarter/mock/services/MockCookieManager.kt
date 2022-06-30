package com.kickstarter.mock.services

import com.kickstarter.libs.CookieManagerType
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookieStore

class MockCookieManager : CookieManagerType {
    override fun cookieStore(): CookieStore {
        TODO("Not yet implemented")
    }

    override fun handler(): CookieHandler {
        TODO("Not yet implemented")
    }

    override fun manager(): CookieManager {
        TODO("Not yet implemented")
    }
}
