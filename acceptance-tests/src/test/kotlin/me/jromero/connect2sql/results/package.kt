package me.jromero.connect2sql.results

import io.selendroid.client.SelendroidDriver
import io.selendroid.client.TouchActionBuilder
import io.selendroid.client.waiter.TestWaiter
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.Point

fun SelendroidDriver.doWaitForResultsScreen(timeoutSecs: Int) {
    TestWaiter.waitForElement(By.id("results_table_container"), timeoutSecs, this)
}

fun SelendroidDriver.doWaitForSuccessAlert(timeoutSecs: Int) {
    TestWaiter.waitForElement(By.partialLinkText("Success"), timeoutSecs, this)
}

fun SelendroidDriver.doWaitAndDismissSuccessAlert(timeoutSecs: Int) {
    doWaitForSuccessAlert(timeoutSecs)
    findElementByLinkText("OK").click()
}

fun SelendroidDriver.doWaitForErrorAlert(timeoutSecs: Int) {
    TestWaiter.waitForElement(By.partialLinkText("Error"), timeoutSecs, this)
}