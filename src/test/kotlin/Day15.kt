import org.amshove.kluent.`should be false`
import org.amshove.kluent.`should be null`
import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

/*
--- Day 15: Timing is Everything ---

The halls open into an interior plaza containing a large kinetic sculpture.
The sculpture is in a sealed enclosure and seems to involve a set of identical spherical capsules
that are carried to the top and allowed to bounce through the maze of spinning pieces.

Part of the sculpture is even interactive!
When a button is pressed, a capsule is dropped and tries to fall through slots in a set of rotating discs
to finally go through a little hole at the bottom and come out of the sculpture.
If any of the slots aren't aligned with the capsule as it passes,
the capsule bounces off the disc and soars away.
You feel compelled to get one of those capsules.

The discs pause their motion each second and come in different sizes;
they seem to each have a fixed number of positions at which they stop.
You decide to call the position with the slot 0,
and count up for each position it reaches next.

Furthermore, the discs are spaced out so that after you push the button,
one second elapses before the first disc is reached,
and one second elapses as the capsule passes from one disc to the one below it.
So, if you push the button at time=100,
then the capsule reaches the top disc at time=101,
the second disc at time=102, the third disc at time=103, and so on.

The button will only drop a capsule at an integer time - no fractional seconds allowed.

For example, at time=0, suppose you see the following arrangement:

Disc #1 has 5 positions; at time=0, it is at position 4.
Disc #2 has 2 positions; at time=0, it is at position 1.

If you press the button exactly at time=0, the capsule would start to fall;
it would reach the first disc at time=1.
Since the first disc was at position 4 at time=0, by time=1 it has ticked one position forward.
As a five-position disc, the next position is 0, and the capsule falls through the slot.

Then, at time=2, the capsule reaches the second disc.
The second disc has ticked forward two positions at this point:
it started at position 1, then continued to position 0, and finally ended up at position 1 again.
Because there's only a slot at position 0, the capsule bounces away.

If, however, you wait until time=5 to push the button, then when the capsule reaches each disc,
the first disc will have ticked forward 5+1 = 6 times (to position 0),
and the second disc will have ticked forward 5+2 = 7 times (also to position 0).
In this case, the capsule would fall through the discs and come out of the machine.

However, your situation has more than two discs; you've noted their positions in your puzzle input.
What is the first time you can press the button to get a capsule?

Puzzle input:

Disc #1 has 17 positions; at time=0, it is at position 5.
Disc #2 has 19 positions; at time=0, it is at position 8.
Disc #3 has 7 positions; at time=0, it is at position 1.
Disc #4 has 13 positions; at time=0, it is at position 7.
Disc #5 has 5 positions; at time=0, it is at position 1.
Disc #6 has 3 positions; at time=0, it is at position 0.

--- Part Two ---

After getting the first capsule (it contained a star! what great fortune!),
the machine detects your success and begins to rearrange itself.

When it's done, the discs are back in their original configuration as if it were time=0 again,
but a new disc with 11 positions and starting at position 0 has appeared
exactly one second below the previously-bottom disc.

With this new disc, and counting again starting from time=0 with the configuration in your puzzle input,
what is the first time you can press the button to get another capsule?
 */

data class Disk(val size: Int, val startPosition: Int) {
    fun slot(time: Int) = (time + startPosition) % size == 0
}

object Day15Spec : Spek({

    describe("part 1") {
        describe("disks") {
            given("disk 1 with size 5") {
                val disk1 = Disk(5, 4)
                it("should have closed slot at time=0") {
                    disk1.slot(0).`should be false`()
                }
                it("should have open slot at time=6") {
                    disk1.slot(6).`should be true`()
                }
            }
            given("list of disks") {
                val disks = listOf(Disk(5, 4), Disk(2, 1))
                it("should not let capsule fall through at starting at time=0") {
                    fallsThrough(disks, 0).`should be false`()
                }
                it("should let capsule fall through at starting at time=5") {
                    fallsThrough(disks, 5).`should be true`()
                }
            }
        }
        describe("example") {
            given("discs from example") {
                val disks = listOf(Disk(5, 4), Disk(2, 1))
                it("should find 5 as time where everything falls through") {
                    findFallThroughTime(disks) `should equal` 5
                }
            }
        }
        describe("exercise") {
            given("discs from exercise") {
                val disks = listOf(
                        Disk(17, 5),
                        Disk(19, 8),
                        Disk(7, 1),
                        Disk(13, 7),
                        Disk(5, 1),
                        Disk(3, 0)
                )
                it("should find time where everything falls through") {
                    findFallThroughTime(disks) `should equal` 16824
                }
            }
        }
    }
    describe("part 2") {
        given("discs from exercise") {
            val disks = listOf(
                    Disk(17, 5),
                    Disk(19, 8),
                    Disk(7, 1),
                    Disk(13, 7),
                    Disk(5, 1),
                    Disk(3, 0),
                    Disk(11, 0)
            )
            it("should find time where everything falls through") {
                findFallThroughTime(disks) `should equal` 3543984
            }
        }
    }
})

fun findFallThroughTime(disks: List<Disk>) =
        generateSequence(0, Int::inc).first { fallsThrough(disks, it) }

fun fallsThrough(disks: List<Disk>, startTime: Int) =
        disks.mapIndexed { time, disk -> disk.slot(time + startTime + 1) }
            .all { it }
