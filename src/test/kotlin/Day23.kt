/*
--- Day 23: Safe Cracking ---

This is one of the top floors of the nicest tower in EBHQ.
The Easter Bunny's private office is here, complete with a safe hidden behind a painting,
and who wouldn't hide a star in a safe behind a painting?

The safe has a digital screen and keypad for code entry.
A sticky note attached to the safe has a password hint on it: "eggs".
The painting is of a large rabbit coloring some eggs. You see 7.

When you go to type the code, though, nothing appears on the display;
instead, the keypad comes apart in your hands, apparently having been smashed.
Behind it is some kind of socket - one that matches a connector in your prototype computer!
You pull apart the smashed keypad and extract the logic circuit, plug it into your computer,
and plug your computer into the safe.

Now, you just need to figure out what output the keypad would have sent to the safe.
You extract the assembunny code from the logic chip (your puzzle input).
The code looks like it uses almost the same architecture and instruction set that the monorail computer used!
You should be able to use the same assembunny interpreter for this as you did there, but with one new instruction:

tgl x toggles the instruction x away
(pointing at instructions like jnz does: positive means forward; negative means backward):

For one-argument instructions, inc becomes dec, and all other one-argument instructions become inc.
For two-argument instructions, jnz becomes cpy, and all other two-instructions become jnz.
The arguments of a toggled instruction are not affected.

If an attempt is made to toggle an instruction outside the program, nothing happens.
If toggling produces an invalid instruction (like cpy 1 2) and an attempt is later made to execute that instruction,
skip it instead.
If tgl toggles itself (for example, if a is 0, tgl a would target itself and become inc a),
the resulting instruction is not executed until the next time it is reached.

For example, given this program:

cpy 2 a
tgl a
tgl a
tgl a
cpy 1 a
dec a
dec a

cpy 2 a initializes register a to 2.
The first tgl a toggles an instruction a (2) away from it, which changes the third tgl a into inc a.
The second tgl a also modifies an instruction 2 away from it, which changes the cpy 1 a into jnz 1 a.
The fourth line, which is now inc a, increments a to 3.
Finally, the fifth line, which is now jnz 1 a, jumps a (3) instructions ahead, skipping the dec a instructions.
In this example, the final value in register a is 3.

The rest of the electronics seem to place the keypad entry (the number of eggs, 7) in register a, run the code,
and then send the value left in register a to the safe.

What value should be sent to the safe?

 */

import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

data class Cpu2(val instructions: MutableList<Instruction>, override var pc: Int = 0, override val registers: Registers = mutableMapOf()) : AbstractCpu()

sealed class Instruction {
    abstract fun execute(before: AbstractCpu): AbstractCpu
}
data class Cpy(val ref: Inp, val reg: Char) : Instruction() {
    override fun execute(before: AbstractCpu): AbstractCpu = cpy(before, ref, reg)
}
data class Jnz(val ref: Inp, val incr: Inp) : Instruction() {
    override fun execute(before: AbstractCpu): AbstractCpu = jnz(before, ref, incr)
}
data class Inc(val reg: Char) : Instruction() {
    override fun execute(before: AbstractCpu): AbstractCpu = inc(before, reg)
}
data class Dec(val reg: Char) : Instruction() {
    override fun execute(before: AbstractCpu): AbstractCpu = dec(before, reg)
}
data class Tgl(val incr: Inp) : Instruction() {
    override fun execute(before: AbstractCpu): AbstractCpu = tgl(before, incr)
}

fun tgl(cpu: AbstractCpu, incr: Inp): AbstractCpu = cpu.apply() {
    TODO("Tgl not implemented")
}


fun parseCpuInstructions(lines: List<String>) = lines.map { parseCpuInstruction(it) }

fun parseCpuInstruction(instrStr: String): Instruction {
    val instrParts = instrStr.split(" ")
    val instrCode = instrParts[0]
    return when(instrCode) {
        "cpy" -> {
            val (_, refStr, to) = instrParts
            val ref = parseRef(refStr)
            val toReg = parseRegister(to)
            Cpy(ref, toReg)
        }
        "jnz" -> {
            val (_, refStr, toStr) = instrParts
            val ref = parseRef(refStr)
            val to = parseRef(toStr)
            Jnz(ref, to)
        }
        "tgl" -> {
            val incrStr = instrParts[1]
            val incr = parseRef(incrStr)
            Tgl(incr)
        }
        "inc" -> parseIncDecInstruction(true, instrParts)
        "dec" -> parseIncDecInstruction(false, instrParts)
        else -> throw IllegalArgumentException("Unkown code $instrCode")
    }
}

private fun parseIncDecInstruction(dec: Boolean, instrParts: List<String>): Instruction {
    val regStr = instrParts[1]
    val reg = parseRegister(regStr)
    if (dec) return Inc(reg)
    else return Dec(reg)
}

fun executeCpuInstructions(instructions: List<Instruction>, cpu: AbstractCpu = Cpu2(instructions.toMutableList()), debug: Boolean = false): AbstractCpu {
    while(cpu.pc in 0 until instructions.size) {
        instructions[cpu.pc].execute(cpu)
        if (debug) println(cpu)
    }
    return cpu
}

class Day23Spec : Spek({

    describe("part 0 - check if instructions still work after refactoring to objects from Day 12") {
        describe("example") {
            given("the input") {
                val input = """
                    cpy 41 a
                    inc a
                    inc a
                    dec a
                    jnz a 2
                    dec a
                    """
                val instructions = parseCpuInstructions(parseTrimedLines(input))
                val result = executeCpuInstructions(instructions, debug = true)
                it("should have the correct value in a") {
                    result.registers['a'] `should equal` 42
                }
            }
        }
    }
    describe("part 1") {
        describe("example") {
            given("the input") {
                val input = """
                    cpy 2 a
                    tgl a
                    tgl a
                    tgl a
                    cpy 1 a
                    dec a
                    dec a
                    """
                val instructions = parseCpuInstructions(parseTrimedLines(input))
                val result = executeCpuInstructions(instructions, debug = true)
                it("should have the correct value in a") {
                    result.registers['a'] `should equal` 3
                }
            }
        }
    }
})
