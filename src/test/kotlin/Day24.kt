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

typealias Pos = Pair<Int, Int>

class Connection(val location: AirDuctLocation, val distance: Int) {
    // Overwrite equals, hashcode, toString to avoid recursive calls from AirDuctLocation - Connection - AirDuctLocation
    // In Connection for equality of AirDuctLocations only the pos is considered
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Connection
        if (location.pos != other.location.pos) return false // Compare only pos
        if (distance != other.distance) return false
        return true
    }

    override fun hashCode(): Int {
        var result = location.pos.hashCode() // Use only pos
        result = 31 * result + distance
        return result
    }

    override fun toString(): String {
        return "Connection(location=${location.pos}, distance=$distance)"
    }
}

data class AirDuctLocation(val pos: Pair<Int, Int>, val number: Int? = null, var connections: Set<Connection> = emptySet()) {
//    lateinit var connections: Set<Connection>
}

data class AirDuctGraphWithJunctions(
        val locations: Set<AirDuctLocation>,
        val locationArray: Array<Array<Char>>,
        val locationByPos: Map<Pos, AirDuctLocation> = initLocationMap(locations),
        val locationByNumber: Map<Int, AirDuctLocation> = initLocationMapByNumber(locations)
    ) : Set<AirDuctLocation> by locations {
    init {
        locations.forEach {
            it.connections = findConnectionsIncludingJunctions(it)
        }
    }
    fun findConnectionsIncludingJunctions(location: AirDuctLocation) =
            adjacentPositions(location.pos, this)
                    .mapNotNull {
                        followConnection(it, location.pos)
                    }
                    .toSet()
    fun followConnection(pos: Pos, from: Pos): Connection? {
        var currentPos = pos
        var currentFrom = from
        var distance = 1
        while (true) {
            if (locationByPos.contains(currentPos)) return Connection(locationByPos[currentPos]!!, distance)
            val adjacentPositions = adjacentPositions(currentPos, this).filter { it != currentFrom }
            when(adjacentPositions.size) {
                0 -> return null // Nothing found
                1 -> { currentFrom = currentPos; currentPos = adjacentPositions[0]; distance++ }
                else -> throw IllegalStateException("Found junction which is not in graph")
            }

        }
    }
}

data class AirDuctGraph(
        val locations: Set<AirDuctLocation>,
        val start: AirDuctLocation
    ): Set<AirDuctLocation> by locations {
    init {
        locations.forEach {
            it.connections = findConnections(it)
        }
    }

    private fun findConnections(location: AirDuctLocation): Set<Connection>  = emptySet()
}

data class AirDuctPathElement(val location: AirDuctLocation, val distance: Int = 0)

data class AirDuctPath(val path: List<AirDuctPathElement>, val length: Int)

fun initLocationMap(locations: Set<AirDuctLocation>): Map<Pos, AirDuctLocation> = locations.map { Pair(it.pos, it) }.toMap()
fun initLocationMapByNumber(locations: Set<AirDuctLocation>): Map<Int, AirDuctLocation> = locations.mapNotNull {
    if (it.number != null)
        Pair(it.number, it)
    else
        null
}.toMap()

fun parseAirDuctMapWithJunctions(input: String): AirDuctGraphWithJunctions = buildAirDuctMapFromArray(parseAirDuctMapToArray(input))

fun buildAirDuctMapFromArray(array: Array<Array<Char>>): AirDuctGraphWithJunctions = AirDuctGraphWithJunctions(
        buildSequence {
            array.forEachIndexed { y, line ->
                line.forEachIndexed { x, c ->
                    when {
                        c.isDigit() -> yield(AirDuctLocation(Pair(x, y), c.toString().toInt()))
                        c == '.' && isJunction(x, y, array) -> yield(AirDuctLocation(Pair(x, y)))
                    }
                }
            }
        }.toSet(),
        array
    )

