package com.kickstarter.mock.services

import android.app.PendingIntent
import com.kickstarter.libs.PushNotifications
import com.kickstarter.models.MessageThread
import com.kickstarter.services.apiresponses.PushNotificationEnvelope

class MockPushNotifications : PushNotifications {
    override fun initialize() {
        TODO("Not yet implemented")
    }

    override fun add(envelope: PushNotificationEnvelope) {
        TODO("Not yet implemented")
    }

    override fun messageThreadIntent(
        envelope: PushNotificationEnvelope,
        messageThread: MessageThread
    ): PendingIntent {
        TODO("Not yet implemented")
    }
}
