
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.api.dsl.xdescribe
import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on as onData

/*
--- Day 1: No Time for a Taxicab ---

Santa's sleigh uses a very high-precision clock to guide its movements,
and the clock's oscillator is regulated by stars.
Unfortunately, the stars have been stolen... by the Easter Bunny.
To save Christmas, Santa needs you to retrieve all fifty stars by December 25th.

Collect stars by solving puzzles. Two puzzles will be made available on each day in the advent calendar;
the second puzzle is unlocked when you complete the first. Each puzzle grants one star. Good luck!

You're airdropped near Easter Bunny Headquarters in a city somewhere.
"Near", unfortunately, is as close as you can get -
the instructions on the Easter Bunny Recruiting Document the Elves intercepted start here,
and nobody had time to work them out further.

The Document indicates that you should start at the given coordinates (where you just landed) and face North.
Then, follow the provided sequence: either turn left (L) or right (R) 90 degrees,
then walk forward the given number of blocks, ending at a new intersection.

There's no time to follow such ridiculous instructions on foot, though,
so you take a moment and work out the destination.
Given that you can only walk on the street grid of the city, how far is the shortest path to the destination?

For example:

Following R2, L3 leaves you 2 blocks East and 3 blocks North, or 5 blocks away.
R2, R2, R2 leaves you 2 blocks due South of your starting position, which is 2 blocks away.
R5, L5, R5, R3 leaves you 12 blocks away.
How many blocks away is Easter Bunny HQ?

*/

// Part 1

enum class Direction { NORTH, EAST, SOUTH, WEST }

sealed class Instruction(open val blocks: Int)
data class TurnRight(override val blocks: Int) : Instruction(blocks)
data class TurnLeft(override val blocks: Int) : Instruction(blocks)

data class Cab(var position: Pair<Int, Int> = Pair(0, 0), var direction: Direction = Direction.NORTH) {
    fun drive(instructions: String) = drive(parseInstructions(instructions))
    fun drive(instructions: List<Instruction>): Cab = apply {
        instructions.forEach {
            drive(it)
        }
    }
    fun drive(instruction: Instruction): Cab = apply {
        direction = turn(instruction)
        position = move(direction, instruction.blocks)
    }

    fun move(direction: Direction, blocks: Int): Pair<Int, Int> =
            when(direction) {
                Direction.NORTH -> Pair(position.first, position.second + blocks)
                Direction.EAST  -> Pair(position.first + blocks, position.second)
                Direction.SOUTH -> Pair(position.first, position.second - blocks)
                Direction.WEST -> Pair(position.first - blocks, position.second)
            }

    fun turn(instruction: Instruction) =
            when(instruction) {
                is TurnRight ->
                    when(direction) {
                        Direction.NORTH -> Direction.EAST
                        Direction.EAST  -> Direction.SOUTH
                        Direction.SOUTH -> Direction.WEST
                        Direction.WEST -> Direction.NORTH
                    }
                is TurnLeft ->
                    when(direction) {
                        Direction.NORTH -> Direction.WEST
                        Direction.WEST  -> Direction.SOUTH
                        Direction.SOUTH -> Direction.EAST
                        Direction.EAST  -> Direction.NORTH
                    }
            }

}

fun parseInstructions(instructions: String) = instructions.split(",").map { parseInstruction(it) }

fun parseInstruction(instruction: String): Instruction {
    fun parseBlocks(instruction: String) = instruction.trim().substring(1).toInt()
    return when(instruction.trim()[0]) {
        'R' -> TurnRight(parseBlocks(instruction))
        'L' -> TurnLeft(parseBlocks(instruction))
        else -> throw IllegalArgumentException("Unexpected instruction ${instruction[0]}")
    }
}

fun distance(point1: Pair<Int, Int>, point2: Pair<Int, Int> = Pair(0, 0)) = Math.abs(point1.first - point2.first) + Math.abs(point1.second - point2.second)


class Day1Spec : Spek({

    describe("part 1") {
        describe("example drive") {
            val testData = arrayOf(
                    //    instructions         distance
                    //--|--------------------|--------------
                    data("R2, L3",         5),
                    data("R2, R2, R2",     2),
                    data("R5, L5, R5, R3", 12)
            )
            onData("input %s", with = *testData) { instructions, distance ->
                it("returns $distance") {
                    distance(Cab().drive(instructions).position, Pair(0,0)) `should equal` distance
                }
            }
        }
        describe("turn right") {
            val testData = arrayOf(
                    //    times         resulting direction
                    //--|-----|-----------------------------
                    data(0,  Direction.NORTH),
                    data(1,  Direction.EAST),
                    data(2,  Direction.SOUTH),
                    data(3,  Direction.WEST),
                    data(4,  Direction.NORTH),
                    data(5,  Direction.EAST)
            )
            onData("input %s", with = *testData) { times, direction ->
                val cab = Cab()
                val instructions = List(times) { TurnRight(0) }
                it("returns $direction") {
                    cab.drive(instructions)
                    cab.direction `should equal` direction
                    cab.position `should equal` Pair(0, 0)
                }
            }
        }
        describe("turn left") {
            val testData = arrayOf(
                    //    times         resulting direction
                    //--|-----|-----------------------------
                    data(0,  Direction.NORTH),
                    data(1,  Direction.WEST),
                    data(2,  Direction.SOUTH),
                    data(3,  Direction.EAST),
                    data(4,  Direction.NORTH),
                    data(5,  Direction.WEST)
            )
            onData("input %s", with = *testData) { times, direction ->
                val cab = Cab()
                val instructions = List(times) { TurnLeft(0) }
                it("returns $direction") {
                    cab.drive(instructions)
                    cab.direction `should equal` direction
                    cab.position `should equal` Pair(0, 0)
                }
            }
        }
        describe("move1") {
            val cab = Cab()
            on("R2") {
                val instructions = listOf(TurnRight(2))
                it("should move 2 blocks to the east") {
                    cab.drive(instructions)
                    cab.direction `should equal` Direction.EAST
                    cab.position `should equal` Pair(2, 0)
                }

            }
        }
        describe("move") {
            val testData = arrayOf(
                    //    times         resulting direction
                    //--|-----|-----------------------------
                    data(Direction.NORTH, TurnRight(1), Pair(1, 0)),
                    data(Direction.EAST, TurnRight(1), Pair(0, -1)),
                    data(Direction.SOUTH, TurnRight(1), Pair(-1, 0)),
                    data(Direction.WEST, TurnRight(1), Pair(0, 1))
            )
            onData("input %s", with = *testData) { direction, instruction, position ->
                val cab = Cab(direction = direction)
                val instructions = listOf(instruction)
                it("returns $position") {
                    cab.drive(instructions)
                    cab.position `should equal` position
                }
            }
        }
        describe("parse") {
            on("R0") {
                val input = "R0"
                it("should be parsed to a single instruction") {
                    parseInstructions(input) `should equal` listOf(TurnRight(0))
                }
            }
            on("R1, L2") {
                val input = "R1, L2"
                it("should be parsed to a single instruction") {
                    parseInstructions(input) `should equal` listOf(TurnRight(1), TurnLeft(2))
                }
            }
        }
        describe("exercise") {
            val input = readResource("day01Input.txt")
            val distance = distance(Cab().drive(input).position, Pair(0,0))
            println("Day01 solution part1=$distance")
        }
    }
})
