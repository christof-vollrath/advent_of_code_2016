import org.amshove.kluent.`should be true`
import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import org.jetbrains.spek.data_driven.data
import org.jetbrains.spek.data_driven.on as onData

/*
--- Day 11: Radioisotope Thermoelectric Generators ---

You come upon a column of four floors that have been entirely sealed off from the rest of the building
except for a small dedicated lobby.
There are some radiation warnings and a big sign which reads "Radioisotope Testing Facility".

According to the project status board, this facility is currently being used to experiment
with Radioisotope Thermoelectric Generators (RTGs, or simply "generators")
that are designed to be paired with specially-constructed microchips.
Basically, an RTG is a highly radioactive rock that generates electricity through heat.

The experimental RTGs have poor radiation containment, so they're dangerously radioactive.
The chips are prototypes and don't have normal radiation shielding,
but they do have the ability to generate an electromagnetic radiation shield when powered.
Unfortunately, they can only be powered by their corresponding RTG.
An RTG powering a microchip is still dangerous to other microchips.

In other words, if a chip is ever left in the same area as another RTG,
and it's not connected to its own RTG, the chip will be fried.
Therefore, it is assumed that you will follow procedure and keep chips connected to their corresponding RTG
when they're in the same room, and away from other RTGs otherwise.

These microchips sound very interesting and useful to your current activities, and you'd like to try to retrieve them.
The fourth floor of the facility has an assembling machine which can make a self-contained,
shielded computer for you to take with you - that is, if you can bring it all of the RTGs and microchips.

Within the radiation-shielded part of the facility (in which it's safe to have these pre-assembly RTGs),
there is an elevator that can move between the four floors.
Its capacity rating means it can carry at most yourself and two RTGs or microchips in any combination.
(They're rigged to some heavy diagnostic equipment - the assembling machine will detach it for you.)
As a security measure, the elevator will only function if it contains at least one RTG or microchip.
The elevator always stops on each floor to recharge, and this takes long enough that the items within it
and the items on that floor can irradiate each other.
(You can prevent this if a Microchip and its Generator end up on the same floor in this way,
as they can be connected while the elevator is recharging.)

You make some notes of the locations of each component of interest (your puzzle input).
Before you don a hazmat suit and start moving things around, you'd like to have an idea of what you need to do.

When you enter the containment area, you and the elevator will start on the first floor.

For example, suppose the isolated area has the following arrangement:

The first floor contains a hydrogen-compatible microchip and a lithium-compatible microchip.
The second floor contains a hydrogen generator.
The third floor contains a lithium generator.
The fourth floor contains nothing relevant.
As a diagram
(F# for a Floor number, E for Elevator, H for Hydrogen, L for Lithium, M for Microchip, and G for Generator),
the initial state looks like this:

F4 .  .  .  .  .
F3 .  .  .  LG .
F2 .  HG .  .  .
F1 E  .  HM .  LM

Then, to get everything up to the assembling machine on the fourth floor, the following steps could be taken:

Bring the Hydrogen-compatible Microchip to the second floor,
which is safe because it can get power from the Hydrogen Generator:

F4 .  .  .  .  .
F3 .  .  .  LG .
F2 E  HG HM .  .
F1 .  .  .  .  LM

Bring both Hydrogen-related items to the third floor,
which is safe because the Hydrogen-compatible microchip is getting power from its generator:

F4 .  .  .  .  .
F3 E  HG HM LG .
F2 .  .  .  .  .
F1 .  .  .  .  LM

Leave the Hydrogen Generator on floor three,
but bring the Hydrogen-compatible Microchip back down with you so you can still use the elevator:

F4 .  .  .  .  .
F3 .  HG .  LG .
F2 E  .  HM .  .
F1 .  .  .  .  LM

At the first floor, grab the Lithium-compatible Microchip,
which is safe because Microchips don't affect each other:

F4 .  .  .  .  .
F3 .  HG .  LG .
F2 .  .  .  .  .
F1 E  .  HM .  LM

Bring both Microchips up one floor, where there is nothing to fry them:

F4 .  .  .  .  .
F3 .  HG .  LG .
F2 E  .  HM .  LM
F1 .  .  .  .  .

Bring both Microchips up again to floor three,
where they can be temporarily connected to their corresponding generators while the elevator recharges,
preventing either of them from being fried:

F4 .  .  .  .  .
F3 E  HG HM LG LM
F2 .  .  .  .  .
F1 .  .  .  .  .

Bring both Microchips to the fourth floor:

F4 E  .  HM .  LM
F3 .  HG .  LG .
F2 .  .  .  .  .
F1 .  .  .  .  .

Leave the Lithium-compatible microchip on the fourth floor,
but bring the Hydrogen-compatible one so you can still use the elevator;
this is safe because although the Lithium Generator is on the destination floor,
you can connect Hydrogen-compatible microchip to the Hydrogen Generator there:

F4 .  .  .  .  LM
F3 E  HG HM LG .
F2 .  .  .  .  .
F1 .  .  .  .  .

Bring both Generators up to the fourth floor,
which is safe because you can connect the Lithium-compatible Microchip to the Lithium Generator upon arrival:

F4 E  HG .  LG LM
F3 .  .  HM .  .
F2 .  .  .  .  .
F1 .  .  .  .  .

Bring the Lithium Microchip with you to the third floor so you can use the elevator:

F4 .  HG .  LG .
F3 E  .  HM .  LM
F2 .  .  .  .  .
F1 .  .  .  .  .

Bring both Microchips to the fourth floor:

F4 E  HG HM LG LM
F3 .  .  .  .  .
F2 .  .  .  .  .
F1 .  .  .  .  .

In this arrangement, it takes 11 steps to collect all of the objects at the fourth floor for assembly.
(Each elevator stop counts as one step, even if nothing is added to or removed from it.)

In your situation, what is the minimum number of steps required to bring all of the objects to the fourth floor?

Your input:

The first floor contains a thulium generator, a thulium-compatible microchip,
a plutonium generator, and a strontium generator.
The second floor contains a plutonium-compatible microchip and a strontium-compatible microchip.
The third floor contains a promethium generator, a promethium-compatible microchip,
a ruthenium generator, and a ruthenium-compatible microchip.
The fourth floor contains nothing relevant.

 */

