import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.*
import kotlin.coroutines.experimental.buildSequence

/*

--- Day 24: Air Duct Spelunking ---

You've finally met your match; the doors that provide access to the roof are locked tight,
and all of the controls and related electronics are inaccessible.
You simply can't reach them.

The robot that cleans the air ducts, however, can.

It's not a very fast little robot, but you reconfigure it to be able to interface
with some of the exposed wires that have been routed through the HVAC system.
If you can direct it to each of those locations, you should be able to bypass the security controls.

You extract the duct layout for this area from some blueprints you acquired
and create a map with the relevant locations marked (your puzzle input).
0 is your current location, from which the cleaning robot embarks;
the other numbers are (in no particular order) the locations the robot needs to visit at least once each.
Walls are marked as #, and open passages are marked as .. Numbers behave like open passages.

For example, suppose you have a map like the following:

###########
#0.1.....2#
#.#######.#
#4.......3#
###########

To reach all of the points of interest as quickly as possible, you would have the robot take the following path:

0 to 4 (2 steps)
4 to 1 (4 steps; it can't move diagonally)
1 to 2 (6 steps)
2 to 3 (2 steps)

Since the robot isn't very fast, you need to find it the shortest route.
This path is the fewest steps (in the above example, a total of 14) required to start at 0
and then visit every other location at least once.

Given your actual map, and starting from location 0, what is the fewest number of steps
required to visit every non-0 number marked on the map at least once?

*/

data class AirDuctLocation(val pos: Pair<Int, Int>, val number: Int? = null)
typealias AirDuctGraph = Set<AirDuctLocation>

class Day24Spec: Spek({
    describe("part 1") {
        describe("parse map to array") {
            given("some input") {
                val input = """
                    ####
                    #.0#
                    ####
                """.trimIndent()
                it("should be parsed to array") {
                    parseAirDuctMapToArray(input) `should equal` arrayOf(
                            arrayOf('#', '#', '#', '#'),
                            arrayOf('#', '.', '0', '#'),
                            arrayOf('#', '#', '#', '#')
                    )
                }
            }

        }
        describe("parse map to graph") {
            given("empty input map") {
                val input = """
                    ####
                    #.#
                    ###
                """.trimIndent()
                it("should be parsed to empty graph") {
                    parseAirDuctMap(input) `should equal` emptySet()
                }
            }
            given("input map with one location") {
                val input = """
                    #####
                    #.1.#
                    #####
                """.trimIndent()
                it("should be parsed to graph with that location") {
                    parseAirDuctMap(input) `should equal` setOf(AirDuctLocation(Pair(2, 1), 1))
                }
            }
            given("input map with two locations") {
                val input = """
                    #####
                    #0#1#
                    #####
                """.trimIndent()
                it("should be parsed to graph with these locations ") {
                    parseAirDuctMap(input) `should equal` setOf(AirDuctLocation(Pair(1, 1), 0), AirDuctLocation(Pair(3, 1), 1))
                }
            }
            given("input map with a junction") {
                val input = """
                    #####
                    #...#
                    ##.##
                """.trimIndent()
                it("should be parsed to graph with this junction ") {
                    parseAirDuctMap(input) `should equal` setOf(AirDuctLocation(Pair(2, 1)))
                }
            }
        }
    }
})

fun parseAirDuctMap(input: String): AirDuctGraph = buildAirDuctMapFromArray(parseAirDuctMapToArray(input))

fun buildAirDuctMapFromArray(array: Array<Array<Char>>): AirDuctGraph = buildSequence {
    array.forEachIndexed { y, line ->
        line.forEachIndexed { x, c ->
            when {
                c.isDigit() -> yield(AirDuctLocation(Pair(x, y), c.toString().toInt()))
                c == '.' && isJunction(x, y, array) -> yield(AirDuctLocation(Pair(x, y)))
            }
        }
    }
}.toSet()

fun parseAirDuctMapToArray(input: String): Array<Array<Char>> = with(parseTrimedLines(input)) {
    Array<Array<Char>>(size) {y ->
        val line = this[y]
        Array<Char>(line.length) { x ->
            line[x]
        }
    }
}

fun isJunction(x: Int, y: Int, array: Array<Array<Char>>) =
    listOf(Pair(1, 0), Pair(0, 1), Pair(-1, 0), Pair(0, -1)).filter {
        val x2 = x + it.first
        val y2 = y + it.second
        y2 > 0 && y2 < array.size && x2 > 0 && x2 < array[y2].size &&array[y + it.second][x + it.first] == '.'
    }.count() > 2 // Junction has more than 2 ways leading to it


