import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

/*

--- Day 12: Leonardo's Monorail ---

You finally reach the top floor of this building: a garden with a slanted glass ceiling.
Looks like there are no more stars to be had.

While sitting on a nearby bench amidst some tiger lilies,
you manage to decrypt some of the files you extracted from the servers downstairs.

According to these documents, Easter Bunny HQ isn't just this building
- it's a collection of buildings in the nearby area.
They're all connected by a local monorail, and there's another building not far from here!
Unfortunately, being night, the monorail is currently not operating.

You remotely connect to the monorail control systems and discover that the boot sequence expects a password.
The password-checking logic (your puzzle input) is easy to extract, but the code it uses is strange:
it's assembunny code designed for the new computer you just assembled.
You'll have to execute the code and get the password.

The assembunny code you've extracted operates on four registers (a, b, c, and d)
that start at 0 and can hold any integer.

However, it seems to make use of only a few instructions:

cpy x y copies x (either an integer or the value of a register) into register y.
inc x increases the value of register x by one.
dec x decreases the value of register x by one.
jnz x y jumps to an instruction y away (positive means forward; negative means backward),
but only if x is not zero.
The jnz instruction moves relative to itself: an offset of -1 would continue at the previous instruction,
while an offset of 2 would skip over the next instruction.

For example:

cpy 41 a
inc a
inc a
dec a
jnz a 2
dec a

The above code would set register a to 41, increase its value by 2, decrease its value by 1,
and then skip the last dec a (because a is not zero, so the jnz a 2 skips it),
leaving register a at 42.
When you move past the last instruction, the program halts.

After executing the assembunny code in your puzzle input, what value is left in register a?

 */

typealias Registers = Map<Char, Int>
typealias Inp = (Registers) -> Int
typealias Instr = (Cpu) -> Cpu

class Day12Spec : Spek({

    describe("part 1") {
        given("copy instruction with constant") {
            val cpu = Cpu()
            val copyInstr: Instr = { cpy(it, { 41 }, 'a')}
            it("should copy value to register") {
                copyInstr(cpu)
                cpu.pc `should equal` 1
                cpu.registers['a'] `should equal` 41
            }
        }
        given("copy instruction with register") {
            val cpu = Cpu(registers = mutableMapOf('b' to 42))
            val copyInstr: Instr = { cpy(it, { ref(it, 'b') }, 'a')}
            it("should copy register to other register") {
                copyInstr(cpu)
                cpu.pc `should equal` 1
                cpu.registers['a'] `should equal` 42
            }
        }
        given("copy instruction with constant as input string") {
            val cpu = Cpu()
            val copyInstr: Instr = parseCpuInstruction("cpy 41 a")
            it("should copy value to register") {
                copyInstr(cpu)
                cpu.pc `should equal` 1
                cpu.registers['a'] `should equal` 41
            }
        }
        given("copy instruction with register as input string") {
            val cpu = Cpu(registers = mutableMapOf('b' to 42))
            val copyInstr: Instr = parseCpuInstruction("cpy b a")
            it("should copy register to other register") {
                copyInstr(cpu)
                cpu.pc `should equal` 1
                cpu.registers['a'] `should equal` 42
            }
        }
    }
})

fun parseCpuInstruction(instrStr: String): Instr {
    val instrParts = instrStr.split(" ")
    val (instrCode, from, to) = instrParts
    return when(instrCode) {
        "cpy" -> { cpu -> cpy(cpu, parseRef(from), to[0]) }
        else -> throw IllegalArgumentException("Unkown code $instrCode")
    }
}

fun parseRef(refStr: String): (Registers) -> Int {
    val intValue = refStr.toIntOrNull()
    if (intValue != null) return { intValue }
    else return { registers -> ref(registers, refStr[0]) }
}

fun ref(registers: Registers, c: Char): Int = registers[c]!!

fun cpy(cpu: Cpu, input: Inp, c: Char): Cpu = cpu.apply() {
    registers[c] = input(cpu.registers)
    cpu.pc++
}


class Cpu(var pc: Int = 0, val registers: MutableMap<Char, Int> = mutableMapOf<Char, Int>())

