package app.devlife.connect2sql.connection

import app.devlife.connect2sql.TestServer
import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import org.openqa.selenium.By

fun AndroidDriver<MobileElement>.doSelectServerType(type: ConnectionType) {
    when (type) {
        ConnectionType.MYSQL -> this.findElement(By.id("bi_mysql")).click()
        ConnectionType.MSSQL -> this.findElement(By.id("bi_mssql")).click()
        ConnectionType.POSTGRES -> this.findElement(By.id("bi_postgres")).click()
        ConnectionType.SYBASE -> this.findElement(By.id("bi_sybase")).click()
    }
}

fun AndroidDriver<MobileElement>.doGoToAddConnection(type: ConnectionType) {
    findElement(By.id("fab")).click()

    doSelectServerType(type)
}

fun AndroidDriver<MobileElement>.doFillConnectionForm(testServer: TestServer) {
    doFillConnectionForm(
        testServer.name,
        testServer.host,
        testServer.port,
        testServer.username,
        testServer.password,
        testServer.database)
}

fun AndroidDriver<MobileElement>.doFillConnectionForm(
    name: String,
    host: String,
    port: Int,
    username: String,
    password: String,
    database: String?) {

    findElementById("form_txt_name").setValue(name)

    findElementById("form_txt_host").setValue(host)

    findElementById("form_txt_port").clear()

    findElementById("form_txt_port").setValue("$port")

    findElementById("form_txt_username").setValue(username)

    findElementByAndroidUIAutomator(
        "new UiScrollable(new UiSelector().scrollable(true).instance(0)).scrollIntoView(new UiSelector().resourceId(\"app.devlife.connect2sql:id/form_txt_password\").instance(0))"
    ).setValue(password)

    if (database != null) {
        findElementById("form_txt_database").setValue(database)
    }
}

fun AndroidDriver<MobileElement>.doPressTestConnection() {
    findElementByLinkText("Test").click()
    findElementByPartialLinkText("Testing")
}


fun AndroidDriver<MobileElement>.doPressSaveConnection() {
    findElementByLinkText("Save").click()
}

fun AndroidDriver<MobileElement>.doPressConnectionWithName(name: String) {
    findElementByLinkText(name).click()
}