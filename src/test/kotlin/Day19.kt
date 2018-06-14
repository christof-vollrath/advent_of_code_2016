import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xgiven

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

--- Part Two ---

Realizing the folly of their present-exchange rules,
the Elves agree to instead steal presents from the Elf directly across the circle.
If two Elves are across the circle, the one on the left (from the perspective of the stealer) is stolen from.
The other rules remain unchanged: Elves with no presents are removed from the circle entirely,
and the other elves move in slightly to keep the circle evenly spaced.

For example, with five Elves (again numbered 1 to 5):

The Elves sit in a circle; Elf 1 goes first:
  1
5   2
 4 3

Elves 3 and 4 are across the circle; Elf 3's present is stolen, being the one to the left.
Elf 3 leaves the circle, and the rest of the Elves move in:

  1           1
5   2  -->  5   2
 4 -          4

Elf 2 steals from the Elf directly across the circle, Elf 5:

  1         1
-   2  -->     2
  4         4

Next is Elf 4 who, choosing between Elves 1 and 2, steals from Elf 1:

 -          2
    2  -->
 4          4

Finally, Elf 2 steals from Elf 4:

 2
    -->  2
 -

So, with five Elves, the Elf that sits starting in position 2 gets all the presents.

With the number of Elves given in your puzzle input, which Elf now gets all the presents?

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
        val swap = nextElfPos(circle, currPos)
        if (swap == null) return circle[currPos]
        transferPresents(circle, currPos, swap)
        val next = nextElfPos(circle, currPos) // now left neighbor without presents will be skipped
        if (next == null) return circle[currPos]
        currPos = next
    }
}

fun transferPresents2(circle: MutableList<Pair<Int, Int>>, currPos: Int, swapPos: Int) {
    val currElf = circle[currPos]
    val swapElf = circle[swapPos]
    circle[currPos] = Pair(currElf.first, currElf.second + swapElf.second)
    circle.removeAt(swapPos)
}

fun elfCircle2(size: Int): Pair<Int, Int> {
    val circle = createCircleOfElves(size)
    var currPos = 0
    while(true) {
        var swapPos = crossCircleElf(circle.size, currPos)
        if (swapPos == null) return circle[currPos]
        transferPresents2(circle, currPos, swapPos)
        if (swapPos < currPos) currPos-- // must be corrected because transferPresent2 removed before current pos
        val nextCurrPos = nextElfPos(circle, currPos) // now left neighbor without presents will be skipped
        if (nextCurrPos == null) return circle[currPos]
        currPos = nextCurrPos
    }
}

fun createCircleOfElves(size: Int) = (1..size).map { Pair(it, 1) }.toMutableList()

fun nextElfPos(circle: List<Pair<Int, Int>>, pos: Int?): Int? {
    if (pos != null) {
        var curr: Int = pos
        while(true) {
            curr = (curr + 1).rem(circle.size)
            if (curr == pos) return null
            if (circle[curr].second > 0) return curr
        }
    } else return null
}

fun crossCircleElf(circleSize: Int, pos: Int): Int? {
    val result = (pos + circleSize / 2).rem(circleSize)
    return if (result == pos) null
    else result
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
        xgiven("exercise") {
            val input = 3012210
            it("should find correct elf") {
                elfCircle(input) `should equal` Pair(1830117, 3012210)
            }
        }

    }
    describe("part 2") {
        describe("next elf position") {
            it("should got to cross circle") {
                crossCircleElf(5, 0) `should equal` 2
            }
            it("should got to cross circle and around") {
                crossCircleElf(5, 4) `should equal` 1
            }
            it("should skip elf without presents") {
                crossCircleElf(4, 1) `should equal` 3
            }
            it("should handle special case correctly") {
                crossCircleElf(3, 2) `should equal` 0
            }
        }
        given("example") {
            val input = 5
            it("should find correct elf") {
                elfCircle2(input) `should equal` Pair(2, 5)
            }
        }
        xgiven("exercise") {
            val input = 3012210
            it("should find correct elf") {
                elfCircle2(input) `should equal` Pair(1417887, 3012210)
            }
        }
    }
})
