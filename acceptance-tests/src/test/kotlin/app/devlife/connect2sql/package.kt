package app.devlife.connect2sql

import org.openqa.selenium.WebDriver

inline fun <T : WebDriver, R> tryWith(receiver: T, block: T.() -> R): R =
    try {
        receiver.block()
    } finally {
        receiver.quit()
    }