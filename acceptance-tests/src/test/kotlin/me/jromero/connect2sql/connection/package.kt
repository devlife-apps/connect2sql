package me.jromero.connect2sql.connection

import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.AndroidElement
import me.jromero.connect2sql.TestServer
import me.jromero.connect2sql.test.findElementContainingText
import org.openqa.selenium.By

fun AndroidDriver<AndroidElement>.doGoToAddConnection(type: ConnectionType) {
    this.findElement(By.id("fab")).click()
    when (type) {
        ConnectionType.MYSQL -> this.findElement(By.id("bi_mysql")).click()
        ConnectionType.MSSQL -> this.findElement(By.id("bi_mssql")).click()
        ConnectionType.POSTGRES -> this.findElement(By.id("bi_postgres")).click()
        ConnectionType.SYBASE -> this.findElement(By.id("bi_sybase")).click()
    }
}

fun AndroidDriver<AndroidElement>.doFillConnectionForm(testServer: TestServer) {
    doFillConnectionForm(
            testServer.name,
            testServer.host,
            testServer.port,
            testServer.username,
            testServer.password,
            testServer.database)
}

fun AndroidDriver<AndroidElement>.doFillConnectionForm(
        name: String,
        host: String,
        port: Int,
        username: String,
        password: String,
        database: String?) {

    findElementById("form_txt_name").also { it.clear() }.also { it.sendKeys(name) }
    findElementById("form_txt_host").also { it.clear() }.also { it.sendKeys(host) }
    findElementById("form_txt_port").also { it.clear() }.also { it.sendKeys("${port}") }

    findElementById("form_txt_username").also { it.clear() }.also { it.sendKeys(username) }
    findElementById("form_txt_password").also { it.clear() }.also { it.sendKeys(password) }

    if (database != null) findElementById("form_txt_database").sendKeys(database)
}

fun AndroidDriver<AndroidElement>.doPressTestConnection() {
    findElementContainingText("Test").click()
    findElementContainingText("Testing")
}


fun AndroidDriver<AndroidElement>.doPressSaveConnection() {
    findElementContainingText("Save").click()
}

fun AndroidDriver<AndroidElement>.doPressConnectionWithName(name: String) {
    findElementByLinkText(name).click()
}