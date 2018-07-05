import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

/*
--- Day 21: Scrambled Letters and Hash ---

The computer system you're breaking into uses a weird scrambling function to store its passwords.
It shouldn't be much trouble to create your own scrambled password so you can add it to the system;
you just have to implement the scrambler.

The scrambling function is a series of operations (the exact list is provided in your puzzle input).
Starting with the password to be scrambled, apply each operation in succession to the string.
The individual operations behave as follows:

(1) swap position X with position Y means that the letters at indexes X and Y (counting from 0) should be swapped.

(2) swap letter X with letter Y means that the letters X and Y should be swapped
(regardless of where they appear in the string).

(3) rotate left/right X steps means that the whole string should be rotated;
for example, one right rotation would turn abcd into dabc.

(4) rotate based on position of letter X means that the whole string should be rotated to the right
based on the index of letter X (counting from 0) as determined before this instruction does any rotations.
Once the index is determined, rotate the string to the right one time, plus a number of times equal to that index,
plus one additional time if the index was at least 4.

(5) reverse positions X through Y means that the span of letters at indexes X through Y (including the letters at X and Y)
should be reversed in order.

(6) move position X to position Y means that the letter which is at index X should be removed from the string,
then inserted such that it ends up at index Y.

For example, suppose you start with abcde and perform the following operations:

swap position 4 with position 0 swaps the first and last letters, producing the input for the next step, ebcda.
swap letter d with letter b swaps the positions of d and b: edcba.
reverse positions 0 through 4 causes the entire string to be reversed, producing abcde.
rotate left 1 step shifts all letters left one position,
  causing the first letter to wrap to the end of the string: bcdea.
move position 1 to position 4 removes the letter at position 1 (c),
  then inserts it at position 4 (the end of the string): bdeac.
move position 3 to position 0 removes the letter at position 3 (a),
  then inserts it at position 0 (the front of the string): abdec.
rotate based on position of letter b finds the index of letter b (1),
  then rotates the string right once plus a number of times equal to that index (2): ecabd.
rotate based on position of letter d finds the index of letter d (4),
  then rotates the string right once, plus a number of times equal to that index,
  plus an additional time because the index was at least 4, for a total of 6 right rotations: decab.

After these steps, the resulting scrambled password is decab.

Now, you just need to generate a new scrambled password and you can access the system.
Given the list of scrambling operations in your puzzle input, what is the result of scrambling abcdefgh?

--- Part Two ---

You scrambled the password correctly, but you discover that you can't actually modify the password file on the system.
You'll need to un-scramble one of the existing passwords by reversing the scrambling process.

What is the un-scrambled version of the scrambled password fbgdceah?

 */

typealias ScramblingOperation = (String) -> String

fun String.applyScramblingOperations(input: List<ScramblingOperation>) = input.fold(this) { acc, current -> current(acc) }

fun parseScramblingOperations(input: List<String>) = input.map {
    parseScramblingOperation(it)
}

fun parseScramblingOperation(input: String): ScramblingOperation {
    val parts = input.split(" ")
    val operation = parts[0]
    return when (operation) {
        "move" -> createMove(parts[2].toInt(), parts[5].toInt())
        "swap" -> when(parts[1]) {
            "position" -> createSwapPosition(parts[2].toInt(), parts[5].toInt())
            "letter" -> createSwapLetter(parts[2][0], parts[5][0])
            else -> throw IllegalArgumentException("swap ${parts[1]} unkown")
        }
        "reverse" ->  createReversePositions(parts[2].toInt(), parts[4].toInt())
        "rotate" -> when(parts[1]) {
            "left" -> createRotateLeft(parts[2].toInt())
            "right" -> createRotateRight(parts[2].toInt())
            "based" -> createRotateLetter(parts[6][0])
            else -> throw IllegalArgumentException("rotate ${parts[1]} unkown")
        }
        else -> throw IllegalArgumentException("unkown ${operation}")
    }
}

fun createRotateLetter(c: Char, reverse: Boolean = false): ScramblingOperation = { input ->
    if (reverse) {
        "abdec"
    } else {
        val pos = input.indexOf(c)
        val rotate = if (pos >= 4) pos + 2 else pos + 1
        rotateRight(input, rotate)
    }
}

