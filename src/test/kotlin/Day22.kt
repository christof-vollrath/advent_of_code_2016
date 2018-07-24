import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

/*

--- Day 22: Grid Computing ---

You gain access to a massive storage cluster arranged in a grid;
each storage node is only connected to the four nodes directly adjacent to it
(three if the node is on an edge, two if it's in a corner).

You can directly access data only on node /dev/grid/node-x0-y0,
but you can perform some limited actions on the other nodes:

You can get the disk usage of all nodes (via df). The result of doing this is in your puzzle input.

You can instruct a node to move (not copy) all of its data to an adjacent node
(if the destination node has enough space to receive the data). The sending node is left empty after this operation.
Nodes are named by their position: the node named node-x10-y10 is adjacent to nodes node-x9-y10,
node-x11-y10, node-x10-y9, and node-x10-y11.

Before you begin, you need to understand the arrangement of data on these nodes.
Even though you can only move data between directly connected nodes,
you're going to need to rearrange a lot of the data to get access to the data you need.
Therefore, you need to work out how you might be able to shift data around.

To do this, you'd like to count the number of viable pairs of nodes.
A viable pair is any two nodes (A,B), regardless of whether they are directly connected, such that:

Node A is not empty (its Used is not zero).
Nodes A and B are not the same node.
The data on node A (its Used) would fit on node B (its Avail).
How many viable pairs of nodes are there?

--- Part Two ---

Now that you have a better understanding of the grid, it's time to get to work.

Your goal is to gain access to the data which begins in the node with y=0 and the highest x
(that is, the node in the top-right corner).

For example, suppose you have the following grid:

Filesystem            Size  Used  Avail  Use%
/dev/grid/node-x0-y0   10T    8T     2T   80%
/dev/grid/node-x0-y1   11T    6T     5T   54%
/dev/grid/node-x0-y2   32T   28T     4T   87%
/dev/grid/node-x1-y0    9T    7T     2T   77%
/dev/grid/node-x1-y1    8T    0T     8T    0%
/dev/grid/node-x1-y2   11T    7T     4T   63%
/dev/grid/node-x2-y0   10T    6T     4T   60%
/dev/grid/node-x2-y1    9T    8T     1T   88%
/dev/grid/node-x2-y2    9T    6T     3T   66%

In this example, you have a storage grid 3 nodes wide and 3 nodes tall.
The node you can access directly, node-x0-y0, is almost full.
The node containing the data you want to access, node-x2-y0 (because it has y=0 and the highest x value),
contains 6 terabytes of data - enough to fit on your node, if only you could make enough space to move it there.

Fortunately, node-x1-y1 looks like it has enough free space to enable you to move some of this data around.
In fact, it seems like all of the nodes have enough space to hold any node's data
(except node-x0-y2, which is much larger, very full, and not moving any time soon).
So, initially, the grid's capacities and connections look like this:

( 8T/10T) --  7T/ 9T -- [ 6T/10T]
    |           |           |
  6T/11T  --  0T/ 8T --   8T/ 9T
    |           |           |
 28T/32T  --  7T/11T --   6T/ 9T

The node you can access directly is in parentheses; the data you want starts in the node marked by square brackets.

In this example, most of the nodes are interchangable: they're full enough that no other node's data would fit,
but small enough that their data could be moved around.
Let's draw these nodes as ..
The exceptions are the empty node, which we'll draw as _,
and the very large, very full node, which we'll draw as #. Let's also draw the goal data as G. Then, it looks like this:

(.) .  G
 .  _  .
 #  .  .

The goal is to move the data in the top right, G, to the node in parentheses.
To do this, we can issue some commands to the grid and rearrange the data:

Move data from node-y0-x1 to node-y1-x1, leaving node node-y0-x1 empty:

(.) _  G
 .  .  .
 #  .  .

Move the goal data from node-y0-x2 to node-y0-x1:

(.) G  _
 .  .  .
 #  .  .

At this point, we're quite close. However, we have no deletion command, so we have to move some more data around.
So, next, we move the data from node-y1-x2 to node-y0-x2:

(.) G  .
 .  .  _
 #  .  .

Move the data from node-y1-x1 to node-y1-x2:

(.) G  .
 .  _  .
 #  .  .
Move the data from node-y1-x0 to node-y1-x1:

(.) G  .
 _  .  .
 #  .  .

Next, we can free up space on our node by moving the data from node-y0-x0 to node-y1-x0:

(_) G  .
 .  .  .
 #  .  .

Finally, we can access the goal data by moving the it from node-y0-x1 to node-y0-x0:

(G) _  .
 .  .  .
 #  .  .

So, after 7 steps, we've accessed the data we want.
Unfortunately, each of these moves takes time, and we need to be efficient:

What is the fewest number of steps required to move your goal data to node-x0-y0?
 */

