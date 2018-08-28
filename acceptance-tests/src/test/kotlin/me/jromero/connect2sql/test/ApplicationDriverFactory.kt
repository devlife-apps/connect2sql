package app.devlife.connect2sql.test

import io.selendroid.client.SelendroidDriver
import io.selendroid.common.SelendroidCapabilities
import io.selendroid.standalone.SelendroidConfiguration
import io.selendroid.standalone.SelendroidLauncher

/**
 *
 */
object ApplicationDriverFactory {

    fun create(): SelendroidDriver {
        val config = SelendroidConfiguration()
        config.appFolderToMonitor = System.getenv("APK_DIR")!!

        val launcher = SelendroidLauncher(config)
        launcher.launchSelendroid()

        val capabilities = SelendroidCapabilities("app.devlife.connect2sql.donate")

        val driver = SelendroidDriver(capabilities)
        driver.switchTo().window("NATIVE_APP")

        return driver
    }
}