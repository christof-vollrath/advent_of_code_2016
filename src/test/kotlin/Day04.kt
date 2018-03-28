import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on as onData

/*
--- Day 4: Security Through Obscurity ---

Finally, you come across an information kiosk with a list of rooms.
Of course, the list is encrypted and full of decoy data,
but the instructions to decode the list are barely hidden nearby.
Better remove the decoy data first.

Each room consists of an encrypted name (lowercase letters separated by dashes) followed by a dash,
a sector ID, and a checksum in square brackets.

A room is real (not a decoy) if the checksum is the five most common letters in the encrypted name,
in order, with ties broken by alphabetization.

For example:

aaaaa-bbb-z-y-x-123[abxyz] is a real room because the most common letters are a (5), b (3),
and then a tie between x, y, and z, which are listed alphabetically.
a-b-c-d-e-f-g-h-987[abcde] is a real room because although the letters are all tied (1 of each),
the first five are listed alphabetically.
not-a-real-room-404[oarel] is a real room.
totally-real-room-200[decoy] is not.

Of the real rooms from the list above, the sum of their sector IDs is 1514.

What is the sum of the sector IDs of the real rooms?

--- Part Two ---

With all the decoy data out of the way, it's time to decrypt this list and get moving.

The room names are encrypted by a state-of-the-art shift cipher,
which is nearly unbreakable without the right software.

However, the information kiosk designers at Easter Bunny HQ were not expecting
to deal with a master cryptographer like yourself.

To decrypt a room name, rotate each letter forward through the alphabet a number of times equal to the room's sector ID.
A becomes B, B becomes C, Z becomes A, and so on. Dashes become spaces.

For example, the real name for qzmt-zixmtkozy-ivhz-343 is very encrypted name.

What is the sector ID of the room where North Pole objects are stored?

*/


fun checksum(input: String) = countCharsSorted(input).map { it.first }.take(5).joinToString("")
fun countCharsSorted(input: String) = countChars(input).sortedWith(compareByDescending<Pair<Char,Int>> { it.second }.thenBy { it.first })
fun countChars(input: String) = input.filter { it != '-' }.groupBy { it }.map { Pair(it.key, it.value.size) }

fun parseEncryptedName(encryptedName: String): EncryptedName {
    val matcher = """([a-z-]*)-(\d*)\[([a-z]*)\]""".toPattern().matcher(encryptedName)
    if (!matcher.find() || matcher.groupCount() != 3) throw IllegalArgumentException("Encrypted name=$encryptedName not well formed.")
    return EncryptedName(matcher.group(1), matcher.group(2).toInt(), matcher.group(3))
}

class EncryptedName(val name: String, val sectorId: Int, val checkSum: String) {
    fun checkEncryptedName() = checksum(name) == checkSum
}

fun alphaToInt(c: Char) = c - 'a'
fun intToAlpha(i: Int) = (i + 'a'.toInt()).toChar()
fun decryptChar(c: Char, count: Int) =
        if (c == '-') ' '
        else intToAlpha((alphaToInt(c) + count) % ('z' - 'a' + 1))

fun decryptName(encryptedName: EncryptedName) = encryptedName.name.map {decryptChar(it, encryptedName.sectorId)}.joinToString("")

