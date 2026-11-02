package com.zoewave.probase.features.ai.local.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.workDataOf
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import java.io.File
import java.security.MessageDigest

@RunWith(AndroidJUnit4::class)
@Config(sdk = [34])
class AiModelDownloadWorkerTest {

    private lateinit var context: Context
    private lateinit var mockWebServer: MockWebServer

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        mockWebServer = MockWebServer()
        mockWebServer.start()

        // Clean up any existing files
        File(context.filesDir, "model.litertlm").delete()
        File(context.filesDir, "model.litertlm.part").delete()
    }

    @After
    fun tearDown() {
        mockWebServer.shutdown()
        File(context.filesDir, "model.litertlm").delete()
        File(context.filesDir, "model.litertlm.part").delete()
    }

    @Test
    fun testWorker_DownloadSuccess() = runBlocking {
        // Create a fake payload
        val fakeModelData = "fake_model_data".toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val expectedHash = md.digest(fakeModelData).joinToString("") { "%02x".format(it) }

        // Enqueue a successful response
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("X-Linked-ETag", "\"$expectedHash\"")
                .setHeader("X-Linked-Size", fakeModelData.size.toString())
                .setBody(okio.Buffer().write(fakeModelData))
        )

        val worker = TestListenableWorkerBuilder<AiModelDownloadWorker>(context)
            .setInputData(workDataOf("MODEL_URL" to mockWebServer.url("/model").toString()))
            .build()

        val result = worker.doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)

        val targetFile = File(context.filesDir, "model.litertlm")
        assertThat(targetFile.exists()).isTrue()
        assertThat(targetFile.readText()).isEqualTo("fake_model_data")
    }

    @Test
    fun testWorker_Http404_FailsImmediately() = runBlocking {
        mockWebServer.enqueue(
            MockResponse().setResponseCode(404)
        )

        val worker = TestListenableWorkerBuilder<AiModelDownloadWorker>(context)
            .setInputData(workDataOf("MODEL_URL" to mockWebServer.url("/model").toString()))
            .build()

        val result = worker.doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Failure::class.java)
        val errorReason = (result as ListenableWorker.Result.Failure).outputData.getString("reason")
        assertThat(errorReason).isEqualTo("HTTP Error: 404")
    }

    @Test
    fun testWorker_Http307Redirect_FollowsRedirect() = runBlocking {
        // Create a fake payload
        val fakeModelData = "fake_model_data_redirected".toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val expectedHash = md.digest(fakeModelData).joinToString("") { "%02x".format(it) }

        // 1st request: 307 Redirect to /real_model
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(307)
                .setHeader("Location", mockWebServer.url("/real_model").toString())
                .setHeader("X-Linked-ETag", "\"$expectedHash\"")
                .setHeader("X-Linked-Size", fakeModelData.size.toString())
        )

        // 2nd request: 200 OK
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setHeader("X-Linked-ETag", "\"$expectedHash\"")
                .setHeader("X-Linked-Size", fakeModelData.size.toString())
                .setBody(okio.Buffer().write(fakeModelData))
        )

        val worker = TestListenableWorkerBuilder<AiModelDownloadWorker>(context)
            .setInputData(workDataOf("MODEL_URL" to mockWebServer.url("/initial").toString()))
            .build()

        val result = worker.doWork()

        assertThat(result).isInstanceOf(ListenableWorker.Result.Success::class.java)
        assertThat(mockWebServer.requestCount).isEqualTo(2)

        val targetFile = File(context.filesDir, "model.litertlm")
        assertThat(targetFile.exists()).isTrue()
        assertThat(targetFile.readText()).isEqualTo("fake_model_data_redirected")
    }
}
