package com.kickstarter.libs

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.google.firebase.messaging.RemoteMessage
import com.kickstarter.KSRobolectricTestCase
import com.kickstarter.libs.braze.BrazeClient
import org.junit.Test

const val mockSenderId = "MockSender"
class BrazeClientTest : KSRobolectricTestCase() {

    lateinit var build: Build
    lateinit var context: Context

    override fun setUp() {
        super.setUp()
        build = requireNotNull(environment().build())
        context = application()
    }

    @Test
    fun testInitialize() {
        val mockClient = MockBrazeClient(build, context)

        mockClient.init()
        assertTrue(mockClient.isSDKEnabled())
        assertEquals(mockSenderId, mockClient.getIdSender())
        assertNotNull(mockClient.getLifeCycleCallbacks())
        assertTrue(mockClient.isInitialized)
    }

    @Test
    fun testHandleMessageNotBraze() {
        val mockClient = MockBrazeClient(build, context)

        mockClient.init()
        assertTrue(mockClient.isSDKEnabled())
        assertEquals(mockSenderId, mockClient.getIdSender())
        assertNotNull(mockClient.getLifeCycleCallbacks())
        assertTrue(mockClient.isInitialized)

        val message: RemoteMessage = RemoteMessage(Bundle())
        assertFalse(mockClient.handleRemoteMessages(context, message))
    }

    class MockBrazeClient(
        private val build: Build,
        private val context: Context
    ) : BrazeClient(build = build, context = context) {
        private var initialized = false

        override val isInitialized: Boolean
            get() = this.initialized

        override fun getIdSender(): String {
            return mockSenderId
        }

        override fun init() {
            initialized = this.isSDKEnabled()
        }

        override fun getLifeCycleCallbacks(): Application.ActivityLifecycleCallbacks {
            return (
                object : Application.ActivityLifecycleCallbacks {
                    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    }

                    override fun onActivityStarted(activity: Activity) {
                    }

                    override fun onActivityResumed(activity: Activity) {
                    }

                    override fun onActivityPaused(activity: Activity) {
                    }

                    override fun onActivityStopped(activity: Activity) {
                    }

                    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {
                    }

                    override fun onActivityDestroyed(activity: Activity) {
                    }
                }
                )
        }
    }
}
