package me.jromero.connect2sql.query

import io.selendroid.client.SelendroidDriver
import io.selendroid.client.waiter.TestWaiter
import org.openqa.selenium.By

/**
 *
 */
private val queryEditTextId = "txtQuery"
private val databaseSelectionId = "lblCurrentDatabase"

fun SelendroidDriver.doReadSql(): String {
    return findElementById(queryEditTextId).text
}

fun SelendroidDriver.doClearSql() {
    findElementById(queryEditTextId).clear()
}

fun SelendroidDriver.doEnterSql(sql: String) {
    doClearSql()
    findElementById(queryEditTextId).sendKeys(sql)
}

fun SelendroidDriver.doExecuteSql(sql: String) {
    doEnterSql(sql)
    findElementById("fab").click()
}

fun SelendroidDriver.doWaitForQueryScreen(timeoutSecs: Int) {
    TestWaiter.waitForElement(By.id(queryEditTextId), timeoutSecs, this)
}

fun SelendroidDriver.doWaitForDatabasesToBeDetected(timeoutSecs: Int) {
    TestWaiter.waitFor({ findElementById(databaseSelectionId).text.contains("None")})
}

fun SelendroidDriver.doAttemptToSelectDatabase(databaseName: String) {
    findElementById(databaseSelectionId).click()
    findElementByPartialLinkText(databaseName).click()
}