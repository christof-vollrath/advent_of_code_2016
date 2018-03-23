import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on as onData

/*
--- Day 7: Internet Protocol Version 7 ---

While snooping around the local network of EBHQ, you compile a list of IP addresses
(they're IPv7, of course; IPv6 is much too limited).
You'd like to figure out which IPs support TLS (transport-layer snooping).

An IP supports TLS if it has an Autonomous Bridge Bypass Annotation, or ABBA.
An ABBA is any four-character sequence which consists of a pair of two different characters
followed by the reverse of that pair, such as xyyx or abba.
However, the IP also must not have an ABBA within any hypernet sequences,
which are contained by square brackets.

For example:

abba[mnop]qrst supports TLS (abba outside square brackets).
abcd[bddb]xyyx does not support TLS (bddb is within square brackets, even though xyyx is outside square brackets).
aaaa[qwer]tyui does not support TLS (aaaa is invalid; the interior characters must be different).
ioxxoj[asdfgh]zxcvbn supports TLS (oxxo is outside square brackets, even though it's within a larger string).

How many IPs in your puzzle input support TLS?

--- Part Two ---

You would also like to know which IPs support SSL (super-secret listening).

An IP supports SSL if it has an Area-Broadcast Accessor, or ABA,
anywhere in the supernet sequences (outside any square bracketed sections),
and a corresponding Byte Allocation Block, or BAB, anywhere in the hypernet sequences.
An ABA is any three-character sequence which consists of the same character twice with a different character between them,
such as xyx or aba. A corresponding BAB is the same characters but in reversed positions: yxy and bab, respectively.

For example:

aba[bab]xyz supports SSL (aba outside square brackets with corresponding bab within square brackets).
xyx[xyx]xyx does not support SSL (xyx, but no corresponding yxy).
aaa[kek]eke supports SSL (eke in supernet with corresponding kek in hypernet;
the aaa sequence is not related, because the interior character must be different).
zazbz[bzb]cdb supports SSL (zaz has no corresponding aza, but zbz has a corresponding bzb, even though zaz and zbz overlap).
How many IPs in your puzzle input support SSL?
 */


fun checkIp7Adress(address: String): Boolean {
    val splitted = splitIp7Address(address)
    val supernetSequences = splitted.first
    val hypernetSequences = splitted.second
    return containsAbba(supernetSequences) && !containsAbba(hypernetSequences)
}

fun containsAbba(parts: List<String>) = parts.any { containsAbba(it) }
fun containsAbba(string: String) = splitNgrams(string, 4).any { checkAbba(it) }

fun checkAbba(string: String)  =
        string[0] != string[1]
                && string[0] == string[3]
                && string [1] == string[2]

fun checkIp7AdressSupportsSsl(address: String): Boolean {
    val splitted = splitIp7Address(address)
    val supernetSequences = splitted.first
    val hypernetSequences = splitted.second
    val abas = findAbas(supernetSequences)
    val babs = abas.map { convertToBab(it) }
    return hypernetSequences.any { hypernetSequence -> babs.any { bab -> hypernetSequence.contains(bab)} }
}

fun convertToBab(aba: String) = String(charArrayOf(aba[1], aba[0],  aba[1]))

fun findAbas(parts: List<String>) = parts.flatMap { findAbas(it) }
fun findAbas(part: String) = splitNgrams(part, 3).filter { checkAba(it) }

fun checkAba(string: String) = string[0] != string[1] && string[0] == string[2]

fun splitNgrams(string: String, length: Int) = string.withIndex().map {
    if (it.index <= string.length - length) ngram(string, it.index, length)
    else null
}.filterNotNull()

fun ngram(string: String, index: Int, length: Int) =
        (index until index+length).map { string[it] }.joinToString("")

fun splitIp7Address(address: String) = with(splitIp7AddressInParts(address).withIndex()) {
    Pair(filter { it.index % 2 == 0 }. map { it.value },
            filter { it.index % 2 != 0 }. map { it.value }   )
}

