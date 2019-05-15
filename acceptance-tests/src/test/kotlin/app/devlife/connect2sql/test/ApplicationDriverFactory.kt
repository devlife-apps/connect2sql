package app.devlife.connect2sql.test

import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.remote.DesiredCapabilities
import java.net.URL
import java.time.Duration


/**
 *
 */
object ApplicationDriverFactory {

    fun create(): AndroidDriver<MobileElement> {
        val capabilities = DesiredCapabilities()
        capabilities.setCapability("deviceName", "Nexus 5X API 26")
        capabilities.setCapability("automationName", "Appium")
//        caps.setCapability("udid", "WUJ01N4RQ3")
//        caps.setCapability("platformName", "Android")
//        caps.setCapability("platformVersion", "7.0")
        capabilities.setCapability("skipUnlock", true)
        capabilities.setCapability("appPackage", "app.devlife.connect2sql")
        capabilities.setCapability("appActivity", "app.devlife.connect2sql.activity.LaunchActivity")
        capabilities.setCapability("noReset", "false")

        return AndroidDriver<MobileElement>(URL("http://0.0.0.0:4723/wd/hub"), capabilities).apply {
            configuratorSetWaitForSelectorTimeout(Duration.ofSeconds(10))
        }
    }
}