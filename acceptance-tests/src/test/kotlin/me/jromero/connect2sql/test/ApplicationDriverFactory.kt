package me.jromero.connect2sql.test

import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.AndroidElement
import org.apache.xpath.operations.And
import org.openqa.selenium.Capabilities
import org.openqa.selenium.remote.DesiredCapabilities
import java.io.File
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 *
 */
object ApplicationDriverFactory {

    fun create(): AndroidDriver<AndroidElement> {

        val apk = File(System.getenv("APK")!!)

        val capabilities = DesiredCapabilities()
        capabilities.setCapability("device", "Android")
        capabilities.setCapability("deviceName", "Android")
        capabilities.setCapability("platformName", "Android")
        capabilities.setCapability("allowTestPackages", true)
        capabilities.setCapability("automationName", "uiautomator2")
        capabilities.setCapability("app", apk.absolutePath)
        capabilities.setCapability("appWaitActivity", "me.jromero.connect2sql.*")
        capabilities.setCapability("unicodeKeyboard", true)
        capabilities.setCapability("resetKeyboard", true)
        capabilities.setCapability("disableAndroidWatchers", true)
        capabilities.setCapability("ignoreUnimportantViews", true)

        val driver = AndroidDriver<AndroidElement>(URL("http://127.0.0.1:4723/wd/hub"), capabilities)
        driver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS)
        return driver
    }
}