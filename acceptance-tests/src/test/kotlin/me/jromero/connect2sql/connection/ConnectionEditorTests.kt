package me.jromero.connect2sql.connection

import io.selendroid.client.waiter.TestWaiter
import me.jromero.connect2sql.TestServer
import me.jromero.connect2sql.connection.*
import me.jromero.connect2sql.lock.*
import me.jromero.connect2sql.test.ApplicationDriverFactory
import org.junit.Test
import org.openqa.selenium.By

class ConnectionEditorTests {

    val testServers = arrayListOf(
        TestServer.MYSQL_BASIC_USER,
        TestServer.MSSQL_BASIC_USER,
        TestServer.POSTGRES_ADMIN
    )

    @Test
    fun shouldBeAbleToTestConnection() {

        testServers.forEach { testServer ->
            val driver = ApplicationDriverFactory.create()

            try {
                driver.doSetUnlockPattern()

                driver.doGoToAddConnection(testServer.type)
                driver.doFillConnectionForm(testServer)
                driver.doPressTestConnection()

                TestWaiter.waitForElement(By.ByPartialLinkText("Success"), 10, driver)
                driver.findElementByLinkText("OK").click()
            } finally {
                driver.quit()
            }
        }
    }

    @Test
    fun shouldBeAbleToSaveConnection() {

        testServers.forEach { testServer ->
            val driver = ApplicationDriverFactory.create()

            try {
                driver.doSetUnlockPattern()

                val server = TestServer.MYSQL_BASIC_USER
                driver.doGoToAddConnection(ConnectionType.MYSQL)
                driver.doFillConnectionForm(server)
                driver.doPressSaveConnection()

                driver.findElementByPartialLinkText(server.name.take(5))
            } finally {
                driver.quit()
            }
        }
    }
}