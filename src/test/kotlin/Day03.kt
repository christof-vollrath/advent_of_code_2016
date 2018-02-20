import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on as onData


/*
--- Day 3: Squares With Three Sides ---

Now that you can think clearly, you move deeper into the labyrinth of hallways
and office furniture that makes up this part of Easter Bunny HQ.
This must be a graphic design department; the walls are covered in specifications for triangles.

Or are they?

The design document gives the side lengths of each triangle it describes, but... 5 10 25?
Some of these aren't triangles. You can't help but mark the impossible ones.

In a valid triangle, the sum of any two sides must be larger than the remaining side.
For example, the "triangle" given above is impossible, because 5 + 10 is not larger than 25.

In your puzzle input, how many of the listed triangles are possible?

--- Part Two ---

Now that you've helpfully marked up their design documents,
it occurs to you that triangles are specified in groups of three vertically.
Each set of three numbers in a column specifies a triangle. Rows are unrelated.

For example, given the following specification, numbers with the same hundreds digit would be part of the same triangle:

101 301 501
102 302 502
103 303 503
201 401 601
202 402 602
203 403 603

In your puzzle input, and instead reading by columns, how many of the listed triangles are possible?


 */


fun checkTriangle(sides: List<Int>) = (0..2).all { sides.sumExceptIndex(it) > sides[it] }

fun Collection<Int>.sumExceptIndex(exceptIndex: Int) = filterIndexed { index, _ -> index != exceptIndex  }.sum() // TODO make more generic

fun countTriangles(triangles: List<List<Int>>) = triangles.filter { checkTriangle(it) }.count()


fun parseTriangles(input: String): List<List<Int>> =
        input.split("\n")
                .filter { ! it.isBlank() }
                .map { parseTriangle(it) }

fun parseTriangle(input: String) =
        input.split(" ")
                .filter { ! it.isBlank() }
                .map { it.toInt() }


fun parseTriangles2(input: String): List<List<Int>> {
    val rowsList = parseTriangles(input)
    val all = rowsList.map { it[0] } + rowsList.map { it[1] } + rowsList.map { it[2] }
    return all.withIndex()
            .groupBy { it.index / 3 }
            .map { it.value.map { it.value } }
}


class Day3Spec : Spek({

    describe("part 1") {
        describe("check triangle") {
            val testData = arrayOf(
                    //       sides      result
                    //--|-------------|--------------
                    data(listOf(5, 10, 25), false),
                    data(listOf(25, 10, 5), false),
                    data(listOf(10, 5, 25), false),
                    data(listOf(5, 10, 12), true),
                    data(listOf(14, 10, 5), true),
                    data(listOf(10, 5, 10), true),
                    data(listOf(15, 10, 25), false) // Edge case 15 + 10 = 25
            )
            onData("input %s", with = *testData) { sides, expected ->
                it("returns $expected") {
                    checkTriangle(sides) `should equal` expected
                }
            }
        }
        describe("parse") {
            val input = """
                        1 2 3
                        4 5 6
                        """
            it("should be parsed to two lists") {
                parseTriangles(input) `should equal` listOf(
                        listOf(1, 2, 3),
                        listOf(4, 5, 6)
                )
            }
        }
        describe("count triangles") {
            val input = """
                        1 2 3
                        2 3 4
                        3 6 12
                        4 5 6
                        """
            val triangles = parseTriangles(input)
            countTriangles(triangles) `should equal` 2
        }

        describe("exercise") {
            val input = readResource("day03Input.txt")
            val triangles = parseTriangles(input)
            val result = countTriangles(triangles)
            println(result)
            result `should equal` 993

        }
    }

    describe("part 2") {
        describe("different parser") {
            val input = """
                    101 301 501
                    102 302 502
                    103 303 503
                    201 401 601
                    202 402 602
                    203 403 603
                    """
            val triangles = parseTriangles2(input)
            it("should be parsed differently") {
                triangles `should equal`listOf(
                        listOf(101, 102, 103),
                        listOf(201, 202, 203),
                        listOf(301, 302, 303),
                        listOf(401, 402, 403),
                        listOf(501, 502, 503),
                        listOf(601, 602, 603)
                )
            }
        }
        describe("exercise") {
            val input = readResource("day03Input.txt")
            val triangles = parseTriangles2(input)
            val result = countTriangles(triangles)
            println(result)
            result `should equal` 1849
        }
    }

})


