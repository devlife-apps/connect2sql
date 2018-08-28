package app.devlife.connect2sql.test

import io.selendroid.client.SelendroidDriver
import io.selendroid.client.TouchActionBuilder
import org.openqa.selenium.By
import org.openqa.selenium.WebElement
import org.openqa.selenium.interactions.touch.TouchActions

fun SelendroidDriver.withElement(by: By) : ActionBuilder {
    return ActionBuilder(this, findElement(by))
}

fun SelendroidDriver.ensure(): ValidationBuilder {
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

class ActionBuilder(protected val driver: SelendroidDriver, protected val element: WebElement) {
    private val actions: MutableList<() -> Unit> = arrayListOf()

    fun tap(): ActionBuilder {
        actions.add({element.click()})
        return this;
    }

    fun doubleTap(): ActionBuilder {
        actions.add({TouchActions(driver).doubleTap(element).perform()})
        return this;
    }

    fun longPress(ms: Int): ActionBuilder {
        actions.add({
            val t = TouchActionBuilder()
            t.pointerDown(element)
            t.pause(ms)
            t.pointerUp()
            t.build()
            .perform(driver)
        })
        return this;
    }

    fun perform() {
        actions.forEach { f -> f() }
    }
}

class ValidationBuilder(protected val driver: SelendroidDriver) {
    private val validations: MutableList<() -> Unit> = arrayListOf()

    fun element(by: By): ValidationCompleter {
        return ValidationCompleter(driver, this, by)
    }

    fun validate() {
        validations.forEach { f -> f() }
    }

    class ValidationCompleter(protected val driver: SelendroidDriver, protected val validationBuilder: ValidationBuilder, protected val by: By) {

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