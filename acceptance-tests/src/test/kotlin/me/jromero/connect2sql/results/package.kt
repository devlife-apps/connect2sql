package me.jromero.connect2sql.results

import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.AndroidElement
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.Point

fun AndroidDriver<AndroidElement>.doWaitForResultsScreen(timeoutSecs: Int) {
//    TestWaiter.waitForElement(By.id("results_table_container"), timeoutSecs, this)
}

fun AndroidDriver<AndroidElement>.doWaitForSuccessAlert(timeoutSecs: Int) {
//    TestWaiter.waitForElement(By.partialLinkText("Success"), timeoutSecs, this)
}

fun AndroidDriver<AndroidElement>.doWaitAndDismissSuccessAlert(timeoutSecs: Int) {
    doWaitForSuccessAlert(timeoutSecs)
    findElementByLinkText("OK").click()
}

fun AndroidDriver<AndroidElement>.doWaitForErrorAlert(timeoutSecs: Int) {
//    TestWaiter.waitForElement(By.partialLinkText("Error"), timeoutSecs, this)
}