import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xdescribe
import org.jetbrains.spek.api.dsl.xit
import java.security.MessageDigest

/*

--- Day 5: How About a Nice Game of Chess? ---

You are faced with a security door designed by Easter Bunny engineers that seem
to have acquired most of their security knowledge by watching hacking movies.

The eight-character password for the door is generated one character at a time by finding the MD5 hash
of some Door ID (your puzzle input) and an increasing integer index (starting with 0).

A hash indicates the next character in the password if its hexadecimal representation starts with five zeroes.
If it does, the sixth character in the hash is the next character of the password.

For example, if the Door ID is abc:

The first index which produces a hash that starts with five zeroes is 3231929,
which we find by hashing abc3231929; the sixth character of the hash,
and thus the first character of the password, is 1.
5017308 produces the next interesting hash, which starts with 000008f82...,
so the second character of the password is 8.
The third time a hash starts with five zeroes is for abc5278568, discovering the character f.
In this example, after continuing this search a total of eight times, the password is 18f47a30.

Given the actual Door ID, what is the password?

--- Part Two ---

As the door slides open, you are presented with a second door that uses a slightly more inspired security mechanism.
Clearly unimpressed by the last version (in what movie is the password decrypted in order?!),
the Easter Bunny engineers have worked out a better solution.

Instead of simply filling in the password from left to right,
the hash now also indicates the position within the password to fill.
You still look for hashes that begin with five zeroes; however, now,
the sixth character represents the position (0-7),
and the seventh character is the character to put in that position.

A hash result of 000001f means that f is the second character in the password.
Use only the first result for each position, and ignore invalid positions.

For example, if the Door ID is abc:

The first interesting hash is from abc3231929, which produces 0000015...; so, 5 goes in position 1: _5______.
In the previous method, 5017308 produced an interesting hash; however, it is ignored, because it specifies an invalid position (8).
The second interesting hash is at index 5357525, which produces 000004e...; so, e goes in position 4: _5__e___.
You almost choke on your popcorn as the final character falls into place, producing the password 05ace8e3.

Given the actual Door ID and this new method, what is the password?
Be extra proud of your solution if it uses a cinematic "decrypting" animation.


 */


fun md5(input: String) = md5(stringToAsciiBytes(input))
fun md5(input: ByteArray): String {
    val md = MessageDigest.getInstance("MD5")
    md.update(input);
    val digest = md.digest()
    return bytesToHexString(digest)
}

fun bytesToHexString(bytes: ByteArray) = bytes.map { "%02x".format(it) } .joinToString("")
fun stringToAsciiBytes(input: String) = input.map { it.toByte() } .toByteArray()

class Day5Spec : Spek({

    describe("part 1") {
        describe("md5") {
            val input = "abc3231929"
            it("should calculate the correct md5 hash") {
                md5(input) `should equal` "00000155f8105dff7f56ee10fa9b9abd"
            }
        }

        describe("conversion to ascii bytes") {
            val input = "abc"
            it("should be converted to ascii bytes") {
                val result = stringToAsciiBytes(input)
                result.size `should equal` 3
                result `should equal` byteArrayOf(97, 98, 99)
            }
        }

        describe("example") {
            xit("password should be correct") {
                val input = "abc"
                doorPassword(input, 2, 3231929) `should equal` "18"
            }
        }

        describe("exercise") { // This takes about 3 min
            xit("password should be correct") {
                val input = "ugkcyxxp"
                val pwd = doorPassword(input, 8)
                println(pwd)
                pwd `should equal` "d4cd2ee1"
            }
        }
    }
})

fun doorPassword(input: String, length: Int, startIndex: Int = 0): String {
    val builder = StringBuilder()
    var index = startIndex
    while(builder.length < length) {
        val hash = md5(input + index)
        if (hash.startsWith("00000")) {
            builder.append(hash[5])
            println(builder.toString())
        }
        index++
    }
    return builder.toString()
}
