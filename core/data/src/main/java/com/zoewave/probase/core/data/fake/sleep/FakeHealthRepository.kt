package com.zoewave.probase.core.data.fake.sleep

import com.zoewave.probase.core.model.health.SleepSessionData

class FakeHealthRepository {
    val fakeData = FakeSleepSessionData().getFakeSleepData()

    // Sample fake data for sleep sessions
    fun getData(): List<SleepSessionData> {
        return fakeData
    }
}