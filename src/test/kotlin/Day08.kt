import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

/*
--- Day 8: Two-Factor Authentication ---

You come across a door implementing what you can only assume is an implementation of two-factor authentication
after a long game of requirements telephone.

To get past the door, you first swipe a keycard (no problem; there was one on a nearby desk).
Then, it displays a code on a little screen, and you type that code on a keypad.
Then, presumably, the door unlocks.

Unfortunately, the screen has been smashed.
After a few minutes, you've taken everything apart and figured out how it works.
Now you just have to work out what the screen would have displayed.

The magnetic strip on the card you swiped encodes a series of instructions for the screen;
these instructions are your puzzle input.
The screen is 50 pixels wide and 6 pixels tall, all of which start off,
and is capable of three somewhat peculiar operations:

rect AxB turns on all of the pixels in a rectangle at the top-left of the screen which is A wide and B tall.
rotate row y=A by B shifts all of the pixels in row A (0 is the top row) right by B pixels.
Pixels that would fall off the right end appear at the left end of the row.
rotate column x=A by B shifts all of the pixels in column A (0 is the left column) down by B pixels.
Pixels that would fall off the bottom appear at the top of the column.
For example, here is a simple sequence on a smaller screen:

rect 3x2 creates a small rectangle in the top-left corner:

###....
###....
.......

rotate column x=1 by 1 rotates the second column down by one pixel:

#.#....
###....
.#.....

rotate row y=0 by 4 rotates the top row right by four pixels:

....#.#
###....
.#.....

rotate column x=1 by 1 again rotates the second column down by one pixel, causing the bottom pixel to wrap back to the top:

.#..#.#
#.#....
.#.....

As you can see, this display technology is extremely powerful,
and will soon dominate the tiny-code-displaying-screen market.
That's what the advertisement on the back of the display tries to convince you, anyway.

There seems to be an intermediate check of the voltage used by the display:
after you swipe your card, if the screen did work, how many pixels should be lit?


 */

class Day8Spec : Spek({

    describe("part 1") {
        describe("example") {
            given("an initial rectancle") {
                val initialDisplay = createDisplay(7, 3)
                val afterRecty3x2 = initialDisplay.rect(3, 2)
                it ("should show the correct pixels after rect 3, 2") {
                    afterRecty3x2.convertToString() `should equal`
                            """
                            ###....
                            ###....
                            .......
                            """.trimIndent()
                }
                val afterRotateX1by0 = afterRecty3x2.rotateX(1, 0)
                it("should show the correct pixels after rotate x = 1 by 0") {
                    afterRotateX1by0.convertToString() `should equal`
                            """
                            ###....
                            ###....
                            .......
                            """.trimIndent()
                }
                val afterRotateX1by1 = afterRotateX1by0.rotateX(1, 1)
                it("should show the correct pixels after rotate x = 1 by 1") {
                    afterRotateX1by1.convertToString() `should equal`
                            """
                            #.#....
                            ###....
                            .#.....
                            """.trimIndent()
                }
                val afterRotateY0by4 = afterRotateX1by1.rotateY(0, 4)
                it("should show the correct pixels after rotate y = 0 by 4") {
                    afterRotateY0by4.convertToString() `should equal`
                            """
                            ....#.#
                            ###....
                            .#.....
                            """.trimIndent()
                }
                val afterRotateX1by1again = afterRotateY0by4.rotateX(1, 1)
                it("should show the correct pixels after another rotate x = 1 by 1") {
                    afterRotateX1by1again.convertToString() `should equal`
                            """
                            .#..#.#
                            #.#....
                            .#.....
                            """.trimIndent()
                }
                it("should have 6 pixels") {
                    afterRotateX1by1again.countPixels() `should equal` 6
                }
            }
        }
        describe("exercise") {
            given("extercise input") {
                val input = readResource("day08Input.txt")
                val inputList = parseTrimedLines(input)
//                it("should calculate the correct result") {
//                    val result = inputList.filter { checkIp7Adress(it) }.count()
//                    println(result)
//                    result `should equal` 105
//                }
            }
        }
    }
})

fun Array<CharArray>.rect(xInit: Int, yInit: Int): Array<CharArray> =
        mapIndexed { y, row ->
            row.mapIndexed { x, _ ->
                if (x < xInit && y < yInit) '#'
                else this[y][x]
            }
            .toCharArray()
        }.toTypedArray()

fun createDisplay(xSize: Int, ySize: Int) =
        Array(ySize) {
            CharArray(xSize) {
                '.'
            }
        }

private fun Array<CharArray>.rotateX(xToRotate: Int, decr: Int): Array<CharArray> =
        mapIndexed { y, row ->
            row.mapIndexed { x, _ ->
                if (x == xToRotate) {
                    this[rotateIndex(y, decr, size)][x]
                } else this[y][x]
            }
            .toCharArray()
        }.toTypedArray()

private fun Array<CharArray>.rotateY(yToRotate: Int, decr: Int): Array<CharArray> =
        mapIndexed { y, row ->
            row.mapIndexed { x, _ ->
                if (y == yToRotate) {
                    this[y][rotateIndex(x, decr, row.size)]
                } else this[y][x]
            }.toCharArray()
        }.toTypedArray()

private fun rotateIndex(index: Int, decr: Int, size: Int): Int {
    val h = (index - decr) % size
    if (h < 0) return size + h
    else return h
}

private fun Array<CharArray>.convertToString() = mapIndexed { index, col ->
        col.joinToString("") +
                if (index < size - 1) "\n"
                else ""
    }
    .joinToString("")

private fun Array<CharArray>.countPixels() =
        map {
            it.count {
                it == '#'
            }
        }
        .sum()
