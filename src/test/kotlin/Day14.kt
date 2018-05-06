import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.api.dsl.xdescribe

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

--- Part Two ---

Of course, in order to make this process even more secure, you've also implemented key stretching.

Key stretching forces attackers to spend more time generating hashes.
Unfortunately, it forces everyone else to spend more time, too.

To implement key stretching, whenever you generate a hash, before you use it,
you first find the MD5 hash of that hash, then the MD5 hash of that hash, and so on, a total of 2016 additional hashings.
Always use lowercase hexadecimal representations of hashes.

For example, to find the stretched hash for index 0 and salt abc:

Find the MD5 hash of abc0: 577571be4de9dcce85a041ba0410f29f.
Then, find the MD5 hash of that hash: eec80a0c92dc8a0777c619d9bb51e910.
Then, find the MD5 hash of that hash: 16062ce768787384c81fe17a7a60c7e3.
...repeat many times...
Then, find the MD5 hash of that hash: a107ff634856bb300138cac6568c0f24.

So, the stretched hash for index 0 in this situation is a107ff....

In the end, you find the original hash (one use of MD5), then find the hash-of-the-previous-hash 2016 times,
for a total of 2017 uses of MD5.

The rest of the process remains the same, but now the keys are entirely different. Again for salt abc:

The first triple (222, at index 5) has no matching 22222 in the next thousand hashes.
The second triple (eee, at index 10) hash a matching eeeee at index 89, and so it is the first key.
Eventually, index 22551 produces the 64th key (triple fff with matching fffff at index 22859.

Given the actual salt in your puzzle input and using 2016 extra MD5 calls of key stretching,
what index now produces your 64th one-time pad key?


 */
fun find64thKeypad(seed: String, keyStretching: Int = 0): Int =
    findKeypads(seed, keyStretching, 64).last()

fun findKeypads(seed: String, keyStretching: Int, limit: Int): List<Int> =
        generateSequence(0, Int::inc)
                .filter {
                    val c = findFirstTriplet(createKeypad(seed, it, keyStretching))
                    val result = c != null && check5CharsInMd5(seed, it, c, keyStretching)
                    result
                }
                .take(limit)
                .toList()

fun check5CharsInMd5(seed: String, from: Int, c: Char, keyStretching: Int) =
        (from+1 .. from+1000).any { find5Chars(createKeypad(seed, it, keyStretching), c) }

val md5Cache = mutableMapOf<String, String>()
fun createKeypad(seed: String, nr: Int, keyStretching: Int): String {
    val md5Input = seed + nr.toString()
    val cacheKey = md5Input + '-' + keyStretching
    val cachedValue = md5Cache[cacheKey]
    return if (cachedValue != null) cachedValue
    else {
        var md5Value = md5(md5Input)
        repeat(keyStretching) {
            md5Value = md5(md5Value)
        }
        md5Cache[cacheKey] = md5Value
        md5Value
    }
}

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
                it("should return null for a md5 without triplet") {
                    findFirstTriplet(createKeypad("abc", 1, 0)).`should be null`()
                }
                it("should return '8' for a md5 with triplet") {
                    findFirstTriplet(createKeypad("abc", 18, 0)) `should equal` '8'
                }
                it("should return '8' for a md5 with triplet") {
                    findFirstTriplet(createKeypad("abc", 39, 0)) `should equal` 'e'
                }
            }
            describe("check for five in a row in next 1000 md5 hashs") {
                it("should return false it no five in a row are found") {
                    check5CharsInMd5("abc", 18, '8', 0).`should be false`()
                }
                it("should return true it five in a row are found") {
                    check5CharsInMd5("abc", 39, 'e', 0).`should be true`()
                }
            }

        }
        xdescribe("example") {
            it("should find the 64th key at index 22728") {
                find64thKeypad("abc") `should equal` 22728
            }
        }
        xdescribe("exercise") {
            it("should find the 64th key for the exercise") {
                find64thKeypad("jlmsuwbz") `should equal` 35186
            }
        }
    }
    describe("part 2") {
        describe("key stretching") {
            createKeypad("abc", 0, 2016) `should equal` "a107ff634856bb300138cac6568c0f24"
        }
        // These tasks will take about 20 min
        xdescribe("example") {
            it("should find the 64th key at index 22551") {
                find64thKeypad("abc", 2016) `should equal` 22551
            }
        }
        describe("exercise") {
            it("should find the 64th key at index 22551") {
                find64thKeypad("jlmsuwbz", 2016) `should equal` 22429
            }
        }
    }
})
