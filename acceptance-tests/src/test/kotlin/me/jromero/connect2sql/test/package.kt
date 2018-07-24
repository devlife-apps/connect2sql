package me.jromero.connect2sql.test

import io.appium.java_client.FindsByAndroidUIAutomator
import io.appium.java_client.MobileBy
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.AndroidElement
import org.openqa.selenium.interactions.touch.TouchActions
import org.openqa.selenium.support.ui.WebDriverWait
import java.time.Duration

/**
 *
 */

fun AndroidDriver<AndroidElement>.touchActions(): TouchActions {
    return TouchActions(this)
}

fun AndroidDriver<AndroidElement>.findElementContainingText(text: String): AndroidElement {
    return this.findElementByUiAutomator("""new UiSelector().textContains("${text}")""")
}

fun AndroidDriver<AndroidElement>.findElementByUiAutomator(uiAutomator: String): AndroidElement {
    return this.findElement(MobileBy.ByAndroidUIAutomator(uiAutomator))
}

fun AndroidDriver<AndroidElement>.waitAtMostUntil(atMost: Duration, until: (driver: AndroidDriver<AndroidElement>) -> Boolean) {
    WebDriverWait(this, atMost.seconds).until { _ ->
        try {
            until(this)
        } catch (_: Exception) {
            false
        }
    }
}