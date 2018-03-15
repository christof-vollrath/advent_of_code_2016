import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it

/*

--- Day 6: Signals and Noise ---

Something is jamming your communications with Santa.
Fortunately, your signal is only partially jammed, and protocol in situations like this is
to switch to a simple repetition code to get the message through.

In this model, the same message is sent repeatedly.
You've recorded the repeating message signal (your puzzle input), but the data seems quite corrupted
- almost too badly to recover.
Almost.

All you need to do is figure out which character is most frequent for each position.
For example, suppose you had recorded the following messages:

eedadn
drvtee
eandsr
raavrd
atevrs
tsrnev
sdttsa
rasrtv
nssdts
ntnada
svetve
tesnvt
vntsnd
vrdear
dvrsen
enarar

The most common character in the first column is e;
in the second, a; in the third, s, and so on.
Combining these characters returns the error-corrected message, easter.

Given the recording in your puzzle input,
what is the error-corrected version of the message being sent?

--- Part Two ---

Of course, that would be the message - if you hadn't agreed to use a modified repetition code instead.

In this modified code, the sender instead transmits what looks like random data, but for each character,
the character they actually want to send is slightly less likely than the others.
Even after signal-jamming noise, you can look at the letter distributions in each column
and choose the least common letter to reconstruct the original message.

In the above example, the least common character in the first column is a; in the second, d, and so on.
Repeating this process for the remaining characters produces the original message, advent.

Given the recording in your puzzle input and this new decoding methodology,
what is the original message that Santa is trying to send?

 */

fun decodeMessages2(input: List<String>) = lessFrequentCharsInMessages(input).joinToString("")

fun lessFrequentCharsInMessages(input: List<String>) =
        countCharsInMessages(input)
                .map {
                    it.toList()
                            .minBy { it.second }
                }
                .map { it!!.first}

fun decodeMessages(input: List<String>) = mostFrequentCharsInMessages(input).joinToString("")

fun mostFrequentCharsInMessages(input: List<String>) =
        countCharsInMessages(input)
                .map {
                    it.toList()
                            .maxBy { it.second }
                }
                .map { it!!.first}

fun countCharsInMessages(input: List<String>) =
        input.map { it.toList() }
                .transpose()
                .map { countValues(it)}

fun <T> countValues(input: List<T>) =
        input.groupBy { it }
                .mapValues { it.value.count() }

class Day6Spec : Spek({
    val exampleInput = """
            eedadn
            drvtee
            eandsr
            raavrd
            atevrs
            tsrnev
            sdttsa
            rasrtv
            nssdts
            ntnada
            svetve
            tesnvt
            vntsnd
            vrdear
            dvrsen
            enarar
            """

    describe("part 1") {
        describe("parse, count, decode") {
            val input = """
                abc
                dbc
                abd
                """
            val inputList = parseTrimedLines(input)
            it("should count chars") {
                countCharsInMessages(inputList) `should equal` listOf(mapOf('a' to 2, 'd' to 1), mapOf('b' to 3), mapOf('c' to 2, 'd' to 1))
            }
            it("should find frequent chars") {
                mostFrequentCharsInMessages(inputList) `should equal` listOf('a', 'b', 'c')
            }
            it("should decode message") {
                decodeMessages(inputList) `should equal` "abc"
            }
        }
        describe("count values") {
            val input = listOf(1, 2, 3, 1, 1, 2)
            it("should count") {
                countValues(input) `should equal` mapOf(1 to 3, 2 to 2, 3 to 1)
            }
        }
        describe("example") {
            val inputList = parseTrimedLines(exampleInput)
            it("should decode message") {
                decodeMessages(inputList) `should equal` "easter"
            }
        }
        describe("exercise") {
            val input = readResource("day06Input.txt")
            val inputList = parseTrimedLines(input)
            val result = decodeMessages(inputList)
            println(result)
            result `should equal` "nabgqlcw"
        }
    }
    describe("part 2") {
        describe("example") {
            val inputList = parseTrimedLines(exampleInput)
            it("should decode message") {
                decodeMessages2(inputList) `should equal` "advent"
            }
        }
        describe("exercise") {
            val input = readResource("day06Input.txt")
            val inputList = parseTrimedLines(input)
            val result = decodeMessages2(inputList)
            println(result)
            result `should equal` "ovtrjcjh"
        }

    }

})