class Day11Spec : Spek({

    describe("part 1") {
        val allEquipment = setOf(
                Generator(Radioisotope.HYDROGEN),
                Microchip(Radioisotope.HYDROGEN),
                Generator(Radioisotope.LITHIUM),
                Microchip(Radioisotope.LITHIUM))
        describe("example") {
            given("the final arrangement") {
                val arrangement = Arrangement(4, listOf(setOf(), setOf(), setOf(), allEquipment))
                it("should terminate with 0 steps") {
                    val steps = BreadthFirstSearcher(arrangement,
                            { checkArrangement(it, allEquipment) },
                            ::createMoves, :: applyMove
                    ).search()
                    steps.size `should equal` 1
                }
            }
            given("the arrangement before final") {
                val arrangement = Arrangement(3, listOf(
                        setOf(), setOf(),
                        setOf(
                                Microchip(Radioisotope.HYDROGEN),
                                Microchip(Radioisotope.LITHIUM)
                        ),
                        setOf(
                                Generator(Radioisotope.HYDROGEN),
                                Generator(Radioisotope.LITHIUM)
                        )
                ))
                it("should terminate with 1 steps") {
                    val steps = BreadthFirstSearcher(arrangement,
                            { checkArrangement(it, allEquipment) },
                            ::createMoves, :: applyMove
                    ).search()
                    steps.size `should equal` 2
                    steps[1].first `should equal`
                            ElevatorMove(3, 4, setOf(
                                    Microchip(Radioisotope.HYDROGEN),
                                    Microchip(Radioisotope.LITHIUM)
                                )
                            )
                }
            }
        }
        describe("apply move") {
            given("an arrangement and a move") {
                val arrangement = Arrangement(3, listOf(
                        setOf(), setOf(),
                        setOf(
                                Microchip(Radioisotope.HYDROGEN),
                                Microchip(Radioisotope.LITHIUM)
                        ),
                        setOf(
                                Generator(Radioisotope.HYDROGEN),
                                Generator(Radioisotope.LITHIUM)
                        )
                ))
                val move = ElevatorMove(3, 4,
                        setOf(
                                Microchip(Radioisotope.HYDROGEN),
                                Microchip(Radioisotope.LITHIUM)
                        )
                )
                val nextArrangement = applyMove(arrangement, move)
                it("should result the correct arrangement") {
                    nextArrangement `should equal` Arrangement(4, listOf(
                            setOf(), setOf(), setOf(),
                            setOf(
                                    Microchip(Radioisotope.HYDROGEN),
                                    Microchip(Radioisotope.LITHIUM),
                                    Generator(Radioisotope.HYDROGEN),
                                    Generator(Radioisotope.LITHIUM)
                            )
                    ))
                }
            }
        }
        describe("check found") {
            given("the final arrangement") {
                val arrangement = Arrangement(4, listOf(
                        setOf(), setOf(), setOf(),
                        setOf(
                                Microchip(Radioisotope.HYDROGEN),
                                Microchip(Radioisotope.LITHIUM),
                                Generator(Radioisotope.HYDROGEN),
                                Generator(Radioisotope.LITHIUM)
                        )
                ))
                it("should be checked positively") {
                    checkArrangement(arrangement, allEquipment).`should be true`()
                }
            }

        }
        describe("combine equipment for elevator more") {
            val testData = arrayOf(
                    //    equipment             | moves
                    //--|-----------------------|--------------------------------
                    data(setOf(), setOf()),
                    data(setOf(Microchip(Radioisotope.HYDROGEN)), setOf(setOf(Microchip(Radioisotope.HYDROGEN)))),
                    data(setOf(Microchip(Radioisotope.HYDROGEN), Microchip(Radioisotope.LITHIUM)), setOf(
                            setOf(Microchip(Radioisotope.HYDROGEN)),
                            setOf(Microchip(Radioisotope.LITHIUM)),
                            setOf(Microchip(Radioisotope.HYDROGEN), Microchip(Radioisotope.LITHIUM))
                        )
                    ),
                    data(setOf(Microchip(Radioisotope.HYDROGEN), Microchip(Radioisotope.LITHIUM), Microchip(Radioisotope.THULIUM)), setOf(
                            setOf(Microchip(Radioisotope.HYDROGEN)),
                            setOf(Microchip(Radioisotope.LITHIUM)),
                            setOf(Microchip(Radioisotope.THULIUM)),
                            setOf(Microchip(Radioisotope.HYDROGEN), Microchip(Radioisotope.LITHIUM)),
                            setOf(Microchip(Radioisotope.HYDROGEN), Microchip(Radioisotope.THULIUM)),
                            setOf(Microchip(Radioisotope.LITHIUM), Microchip(Radioisotope.THULIUM))
                        )
                    )

            )
            onData("input %s", with = *testData) { equipment, moves ->
                it("returns $moves") {
                    combineEquipmentForMove(equipment) `should equal` moves
                }
            }

        }
    }
})

