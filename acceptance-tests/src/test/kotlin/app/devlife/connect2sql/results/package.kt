package app.devlife.connect2sql.results

import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import io.selendroid.client.SelendroidDriver
import io.selendroid.client.TouchActionBuilder
import io.selendroid.client.waiter.TestWaiter
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.Point

fun AndroidDriver<MobileElement>.doWaitForResultsScreen(timeoutSecs: Int) {
    TestWaiter.waitForElement(By.id("results_table_container"), timeoutSecs, this)
}

fun AndroidDriver<MobileElement>.doWaitForSuccessAlert(timeoutSecs: Int) {
    TestWaiter.waitForElement(By.partialLinkText("Success"), timeoutSecs, this)
}

fun AndroidDriver<MobileElement>.doWaitAndDismissSuccessAlert(timeoutSecs: Int) {
    doWaitForSuccessAlert(timeoutSecs)
    findElementByLinkText("OK").click()
}

fun AndroidDriver<MobileElement>.doWaitForErrorAlert(timeoutSecs: Int) {
    TestWaiter.waitForElement(By.partialLinkText("Error"), timeoutSecs, this)
}