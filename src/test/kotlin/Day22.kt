import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xit

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

 */

object Day22Spec : Spek({

    describe("part 1") {
        given("simple example") {
            val inputStrings = listOf(
                    "root@ebhq-gridcenter# df -h",
                    "Filesystem              Size  Used  Avail  Use%",
                    "/dev/grid/node-x0-y0     94T   70T    24T   77%",
                    "/dev/grid/node-x0-y1     87T    7T    80T   73%",
                    "/dev/grid/node-x1-y0     94T   14T    80T   71%",
                    "/dev/grid/node-x1-y1     89T   69T    20T   77%"
            )
            val input = parseStorageCluster(inputStrings)
            it ("should be parsed correctly") {
                input.size `should equal` 4
                input[0] `should equal` StorageNode("node-x0-y0", 94, 70)
            }
            it("should find the correct viable pairs") {
                findViablePairs(input) `should equal` listOf(
                        Pair("node-x0-y0", "node-x0-y1"), Pair("node-x0-y0", "node-x1-y0"),
                        Pair("node-x0-y1", "node-x0-y0"), Pair("node-x0-y1", "node-x1-y0"), Pair("node-x0-y1", "node-x1-y1"),
                        Pair("node-x1-y0", "node-x0-y0"), Pair("node-x1-y0", "node-x0-y1"), Pair("node-x1-y0", "node-x1-y1"),
                        Pair("node-x1-y1", "node-x0-y1"), Pair("node-x1-y1", "node-x1-y0")
                )
            }
        }
        given("exercise") {
            val inputString = readTrimedLinesFromResource("day22Input.txt")
            val input = parseStorageCluster(inputString)
            it("should find viable pairs") {
                val viablePairs = findViablePairs(input)
                viablePairs.size `should equal` 934
            }
        }
    }
})

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
        val nameParts = str.split("/")
        return nameParts[nameParts.size - 1]
    }
    val parts = input.split("""\s+""".toPattern())
    val name = parseName(parts[0])
    val size = parseSize(parts[1])
    val used = parseSize(parts[2])
    return StorageNode(name, size, used)
}

data class StorageNode(val name: String, val size: Int, val used: Int) {
    val avail: Int get() = size - used
}