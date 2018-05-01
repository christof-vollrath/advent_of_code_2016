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

--- Part Two ---

How many locations (distinct x,y coordinates, including your starting location)
can you reach in at most 50 steps?


 */

typealias MazeCoord = Pair<Int, Int>

data class VirtualMaze(val seed: Int) {
    fun get(x: Int, y: Int): Char = if (createMazeField(seed, x, y)) '#' else '.'
    fun get(pos: MazeCoord): Char = get(pos.first, pos.second)
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


fun findPath(virtualMaze: VirtualMaze, start: MazeCoord, goal: MazeCoord): MazePath =
        findPathBreathFirst(virtualMaze, start, goal, listOf(MazePath(start, start, emptyList())), emptySet())

fun findPathBreathFirst(virtualMaze: VirtualMaze, start: MazeCoord, goal: MazeCoord, pathes: List<MazePath>, alreadyVisited: Set<MazeCoord>): MazePath {
    for(path in pathes) {
        if (path.end == goal) return path
    }
    val nextPathes = pathes.flatMap {
        if (it.end in alreadyVisited) emptyList()
        else it.nextMazePathes(virtualMaze)
    }
    if (nextPathes.isEmpty()) throw IllegalArgumentException("Nothing found")
    val ends = pathes.map { it.end }

    return findPathBreathFirst(virtualMaze, start, goal, nextPathes, alreadyVisited + ends)
}

data class MazePath(val start: MazeCoord, val end: MazeCoord, val moves: List<MazeCoord>) {
    fun toCoordinates(): List<MazeCoord> {
        var currentPos = start
        return listOf(start) + moves.map {
            currentPos = movePos(currentPos, it)
            currentPos
        }
    }

    fun movePos(pos: MazeCoord, move: MazeCoord): MazeCoord  =
            Pair(pos.first + move.first, pos.second + move.second)

    fun nextMazePathes(virtualMaze: VirtualMaze): List<MazePath> =
            listOf(
                    Pair(0, 1),
                    Pair(0, -1),
                    Pair(1, 0),
                    Pair(-1, 0)
            )
            .map {
                val nextPos = movePos(end, it)
                if (virtualMaze.get(nextPos) == '#') null
                else this.copy(end = movePos(end, it), moves = moves + it)
            }
            .filterNotNull()

}

// Part 2

fun countReachableLocations(virtualMaze: VirtualMaze, start: MazeCoord, steps: Int) =
        countReachableLocations(virtualMaze, start, steps, listOf(MazePath(start, start, emptyList())), emptySet(), 0)

fun countReachableLocations(virtualMaze: VirtualMaze, start: MazeCoord, steps: Int, pathes: List<MazePath>, alreadyVisited: Set<MazeCoord>, currentSteps: Int): Int {
    val ends = pathes.map { it.end }
    val nextAlreadyVisited = alreadyVisited + ends
    if (currentSteps == steps)
        return nextAlreadyVisited.size
    else {
        val nextPathes = pathes.flatMap {
            if (it.end in alreadyVisited) emptyList()
            else it.nextMazePathes(virtualMaze)
        }
        if (nextPathes.isEmpty()) nextAlreadyVisited.size // Nothing more can be found
        return countReachableLocations(virtualMaze, start, steps, nextPathes, nextAlreadyVisited, currentSteps+1)
    }
}

object Day13Spec : Spek({

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
        describe("find path") {
            given("the start is the goal") {
                val virtualMaze = VirtualMaze(10)
                it("should find path") {
                    val path = findPath(virtualMaze, Pair(1, 1), Pair(1, 1))
                    printVirtualMaze(virtualMaze, 10, 7, path) `should equal` """
                        .#.####.##
                        .O#..#...#
                        #....##...
                        ###.#.###.
                        .##..#..#.
                        ..##....#.
                        #...##.###

                        """.trimIndent()
                }
            }
            given("path only one step") {
                val virtualMaze = VirtualMaze(10)
                it("should find path") {
                    val path = findPath(virtualMaze, Pair(1, 1), Pair(1, 2))
                    printVirtualMaze(virtualMaze, 10, 7, path) `should equal` """
                        .#.####.##
                        .O#..#...#
                        #O...##...
                        ###.#.###.
                        .##..#..#.
                        ..##....#.
                        #...##.###

                        """.trimIndent()
                }
            }
        }
        describe("exercise path") {
            given("virtual maze with seed 1362") {
                val virtualMaze = VirtualMaze(1362)
                it("should find path") {
                    val path = findPath(virtualMaze, Pair(1, 1), Pair(31, 39))
                    val len = path.moves.size
                    println("Day13, part 1 path length=$len")
                    len `should equal` 82
                }
            }
        }
    }
    describe("part 2") {
        describe("count reachable locations") {
            given("0 steps") {
                val virtualMaze = VirtualMaze(10)
                it("should be 1 (the start)") {
                    val nr = countReachableLocations(virtualMaze, Pair(1, 1), 0)
                    nr `should equal` 1
                }
            }
            given("1 step") {
                val virtualMaze = VirtualMaze(10)
                it("should be 3") {
                    val nr = countReachableLocations(virtualMaze, Pair(1, 1), 1)
                    nr `should equal` 3
                }
            }
            given("2 steps") {
                val virtualMaze = VirtualMaze(10)
                it("should be 5") {
                    val nr = countReachableLocations(virtualMaze, Pair(1, 1), 2)
                    nr `should equal` 5
                }
            }
            given("3 steps") {
                val virtualMaze = VirtualMaze(10)
                it("should be 6") {
                    val nr = countReachableLocations(virtualMaze, Pair(1, 1), 3)
                    nr `should equal` 6
                }
            }
            given("4 steps") {
                val virtualMaze = VirtualMaze(10)
                it("should be 9") {
                    val nr = countReachableLocations(virtualMaze, Pair(1, 1), 4)
                    nr `should equal` 9
                }
            }
            given("5 steps") {
                val virtualMaze = VirtualMaze(10)
                it("should be 9") {
                    val nr = countReachableLocations(virtualMaze, Pair(1, 1), 5)
                    nr `should equal` 9
                }
            }
            describe("exercise") {
                given("virtual maze with seed 1362") {
                    val virtualMaze = VirtualMaze(1362)
                    it("should find path") {
                        val nr = countReachableLocations(virtualMaze, Pair(1, 1), 50)
                        println("Day13, part 2 reachable locations=$nr")
                        nr `should equal` 3
                    }
                }
            }
        }

    }
})
