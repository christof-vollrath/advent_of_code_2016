import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

/*
--- Day 19: An Elephant Named Joseph ---

The Elves contact you over a highly secure emergency channel.
Back at the North Pole, the Elves are busy misunderstanding White Elephant parties.

Each Elf brings a present.
They all sit in a circle, numbered starting with position 1.
Then, starting with the first Elf, they take turns stealing all the presents from the Elf to their left.
An Elf with no presents is removed from the circle and does not take turns.

For example, with five Elves (numbered 1 to 5):

  1
5   2
 4 3

Elf 1 takes Elf 2's present.
Elf 2 has no presents and is skipped.
Elf 3 takes Elf 4's present.
Elf 4 has no presents and is also skipped.
Elf 5 takes Elf 1's two presents.
Neither Elf 1 nor Elf 2 have any presents, so both are skipped.
Elf 3 takes Elf 5's three presents.
So, with five Elves, the Elf that sits starting in position 3 gets all the presents.

With the number of Elves given in your puzzle input, which Elf gets all the presents?

Your puzzle input is 3012210.

 */

fun transferPresents(circle: MutableList<Pair<Int, Int>>, curr: Int, next: Int) {
    val currElf = circle[curr]
    val nextElf = circle[next]
    circle[curr] = Pair(currElf.first, currElf.second + nextElf.second)
    circle[next] = Pair(nextElf.first, 0)
}

fun elfCircle(size: Int): Pair<Int, Int> {
    val circle = createCircleOfElves(size)
    var currPos = 0
    while(true) {
        val left = nextElfPos(circle, currPos)
        if (left == null) return circle[currPos]
        transferPresents(circle, currPos, left)
        val next = nextElfPos(circle, currPos) // now left neighbor without will be skipped
        if (next == null) return circle[currPos]
        currPos = next
    }
}

fun createCircleOfElves(size: Int) = (1..size).map { Pair(it, 1) }.toMutableList()

fun nextElfPos(circle: List<Pair<Int, Int>>, pos: Int): Int? {
    var curr = pos
    while(true) {
        curr = (curr + 1).rem(circle.size)
        if (curr == pos) return null
        if (circle[curr].second > 0) return curr
    }
}

object Day19Spec : Spek({

    describe("part 1") {
        given("example") {
            val input = 5
            it("should find correct elf") {
                elfCircle(input) `should equal` Pair(3, 5)
            }
        }
        it("should create a circle of elves") {
            val circle = createCircleOfElves(5)
            circle.map { it.first} `should equal` (1..5).toList()
            circle.map { it.second }.all { it == 1 }.`should be true`()
        }
        describe("next elf position") {
            given("a simple circle of elfs") {
                val circle = mutableListOf(Pair(1,1), Pair(2,0), Pair(3,2), Pair(4,5))
                it("should got to next elf") {
                    nextElfPos(circle, 2) `should equal` 3
                }
                it("should skip elf without presents") {
                    nextElfPos(circle, 0) `should equal` 2
                }
                it("should turn around") {
                    nextElfPos(circle, 3) `should equal` 0
                }
            }
            given("another circle of elfs") {
                val circle = mutableListOf(Pair(1,0), Pair(2,0), Pair(3,2), Pair(4,0), Pair(5,3))
                it("should turn around") {
                    nextElfPos(circle, 4) `should equal` 2
                }
            }
        }
        describe("transfer presents") {
            given("a simple circle of elfs") {
                val circle = listOf(Pair(1, 1), Pair(2, 0), Pair(3, 2), Pair(4, 5))
                it("should got to next elf") {
                    val testCircle = circle.toMutableList()
                    transferPresents(testCircle, 2, 3)
                    testCircle `should equal` listOf(Pair(1, 1), Pair(2, 0), Pair(3, 7), Pair(4, 0))
                }
                it("should skip elf without presents") {
                    val testCircle = circle.toMutableList()
                    transferPresents(testCircle, 0, 2)
                    testCircle  `should equal` listOf(Pair(1, 3), Pair(2, 0), Pair(3, 0), Pair(4, 5))
                }
                it("should turn around") {
                    val testCircle = circle.toMutableList()
                    transferPresents(testCircle, 3, 0)
                    testCircle `should equal` listOf(Pair(1, 0), Pair(2, 0), Pair(3, 2), Pair(4, 6))
                }
            }
        }
        given("exercise") {
            val input = 3012210
            it("should find correct elf") {
                elfCircle(input) `should equal` Pair(1830117, 3012210)
            }
        }

    }
})