class BreadthFirstSearcher(val arrangement: Arrangement,
                           val check: (Arrangement) -> Boolean,
                           val createMoves: (Arrangement) -> Set<ElevatorMove>,
                           val applyMove: (Arrangement, ElevatorMove) -> Arrangement) {
    fun search(): List<Pair<ElevatorMove?, Arrangement>> = search(listOf(listOf(Pair(null, arrangement))))
    private fun search(toCheck: List<List<Pair<ElevatorMove?, Arrangement>>>): List<Pair<ElevatorMove?, Arrangement>> {
        val found = toCheck.filter { check(it.last().second)}
        return if (found.isNotEmpty()) found.first()
        else {
            val nextToCheck = toCheck.flatMap { moves ->
                val lastArrangement = moves.last().second
                val nextMoves = createMoves(lastArrangement)
                nextMoves.map { nextMove: ElevatorMove ->
                    val nextArrangement: Arrangement = applyMove(lastArrangement, nextMove)
                    moves + Pair(nextMove, nextArrangement)
                }
            }
            search(nextToCheck)
        }
    }
}

enum class Radioisotope { HYDROGEN, LITHIUM, THULIUM }

sealed class Equipment
data class Generator(val isotope: Radioisotope) : Equipment()
data class Microchip(val isotope: Radioisotope) : Equipment()

data class Arrangement(val elevator: Int, val floors: List<Set<Equipment>>)

data class ElevatorMove(val fromFloor: Int, val toFloor: Int, val content: Set<Equipment>)

fun checkArrangement(arrangement: Arrangement, allEquipment: Set<Equipment>) =
        arrangement.elevator == arrangement.floors.size &&
        arrangement.floors.last() == allEquipment

fun applyMove(arrangement: Arrangement, elevatorMove: ElevatorMove): Arrangement {
    val floors = arrangement.floors.mapIndexed { index, equipments ->
        when(index) {
            elevatorMove.fromFloor-1 -> equipments - elevatorMove.content
            elevatorMove.toFloor-1 -> equipments + elevatorMove.content
            else -> equipments
        }
    }
    return Arrangement(elevatorMove.toFloor, floors)
}

fun createMoves(arrangement: Arrangement): Set<ElevatorMove> =
        if (arrangement.elevator < arrangement.floors.size)
            combineEquipmentForMove(arrangement.floors[arrangement.elevator-1])
                    .map {
                        ElevatorMove(arrangement.elevator, arrangement.elevator+1, it)
                    }.toSet()
        else setOf<ElevatorMove>() +
        if (arrangement.elevator > 1)
            combineEquipmentForMove(arrangement.floors[arrangement.elevator-1])
            .map {
                ElevatorMove(arrangement.elevator, arrangement.elevator-1, it)
            }.toSet()
        else setOf()

fun combineEquipmentForMove(equipments: Set<Equipment>) =
        equipments.flatMap {equipment1 ->
            listOf(setOf(equipment1)) + (equipments - equipment1).map { setOf(equipment1, it) }
        }.toSet()
