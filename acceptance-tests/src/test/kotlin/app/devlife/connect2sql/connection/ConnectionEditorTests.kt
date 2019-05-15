package app.devlife.connect2sql.connection

import io.selendroid.client.waiter.TestWaiter
import app.devlife.connect2sql.TestServer
import app.devlife.connect2sql.connection.*
import app.devlife.connect2sql.lock.*
import app.devlife.connect2sql.test.ApplicationDriverFactory
import app.devlife.connect2sql.tryWith
import org.junit.Test
import org.openqa.selenium.By

class ConnectionEditorTests {

    private val testServers = arrayListOf(
        TestServer.MYSQL_BASIC_USER
    )

    @Test
    fun shouldBeAbleToTestConnection() {

        testServers.forEach { testServer ->
            tryWith(ApplicationDriverFactory.create()) {
                doSetUnlockPattern()

                doSelectServerType(testServer.type)
                doFillConnectionForm(testServer)
                doPressTestConnection()

                TestWaiter.waitForElement(By.ByPartialLinkText("Success"), 10, this)
                findElementByLinkText("OK").click()
            }
        }
    }

    @Test
    fun shouldBeAbleToSaveConnection() {

        testServers.forEach { testServer ->
            tryWith(ApplicationDriverFactory.create()) {
                doSetUnlockPattern()

                doSelectServerType(testServer.type)
                doFillConnectionForm(testServer)
                doPressSaveConnection()

                findElementByPartialLinkText(testServer.name.take(5))
            }
        }
    }
}