package com.zoewave.probase.features.ai.local

import com.google.common.truth.Truth.assertThat
import com.zoewave.probase.features.ai.local.data.NanoState
import org.junit.Test

class LocalAiEngineTest {

    @Test
    fun testNanoStateUnsupported() {
        val state: NanoState = NanoState.Unsupported
        assertThat(state).isInstanceOf(NanoState.Unsupported::class.java)
    }

    @Test
    fun testNanoStateAvailable() {
        val state: NanoState = NanoState.Available
        assertThat(state).isInstanceOf(NanoState.Available::class.java)
    }

    @Test
    fun testNanoStateDownloading() {
        val state: NanoState = NanoState.Downloading(45)
        assertThat((state as NanoState.Downloading).progress).isEqualTo(45)
    }
}
