import org.amshove.kluent.`should equal`
import kotlin.coroutines.experimental.buildSequence
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on as onData

/*
--- Day 9: Explosives in Cyberspace ---

Wandering around a secure area, you come across a datalink port to a new part of the network.
After briefly scanning it for interesting files, you find one file in particular that catches your attention.
It's compressed with an experimental format, but fortunately, the documentation for the format is nearby.

The format compresses a sequence of characters. Whitespace is ignored.
To indicate that some sequence should be repeated, a marker is added to the file, like (10x2).
To decompress this marker, take the subsequent 10 characters and repeat them 2 times.
Then, continue reading the file after the repeated data.
The marker itself is not included in the decompressed output.

If parentheses or other characters appear within the data referenced by a marker,
that's okay - treat it like normal data, not a marker,
and then resume looking for markers after the decompressed section.

For example:

ADVENT contains no markers and decompresses to itself with no changes, resulting in a decompressed length of 6.

A(1x5)BC repeats only the B a total of 5 times, becoming ABBBBBC for a decompressed length of 7.

(3x3)XYZ becomes XYZXYZXYZ for a decompressed length of 9.

A(2x2)BCD(2x2)EFG doubles the BC and EF, becoming ABCBCDEFEFG for a decompressed length of 11.

(6x1)(1x3)A simply becomes (1x3)A - the (1x3) looks like a marker,
but because it's within a data section of another marker,
it is not treated any differently from the A that comes after it. It has a decompressed length of 6.

X(8x2)(3x3)ABCY becomes X(3x3)ABC(3x3)ABCY (for a decompressed length of 18),
because the decompressed data from the (8x2) marker (the (3x3)ABC) is skipped and not processed further.

What is the decompressed length of the file (your puzzle input)? Don't count whitespace.

--- Part Two ---

Apparently, the file actually uses version two of the format.

In version two, the only difference is that markers within decompressed data are decompressed.
This, the documentation explains, provides much more substantial compression capabilities,
allowing many-gigabyte files to be stored in only a few kilobytes.

For example:

(3x3)XYZ still becomes XYZXYZXYZ, as the decompressed section contains no markers.

X(8x2)(3x3)ABCY becomes XABCABCABCABCABCABCY, because the decompressed data from the (8x2) marker is then
further decompressed, thus triggering the (3x3) marker twice for a total of six ABC sequences.

(27x12)(20x12)(13x14)(7x10)(1x12)A decompresses into a string of A repeated 241920 times.

(25x3)(3x3)ABC(2x3)XY(5x2)PQRSTX(18x9)(3x2)TWO(5x7)SEVEN becomes 445 characters long.

Unfortunately, the computer you brought probably doesn't have enough memory to actually decompress the file;
you'll have to come up with another way to get its decompressed length.

What is the decompressed length of the file using this improved format?

 */

sealed class Token { abstract fun calculateLength(): Long }
data class Marker(val repeat: Int, val subTokens: List<Token>): Token() {
    override fun calculateLength(): Long =
            subTokens.map { it.calculateLength() }
                    .sum() * repeat
}
data class PlainText(val text: String): Token() {
    override fun calculateLength(): Long = text.length.toLong()
}

fun countDecompressedString(input: String, decompressMarker: Boolean = false): Long =
        parseCompressedString(input, decompressMarker)
                .map { it.calculateLength() }
                .sum()

fun parseCompressedString(input: String, decompressMarker: Boolean = false) = buildSequence {
    val iterator = PeekingIterator(input.iterator())
    while(iterator.hasNext()) {
        val c = iterator.peek()
        if (c == '(') yield(parseMarkerWithContent(iterator, decompressMarker))
        else yield(parsePlaintext(iterator))
    }
}

fun parseMarkerWithContent(iterator: PeekingIterator<Char>, decompressMarker: Boolean): Marker {
    iterator.next()
    val marker = parseMarker(iterator)
    val content = iterator.asSequence().take(marker.first).joinToString("")
    val subTokens =
            if (decompressMarker) parseCompressedString(content, decompressMarker).toList()
            else listOf(PlainText(content))
    return Marker(marker.second, subTokens)
}

fun parsePlaintext(iterator: PeekingIterator<Char>): PlainText {
    val text = buildSequence {
        while(iterator.hasNext() && iterator.peek() != '(') {
            yield(iterator.next())
        }
    }.joinToString("")
    return PlainText(text)
}

fun decompressString(input: String) = buildSequence {
    val iterator = input.iterator()
    while(iterator.hasNext()) {
        val c = iterator.next()
        if (c == '(') yield(handleMarker(iterator))
        else yield(c)
    }
}.joinToString("")

