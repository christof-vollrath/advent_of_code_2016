import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

/*
--- Day 14: One-Time Pad ---

In order to communicate securely with Santa while you're on this mission,
you've been using a one-time pad that you generate using a pre-agreed algorithm.
Unfortunately, you've run out of keys in your one-time pad, and so you need to generate some more.

To generate keys, you first get a stream of random data by taking the MD5 of a pre-arranged salt (your puzzle input)
and an increasing integer index (starting with 0, and represented in decimal);
the resulting MD5 hash should be represented as a string of lowercase hexadecimal digits.

However, not all of these MD5 hashes are keys, and you need 64 new keys for your one-time pad.

A hash is a key only if:

It contains three of the same character in a row, like 777. Only consider the first such triplet in a hash.
One of the next 1000 hashes in the stream contains that same character five times in a row, like 77777.
Considering future hashes for five-of-a-kind sequences does not cause those hashes to be skipped;
instead, regardless of whether the current hash is a key, always resume testing for keys starting with the very next hash.

For example, if the pre-arranged salt is abc:

The first index which produces a triple is 18, because the MD5 hash of abc18 contains ...cc38887a5....
However, index 18 does not count as a key for your one-time pad,
because none of the next thousand hashes (index 19 through index 1018) contain 88888.
The next index which produces a triple is 39; the hash of abc39 contains eee.
It is also the first key: one of the next thousand hashes (the one at index 816) contains eeeee.
None of the next six triples are keys, but the one after that, at index 92, is:
it contains 999 and index 200 contains 99999.
Eventually, index 22728 meets all of the criteria to generate the 64th key.
So, using our example salt of abc, index 22728 produces the 64th key.

Given the actual salt in your puzzle input, what index produces your 64th one-time pad key?

Your puzzle input is jlmsuwbz.

 */

object Day14Spec : Spek({

    describe("part 1") {
        describe("regular expression experiments") {
            describe("find triplets") {
                given("a string containing no triplet") {
                    val str = "Alles in Butter."
                    it("should return null") {
                        findFirstTriplet(str).`should be null`()
                    }
                }
                given("a string containing a triplet") {
                    val str = "Alles in Buttter."
                    it("should find the triplet") {
                        findFirstTriplet(str) `should equal` 't'
                    }
                }
                given("a string containing two triplets") {
                    val str = "111222"
                    it("should find the frist triplet") {
                        findFirstTriplet(str) `should equal` '1'
                    }
                }
            }
            describe("check char five times") {
                given("a string not containing the char 5 times") {
                    val str =  "Alles in Butter. 11111"
                    it("should return false") {
                        find5Chars(str, 't').`should be false`()
                    }
                }
                given("a string containing the char 5 times") {
                    val str =  "Alles in Buttttter. 11111"
                    it("should return true") {
                        find5Chars(str, 't').`should be true`()
                    }
                }
            }
        }
        describe("md5 checks") {
            describe("check for triplets") {
                it("should return false for a md5 without triplet") {
                    checkForTriples(createKeypad("abc", 1)).`should be false`()
                }
                it("should return true for a md5 with triplet") {
                    checkForTriples(createKeypad("abc", 18)).`should be true`()
                }
            }

        }
    }
})

fun checkForTriples(keypad: String) = findFirstTriplet(keypad) != null

fun createKeypad(seed: String, nr: Int) = md5(seed + nr.toString())

fun find5Chars(str: String, c: Char): Boolean {
    val pattern =  c.toString().repeat(5).toPattern()
    return pattern.matcher(str).find()
}

fun findFirstTriplet(str: String): Char? {
    val pattern = """(.)\1\1""".toPattern()
    val matcher = pattern.matcher(str)
    return if (!matcher.find())  null
    else matcher.group(0)[0]

}
