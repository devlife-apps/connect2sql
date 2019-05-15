package app.devlife.connect2sql.query.mysql

import app.devlife.connect2sql.TestServer
import app.devlife.connect2sql.connection.*
import app.devlife.connect2sql.lock.doSetUnlockPattern
import app.devlife.connect2sql.query.*
import app.devlife.connect2sql.results.doWaitAndDismissSuccessAlert
import app.devlife.connect2sql.test.*
import app.devlife.connect2sql.test.Matcher.withId
import app.devlife.connect2sql.test.Matcher.withText
import app.devlife.connect2sql.test.db.MySqlTestSpec
import org.junit.Test
import org.openqa.selenium.By
import java.util.*

/**
 *
 */
class QueryHistoryTests : MySqlTestSpec() {
    override val testServer: TestServer = TestServer.MYSQL_BASIC_USER

    @Test
    fun shouldPersistQueryAndRestoreIt() {
        val driver = ApplicationDriverFactory.create()
        try {
            driver.doSetUnlockPattern()
            driver.doGoToAddConnection(ConnectionType.MYSQL)
            driver.doFillConnectionForm(testServer)
            driver.doPressSaveConnection()
            driver.doPressConnectionWithName(testServer.name)

            val dbName = "test_" + UUID.randomUUID().toString()
            val sql = "CREATE DATABASE `${dbName}`;"

            driver.doWaitForQueryScreen(10)
            driver.doExecuteSql(sql)
            driver.doWaitAndDismissSuccessAlert(10)

            driver.doWaitForQueryScreen(10)
            driver.doClearSql()
            driver.findElementById("open").click()
            driver.findElementByLinkText("History").click()
            driver.findElementByPartialLinkText("CREATE DATABASE").click()
            assert(driver.doReadSql().equals(sql))
        } finally {
            driver.quit()
        }
    }

    @Test
    fun shouldRestoreItFromContextMenu() {
        val driver = ApplicationDriverFactory.create()
        try {
            driver.doSetUnlockPattern()
            driver.doGoToAddConnection(ConnectionType.MYSQL)
            driver.doFillConnectionForm(testServer)
            driver.doPressSaveConnection()
            driver.doPressConnectionWithName(testServer.name)

            val dbName = "test_" + UUID.randomUUID().toString()
            val sql = "CREATE DATABASE `${dbName}`;"

            driver.doWaitForQueryScreen(10)
            driver.doExecuteSql(sql)
            driver.doWaitAndDismissSuccessAlert(10)

            driver.doWaitForQueryScreen(10)
            driver.doClearSql()
            driver.withElement(withId("open")).tap().perform()
            driver.withElement(withText("History")).tap().perform()

            driver
                .withElement(withText("CREATE DATABASE"))
                .longPress(2500)
                .perform()

            driver.withElement(withText("Open")).tap().perform()
            assert(driver.doReadSql().equals(sql))
        } finally {
            driver.quit()
        }
    }


    @Test
    fun shouldDeleteItFromContextMenu() {
        val driver = ApplicationDriverFactory.create()
        try {
            driver.doSetUnlockPattern()
            driver.doGoToAddConnection(ConnectionType.MYSQL)
            driver.doFillConnectionForm(testServer)
            driver.doPressSaveConnection()
            driver.doPressConnectionWithName(testServer.name)

            val dbName = "test_" + UUID.randomUUID().toString()
            val sql = "CREATE DATABASE `${dbName}`;"

            driver.doWaitForQueryScreen(10)
            driver.doExecuteSql(sql)
            driver.doWaitAndDismissSuccessAlert(10)

            driver.doWaitForQueryScreen(10)
            driver.doClearSql()
            driver.withElement(withId("open")).tap().perform()
            driver.withElement(withText("History")).tap().perform()

            driver
                .withElement(withText("CREATE DATABASE"))
                .longPress(2500)
                .perform()

            driver.withElement(withText("Delete")).tap().perform()
            driver.withElement(withText("Yes")).tap().perform()
            driver.ensure().element(withText("CREATE DATABASE")).doesNotExist().validate()
        } finally {
            driver.quit()
        }
    }
}