fun handleMarker(iterator: Iterator<Char>): String = buildSequence {
    val marker = parseMarker(iterator)
    val toRepeat = readFromIterator(iterator, marker.first)
    repeat(marker.second) {
        yield(toRepeat)
    }
}.joinToString("")

fun readFromIterator(iterator: Iterator<Char>, nr: Int): String = iterator.asSequence().take(nr).joinToString("")

fun parseMarker(iterator: Iterator<Char>): Pair<Int, Int> {
    val markerString = readMarker(iterator)
    val markerParts = markerString.split("x")
    return Pair(markerParts[0].toInt(), markerParts[1].toInt())
}

fun readMarker(iterator: Iterator<Char>): String = buildSequence {
    while(iterator.hasNext()) {
        val c = iterator.next()
        if (c != ')') yield(c)
        else break
    }
}.joinToString("")

class PeekingIterator<T>(val iterator: Iterator<T>) : Iterator<T> {
    var peeked: T? = null
    override fun hasNext() = peeked != null || iterator.hasNext()
    override fun next(): T =
            if (peeked != null) {
                val result = peeked as T
                peeked = null
                result
            }
            else iterator.next()
    fun peek(): T =
            if (peeked != null) peeked as T
            else {
                peeked = iterator.next()
                peeked as T
            }
}

class Day9Spec : Spek({

    describe("part 1") {
        describe("example input") {
            val testData = arrayOf(
                    //    input                 | decompressed
                    //--|-----------------------|--------------------------------
                    data("ADVENT", "ADVENT"),
                    data("A(1x5)BC", "ABBBBBC"),
                    data("(3x3)XYZ", "XYZXYZXYZ"),
                    data("A(2x2)BCD(2x2)EFG", "ABCBCDEFEFG"),
                    data("(6x1)(1x3)A", "(1x3)A"),
                    data("X(8x2)(3x3)ABCY", "X(3x3)ABC(3x3)ABCY")
            )
            onData("input %s", with = *testData) { input, decompressed ->
                it("returns $decompressed") {
                    decompressString(input) `should equal` decompressed
                }
            }
        }
        describe("exercise") {
            given("exercise input") {
                val input = readResource("day09Input.txt")
                it("should decompress correctly") {
                    val decompressed = decompressString(input)
                    println(decompressed.length)
                    decompressed.length `should equal` 120765
                }
                it("should count correctly") {
                    val count = countDecompressedString(input)
                    println(count)
                    count `should equal` 120765
                }
            }
        }
    }
    describe("part 2") {
        describe("example input") {
            val testData = arrayOf(
                    //    input                 | decompressed length
                    //--|-----------------------|--------------------------------
                    data("ADVENT", 6L),
                    data("A(1x5)BC", 7L),
                    data("(3x3)XYZ", 9L),
                    data("X(8x2)(3x3)ABCY", 20L),
                    data("(27x12)(20x12)(13x14)(7x10)(1x12)A", 241920L),
                    data("(25x3)(3x3)ABC(2x3)XY(5x2)PQRSTX(18x9)(3x2)TWO(5x7)SEVEN", 445L)
            )
            onData("input %s", with = *testData) { input, decompressedLength ->
                it("returns $decompressedLength") {
                    countDecompressedString(input, decompressMarker = true) `should equal` decompressedLength
                }
            }
        }
        describe("parse compressed String") {
            given("a simple string") {
                val input = "ADVENT"
                it("should be parsed") {
                    parseCompressedString(input).toList() `should equal` listOf(PlainText("ADVENT"))
                }
            }
            given("a string with marker") {
                val input = "A(1x5)BC"
                it("should be parsed") {
                    parseCompressedString(input).toList() `should equal` listOf(
                            PlainText("A"),
                            Marker(5, listOf(PlainText("B"))),
                            PlainText("C")
                    )
                }
            }
            given("a string with nested marker and not uncompressing markers") {
                val input = "X(8x2)(3x3)ABCY"
                it("should be parsed") {
                    parseCompressedString(input).toList() `should equal` listOf(
                            PlainText("X"),
                            Marker(2, listOf(PlainText("(3x3)ABC"))),
                            PlainText("Y")
                    )
                }
            }
            given("a string with nested marker and uncompressing markers") {
                val input = "X(8x2)(3x3)ABCY"
                it("should be parsed") {
                    parseCompressedString(input, decompressMarker = true).toList() `should equal` listOf(
                            PlainText("X"),
                            Marker(2, listOf(
                                    Marker(3, listOf(PlainText("ABC")))
                            )),
                            PlainText("Y")
                    )
                }
            }
        }
        describe("exercise") {
            given("exercise input") {
                val input = readResource("day09Input.txt")
                it("should count correctly") {
                    val count = countDecompressedString(input, decompressMarker = true)
                    println(count)
                    count `should equal` 11658395076
                }
            }
        }
    }
})
