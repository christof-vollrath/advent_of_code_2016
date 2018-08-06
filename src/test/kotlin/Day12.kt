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

--- Part Two ---

As you head down the fire escape to the monorail, you notice it didn't start;
register c needs to be initialized to the position of the ignition key.

If you instead initialize register c to be 1, what value is now left in register a?

 */
abstract class AbstractCpu {
    abstract var pc: Int
    abstract val registers: Registers
}
data class Cpu(override var pc: Int = 0, override val registers: Registers = mutableMapOf()) : AbstractCpu()

typealias Registers = MutableMap<Char, Int>
typealias Inp = (Registers) -> Int
typealias Instr = (AbstractCpu) -> AbstractCpu

fun parseCpuInstrs(lines: List<String>) = lines.map { parseCpuInstr(it) }

fun parseCpuInstr(instrStr: String): Instr {
    val instrParts = instrStr.split(" ")
    val instrCode = instrParts[0]
    when(instrCode) {
        "cpy" -> {
            val (_, refStr, to) = instrParts
            val ref = parseRef(refStr)
            val toReg = parseRegister(to)
            return { cpu -> cpy(cpu, ref, toReg) }
        }
        "jnz" -> {
            val (_, refStr, toStr) = instrParts
            val ref = parseRef(refStr)
            val to = parseRef(toStr)
            return { cpu -> jnz(cpu, ref, to) }
        }
        "inc" -> return parseIncDec(true, instrParts)
        "dec" -> return parseIncDec(false, instrParts)
        else -> throw IllegalArgumentException("Unkown code $instrCode")
    }
}

private fun parseIncDec(dec: Boolean, instrParts: List<String>): (AbstractCpu) -> AbstractCpu {
    val regStr = instrParts[1]
    val reg = parseRegister(regStr)
    if (dec) return { cpu -> inc(cpu, reg) }
    else return { cpu -> dec(cpu, reg) }
}

fun parseRef(refStr: String): Inp {
    val intValue = refStr.toIntOrNull()
    if (intValue != null) return { intValue }
    else return { registers -> ref(registers, parseRegister(refStr)) }
}

fun parseRegister(to: String) = to[0]

fun ref(registers: Registers, c: Char): Int = registers[c] ?: 0

fun cpy(cpu: AbstractCpu, input: Inp, c: Char): AbstractCpu = cpu.apply() {
    registers[c] = input(cpu.registers)
    incrPc(cpu)
}

fun jnz(cpu: AbstractCpu, ref: Inp, incr: Inp): AbstractCpu = cpu.apply() {
    if (ref(registers) != 0) cpu.pc += incr(registers)
    else incrPc(cpu)
}

fun inc(cpu: AbstractCpu, reg: Char): AbstractCpu = cpu.apply() {
    registers[reg] = (registers[reg] ?: 0) + 1
    incrPc(cpu)
}

fun dec(cpu: AbstractCpu, reg: Char): AbstractCpu = cpu.apply() {
    registers[reg] = (registers[reg] ?: 0) - 1
    incrPc(cpu)
}

fun incrPc(cpu: AbstractCpu) {
    cpu.pc++
}

fun executeCpuInstrs(instrs: List<Instr>, cpu: AbstractCpu = Cpu(), debug: Boolean = false): AbstractCpu {
    while(cpu.pc in 0 until instrs.size) {
        instrs[cpu.pc](cpu)
        if (debug) println(cpu)
    }
    return cpu
}

class Day12Spec : Spek({

    describe("part 1") {
        given("copy instruction with constant") {
            val cpu = Cpu()
            val copyInstr: Instr = { cpy(it, { 41 }, 'a') }
            it("should copy value to register") {
                copyInstr(cpu)
                cpu.pc `should equal` 1
                cpu.registers['a'] `should equal` 41
            }
        }
        given("copy instruction with register") {
            val cpu = Cpu(registers = mutableMapOf('b' to 42))
            val copyInstr: Instr = { cpy(it, { ref(it, 'b') }, 'a') }
            it("should copy register to other register") {
                copyInstr(cpu)
                cpu.pc `should equal` 1
                cpu.registers['a'] `should equal` 42
            }
        }
        given("copy instruction with constant as input string") {
            val cpu = Cpu()
            val copyInstr: Instr = parseCpuInstr("cpy 41 a")
            it("should copy value to register") {
                copyInstr(cpu)
                cpu.pc `should equal` 1
                cpu.registers['a'] `should equal` 41
            }
        }
        given("copy instruction with register as input string") {
            val cpu = Cpu(registers = mutableMapOf('b' to 42))
            val copyInstr: Instr = parseCpuInstr("cpy b a")
            it("should copy register to other register") {
                copyInstr(cpu)
                cpu.pc `should equal` 1
                cpu.registers['a'] `should equal` 42
            }
        }
        given("inc instruction for empty register as input string") {
            val cpu = Cpu()
            val incInstr: Instr = parseCpuInstr("inc a")
            it("should increment register") {
                incInstr(cpu)
                cpu.pc `should equal` 1
                cpu.registers['a'] `should equal` 1
            }
        }
        given("inc instruction for register with value as input string") {
            val cpu = Cpu(registers = mutableMapOf('a' to 42))
            val incInstr: Instr = parseCpuInstr("inc a")
            it("should increment register") {
                incInstr(cpu)
                cpu.pc `should equal` 1
                cpu.registers['a'] `should equal` 43
            }
        }
        given("dec instruction as input string") {
            val cpu = Cpu()
            val decInstr: Instr = parseCpuInstr("dec a")
            it("should decrement register") {
                decInstr(cpu)
                cpu.pc `should equal` 1
                cpu.registers['a'] `should equal` -1
            }
        }
        given("jnz instruction as input string on an empty register") {
            val cpu = Cpu()
            val jnzInstr: Instr = parseCpuInstr("jnz a 2")
            it("should not jump") {
                jnzInstr(cpu)
                cpu.pc `should equal` 1
            }
        }
        given("jnz instruction as input string on a non zero register") {
            val cpu = Cpu(registers = mutableMapOf('a' to 1))
            val jnzInstr: Instr = parseCpuInstr("jnz a 2")
            it("should not jump") {
                jnzInstr(cpu)
                cpu.pc `should equal` 2
            }
        }
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
                val commands = parseCpuInstrs(parseTrimedLines(input))
                val result = executeCpuInstrs(commands, debug = true)
                result.registers['a'] `should equal` 42
            }
        }
        describe("exercise") {
            given("exercise input") {
                val input = readResource("day12Input.txt")
                val commands = parseCpuInstrs(parseTrimedLines(input))
                val result = executeCpuInstrs(commands)
                println(result.registers['a'])
                result.registers['a'] `should equal` 318003
            }
        }
    }
    describe("part 2") {
        describe("exercise") {
            given("exercise input") {
                val input = readResource("day12Input.txt")
                val commands = parseCpuInstrs(parseTrimedLines(input))
                val cpu = Cpu(registers = mutableMapOf('c' to 1))
                val result = executeCpuInstrs(commands, cpu)
                println(result.registers['a'])
                result.registers['a'] `should equal` 9227657
            }
        }
    }
})
