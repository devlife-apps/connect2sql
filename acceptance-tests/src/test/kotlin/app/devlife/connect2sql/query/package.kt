package app.devlife.connect2sql.query

import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import io.selendroid.client.SelendroidDriver
import io.selendroid.client.waiter.TestWaiter
import org.openqa.selenium.By

/**
 *
 */
private val queryEditTextId = "txtQuery"
private val databaseSelectionId = "lblCurrentDatabase"

fun AndroidDriver<MobileElement>.doReadSql(): String {
    return findElementById(queryEditTextId).text
}

fun AndroidDriver<MobileElement>.doClearSql() {
    findElementById(queryEditTextId).clear()
}

fun AndroidDriver<MobileElement>.doEnterSql(sql: String) {
    doClearSql()
    findElementById(queryEditTextId).sendKeys(sql)
}

fun AndroidDriver<MobileElement>.doExecuteSql(sql: String) {
    doEnterSql(sql)
    findElementById("fab").click()
}

fun AndroidDriver<MobileElement>.doWaitForQueryScreen(timeoutSecs: Int) {
    TestWaiter.waitForElement(By.id(queryEditTextId), timeoutSecs, this)
}

fun AndroidDriver<MobileElement>.doWaitForDatabasesToBeDetected(timeoutSecs: Int) {
    TestWaiter.waitFor { findElementById(databaseSelectionId).text.contains("None")}
}

fun AndroidDriver<MobileElement>.doAttemptToSelectDatabase(databaseName: String) {
    findElementById(databaseSelectionId).click()
    findElementByPartialLinkText(databaseName).click()
}