package app.devlife.connect2sql.lock

import io.appium.java_client.MobileElement
import io.appium.java_client.android.AndroidDriver
import io.appium.java_client.android.AndroidTouchAction
import io.appium.java_client.touch.offset.PointOption.point
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.Point

/**
 * Goes through the setting of the unlock pattern
 */
fun AndroidDriver<MobileElement>.doSetUnlockPattern() {
    drawUnlockPattern()
    findElement(By.id("pl_right_button")).click()

    drawUnlockPattern()
    findElement(By.id("pl_right_button")).click()
}

/**
 * Simply goes through the motions of drawing the unlock pattern
 */
fun AndroidDriver<MobileElement>.drawUnlockPattern() {
    val patternView = findElement(By.id("pl_pattern"))

    val cell1point = getCoordinatesOf(1, patternView.size, patternView.location)
    val cell3point = getCoordinatesOf(3, patternView.size, patternView.location)
    val cell9point = getCoordinatesOf(9, patternView.size, patternView.location)
    AndroidTouchAction(this)
        .press(point(cell1point.x, cell1point.y))
        .moveTo(point(cell3point.x, cell3point.y))
        .moveTo(point(cell9point.x, cell9point.y))
        .release()
        .perform()
}

/**
 * Given cell matrix as below calculate center position of cell given size and location of matrix.
 * ```
 * [1 2 3]
 * [4 5 6]
 * [7 8 9]
 * ```
 *
 * @param location top left point of matrix
 */
fun getCoordinatesOf(cell: Int, size: Dimension, location: Point): Point {
    val xSize = (size.width / 3)
    val xPos = if (cell % 3 == 0) 3 else cell % 3
    val xOffset = (xSize * xPos) - xSize / 2

    val ySize = (size.height / 3)
    val yPos = ((cell - 1) / 3) + 1
    val yOffset = (ySize * yPos) - ySize / 2

    return Point(location.x + xOffset, location.y + yOffset)
}