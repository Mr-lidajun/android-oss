package com.kickstarter.mock.services

import com.kickstarter.libs.AnalyticEvents
import com.kickstarter.libs.TrackingClientType

class MockAnalytics(trackingClients: List<TrackingClientType?> = emptyList()) : AnalyticEvents(trackingClients)
