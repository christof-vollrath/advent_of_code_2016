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
            it.connections = findDirectConnections(it)
        }
    }

    private fun findDirectConnections(location: AirDuctLocation): Set<Connection> = findDirectConnectionsRec(
            location.connections.filter { connection ->
                connection.location.number != null
            }
            .map { it.location.number!! to it }.toMap(),
            location.connections.filter { it.location.number == null }.toSet(),
            setOf(location)
    )
    private tailrec fun findDirectConnectionsRec(foundConnections: Map<Int, Connection>, connectionsToJunctions: Set<Connection>, visitedJunctions: Set<AirDuctLocation>): Set<Connection> =
            if (connectionsToJunctions.isEmpty()) foundConnections.values.toSet()
            else {
                val nextVisitedJunctions = visitedJunctions + connectionsToJunctions.map { it.location }
                val newConnectionMap = connectionsToJunctions.flatMap { connectionToJunction ->
                    val junction = connectionToJunction.location
                    val junctionDistance = connectionToJunction.distance
                    junction.connections.mapNotNull { junctionConnection ->
                        val junctionLocation = junctionConnection.location
                        if (!nextVisitedJunctions.contains(junctionLocation) && junctionLocation.number != null) junctionLocation.number to Connection(junctionLocation, junctionDistance + junctionConnection.distance)
                        else null
                    }
                }.toMap()
                val nextFoundConnections = foundConnections.toMutableMap()
                newConnectionMap.forEach { (number, connection) ->
                    val existingConnection = nextFoundConnections[number]
                    if (existingConnection != null) {
                        if (existingConnection.distance > connection.distance) nextFoundConnections[number] = connection // use smaller connection
                    } else nextFoundConnections[number] = connection
                }
                val nextConnectionsToJunctions = connectionsToJunctions.flatMap { connectionToJunction ->
                    val junction = connectionToJunction.location
                    val junctionDistance = connectionToJunction.distance
                    junction.connections.mapNotNull { junctionConnection ->
                        val junctionLocation = junctionConnection.location
                        if (!nextVisitedJunctions.contains(junctionLocation) && junctionLocation.number == null)
                            Connection(junctionLocation, junctionDistance + junctionConnection.distance)
                        else null
                    }
                }.toSet()
                findDirectConnectionsRec(nextFoundConnections, nextConnectionsToJunctions, nextVisitedJunctions)
            }
}

data class AirDuctPathElement(val location: AirDuctLocation, val distance: Int = 0)

data class AirDuctPath(val path: List<AirDuctPathElement>, val length: Int) {
    fun toCompactString() = path.map { it.location.number}.joinToString(" ") + " ($length)"
}

fun detectLoop(airDuctPath: AirDuctPath): Boolean {
    getSnippets(airDuctPath).fold(mutableSetOf<Pair<Int?,Int?>>()) { snippetSet, snippet ->
        if(snippet in snippetSet) detectLoop@return true
        snippetSet.add(snippet)
        snippetSet
    }
    return false
}

fun getSnippets(airDuctPath: AirDuctPath) = airDuctPath.path.map {
            it.location.number
        }
        .zipWithNext { a, b ->  Pair(a, b)}


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

fun findShortestPath(airDuctGraph: AirDuctGraph): AirDuctPath {
    val start = airDuctGraph.start
    val nodesToVisit = airDuctGraph.locations.mapNotNull { it.number } - 0
    val startPath = AirDuctPath(listOf(AirDuctPathElement(start,0)), 0)
    var searches = listOf(Pair(nodesToVisit, startPath))
    return if (nodesToVisit.isEmpty()) startPath
    else findShortestPath(searches, null)
}

tailrec fun findShortestPath(searches: List<Pair<List<Int>, AirDuctPath>>, shortestPath: AirDuctPath?): AirDuctPath {
    fun expandPath(remainingNodes: List<Int>, path: AirDuctPath): List<Pair<List<Int>, AirDuctPath>> {
        val lastLocationInPath = path.path.last().location
        return lastLocationInPath.connections.map {connection ->
            val nextRemainingNodes =
                    if (connection.location.number != null) remainingNodes - connection.location.number
                    else remainingNodes
            val nextPath = AirDuctPath(path.path + AirDuctPathElement(connection.location, connection.distance), path.length + connection.distance)
            Pair(nextRemainingNodes, nextPath)
        }
    }
    fun purgeSearches(searches: List<Pair<List<Int>, AirDuctPath>>, shortestPath: AirDuctPath?): List<Pair<List<Int>, AirDuctPath>> {
        //println("Shortest: " + shortestPath?.toCompactString())
        //searches.forEach { println(it.second.toCompactString()) }
        val shortestPathLength = shortestPath?.length ?: Int.MAX_VALUE
        val purgedSearches = searches.filter {
            it.second.length < shortestPathLength && // No search should be longer than the best solution found so far
            !detectLoop(it.second)
        }
        println("Interims=${searches.size} Shortest=$shortestPathLength  Purged=${purgedSearches.size} MinUncompleted=${purgedSearches.map{it.second}.minBy { it.length }?.length}")
        return purgedSearches
    }
    val moreToCheck = searches.any { it.first.size > 0}
    if (! moreToCheck) {
        return shortestPath!!
    } else {
        val nextSearches = searches.flatMap {
            val remainingNodes = it.first
            val path = it.second
            if (remainingNodes.size == 0) listOf(it)
            else expandPath(remainingNodes, path)
        }
        val completePathes = nextSearches.filter { it.first.size == 0 }.map { it.second }
        val nextShortestPathCandidate = completePathes.minBy { it.length }
        val nextShortestPath =
                when {
                    shortestPath == null -> nextShortestPathCandidate
                    nextShortestPathCandidate == null -> shortestPath
                    else -> if (nextShortestPathCandidate.length < shortestPath.length) nextShortestPathCandidate else shortestPath
                }

        return findShortestPath(purgeSearches(nextSearches, nextShortestPath), nextShortestPath)
    }
}

