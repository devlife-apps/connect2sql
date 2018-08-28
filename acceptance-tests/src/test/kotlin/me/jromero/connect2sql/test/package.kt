package app.devlife.connect2sql.test

import io.selendroid.client.SelendroidDriver
import io.selendroid.client.TouchAction
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.touch.TouchActions

/**
 *
 */

fun SelendroidDriver.touchActions(): TouchActions {
    return TouchActions(this)
}