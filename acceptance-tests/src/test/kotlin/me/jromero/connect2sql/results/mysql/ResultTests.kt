package me.jromero.connect2sql.results.mysql

import io.selendroid.client.waiter.TestWaiter
import kdbc.execute
import kdbc.insert
import me.jromero.connect2sql.TestServer
import me.jromero.connect2sql.connection.*
import me.jromero.connect2sql.lock.doSetUnlockPattern
import me.jromero.connect2sql.query.doAttemptToSelectDatabase
import me.jromero.connect2sql.query.doExecuteSql
import me.jromero.connect2sql.query.doWaitForDatabasesToBeDetected
import me.jromero.connect2sql.query.doWaitForQueryScreen
import me.jromero.connect2sql.results.doWaitAndDismissSuccessAlert
import me.jromero.connect2sql.test.ApplicationDriverFactory
import me.jromero.connect2sql.test.db.MySqlTestSpec
import org.junit.Test
import java.util.*

/**
 *
 */

class ResultTests : MySqlTestSpec() {

    override val testServer: TestServer = TestServer.MYSQL_BASIC_USER

    @Test
    fun shouldAlertWhenDatabaseIsCreatedOrDropped() {
        val driver = ApplicationDriverFactory.create()
        try {
            driver.doSetUnlockPattern()
            driver.doGoToAddConnection(ConnectionType.MYSQL)
            driver.doFillConnectionForm(testServer)
            driver.doPressSaveConnection()
            driver.doPressConnectionWithName(testServer.name)

            val dbName = "test_" + UUID.randomUUID().toString()

            driver.doWaitForQueryScreen(10)
            driver.doExecuteSql("CREATE DATABASE `${dbName}`;")
            driver.doWaitAndDismissSuccessAlert(10)

            driver.doWaitForQueryScreen(10)
            driver.doExecuteSql("DROP DATABASE `${dbName}`;")
            driver.doWaitAndDismissSuccessAlert(10)
        } finally {
            driver.quit()
        }
    }

    @Test
    fun shouldDisplayBasicTable() {
        val dbName = "test_" + UUID.randomUUID().toString()
        val tableName = "City"
        with(db(testServer)) {
            execute("CREATE DATABASE `${dbName}`;")
            execute("USE `${dbName}`;")
            execute("""
                CREATE TABLE `${tableName}` (
                  `ID` int(11) NOT NULL AUTO_INCREMENT,
                  `Name` char(35) NOT NULL DEFAULT '',
                  `CountryCode` char(3) NOT NULL DEFAULT '',
                  `District` char(20) NOT NULL DEFAULT '',
                  `Population` int(11) NOT NULL DEFAULT '0',
                  PRIMARY KEY (`ID`)
                ) ENGINE=InnoDB AUTO_INCREMENT=4080 DEFAULT CHARSET=latin1;
            """)
            insert("INSERT INTO `${tableName}` VALUES (1,'Kabul','AFG','Kabol',1780000);")
            insert("INSERT INTO `${tableName}` VALUES (2,'Qandahar','AFG','Qandahar',237500);")
            insert("INSERT INTO `${tableName}` VALUES (3,'Herat','AFG','Herat',186800);")
            insert("INSERT INTO `${tableName}` VALUES (4,'Mazar-e-Sharif','AFG','Balkh',127800);")
            insert("INSERT INTO `${tableName}` VALUES (5,'Amsterdam','NLD','Noord-Holland',731200);")
            insert("INSERT INTO `${tableName}` VALUES (6,'Rotterdam','NLD','Zuid-Holland',593321);")
            close()
        }

        val driver = ApplicationDriverFactory.create()
        try {
            driver.doSetUnlockPattern()
            driver.doGoToAddConnection(ConnectionType.MYSQL)
            driver.doFillConnectionForm(testServer)
            driver.doPressSaveConnection()
            driver.doPressConnectionWithName(testServer.name)

            driver.doWaitForQueryScreen(10)
            driver.doWaitForDatabasesToBeDetected(10)
            driver.doAttemptToSelectDatabase(dbName)
            driver.doExecuteSql("SELECT * FROM `City`")
            TestWaiter.waitFor { driver.findElementByLinkText("Kabul") }
        } finally {
            driver.quit()
        }

        with(db(testServer)) {
            execute("DROP DATABASE `${dbName}`;")
            close()
        }
    }
}