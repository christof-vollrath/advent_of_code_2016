import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import java.lang.Long.max
import java.lang.Long.min

/*
--- Day 20: Firewall Rules ---

You'd like to set up a small hidden computer here so you can use it to get back into the network later.
However, the corporate firewall only allows communication with certain external IP addresses.

You've retrieved the list of blocked IPs from the firewall,
but the list seems to be messy and poorly maintained, and it's not clear which IPs are allowed.
Also, rather than being written in dot-decimal notation, they are written as plain 32-bit integers,
which can have any value from 0 through 4294967295, inclusive.

For example, suppose only the values 0 through 9 were valid, and that you retrieved the following blacklist:

5-8
0-2
4-7

The blacklist specifies ranges of IPs (inclusive of both the start and end value) that are not allowed.
Then, the only IPs that this firewall allows are 3 and 9, since those are the only numbers not in any range.

Given the list of blocked IPs you retrieved from the firewall (your puzzle input),
what is the lowest-valued IP that is not blocked?

--- Part Two ---

How many IPs are allowed by the blacklist?

 */

fun parseIpRanges(lines: List<String>): List<IpRange> =
        lines.map {
            val parts = it.split("-")
            IpRange(parts[0].toLong(), parts[1].toLong())
        }

typealias IpRange = Pair<Long, Long>

fun mergeRanges(input: List<IpRange>): List<IpRange> {
    val sorted = input.sortedBy { it.first }
    // Sorting makes sure that after a merge not additional merges must be considered, e.g (1,2), (5,6), (3,4)
    // after merging (1,2) with (3,4) to (1,4) it could be merged to (1,6)

    var result = listOf<IpRange>()
    sorted.forEach { newIpRange ->
        val modifiedResult = mutableListOf<IpRange>()
        var merged = false
        result.forEach { existingIpRange ->
            if (!merged && overlapping(existingIpRange, newIpRange)) {
                modifiedResult.add(mergeIpRange(newIpRange, existingIpRange))
                merged = true
            } else modifiedResult.add(existingIpRange)
        }
        if (! merged) modifiedResult.add(newIpRange)
        result = modifiedResult
    }
    return result.toList()
}

fun mergeIpRange(newIpRange: IpRange, existingIpRange: IpRange) =
        IpRange(min(newIpRange.first, existingIpRange.first), max(newIpRange.second, existingIpRange.second))


fun overlapping(ipRange1: IpRange, ipRange2: IpRange) =
        (ipRange1.first <= ipRange2.first && ipRange1.second >= ipRange2.first - 1) ||
                (ipRange2.first <= ipRange1.first && ipRange2.second >= ipRange1.first - 1)

fun lowestValidIp(input: List<IpRange>): Long {
    val firstRange = input.minBy { it.first }
    if (firstRange != null && firstRange.first <= 0) return firstRange.second + 1L
    else return 0L
}


fun countOutsideRange(ranges: List<IpRange>, max: Long): Long {
    val upToLastElement = ranges.fold(Pair(0L, 0L)) { acc, ipRange: IpRange ->
        val n = if (acc.first == 0L) ipRange.first
        else if (ipRange.first > acc.first) ipRange.first - 1 - acc.first else 0L
        Pair(ipRange.second, acc.second + n)
    }
    return upToLastElement.second + max - upToLastElement.first
}

object Day20Spec : Spek({

    describe("part 1") {
        given("example") {
            val input = listOf(IpRange(5, 8), IpRange(0, 2), IpRange(4,7))
            it("should find correct lowest value") {
                lowestValidIp(mergeRanges(input)) `should equal` 3
            }
        }
        describe("merge ranges") {
            given("an empty list") {
                val emptyList = listOf<IpRange>()
                it("should return the empty list") {
                    mergeRanges(emptyList) `should equal` listOf()
                }
            }
            given("a list with one element") {
                val emptyList = listOf<IpRange>(IpRange(1, 2))
                it("should return the list with one element") {
                    mergeRanges(emptyList) `should equal` listOf(IpRange(1, 2))
                }
            }
            given("a list with non overlapping elements") {
                val emptyList = listOf<IpRange>(IpRange(1, 2), IpRange(4, 6))
                it("should return the list with these elements") {
                    mergeRanges(emptyList) `should equal` listOf(IpRange(1, 2), IpRange(4, 6))
                }
            }
            given("a list with overlapping elements") {
                val emptyList = listOf<IpRange>(IpRange(1, 5), IpRange(4, 6))
                it("should return a list with one merged element") {
                    mergeRanges(emptyList) `should equal` listOf(IpRange(1, 6))
                }
            }
            given("a list with overlapping elements, different order") {
                val emptyList = listOf<IpRange>(IpRange(4, 6), IpRange(1, 5))
                it("should return a list with one merged element") {
                    mergeRanges(emptyList) `should equal` listOf(IpRange(1, 6))
                }
            }
            given("a list with nearby elements") {
                val emptyList = listOf<IpRange>(IpRange(1, 3), IpRange(4, 7))
                it("should return a list with one merged element") {
                    mergeRanges(emptyList) `should equal` listOf(IpRange(1, 7))
                }
            }
            given("a list with nearby elements, different order") {
                val emptyList = listOf<IpRange>(IpRange(4, 7), IpRange(1, 3))
                it("should return a list with one merged element") {
                    mergeRanges(emptyList) `should equal` listOf(IpRange(1, 7))
                }
            }
            given("example") {
                val input = listOf(IpRange(5, 8), IpRange(0, 2), IpRange(4, 7))
                it("should merge to correct list") {
                    mergeRanges(input).toSet() `should equal` listOf(IpRange(0, 2), IpRange(4, 8)).toSet()
                }
            }
            given("bugs") {
                val input = listOf(IpRange(868214, 1216244), IpRange(591078, 868213))
                it("should merge to correct list") {
                    mergeRanges(input) `should equal` listOf(IpRange(591078, 1216244))
                }
            }
        }
        given("exercise") {
            val inputString = readTrimedLinesFromResource("day20Input.txt")
            val input = parseIpRanges(inputString)
            it("should find correct lowest value") {
                lowestValidIp(mergeRanges(input)) `should equal` 4793564
            }
        }
    }

    describe("part 2") {
        describe("count outside range") {
            given("no ranges") {
                val ranges = listOf<IpRange>()
                it("should return number") {
                    countOutsideRange(ranges, 10) `should equal` 10
                }
            }
            given("one ranges") {
                val ranges = listOf(IpRange(0, 5))
                it("should return number") {
                    countOutsideRange(ranges, 10) `should equal` 5
                }
            }
            given("more ranges") {
                val ranges = listOf(IpRange(0, 5), IpRange(6,7), IpRange(9, 10))
                it("should return number") {
                    countOutsideRange(ranges, 12) `should equal` 3
                }
            }
            given("more ranges, not starting from 0") {
                val ranges = listOf(IpRange(1, 5), IpRange(6,7), IpRange(9, 10))
                it("should return number") {
                    countOutsideRange(ranges, 12) `should equal` 4
                }
            }
        }
        given("example") {
            val input = listOf(IpRange(5, 8), IpRange(0, 2), IpRange(4, 7))
            it("should find all allowed ips") {
                countOutsideRange(mergeRanges(input), 9) `should equal` 2
            }
        }
        given("exercise") {
            val inputString = readTrimedLinesFromResource("day20Input.txt")
            val input = parseIpRanges(inputString)
            countOutsideRange(mergeRanges(input), 4294967295) `should equal` 146
        }
    }
})
