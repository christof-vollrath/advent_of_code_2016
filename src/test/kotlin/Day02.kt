import org.amshove.kluent.`should equal`
import org.amshove.kluent.shouldThrow
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on

/*
--- Day 2: Bathroom Security ---
You arrive at Easter Bunny Headquarters under cover of darkness.
However, you left in such a rush that you forgot to use the bathroom!
Fancy office buildings like this one usually have keypad locks on their bathrooms,
so you search the front desk for the code.

"In order to improve security," the document you find says, "bathroom codes will no longer be written down.
Instead, please memorize and follow the procedure below to access the bathrooms."

The document goes on to explain that each button to be pressed can be found by starting on the previous button
and moving to adjacent buttons on the keypad: U moves up, D moves down, L moves left, and R moves right.
Each line of instructions corresponds to one button, starting at the previous button
(or, for the first line, the "5" button); press whatever button you're on at the end of each line.
If a move doesn't lead to a button, ignore it.

You can't hold it much longer, so you decide to figure out the code as you walk to the bathroom.
You picture a keypad like this:

1 2 3
4 5 6
7 8 9

Suppose your instructions are:

ULL
RRDDD
LURDL
UUUUD

You start at "5" and move up (to "2"), left (to "1"), and left (you can't, and stay on "1"), so the first button is 1.
Starting from the previous button ("1"), you move right twice (to "3")
and then down three times (stopping at "9" after two moves and ignoring the third),
ending up with 9.
Continuing from "9", you move left, up, right, down, and left, ending with 8.
Finally, you move up four times (stopping at "2"), then down once, ending with 5.

So, in this example, the bathroom code is 1985.

--- Part Two ---
You finally arrive at the bathroom (it's a several minute walk from the lobby
so visitors can behold the many fancy conference rooms and water coolers on this floor)
and go to punch in the code.
 Much to your bladder's dismay, the keypad is not at all like you imagined it.
 Instead, you are confronted with the result of hundreds of man-hours of bathroom-keypad-design meetings:

    1
  2 3 4
5 6 7 8 9
  A B C
    D
You still start at "5" and stop when you're at an edge, but given the same instructions as above,
the outcome is very different:

You start at "5" and don't move at all (up and left are both edges), ending at 5.
Continuing from "5", you move right twice and down three times (through "6", "7", "B", "D", "D"), ending at D.
Then, from "D", you move five more times (through "D", "B", "C", "C", "B"), ending at B.
Finally, after five more moves, you end at 3.
So, given the actual keypad layout, the code would be 5DB3.

Using the same instructions in your puzzle input, what is the correct bathroom code?
 */

fun decodeKeypad(input: List<List<KeypadInstruction>>): String {
    val keypad = Keypad()
    return input.map {
        keypad.apply(it).button
    }.joinToString("")
}

val TRANSLATION_MATRIX = listOf(
        listOf(' ', ' ', ' ', ' ', ' '),
        listOf(' ', '1', '2', '3', ' '),
        listOf(' ', '4', '5', '6', ' '),
        listOf(' ', '7', '8', '9', ' '),
        listOf(' ', ' ', ' ', ' ', ' ')
)

class Keypad(var pos: Pair<Int, Int> = findButton('5')) {
    val button: Char
        get() = translateKeypad(pos)


    fun apply(instructions: List<KeypadInstruction>) = instructions.fold(this) { kp, instr -> instr(kp) }
}

fun translateKeypad(pos: Pair<Int, Int>) = TRANSLATION_MATRIX[pos.second][pos.first]
fun findButton(button: Char): Pair<Int, Int> {
    TRANSLATION_MATRIX.forEachIndexed { first, row ->
        row.forEachIndexed {
            second, c ->  if (TRANSLATION_MATRIX[first][second] == button) return Pair(second, first)
        }
    }
    throw IllegalArgumentException("Button=$button not found")
}

typealias KeypadInstruction = (Keypad) -> Keypad
fun up(keypad: Keypad) = keypad.apply { keypad.pos = checkBounds(keypad.pos, Pair(keypad.pos.first, keypad.pos.second - 1)) }
fun down(keypad: Keypad) = keypad.apply { keypad.pos = checkBounds(keypad.pos, Pair(keypad.pos.first, keypad.pos.second + 1)) }
fun right(keypad: Keypad) = keypad.apply { keypad.pos = checkBounds(keypad.pos, Pair(keypad.pos.first + 1, keypad.pos.second)) }
fun left(keypad: Keypad) = keypad.apply { keypad.pos = checkBounds(keypad.pos, Pair(keypad.pos.first - 1, keypad.pos.second)) }
fun checkBounds(current: Pair<Int, Int>, next: Pair<Int, Int>) = if (translateKeypad(next) == ' ') current else next

fun parseKeypadInstructionsList(string: String) =
        string.split("\n")
                .filter { ! it.isBlank() }
                .map {
                    parseKeypadInstructions(it)
                }

fun parseKeypadInstructions(string: String) =
        string.filter { it != ' ' }
                .map {
                    when(it) {
                        'R' -> :: right
                        'L' -> :: left
                        'U' -> :: up
                        'D' -> :: down
                        else -> throw IllegalArgumentException("Code $it unkown")
                    }
                }

class Day2Spec : Spek({

    describe("part 1") {
        describe("example bathroom") {
            val input = """
                ULL
                RRDDD
                LURDL
                UUUUD
                """
            decodeKeypad(parseKeypadInstructionsList(input)) `should equal` "1985"
        }
        describe("keypad") {
            on("creation of keyapd") {
                val keypad = Keypad()
                it("should start with button 5") {
                    keypad.button `should equal` '5'
                }
            }
        }
        describe("instructions") {
            on("up") {
                val keypad = Keypad()
                it("should go up") {
                    up(keypad).button `should equal` '2'
                }
            }
            on("down") {
                val keypad = Keypad()
                it("should go down") {
                    down(keypad).button `should equal` '8'
                }
            }
            on("right") {
                val keypad = Keypad()
                it("should go right") {
                    right(keypad).button `should equal` '6'
                }
            }
            on("left") {
                val keypad = Keypad()
                it("should go right") {
                    left(keypad).button `should equal` '4'
                }
            }
            on("two times up") {
                val keypad = Keypad()
                it("should stop after first move") {
                    keypad.apply(listOf(::up, ::up)).button `should equal` '2'
                }
            }
            on("some instructions") {
                val keypad = Keypad()
                it("should move to 9") {
                    keypad.apply(parseKeypadInstructions("RRDDD")).button `should equal` '9'
                }
            }
            on("some other instructions starting at 9") {
                val keypad = Keypad(findButton('9'))
                it("should move to 8") {
                    keypad.apply(parseKeypadInstructions("LURDL")).button `should equal` '8'
                }
            }
        }
        describe("parse") {
            it("should parse a single input") {
                parseKeypadInstructions("R") `should equal` listOf(::right)
            }
            it("should parse a sequence input") {
                parseKeypadInstructions("LURDL") `should equal` listOf(::left, ::up, ::right, ::down, ::left)
            }
            it("should throw exception with illegal input") {
                { parseKeypadInstructions("X") } shouldThrow IllegalArgumentException::class
            }
            it("should parse a list of instructions") {
                parseKeypadInstructionsList(
                        """
                            UL
                            RD
                        """)  `should equal`
                        listOf(
                            listOf(::up, ::left),
                            listOf(::right, ::down)
                        )
            }
        }
        describe("exercise") {
            val input = readResource("day02Input.txt")
            val result = decodeKeypad(parseKeypadInstructionsList(input))
            println(result)
            result `should equal` "35749"
        }
    }
})

