package me.jromero.connect2sql.connection

import io.selendroid.client.SelendroidDriver
import me.jromero.connect2sql.TestServer
import org.openqa.selenium.By

fun SelendroidDriver.doGoToAddConnection(type: ConnectionType) {
    this.findElement(By.id("fab")).click()

    when (type) {
        ConnectionType.MYSQL -> this.findElement(By.id("bi_mysql")).click()
        ConnectionType.MSSQL -> this.findElement(By.id("bi_mssql")).click()
        ConnectionType.POSTGRES -> this.findElement(By.id("bi_postgres")).click()
        ConnectionType.SYBASE -> this.findElement(By.id("bi_sybase")).click()
    }
}

fun SelendroidDriver.doFillConnectionForm(testServer: TestServer) {
    doFillConnectionForm(
            testServer.name,
            testServer.host,
            testServer.port,
            testServer.username,
            testServer.password,
            testServer.database)
}

fun SelendroidDriver.doFillConnectionForm(
        name: String,
        host: String,
        port: Int,
        username: String,
        password: String,
        database: String?) {

    findElementById("form_txt_name").sendKeys(name)
    findElementById("form_txt_host").sendKeys(host)
    findElementById("form_txt_port").clear()
    findElementById("form_txt_port").sendKeys("${port}")
    findElementById("form_txt_username").sendKeys(username)
    findElementById("form_txt_password").sendKeys(password)

    if (database != null) findElementById("form_txt_database").sendKeys(database)
}

fun SelendroidDriver.doPressTestConnection() {
    findElementByLinkText("Test").click()
    findElementByPartialLinkText("Testing")
}


fun SelendroidDriver.doPressSaveConnection() {
    findElementByLinkText("Save").click()
}

fun SelendroidDriver.doPressConnectionWithName(name: String) {
    findElementByLinkText(name).click()
}