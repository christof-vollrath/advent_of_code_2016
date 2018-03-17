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


 */

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
                    //    string  index                 correct
                    //--|-------------------------|--------------
                    data("abba", 0, true),
                    data("abba", 1, false),
                    data("xabba", 1, true)
            )
            onData("input %s", with = *testData) { string, index, result ->
                it("returns $result") {
                    checkAbba(string, index) `should equal` result
                }
            }
        }
        describe("exercise") {
            val input = readResource("day07Input.txt")
            val inputList = parseTrimedLines(input)
            val result = inputList.filter { checkIp7Adress(it) }.count()
            println(result)
            result `should equal` 105
        }
    }
})

fun checkIp7Adress(address: String): Boolean {
    val splitted = splitIp7Address(address)
    val outsideBrackets = splitted.first
    val insideBrackets = splitted.second
    return containsAbba(outsideBrackets) && !containsAbba(insideBrackets)
}

fun splitIp7Address(address: String) = with(splitIp7AddressInParts(address).withIndex()) {
    Pair(filter { it.index % 2 == 0 }. map { it.value },
            filter { it.index % 2 != 0 }. map { it.value }   )
}

fun splitIp7AddressInParts(address: String) = address.split("[", "]")

fun containsAbba(parts: List<String>) = parts.any {
    containsAbba(it)
}

fun containsAbba(string: String) = string.withIndex().any { checkAbba(string, it.index)  }

fun checkAbba(string: String, index: Int)  = index <= string.length - 4
        && string[index] != string[index+1]
        && string[index] == string[index+3]
        && string [index+1] == string[index+2]