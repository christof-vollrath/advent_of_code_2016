import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on as onData

/*
--- Day 13: A Maze of Twisty Little Cubicles ---

You arrive at the first floor of this new building to discover a much less welcoming environment
than the shiny atrium of the last one.
Instead, you are in a maze of twisty little cubicles, all alike.

Every location in this area is addressed by a pair of non-negative integers (x,y).
Each such coordinate is either a wall or an open space.
You can't move diagonally.
The cube maze starts at 0,0 and seems to extend infinitely toward positive x and y;
negative values are invalid, as they represent a location outside the building.
You are in a small waiting area at 1,1.

While it seems chaotic, a nearby morale-boosting poster explains, the layout is actually quite logical.

You can determine whether a given x,y coordinate will be a wall or an open space using a simple system:

Find x*x + 3*x + 2*x*y + y + y*y.

Add the office designer's favorite number (your puzzle input).
Find the binary representation of that sum; count the number of bits that are 1.
If the number of bits that are 1 is even, it's an open space.
If the number of bits that are 1 is odd, it's a wall.

For example, if the office designer's favorite number were 10, drawing walls as # and open spaces as .,
the corner of the building containing 0,0 would look like this:

  0123456789
0 .#.####.##
1 ..#..#...#
2 #....##...
3 ###.#.###.
4 .##..#..#.
5 ..##....#.
6 #...##.###

Now, suppose you wanted to reach 7,4. The shortest route you could take is marked as O:

  0123456789
0 .#.####.##
1 .O#..#...#
2 #OOO.##...
3 ###O#.###.
4 .##OO#OO#.
5 ..##OOO.#.
6 #...##.###

Thus, reaching 7,4 would take a minimum of 11 steps (starting from your current location, 1,1).

What is the fewest number of steps required for you to reach 31,39?

Your puzzle input is 1362.

 */

class Day13Spec : Spek({

    describe("part 1") {
        describe("example") {
            given("the example number 10") {
                val input = 10
                it("should create the correct maze") {
                    val maze = createMaze(input, width=10, hight=7)
                    printMaze(maze) `should equal` """
                        .#.####.##
                        ..#..#...#
                        #....##...
                        ###.#.###.
                        .##..#..#.
                        ..##....#.
                        #...##.###

                        """.trimIndent()
                }
            }
        }
        describe("countBits") {
            val testData = arrayOf(
                    //    nr    bits
                    //--|-----|-----------------------------
                    data(0, 0),
                    data(1, 1),
                    data(2, 1),
                    data(3, 2),
                    data(4, 1),
                    data(36, 2),
                    data(256, 1),
                    data(255, 8),
                    data(Int.MAX_VALUE, 31)
            )
            onData("input %s", with = *testData) { nr, bits ->
                    nr.countBits() `should equal` bits
            }
        }
        describe("mazeSum") {
            val testData = arrayOf(
                    //    x     y      sum
                    //--|-----|------|------------
                    data(0, 0, 0),
                    data(1, 0, 4),
                    data(0, 1, 1),
                    data(3, 2, 36)
            )
            onData("input %s", with = *testData) { x, y, sum ->
                mazeSum(x, y) `should equal` sum
            }
        }
    }
})

private fun Int.countBits(): Int {
    // Only for positive Int
    var result = 0
    var bit = 1
    while (bit != 0) {
        if (this and bit > 0) result++
        bit = bit shl 1
    }
    return result
}

fun printMaze(maze: Array<Array<Char>>) =
        maze.map { row ->
            row.joinToString("")+ '\n'
        }.joinToString("")

fun createMaze(seed: Int, width: Int, hight: Int): Array<Array<Char>> {
    val result = Array(hight, { Array(width, { '.' })})
    for (y in 0 until hight)
        for (x in 0 until width)
            result[y][x] = if (createMazeField(seed, x, y)) '#' else '.'
    return result
}

fun createMazeField(seed: Int, x: Int, y: Int) =
        (seed + mazeSum(x, y)).countBits() % 2 != 0

fun mazeSum(x: Int, y: Int) = x*x + 3*x + 2*x*y + y + y*y