fun parseAirDuctMap(input: String): AirDuctGraph {
    val airDuctGraphWithJunctions = parseAirDuctMapWithJunctions(input)
    val startLocation = airDuctGraphWithJunctions.locationByNumber[0]!!
    val locations = airDuctGraphWithJunctions.locations.filter { it.number != null }.toSet()
    return AirDuctGraph(locations, startLocation)
}

fun parseAirDuctMapToArray(input: String): Array<Array<Char>> = with(parseTrimedLines(input)) {
    Array<Array<Char>>(size) {y ->
        val line = this[y]
        Array<Char>(line.length) { x ->
            line[x]
        }
    }
}

fun isJunction(x: Int, y: Int, array: Array<Array<Char>>) =
        adjacentPositions(x, y, array).count() > 2 // Junction has more than 2 ways leading to it

fun adjacentPositions(x: Int, y: Int, array: Array<Array<Char>>): Set<Pos> =
    listOf(Pair(1, 0), Pair(0, 1), Pair(-1, 0), Pair(0, -1)).map {
        Pair(x + it.first, y + it.second)
    }
    .filter {
        it.second > 0 && it.second < array.size && it.first > 0 && it.first < array[it.second].size && array[it.second][it.first] != '#'
    }
    .toSet()

fun adjacentPositions(pos: Pos, graph: AirDuctGraphWithJunctions): Set<Pos> = adjacentPositions(pos.first, pos.second, graph.locationArray)

fun findShortestPath(airDuctGraph: AirDuctGraphWithJunctions): AirDuctPath {
    val start = airDuctGraph.locationByNumber[0] ?: throw IllegalStateException("No starting node found")
    val nodesToVisit = airDuctGraph.locations.mapNotNull { it.number } - 0
    val startPath = AirDuctPath(listOf(AirDuctPathElement(start,0)), 0)
    var searches = listOf(Pair(nodesToVisit - 0, startPath))
    return findShortestPath(searches)
}

