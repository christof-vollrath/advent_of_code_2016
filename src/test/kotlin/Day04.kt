import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.on
import org.jetbrains.spek.api.dsl.xdescribe
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

*/

class Day4Spec : Spek({

    describe("part 1") {
        xdescribe("examples check encrypted names") {
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
        xdescribe("examples sum sector ids") {
            val input = listOf(
                    "aaaaa-bbb-z-y-x-123[abxyz]",
                    "a-b-c-d-e-f-g-h-987[abcde]",
                    "not-a-real-room-404[oarel]",
                    "totally-real-room-200[decoy]")
            it("should calculate the correct sum of sector ids") {
                input.map { parseEncryptedName(it) }.filter { it.checkEncryptedName() }.map { it.sectorId }.sum() `should equal` 1514
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
                    encryptedName.name `should equal` "aaaaabbbzyx"
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
    }

})

fun countCharsSorted(input: String) = countChars(input).sortedWith(compareByDescending<Pair<Char,Int>> { it.second } .thenBy { it.first })

fun countChars(input: String) = input.groupBy { it }.map { Pair(it.key, it.value.size) }

fun parseEncryptedName(encryptedName: String): EncryptedName {
    val pattern = """([a-z-]*)-(\d*)\[([a-z]*)\]""".toPattern();
    val matcher = pattern.matcher(encryptedName)
    if (!matcher.find() || matcher.groupCount() != 3) throw IllegalArgumentException("Encrypted name=$encryptedName not well formed.")

    val name = matcher.group(1).filter { it != '-' }
    val sectorId = matcher.group(2).toInt()
    val checkSum = matcher.group(3)
    return EncryptedName(name, sectorId, checkSum)

}

class EncryptedName(val name: String, val sectorId: Int, val checkSum: String) {
    fun checkEncryptedName() = true
}