fun createMove(p1: Int, p2: Int, reverse: Boolean = false): ScramblingOperation =
    if (reverse) createMove(p2, p1)
    else { input ->
        val strList = input.toMutableList()
        val c = strList[p1]
        strList.removeAt(p1)
        strList.add(p2, c)
        strList.joinToString("")
    }


fun createRotateLeft(n: Int, reverse: Boolean = false): ScramblingOperation = { input ->
    if (reverse) rotateRight(input, n)
    else rotateLeft(input, n)
}

fun createRotateRight(n: Int, reverse: Boolean = false): ScramblingOperation = { input ->
    if (reverse) rotateLeft(input, n)
    else rotateRight(input, n)
}

fun createReversePositions(p1: Int, p2: Int): ScramblingOperation = { input -> reversePositions(input, p1, p2) }

fun createSwapLetter(c1: Char, c2: Char): ScramblingOperation = { input ->
    input.replace(c1, '#').replace(c2, c1).replace('#', c2)
}

fun createSwapPosition(p1: Int, p2: Int): ScramblingOperation = { input ->
    val strList = input.toMutableList()
    val swap = strList[p1]
    strList[p1] = strList[p2]
    strList[p2] = swap
    strList.joinToString("")
}

fun rotateRight(input: String, n: Int): String =
        when {
            n == 0 -> input
            input.isEmpty() -> input
            else -> {
                val rotated = rotateRight(input, n - 1)
                rotated[rotated.length - 1] + rotated.substring(0, rotated.length - 1)
            }
        }

fun rotateLeft(input: String, n: Int): String =
        when {
            n == 0 -> input
            input.isEmpty() -> input
            else -> {
                val rotated = rotateLeft(input, n - 1)
                rotated.substring(1) + rotated[0]
            }
        }


fun reversePositions(input: String, p1: Int, p2: Int): String {
    val before = input.substring(0, p1)
    val after = input.substring(p2+1)
    val toReverse = input.substring(p1, p2+1)
    return before + StringBuilder(toReverse).reverse() + after
}

object Day21Spec : Spek({

    describe("part 1") {
        given("example") {
            val input = listOf<ScramblingOperation>(
                    createSwapPosition(4, 0),
                    createSwapLetter('d', 'b'),
                    createReversePositions(0, 4),
                    createRotateLeft(1),
                    createMove(1, 4),
                    createMove(3, 0),
                    createRotateLetter('b'),
                    createRotateLetter('d')
            )
            it("should find the correctly scrambled value") {
                "abcde".applyScramblingOperations(input) `should equal` "decab"
            }
        }
        given("example as string") {
            val input = listOf(
                "swap position 4 with position 0",
                "swap letter d with letter b",
                "reverse positions 0 through 4",
                "rotate left 1 step",
                "move position 1 to position 4",
                "move position 3 to position 0",
                "rotate based on position of letter b",
                "rotate based on position of letter d"
            )
            it("should also find the correctly scrambled value") {
                "abcde".applyScramblingOperations(parseScramblingOperations(input)) `should equal` "decab"
            }
        }
        describe("reverse positions") {
            val input = "abcdef"
            it("should reverse substring") {
                reversePositions(input, 1, 3) `should equal` "adcbef"
            }
        }
        describe("rotate left") {
            val input = "abcdef"
            it("should rotate") {
                rotateLeft(input, 2) `should equal` "cdefab"
            }
        }
        describe("rotate right") {
            val input = "abcdef"
            it("should rotate") {
                rotateRight(input, 2) `should equal` "efabcd"
            }
        }
        given("exercise") {
            val input = readTrimedLinesFromResource("day21Input.txt")
            "abcdefgh".applyScramblingOperations(parseScramblingOperations(input)) `should equal` "bfheacgd"
        }

    }
    describe("part 2") {
        given("example") {
            val input = listOf<ScramblingOperation>(
                createSwapPosition(4, 0),
                createSwapLetter('d', 'b'),
                createReversePositions(0, 4),
                createRotateLeft(1, true),
                createMove(1, 4, true),
                createMove(3, 0, true),
                createRotateLetter('b', true),
                createRotateLetter('d', true)
            )
            it("should find the correctly unscrambled value") {
                "decab".applyScramblingOperations(input.reversed()) `should equal` "abcde"
            }
        }
    }
})

