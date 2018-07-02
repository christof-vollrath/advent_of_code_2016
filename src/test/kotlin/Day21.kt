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

 */

typealias ScramblingOperation = (String) -> String

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
            it("should find correct lowest value") {
                "abcde".applyScramblingOperation(input) `should equal` "decab"
            }
        }
    }
})

private fun String.applyScramblingOperation(input: List<ScramblingOperation>) = input.fold(this) { acc, current -> current(acc) }

fun createRotateLetter(c: Char): ScramblingOperation = { input ->
    val pos = input.indexOf(c)
    val rotate = if (pos >= 4) pos + 2 else pos + 1
    rotateRight(input, rotate)
}

fun createMove(p1: Int, p2: Int): ScramblingOperation = { input ->
    val strList = input.toMutableList()
    val c = strList[p1]
    strList.removeAt(p1)
    strList.add(p2, c)
    strList.joinToString("")
}

fun createRotateLeft(n: Int): ScramblingOperation = { input -> "bcdea"}

fun createReversePositions(p1: Int, p2: Int): ScramblingOperation = { input -> "abcde"}

fun createSwapLetter(c1: Char, c2: Char): ScramblingOperation = { input -> "edcba"}

fun createSwapPosition(p1: Int, p2: Int): ScramblingOperation = { input -> "ebcda"}

fun rotateRight(input: String, n: Int): String =
    when {
        n == 0 -> input
        input.length == 0 -> input
        else -> {
            val rotated = rotateRight(input, n - 1)
            rotated[rotated.length - 1] + rotated.substring(0, rotated.length - 1)
        }
    }