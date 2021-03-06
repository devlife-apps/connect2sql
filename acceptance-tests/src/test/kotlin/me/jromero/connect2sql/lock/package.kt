package app.devlife.connect2sql.lock

import io.selendroid.client.SelendroidDriver
import io.selendroid.client.TouchActionBuilder
import org.openqa.selenium.By
import org.openqa.selenium.Dimension
import org.openqa.selenium.Point

/**
 * Goes through the setting of the unlock pattern
 */
fun SelendroidDriver.doSetUnlockPattern() {
    drawUnlockPattern()
    findElement(By.linkText("Continue")).click()

    drawUnlockPattern()
    findElement(By.linkText("Confirm")).click()
}

/**
 * Simply goes through the motions of drawing the unlock pattern
 */
fun SelendroidDriver.drawUnlockPattern() {
    val patternView = this.findElement(By.id("pl_pattern"))

    val cell1point = getCoordinatesOf(1, patternView.size, patternView.location)
    val cell3point = getCoordinatesOf(3, patternView.size, patternView.location)
    val cell9point = getCoordinatesOf(9, patternView.size, patternView.location)
    TouchActionBuilder()
            .pointerDown(cell1point.x, cell1point.y)
            .pointerMove(cell3point.x, cell3point.y)
            .pointerMove(cell9point.x, cell9point.y)
            .pointerUp()
            .build().perform(this)
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