tailrec fun findShortestPath(searches: List<Pair<List<Int>, AirDuctPath>>): AirDuctPath {
    fun expandPath(remainingNodes: List<Int>, path: AirDuctPath): List<Pair<List<Int>, AirDuctPath>> {
        val lastLocationInPath = path.path.last().location
        val prevLocationInPath = if (path.path.size > 1) path.path[path.path.size - 2]
        else null
        val connections = lastLocationInPath.connections
        val minimizedConnections = if (lastLocationInPath.number != null || prevLocationInPath == null) connections // for number nodes going back might be needed (in the example the path is 0 - 4 - 0
        else connections.filter { it.location != prevLocationInPath.location } // but for junctions it doesn't make sense
        return minimizedConnections.map {connection ->
            val nextRemainingNodes =
                    if (connection.location.number != null) remainingNodes - connection.location.number
                    else remainingNodes
            val nextPath = AirDuctPath(path.path + AirDuctPathElement(connection.location, connection.distance), path.length + connection.distance)
            Pair(nextRemainingNodes, nextPath)
        }
    }
    fun purgeSearches(searches: List<Pair<List<Int>, AirDuctPath>>): List<Pair<List<Int>, AirDuctPath>> {
        val completePathes = searches.filter { it.first.size == 0 }
        val shortestSolution = completePathes
                .map { it.second }
                .minBy { it.length }
        val shortestSolutionLength = shortestSolution?.length ?: Int.MAX_VALUE
        val purgedSearches = searches.filter {
            it.first.size == 0 || it.second.length < shortestSolutionLength
        }
        println("Interims=${searches.size} Completed=${completePathes.size} Purged=${purgedSearches.size}")
        return purgedSearches
    }
    val moreToCheck = searches.any { it.first.size > 0}
    if (! moreToCheck) {
        return searches.map { it.second }.minBy { it.length }!!
    } else {
        val nextSearches = searches.flatMap {
            val remainingNodes = it.first
            val path = it.second
            if (remainingNodes.size == 0) listOf(it)
            else expandPath(remainingNodes, path)
        }
        return findShortestPath(purgeSearches(nextSearches))
    }
}

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
        describe("parse map to graph with junctions") {
            given("empty input map") {
                val input = """
                    ####
                    #.#
                    ###
                """.trimIndent()
                it("should be parsed to empty graph") {
                    parseAirDuctMapWithJunctions(input) `should equal` emptySet()
                }
            }
            given("input map with one location") {
                val input = """
                    #####
                    #.0.#
                    #####
                """.trimIndent()
                it("should be parsed to graph with that location") {
                    parseAirDuctMapWithJunctions(input) `should equal` setOf(AirDuctLocation(Pair(2, 1), 0))
                }
            }
            given("input map with two locations") {
                val input = """
                    #####
                    #0#1#
                    #####
                """.trimIndent()
                it("should be parsed to graph with these locations ") {
                    parseAirDuctMapWithJunctions(input) `should equal` setOf(AirDuctLocation(Pair(1, 1), 0), AirDuctLocation(Pair(3, 1), 1))
                }
            }
            given("input map with a junction") {
                val input = """
                    #####
                    #...#
                    ##.##
                """.trimIndent()
                it("should be parsed to graph with this junction ") {
                    parseAirDuctMapWithJunctions(input) `should equal` setOf(AirDuctLocation(Pair(2, 1)))
                }
            }
            given("input map with two locations") {
                val input = """
                    #####
                    #0#1#
                    #####
                """.trimIndent()
                it("should be parsed to graph with these locations ") {
                    parseAirDuctMapWithJunctions(input) `should equal` setOf(AirDuctLocation(Pair(1, 1), 0), AirDuctLocation(Pair(3, 1), 1))
                }
            }
            given("input map with two adjacent locations") {
                val input = """
                    ####
                    #01#
                    ####
                """.trimIndent()
                it("should be parsed to graph with these locations ") {
                    val airDuctGraph = parseAirDuctMapWithJunctions(input)

                    val location0 = AirDuctLocation(Pair(1, 1), 0)
                    val location1 = AirDuctLocation(Pair(2, 1), 1)
                    location0.connections = setOf(Connection(location1, 1))
                    location1.connections = setOf(Connection(location0, 1))
                    airDuctGraph `should equal` setOf(location0, location1)
                }
            }
            given("input map with two connected locations") {
                val input = """
                    #####
                    #0.1#
                    #####
                """.trimIndent()
                it("should be parsed to graph with these locations ") {
                    val airDuctGraph = parseAirDuctMapWithJunctions(input)

                    val location0 = AirDuctLocation(Pair(1, 1), 0)
                    val location1 = AirDuctLocation(Pair(3, 1), 1)
                    location0.connections = setOf(Connection(location1, 2))
                    location1.connections = setOf(Connection(location0, 2))
                    airDuctGraph `should equal` setOf(location0, location1)
                }
            }
            given("input map with two connected locations and a difficult path") {
                val input = """
                    #########
                    #0#...#1#
                    #...#...#
                    #########
                """.trimIndent()
                it("should be parsed to graph with these locations ") {
                    val airDuctGraph = parseAirDuctMapWithJunctions(input)

                    val location0 = AirDuctLocation(Pair(1, 1), 0)
                    val location1 = AirDuctLocation(Pair(7, 1), 1)
                    location0.connections = setOf(Connection(location1, 10))
                    location1.connections = setOf(Connection(location0, 10))
                    airDuctGraph `should equal` setOf(location0, location1)
                }
            }
            given("input map with different locations and a dead end") {
                val input = """
                    ######
                    #0....#
                    ##1#2#
                    #####
                """.trimIndent()
                it("should be parsed to graph with these locations ") {
                    val airDuctGraph = parseAirDuctMapWithJunctions(input)

                    val location0 = AirDuctLocation(Pair(1, 1), 0)
                    val location1 = AirDuctLocation(Pair(2, 2), 1)
                    val location2 = AirDuctLocation(Pair(4, 2), 2)
                    val junctionA = AirDuctLocation(Pair(2, 1))
                    val junctionB = AirDuctLocation(Pair(4, 1))
                    location0.connections = setOf(Connection(junctionA, 1))
                    location1.connections = setOf(Connection(junctionA, 1))
                    location2.connections = setOf(Connection(junctionB, 1))
                    junctionA.connections = setOf(Connection(location0, 1), Connection(location1, 1), Connection(junctionB, 2))
                    junctionB.connections = setOf(Connection(location2, 1), Connection(junctionA, 2))
                    airDuctGraph `should equal` setOf(location0, location1, location2, junctionA, junctionB)
                }
            }
            given("example input map") {
                val input = """
                    ###########
                    #0.1.....2#
                    #.#######.#
                    #4.......3#
                    ###########
                """.trimIndent()
                it("should be parsed to graph with these locations ") {
                    val airDuctGraph = parseAirDuctMapWithJunctions(input)
                    val location0 = AirDuctLocation(Pair(1, 1), 0)
                    val location1 = AirDuctLocation(Pair(3, 1), 1)
                    val location2 = AirDuctLocation(Pair(9, 1), 2)
                    val location3 = AirDuctLocation(Pair(9, 3), 3)
                    val location4 = AirDuctLocation(Pair(1, 3), 4)
                    location0.connections = setOf(Connection(location1, 2), Connection(location4, 2))
                    location1.connections = setOf(Connection(location0, 2), Connection(location2, 6))
                    location2.connections = setOf(Connection(location1, 6), Connection(location3, 2))
                    location3.connections = setOf(Connection(location2, 2), Connection(location4, 8))
                    location4.connections = setOf(Connection(location0, 2), Connection(location3, 8))

                    airDuctGraph `should equal` setOf(location0, location1, location2, location3, location4)
                }
            }
            given("exercise input") {
                val input = readResource("day24Input.txt")
                it ("should be parsed to a graph with 8 number nodes") {
                    val airDuctGraph = parseAirDuctMapWithJunctions(input)
                    val nrNodes = airDuctGraph.locations.size
                    val nrNumberNodes = airDuctGraph.locations.filter { it.number != null }.size
                    val nrJunctionNodes = airDuctGraph.locations.filter { it.number == null }.size
                    println("Exercise graph contains $nrNodes nodes of which $nrNumberNodes are numbers and $nrJunctionNodes junctions")
                    nrNumberNodes `should equal` 8  // 0 -7
                }

            }
        }
        describe("parse map to graph without junctions") {
            given("input map with one location") {
                val input = """
                    #####
                    #.0.#
                    #####
                """.trimIndent()
                it("should be parsed to graph with that location") {
                    parseAirDuctMap(input) `should equal` setOf(AirDuctLocation(Pair(2, 1), 0))
                }
            }
            given("input map with two connected locations") {
                val input = """
                    #####
                    #0.1#
                    #####
                """.trimIndent()
                it("should be parsed to graph with these locations ") {
                    val airDuctGraph = parseAirDuctMap(input)
                    airDuctGraph `should equal` setOf(AirDuctLocation(Pair(1, 1), 0), AirDuctLocation(Pair(3, 1), 1))
                    airDuctGraph.locations `should equal` setOf(
                            AirDuctLocation(Pair(1, 1), 0), AirDuctLocation(Pair(3, 1), 1)
                    )
                }
            }
            given("input map with two connected locations and a difficult path with junctions") {
                val input = """
                    #########
                    #0#.#.#1#
                    #.......#
                    #########
                """.trimIndent()
                it("should be parsed to graph with these locations without junctions") {
                    val airDuctGraph = parseAirDuctMap(input)
                    val location0 = AirDuctLocation(Pair(1, 1), 0)
                    val location1 = AirDuctLocation(Pair(7, 1), 1)
                    location0.connections = setOf(Connection(location1, 10))
                    location1.connections = setOf(Connection(location0, 10))
                    airDuctGraph `should equal` setOf(location0, location1)
                }
            }
        }
        describe("adjacentPositions") {
            given("input map with a junction and some numbers") {
                val input = """
                    #####
                    #0..#
                    ##1##
                    #####
                """.trimIndent()
                val graph = parseAirDuctMapWithJunctions(input)
                it("should find adjacent positions") {
                    adjacentPositions(Pair(2, 1), graph) `should equal` setOf(Pair(1, 1), Pair(2, 2), Pair(3, 1))
                }
            }
        }
        describe("shortest path") {
            given("a map with only the starting node") {
                val input = """
                    ###########
                    #0........#
                    ###########
                """.trimIndent()
                val airDuctGraph = parseAirDuctMapWithJunctions(input)

                it("should find a path with only the starting node") {
                    val airDuctPath = findShortestPath(airDuctGraph)
                    airDuctPath `should equal` AirDuctPath(
                            listOf(
                                AirDuctPathElement(AirDuctLocation(Pair(1, 1), 0), 0)
                            ),
                            0
                    )
                }
            }
            given("a map with two nodes") {
                val input = """
                    ###########
                    #0.......1#
                    ###########
                """.trimIndent()
                val airDuctGraph = parseAirDuctMapWithJunctions(input)

                it("should find a path with only the starting node") {
                    val airDuctPath = findShortestPath(airDuctGraph)
                    airDuctPath `should equal`  AirDuctPath(
                            listOf(
                                AirDuctPathElement(AirDuctLocation(Pair(1, 1), 0), 0),
                                AirDuctPathElement(AirDuctLocation(Pair(9, 1), 1), 8)
                            ),
                            8
                    )
                }
            }
            given("example input map") {
                val input = """
                    ###########
                    #0.1.....2#
                    #.#######.#
                    #4.......3#
                    ###########
                """.trimIndent()
                val airDuctGraph = parseAirDuctMapWithJunctions(input)

                    it("should find the shortest path") {
                    val airDuctPath = findShortestPath(airDuctGraph)
                    airDuctPath `should equal` AirDuctPath(
                            listOf(
                                AirDuctPathElement(AirDuctLocation(Pair(1, 1), 0), 0),
                                AirDuctPathElement(AirDuctLocation(Pair(1, 3), 4), 2),
                                AirDuctPathElement(AirDuctLocation(Pair(1, 1), 0), 2),
                                AirDuctPathElement(AirDuctLocation(Pair(3, 1), 1), 2),
                                AirDuctPathElement(AirDuctLocation(Pair(9, 1), 2), 6),
                                AirDuctPathElement(AirDuctLocation(Pair(9, 3), 3), 2)
                            ),
                            14
                    )
                }
            }
            given("exercise input map") {
                val input = readResource("day24Input.txt")
                val airDuctGraph = parseAirDuctMapWithJunctions(input)

                xit("should find the shortest path") {
                    val airDuctPath = findShortestPath(airDuctGraph)
                    airDuctPath `should equal` AirDuctPath(
                            listOf(
                                    AirDuctPathElement(AirDuctLocation(Pair(1, 1), 0), 0),
                                    AirDuctPathElement(AirDuctLocation(Pair(1, 3), 4), 2),
                                    AirDuctPathElement(AirDuctLocation(Pair(1, 1), 0), 2),
                                    AirDuctPathElement(AirDuctLocation(Pair(3, 1), 1), 2),
                                    AirDuctPathElement(AirDuctLocation(Pair(9, 1), 2), 6),
                                    AirDuctPathElement(AirDuctLocation(Pair(9, 3), 3), 2)
                            ),
                            14
                    )
                }
            }
        }
    }
})