fun splitIp7AddressInParts(address: String) = address.split("[", "]")

class Day7Spec : Spek({

    describe("part 1") {
        describe("example addresses") {
            val testData = arrayOf(
                    //    ip7 address              correct
                    //--|-------------------------|--------------
                    data("abba[mnop]qrst", true),
                    data("abcd[bddb]xyyx", false),
                    data("aaaa[qwer]tyui", false),
                    data("ioxxoj[asdfgh]zxcvbn", true)
            )
            onData("input %s", with = *testData) { address, result ->
                it("returns $result") {
                    checkIp7Adress(address) `should equal` result
                }
            }
        }
        describe("split into parts") {
            given("ip7address") {
                val input = "abba[mnop]qrst[bddb]xyyx"
                it("should be splitted") {
                    val splitted = splitIp7AddressInParts(input)
                    splitted `should equal` listOf("abba", "mnop", "qrst", "bddb", "xyyx")
                }
            }
        }
        describe("split into part and hypernet sequence") {
            given("ip7address") {
                val input = "abba[mnop]qrst[bddb]xyyx"
                it("should be splitted") {
                    val splitted = splitIp7Address(input)
                    splitted.first `should equal` listOf("abba", "qrst", "xyyx")
                    splitted.second `should equal` listOf("mnop", "bddb")
                }
            }
        }
        describe("check abba") {
            val testData = arrayOf(
                    //    string       correct
                    //--|-------------|--------------
                    data("abba",  true),
                    data("abba",  false),
                    data("aaaa",  false)
            )
            onData("input %s", with = *testData) { string, result ->
                it("returns $result") {
                    checkAbba(string) `should equal` result
                }
            }
        }
        describe("split ngrams") {
            val testData = arrayOf(
                    //    string     lenght               abas
                    //--|-----------|-------|----------------------
                    data("",       3, emptyList<String>()),
                    data("xyz",    3, listOf("xyz")),
                    data("abcde",  3, listOf("abc", "bcd", "cde")),
                    data("ioxxoj", 4, listOf("ioxx", "oxxo", "xxoj"))
            )
            onData("input %s", with = *testData) { part, length, result ->
                it("returns $result") {
                    splitNgrams(part, length) `should equal` result
                }
            }
        }
        describe("exercise") {
            given("extercise input") {
                val input = readResource("day07Input.txt")
                val inputList = parseTrimedLines(input)
                it("should calculate the correct result") {
                    val result = inputList.filter { checkIp7Adress(it) }.count()
                    println(result)
                    result `should equal` 105
                }
            }
        }
    }

    describe("part 2") {
        describe("example addresses") {
            val testData = arrayOf(
                    //    ip7 address              correct
                    //--|-------------------------|--------------
                    data("aba[bab]xyz", true),
                    data("xyx[xyx]xyx", false),
                    data("aaa[kek]eke", true),
                    data("zazbz[bzb]cdb", true)
            )
            onData("input %s", with = *testData) { address, result ->
                it("returns $result") {
                    checkIp7AdressSupportsSsl(address) `should equal` result
                }
            }
        }
        describe("find aba") {
            val testData = arrayOf(
                    //    part              abas
                    //--|-----------------|----------------------
                    data("",            emptyList<String>()),
                    data("xyz",         emptyList<String>()),
                    data("aba",         listOf("aba")),
                    data("aabaxyxzabc", listOf("aba", "xyx"))
            )
            onData("input %s", with = *testData) { part, result ->
                it("returns $result") {
                    findAbas(part) `should equal` result
                }
            }
        }
        describe("exercise") {
            given("exercise input") {
                val input = readResource("day07Input.txt")
                val inputList = parseTrimedLines(input)
                it("should calculate the correct result") {
                    val result = inputList.filter { checkIp7AdressSupportsSsl(it) }.count()
                    println(result)
                    result `should equal` 258
                }
            }
        }
    }
})

