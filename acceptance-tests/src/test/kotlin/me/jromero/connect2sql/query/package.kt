package me.jromero.connect2sql.query

import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.AndroidElement

/**
 *
 */
private val queryEditTextId = "txtQuery"
private val databaseSelectionId = "lblCurrentDatabase"

fun AndroidDriver<AndroidElement>.doReadSql(): String {
    return findElementById(queryEditTextId).text
}

fun AndroidDriver<AndroidElement>.doClearSql() {
    findElementById(queryEditTextId).clear()
}

fun AndroidDriver<AndroidElement>.doEnterSql(sql: String) {
    doClearSql()
    findElementById(queryEditTextId).sendKeys(sql)
}

fun AndroidDriver<AndroidElement>.doExecuteSql(sql: String) {
    doEnterSql(sql)
    findElementById("fab").click()
}

fun AndroidDriver<AndroidElement>.doWaitForQueryScreen(timeoutSecs: Int) {
//    TestWaiter.waitForElement(By.id(queryEditTextId), timeoutSecs, this)
}

fun AndroidDriver<AndroidElement>.doWaitForDatabasesToBeDetected(timeoutSecs: Int) {
//    TestWaiter.waitFor({ findElementById(databaseSelectionId).text.contains("None")})
}

fun AndroidDriver<AndroidElement>.doAttemptToSelectDatabase(databaseName: String) {
    findElementById(databaseSelectionId).click()
    findElementByPartialLinkText(databaseName).click()
}