class Day4Spec : Spek({

    describe("part 1") {
        describe("examples check encrypted names") {
            val testData = arrayOf(
                    //       encrypted name      result
                    //--|-------------------|--------------
                    data("aaaaa-bbb-z-y-x-123[abxyz]", true),
                    data("a-b-c-d-e-f-g-h-987[abcde]", true),
                    data("not-a-real-room-404[oarel]", true),
                    data("totally-real-room-200[decoy]", false))
            onData("input %s", with = *testData) { encryptedName, expected ->
                it("returns $expected") {
                    parseEncryptedName(encryptedName).checkEncryptedName() `should equal` expected
                }
            }
        }
        describe("examples sum sector ids") {
            val input = listOf(
                    "aaaaa-bbb-z-y-x-123[abxyz]",
                    "a-b-c-d-e-f-g-h-987[abcde]",
                    "not-a-real-room-404[oarel]",
                    "totally-real-room-200[decoy]")
            it("should calculate the correct sum of sector ids") {
                input.map { parseEncryptedName(it) }.filter { it.checkEncryptedName() }.map { it.sectorId }.sum() `should equal` 1514
            }
        }
        describe("parse examples") {
            val input = """
                    aaaaa-bbb-z-y-x-123[abxyz]
                    a-b-c-d-e-f-g-h-987[abcde]
                    not-a-real-room-404[oarel]
                    totally-real-room-200[decoy]
                    """
            it("should parse") {
                parseTrimedLines(input) `should equal` listOf(
                        "aaaaa-bbb-z-y-x-123[abxyz]",
                        "a-b-c-d-e-f-g-h-987[abcde]",
                        "not-a-real-room-404[oarel]",
                        "totally-real-room-200[decoy]")
            }
        }
        describe("parse encrypted name") {
            on("simple input") {
                val input = "a-1[a]"
                it("should be parsed into name, sector id and check sum") {
                    val encryptedName = parseEncryptedName(input)
                    encryptedName.name `should equal` "a"
                    encryptedName.sectorId `should equal` 1
                    encryptedName.checkSum `should equal` "a"
                }
            }
            on("complexer input") {
                val input = "abc-123[def]"
                it("should be parsed into name, sector id and check sum") {
                    val encryptedName = parseEncryptedName(input)
                    encryptedName.name `should equal` "abc"
                    encryptedName.sectorId `should equal` 123
                    encryptedName.checkSum `should equal` "def"
                }
            }
            on("example input") {
                val input = "aaaaa-bbb-z-y-x-123[abxyz]"
                it("should be parsed into name, sector id and check sum") {
                    val encryptedName = parseEncryptedName(input)
                    encryptedName.name `should equal` "aaaaa-bbb-z-y-x"
                    encryptedName.sectorId `should equal` 123
                    encryptedName.checkSum `should equal` "abxyz"
                }
            }
        }
        describe("count chars") {
            on("some chars") {
                val input = "abcaba"
                it("should return chars with count") {
                    countChars(input) `should equal` listOf(Pair('a', 3), Pair('b', 2), Pair('c', 1))
                }
            }
        }
        describe("count chars sorted") {
            on("some chars") {
                val input = "ccdbbaaa"
                it("should return chars with count") {
                    countCharsSorted(input) `should equal` listOf(Pair('a', 3), Pair('b', 2), Pair('c', 2), Pair('d', 1))
                }
            }
        }
        describe("checksum") {
            on("some chars") {
                val input = "not-a-real-room"
                it("should return checksum") {
                    checksum(input) `should equal` "oarel"
                }
            }
        }
        describe("exercise") {
            val input = readResource("day04Input.txt")
            val result = parseTrimedLines(input)
                    .map { parseEncryptedName(it) }
                    .filter { it.checkEncryptedName() }
                    .map { it.sectorId }.sum()
            println(result)
            result `should equal` 409147
        }
        describe("part 2") {
            describe("example decrypt") {
                val encryptedName = EncryptedName("qzmt-zixmtkozy-ivhz", 343, "")
                val decrypt = decryptName(encryptedName)
                it ("should decrypt to the correct value") {
                    decrypt `should equal` "very encrypted name"
                }
            }
        }
        describe("rotate char") {
            val testData = arrayOf(
                    //       char count     result
                    //--|-------|--------------
                    data('a',  1, 'b'),
                    data('z',  1, 'a'),
                    data('z',  2, 'b'),
                    data('x',  4, 'b'),
                    data('-',  1, ' ')
            )
            onData("input %s", with = *testData) { c, count, expected ->
                it("returns $expected") {
                    decryptChar(c, count) `should equal` expected
                }
            }
        }
        describe("alpha position of char") {
            ('a'..'z').forEach {
                intToAlpha(alphaToInt(it)) `should equal` it
            }
        }
        describe("exercise") {
            val input = readResource("day04Input.txt")
            val result = parseTrimedLines(input)
                    .map { parseEncryptedName(it) }
                    .filter { it.checkEncryptedName() }
                    .first { decryptName(it).startsWith("northpole") }
                    .sectorId
            println(result)
            result `should equal` 991
        }

    }

})



