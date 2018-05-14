import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on as onData

/*

--- Day 17: Two Steps Forward ---
You're trying to access a secure vault protected by a 4x4 grid of small rooms connected by doors.
You start in the top-left room (marked S),
and you can access the vault (marked V) once you reach the bottom-right room:

#########
#S| | | #
#-#-#-#-#
# | | | #
#-#-#-#-#
# | | | #
#-#-#-#-#
# | | |
####### V

Fixed walls are marked with #, and doors are marked with - or |.

The doors in your current room are either open or closed (and locked) based on the hexadecimal MD5 hash of a passcode
(your puzzle input) followed by a sequence of uppercase characters representing the path
you have taken so far (U for up, D for down, L for left, and R for right).

Only the first four characters of the hash are used; they represent, respectively, the doors up, down, left, and right
from your current position.
Any b, c, d, e, or f means that the corresponding door is open;
any other character (any number or a) means that the corresponding door is closed and locked.

To access the vault, all you need to do is reach the bottom-right room;
reaching this room opens the vault and all doors in the maze.

For example, suppose the passcode is hijkl.
Initially, you have taken no steps, and so your path is empty:
you simply find the MD5 hash of hijkl alone.
The first four characters of this hash are ced9, which indicate that up is open (c), down is open (e),
left is open (d), and right is closed and locked (9).
Because you start in the top-left corner, there are no "up" or "left" doors to be open, so your only choice is down.

Next, having gone only one step (down, or D), you find the hash of hijklD.
This produces f2bc, which indicates that you can go back up, left (but that's a wall), or right.
Going right means hashing hijklDR to get 5745 - all doors closed and locked.
However, going up instead is worthwhile:
even though it returns you to the room you started in, your path would then be DU, opening a different set of doors.

After going DU (and then hashing hijklDU to get 528e), only the right door is open;
after going DUR, all doors lock. (Fortunately, your actual passcode is not hijkl).

Passcodes actually used by Easter Bunny Vault Security do allow access to the vault if you know the right path.

For example:

If your passcode were ihgpwlah, the shortest path would be DDRRRD.
With kglvqrro, the shortest path would be DDUDRLRRUDRD.
With ulqzkmiv, the shortest would be DRURDRUDDLLDLUURRDULRLDUUDDDRR.

Given your vault's passcode, what is the shortest path (the actual path, not just the length) to reach the vault?

Your puzzle input is rrrbmfta.

 */

enum class Steps { UP, DOWN, LEFT, RIGHT }

object Day17Spec : Spek({

    describe("part 1") {
        describe("find possible steps") {
            val testData = arrayOf(
                    //       path                          result
                    //--|-------------------------------|-------------------------------------------
                    data(listOf(),                        setOf(Steps.UP, Steps.DOWN, Steps.LEFT)),
                    data(listOf(Steps.DOWN),              setOf(Steps.UP, Steps.LEFT, Steps.RIGHT)),
                    data(listOf(Steps.DOWN, Steps.RIGHT), setOf())
            )
            onData("input %s", with = *testData) { path, expected ->
                it("returns $expected") {
                    findNextSteps("hijkl", path) `should equal` expected
                }
            }
        }
        describe("find possible steps considering walls") {
            val testData = arrayOf(
                    //       path                          result
                    //--|-------------------------------|-------------------------------------------
                    data(listOf<Steps>(),                        setOf(Pair(Steps.DOWN, Pair(0, 1))),
                    data(listOf(Steps.DOWN),              setOf(Pair(Steps.UP, Pair(0, 0)), Pair(Steps.RIGHT, Pair(1, 1))),
                    data(listOf(Steps.DOWN, Steps.RIGHT), setOf<Pair<Steps, Pair<Int, Int>>())
            )
            onData("input %s", with = *testData) { path, expected ->
                it("returns $expected") {
                    findNextStepsConsideringWalls("hijkl", path) `should equal` expected
                }
            }
        }
        describe("steps to string for md5") {
            val testData = arrayOf(
                    //       path           result
                    //--|------------------|--------------
                    data(listOf(), ""),
                    data(listOf(Steps.UP), "U"),
                    data(listOf(Steps.DOWN), "D"),
                    data(listOf(Steps.LEFT), "L"),
                    data(listOf(Steps.RIGHT), "R"),
                    data(listOf(Steps.UP, Steps.DOWN, Steps.LEFT, Steps.RIGHT), "UDLR")
            )
            onData("input %s", with = *testData) { path, expected ->
                it("returns $expected") {
                    stepsToString(path) `should equal` expected
                }
            }
        }
        describe("examples") {
            given("example 1") {
                val seed = "ihgpwlah"
                findStepsToVault(seed) `should equal` "DDRRRD".toPath()
            }
        }
    }
})

fun findStepsToVault(seed: String) = findStepsToVaultBreadthFirst(seed, listOf(), setOf())

fun findStepsToVaultBreadthFirst(seed: String, pathes: List<Set<Steps>>, visited: Set<Pair<Int, Int>>): Any {
    for(path in pathes) {

    }
    TODO()
}

private fun String.toPath() =
        map {
            when(it) {
                'U' -> Steps.UP
                'D' -> Steps.DOWN
                'L' -> Steps.LEFT
                'R' -> Steps.RIGHT
                else -> throw IllegalArgumentException("Unexpected $it")
            }
        }

fun findNextStepsConsideringWalls(seed: String, path: List<Steps>) =
    findNextSteps(seed, path).mapNotNull {
        val pos = movePos(path + it)
        if (pos.first in 0..3 && pos.second in 0..3) Pair(it, pos)
        else null
    }
    .toSet()

fun movePos(list: List<Steps>) = list.fold(Pair(0, 0)) { pos, step ->
    when(step) {
        Steps.UP -> Pair(pos.first - 1, pos.second)
        Steps.DOWN -> Pair(pos.first + 1, pos.second)
        Steps.LEFT -> Pair(pos.first, pos.second - 1)
        Steps.RIGHT -> Pair(pos.first, pos.second + 1)
    }
}

fun findNextSteps(seed: String, path: List<Steps>): Set<Steps> =
    codeCharsToSteps(md5(seed + stepsToString(path)))

fun stepsToString(path: List<Steps>) =
        path.map {
            it.name[0]
        }.joinToString("")

fun codeCharsToSteps(str: String) =
        listOf(Steps.UP, Steps.DOWN, Steps.LEFT, Steps.RIGHT)
        .mapIndexed { i, step ->
            if (str[i] in 'b'..'f') step
            else null
        }
        .filterNotNull()
        .toSet()

