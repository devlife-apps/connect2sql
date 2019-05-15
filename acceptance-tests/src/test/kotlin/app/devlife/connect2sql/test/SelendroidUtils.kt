package app.devlife.connect2sql.test

import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import io.selendroid.client.TouchActionBuilder
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.touch.TouchActions

fun AndroidDriver<MobileElement>.withElement(by: By): ActionBuilder {
    return ActionBuilder(this, findElement(by))
}

fun AndroidDriver<MobileElement>.ensure(): ValidationBuilder {
    return ValidationBuilder(this)
}

object Matcher {
    fun withId(id: String): By {
        return By.id(id)
    }

    fun withText(text: String): By {
        return By.partialLinkText(text)
    }
}

class ActionBuilder(protected val driver: AndroidDriver<MobileElement>,
                    protected val element: WebElement) {
    private val actions: MutableList<() -> Unit> = arrayListOf()

    fun tap(): ActionBuilder {
        actions.add { element.click() }
        return this
    }

    fun doubleTap(): ActionBuilder {
        actions.add { TouchActions(driver).doubleTap(element).perform() }
        return this
    }

    fun longPress(ms: Int): ActionBuilder {
        actions.add {
            TouchActionBuilder().apply {
                pointerDown(element)
                pause(ms)
                pointerUp()
            }.build().perform(driver)
        }
        return this
    }

    fun perform() {
        actions.forEach { f -> f() }
    }
}

class ValidationBuilder(private val driver: AndroidDriver<MobileElement>) {
    private val validations: MutableList<() -> Unit> = arrayListOf()

    fun element(by: By): ValidationCompleter {
        return ValidationCompleter(driver, this, by)
    }

    fun validate() {
        validations.forEach { f -> f() }
    }

    class ValidationCompleter(private val driver: AndroidDriver<MobileElement>,
                              private val validationBuilder: ValidationBuilder,
                              private val by: By) {

        fun exists(): ValidationBuilder {
            validationBuilder.validations.add {
                assert(driver.findElements(by).size >= 1)
            }

            return validationBuilder
        }

        fun doesNotExist(): ValidationBuilder {
            validationBuilder.validations.add {
                assert(driver.findElements(by).size == 0)
            }

            return validationBuilder
        }
    }
}