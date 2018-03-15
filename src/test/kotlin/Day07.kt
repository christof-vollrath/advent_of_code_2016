import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
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
    }
})

fun checkIp7Adress(address: String) = true