object Day22Spec : Spek({

    describe("part 1") {
        given("simple example") {
            val inputStrings = listOf(
                    "root@ebhq-gridcenter# df -h",
                    "Filesystem              Size  Used  Avail  Use%",
                    "/dev/grid/node-x0-y0     94T   70T    24T   77%",
                    "/dev/grid/node-x0-y1     87T    7T    80T   73%",
                    "/dev/grid/node-x1-y0     94T    0T    80T   0%",
                    "/dev/grid/node-x1-y1     89T   69T    20T   77%"
            )
            val input = parseStorageCluster(inputStrings)
            val grid = createGrid(input)
            it ("should be parsed correctly") {
                input.size `should equal` 4
                input[0] `should equal` StorageNode("node-x0-y0", Pair(0, 0), 94, 70)
            }
            it("should find the correct viable pairs") {
                findViablePairs(input) `should equal` listOf(
                        Pair("node-x0-y0", "node-x0-y1"), Pair("node-x0-y0", "node-x1-y0"),
                        Pair("node-x0-y1", "node-x0-y0"), Pair("node-x0-y1", "node-x1-y0"), Pair("node-x0-y1", "node-x1-y1"),
                        Pair("node-x1-y1", "node-x0-y1"), Pair("node-x1-y1", "node-x1-y0")
                )
            }
            it("should print grid") {
                printGrid(grid)
            }
            it("should find empty node") {
                val emptyNodePos = findEmptyNode(grid)
                emptyNodePos `should equal` Pair(1, 0)
            }
        }
        given("example") {
            val inputStrings = listOf(
                "root@ebhq-gridcenter# df -h",
                "Filesystem            Size  Used  Avail  Use%",
                "/dev/grid/node-x0-y0   10T    8T     2T   80%",
                "/dev/grid/node-x0-y1   11T    6T     5T   54%",
                "/dev/grid/node-x0-y2   32T   28T     4T   87%",
                "/dev/grid/node-x1-y0    9T    7T     2T   77%",
                "/dev/grid/node-x1-y1    8T    0T     8T    0%",
                "/dev/grid/node-x1-y2   11T    7T     4T   63%",
                "/dev/grid/node-x2-y0   10T    6T     4T   60%",
                "/dev/grid/node-x2-y1    9T    8T     1T   88%",
                "/dev/grid/node-x2-y2    9T    6T     3T   66%"
            )
            val input = parseStorageCluster(inputStrings)
            val grid = createGrid(input)
            it("should print grid") {
                printGrid(grid)
            }
            it("should find empty node") {
                val emptyNodePos = findEmptyNode(grid)
                emptyNodePos `should equal` Pair(1, 1)
            }
            it("should count steps") {
                val steps = countSteps(grid)
                steps `should equal` 7
            }

        }
        given("exercise") {
            val inputString = readTrimedLinesFromResource("day22Input.txt")
            val input = parseStorageCluster(inputString)
            it("should find viable pairs") {
                val viablePairs = findViablePairs(input)
                viablePairs.size `should equal` 934
            }
            it("should print grid") {
                val grid = createGrid(input)
                printGrid(grid)
            }
            it("should count steps") {
                val grid = createGrid(input)
                val steps = countSteps(grid)
                steps `should equal` -1 // TODO
            }
        }
    }
})

fun countSteps(grid: Array<Array<StorageNode?>>): Int {
    val stepsToMoveGoalToTarget = countStepsToMoveGoalToTarget(grid)
    return countStepsToMoveEmptyNodeToGoal(grid) +
            stepsToMoveGoalToTarget +
            4 * (stepsToMoveGoalToTarget - 1) // move empty node arround
}

fun countStepsToMoveEmptyNodeToGoal(grid: Array<Array<StorageNode?>>) = 1

fun countStepsToMoveGoalToTarget(grid: Array<Array<StorageNode?>>) = 2

fun findEmptyNode(grid: Array<Array<StorageNode?>>): Pair<Int, Int> {
    grid.forEachIndexed { y, row ->
        row.forEachIndexed { x, storageNode ->
            println(storageNode?.used)
            if (storageNode?.used == 0) findEmptyNode@return Pair(x, y)
        }
    }
    throw IllegalArgumentException("No empty node")
}


fun findViablePairs(input: List<StorageNode>) = input.flatMap { node1 ->
    input.mapNotNull { node2 ->
        if (node1 != node2 && node1.used != 0 && node1.used < node2.avail) Pair(node1.name, node2.name)
        else null
    }
}


fun parseStorageCluster(input: List<String>): List<StorageNode>  = input.drop(2).map { parseStorageNode(it) }

fun parseStorageNode(input: String): StorageNode {
    fun parseSize(str: String) = str.substring(0, str.length-1).toInt()
    fun parseName(str: String): String {
        val pathParts = str.split("/")
        return pathParts[pathParts.size - 1]
    }
    fun parsePos(name: String): Pair<Int, Int> {
        val nameParts = name.split("-")
        val x = nameParts[1].substring(1).toInt()
        val y = nameParts[2].substring(1).toInt()
        return Pair(x, y)
    }
    val parts = input.split("""\s+""".toPattern())
    val name = parseName(parts[0])
    val pos = parsePos(name)
    val size = parseSize(parts[1])
    val used = parseSize(parts[2])
    return StorageNode(name, pos, size, used)
}

fun createGrid(nodes: List<StorageNode>): Array<Array<StorageNode?>> {
    val maxX = nodes.map { it.pos.first }.max() ?: 0
    val maxY = nodes.map { it.pos.second }.max() ?: 0
    val grid = Array(maxY+1, { Array<StorageNode?>(maxX+1, { null }) })
    nodes.forEach {
        grid[it.pos.second][it.pos.first] = it
    }
    return grid
}

fun printGrid(grid: Array<Array<StorageNode?>>) {
    grid.forEach { row ->
        row.forEach { node ->
            val c = when {
                node == null -> ' '
                node.pos == Pair(0, 0) -> '@'
                node.pos == Pair(0, row.size-1) -> 'G'
                node.used == 0 -> '_'
                node.used <= 100 -> '.'
                node.used > 100 -> '#'
                else -> '?'
            }
            print(c)
        }
        println()
    }
}

data class StorageNode(val name: String, val pos: Pair<Int, Int>, val size: Int, val used: Int) {
    val avail: Int get() = size - used
}