fun AirDuctPath.dropConnections() = copy(path = path.map { it.dropConnections() })
private fun AirDuctPathElement.dropConnections() = copy(location = location.copy(connections = emptySet()))

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
                    val location0 = AirDuctLocation(Pair(1, 1), 0)
                    val location1 = AirDuctLocation(Pair(3, 1), 1)
                    location0.connections = setOf(Connection(location1, 2))
                    location1.connections = setOf(Connection(location0, 2))
                    airDuctGraph `should equal` setOf(location0, location1)
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
                    location0.connections = setOf(Connection(location1, 8))
                    location1.connections = setOf(Connection(location0, 8))
                    airDuctGraph `should equal` setOf(location0, location1)
                }
            }
            given("input map with three connected locations and a difficult path with junctions") {
                val input = """
                    #########
                    #0#.#.#2#
                    #...1...#
                    #########
                """.trimIndent()
                it("should be parsed to graph with these locations without junctions") {
                    val airDuctGraph = parseAirDuctMap(input)
                    val location0 = AirDuctLocation(Pair(1, 1), 0)
                    val location1 = AirDuctLocation(Pair(4, 2), 1)
                    val location2 = AirDuctLocation(Pair(7, 1), 2)
                    location0.connections = setOf(Connection(location1, 4))
                    location1.connections = setOf(Connection(location0, 4), Connection(location2, 4))
                    location2.connections = setOf(Connection(location1, 4))
                    airDuctGraph `should equal` setOf(location0, location1, location2)
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
                    val airDuctGraph = parseAirDuctMap(input)
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
                    val airDuctGraph = parseAirDuctMap(input)
                    val nrNodes = airDuctGraph.locations.size
                    nrNodes `should equal` 8  // 0 -7
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
        describe("getPathSnippets") {
            given("empty AirDuctPath") {
                val airDuctPath = AirDuctPath(emptyList(), 0)
                it("should have empty path snippets") {
                    getSnippets(airDuctPath) `should equal` emptyList()
                }
            }
            given("AirDuctPath with one element") {
                val airDuctPath = AirDuctPath(listOf(AirDuctPathElement(AirDuctLocation(Pair(1, 1), 0))), 0)
                it("should have empty path snippets") {
                    getSnippets(airDuctPath) `should equal` emptyList()
                }
            }
            given("AirDuctPath with two elements") {
                val airDuctPath = AirDuctPath(listOf(
                        AirDuctPathElement(AirDuctLocation(Pair(1, 1), 0)),
                        AirDuctPathElement(AirDuctLocation(Pair(1, 2), 1))),
                        1)
                it("should have these two elements as snippet") {
                    getSnippets(airDuctPath) `should equal` listOf(Pair(0, 1))
                }
            }
            given("AirDuctPath with some elements") {
                val airDuctPath = AirDuctPath(listOf(
                        AirDuctPathElement(AirDuctLocation(Pair(1, 1), 0)),
                        AirDuctPathElement(AirDuctLocation(Pair(1, 2), 1)),
                        AirDuctPathElement(AirDuctLocation(Pair(1, 3), 2)),
                        AirDuctPathElement(AirDuctLocation(Pair(1, 4), 3))),
                        3)
                it("should have more snippets") {
                    getSnippets(airDuctPath) `should equal` listOf(Pair(0, 1), Pair(1, 2), Pair(2, 3))
                }
            }
        }
        describe("detectLoop") {
            given("empty AirDuctPath") {
                val airDuctPath = AirDuctPath(emptyList(), 0)
                it("should not detect a loop") {
                    detectLoop(airDuctPath) `should equal` false
                }
            }
            given("AirDuctPath with a simple loop") {
                val airDuctPath = AirDuctPath(listOf(
                        AirDuctPathElement(AirDuctLocation(Pair(1, 1), 0)),
                        AirDuctPathElement(AirDuctLocation(Pair(1, 2), 1)),
                        AirDuctPathElement(AirDuctLocation(Pair(1, 1), 0)),
                        AirDuctPathElement(AirDuctLocation(Pair(1, 2), 1))),
                        0)
                it("should detect loop") {
                    detectLoop(airDuctPath) `should equal` true
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
                val airDuctGraph = parseAirDuctMap(input)

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
                val airDuctGraph = parseAirDuctMap(input)

                it("should find the path between two nodes") {
                    val airDuctPath = findShortestPath(airDuctGraph)
                    val location0 = AirDuctLocation(Pair(1, 1), 0)
                    val location1 = AirDuctLocation(Pair(9, 1), 1)
                    location0.connections = setOf(Connection(location1, 8))
                    location1.connections = setOf(Connection(location0, 8))

                    airDuctPath `should equal`  AirDuctPath(
                            listOf(
                                AirDuctPathElement(location0, 0),
                                AirDuctPathElement(location1, 8)
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
                val airDuctGraph = parseAirDuctMap(input)

                    it("should find the shortest path") {
                    val airDuctPath = findShortestPath(airDuctGraph).dropConnections()
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
                val airDuctGraph = parseAirDuctMap(input)

                it("should find the shortest path") {
                    val airDuctPath = findShortestPath(airDuctGraph).dropConnections()
                    println(airDuctPath.length)
                    val path = airDuctPath.path.map { it.location.number }
                    path `should equal` listOf(
                                    1
                            )
                }
            }
        }
    }
})




