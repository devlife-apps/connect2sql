package app.devlife.connect2sql.test

import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.interactions.touch.TouchActions

/**
 *
 */

fun AndroidDriver<MobileElement>.touchActions(): TouchActions {
    return TouchActions(this)
}