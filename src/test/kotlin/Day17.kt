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

--- Part Two ---

You're curious how robust this security solution really is,
and so you decide to find longer and longer paths which still provide access to the vault.
You remember that paths always end the first time they reach the bottom-right room
(that is, they can never pass through it, only end in it).

For example:

If your passcode were ihgpwlah, the longest path would take 370 steps.
With kglvqrro, the longest path would be 492 steps long.
With ulqzkmiv, the longest path would be 830 steps long.
What is the length of the longest path that reaches the vault?

 */

enum class Steps { UP, DOWN, LEFT, RIGHT }


fun findStepsToVault(seed: String) = findStepsToVaultBreadthFirst(seed, setOf(listOf()))

fun findStepsToVaultBreadthFirst(seed: String, pathes: Set<List<Steps>>): List<Steps> {
    val nextPathes = pathes.flatMap { path ->
        val nextSteps = findNextStepsConsideringWalls(seed, path)
        nextSteps.map { nextStep ->
            val nextPath = path + listOf(nextStep.first)
            if (nextStep.second == Pair(3, 3))
                return@findStepsToVaultBreadthFirst nextPath // Solution found
            else nextPath
        }
    }.toSet()
    if (nextPathes.isEmpty()) throw IllegalArgumentException("No solution found")
    return findStepsToVaultBreadthFirst(seed, nextPathes)
}

fun findLongestSteps(seed: String): List<Steps>? {
    val allPathes = findAllStepsToVault(seed)
    return allPathes.maxBy { it.size }
}

fun findAllStepsToVault(seed: String): Set<List<Steps>> {
    return findAllStepsToVaultBreadthFirst(seed, setOf(listOf()), mutableSetOf())
}

fun findAllStepsToVaultBreadthFirst(seed: String, pathes: Set<List<Steps>>, pathesToVault: MutableSet<List<Steps>>): Set<List<Steps>> {
    val nextPathes = pathes.flatMap { path ->
        val nextSteps = findNextStepsConsideringWalls(seed, path)
        nextSteps.map { nextStep ->
            val nextPath = path + listOf(nextStep.first)
            if (nextStep.second == Pair(3, 3)) {
                pathesToVault.add(nextPath) // Solution found
                null
            }
            else nextPath
        }
                .filterNotNull()
    }.toSet()
    if (nextPathes.isEmpty()) return pathesToVault // All pathes found
    return findAllStepsToVaultBreadthFirst(seed, nextPathes, pathesToVault)
}

fun String.toPath() =
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
        Steps.UP -> Pair(pos.first, pos.second - 1)
        Steps.DOWN -> Pair(pos.first, pos.second + 1)
        Steps.LEFT -> Pair(pos.first - 1, pos.second)
        Steps.RIGHT -> Pair(pos.first + 1, pos.second)
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
                    //   seed           path                          result
                    //--|-------------|-------------------------------|-------------------------------------------
                    data("hijkl",    listOf<Steps>(),                setOf(Pair(Steps.DOWN, Pair(0, 1)))),
                    data("hijkl",    listOf(Steps.DOWN),             setOf(Pair(Steps.UP, Pair(0, 0)), Pair(Steps.RIGHT, Pair(1, 1)))),
                    data("hijkl",   listOf(Steps.DOWN, Steps.RIGHT), setOf<Pair<Steps, Pair<Int, Int>>>()),
                    data("kglvqrro", listOf(Steps.DOWN, Steps.DOWN), setOf(Pair(Steps.UP, Pair(0, 1))))
            )
            onData("input %s", with = *testData) { seed, path, expected ->
                it("returns $expected") {
                    findNextStepsConsideringWalls(seed, path) `should equal` expected
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
                it ("should find solution") {
                    stepsToString(findStepsToVault(seed)) `should equal` "DDRRRD"
                }
            }
            given("example 2") {
                val seed = "kglvqrro"
                it ("should find solution") {
                    stepsToString(findStepsToVault(seed)) `should equal` "DDUDRLRRUDRD"
                }
            }
            given("example 3") {
                val seed = "ulqzkmiv"
                it ("should find solution") {
                    stepsToString(findStepsToVault(seed)) `should equal` "DRURDRUDDLLDLUURRDULRLDUUDDDRR"
                }
            }
        }
        describe("exercise") {
            given("seed") {
                val seed = "rrrbmfta"
                it("should find solution") {
                    stepsToString(findStepsToVault(seed)) `should equal` "RLRDRDUDDR"
                }
            }
        }
    }
    describe("part 2") {
        describe("examples") {
            given("example 1") {
                val seed = "ihgpwlah"
                it ("should find solution") {
                    findLongestSteps(seed)!!.size `should equal` 370
                }
            }
            given("example 2") {
                val seed = "kglvqrro"
                it ("should find solution") {
                    findLongestSteps(seed)!!.size `should equal` 492
                }
            }
            given("example 3") {
                val seed = "ulqzkmiv"
                it ("should find solution") {
                    findLongestSteps(seed)!!.size `should equal` 830
                }
            }
        }
        describe("exercise") {
            given("seed") {
                val seed = "rrrbmfta"
                it("should find solution") {
                    findLongestSteps(seed)!!.size `should equal` 420
                }
            }
        }
    }

})

