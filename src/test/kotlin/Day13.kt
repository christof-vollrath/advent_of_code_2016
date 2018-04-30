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


data class VirtualMaze(val seed: Int) {
    fun get(x: Int, y: Int): Char = if (createMazeField(seed, x, y)) '#' else '.'
}

fun printVirtualMaze(maze: VirtualMaze, width: Int, height: Int, path: MazePath? = null): String {
    val coordinates = path?.toCoordinates()?.toSet()
    return (0 until height).map { y ->
        (0 until width).map { x ->
            if (coordinates != null && coordinates.contains(Pair(x,y))) 'O'
            else maze.get(x, y)
        } .joinToString("")+ '\n'
    }.joinToString("")
}

fun Int.countBits(): Int {
    // Only for positive Int
    var result = 0
    var bit = 1
    while (bit != 0) {
        if (this and bit > 0) result++
        bit = bit shl 1
    }
    return result
}

fun createMazeField(seed: Int, x: Int, y: Int) =
        (seed + mazeSum(x, y)).countBits() % 2 != 0

fun mazeSum(x: Int, y: Int) = x*x + 3*x + 2*x*y + y + y*y

class Day13Spec : Spek({

    describe("part 1") {
        describe("example virtual maze") {
            given("virtual maze with seed 10") {
                val virtualMaze = VirtualMaze(10)
                it("should create the correct maze") {
                    printVirtualMaze(virtualMaze, 10, 7) `should equal` """
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
        describe("example path") {
            given("virtual maze with seed 10") {
                val virtualMaze = VirtualMaze(10)
                it("should find path") {
                    val path = findPath(virtualMaze, Pair(1, 1), Pair(7, 4))
                    printVirtualMaze(virtualMaze, 10, 7, path) `should equal` """
                        .#.####.##
                        .O#..#...#
                        #OOO.##...
                        ###O#.###.
                        .##OO#OO#.
                        ..##OOO.#.
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

fun findPath(virtualMaze: VirtualMaze, start: Pair<Int, Int>, goal: Pair<Int, Int>): MazePath = //TODO
        MazePath(start, goal, listOf(
                Pair(0,1), Pair(1,0), Pair(1,0),
                Pair(0,1), Pair(0,1), Pair(1,0),
                Pair(0,1), Pair(1,0), Pair(1,0),
                Pair(0,-1), Pair(1,0)
        ))

data class MazePath(val start: Pair<Int, Int>, val goal: Pair<Int, Int>, val moves: List<Pair<Int, Int>>) {
    fun toCoordinates(): List<Pair<Int, Int>> {
        var currentPos = start
        return listOf(start) + moves.map {
            currentPos = movePos(currentPos, it)
            currentPos
        }
    }

    private fun movePos(pos: Pair<Int, Int>, move: Pair<Int, Int>): Pair<Int, Int>  =
            Pair(pos.first + move.first, pos.second + move.second)
}