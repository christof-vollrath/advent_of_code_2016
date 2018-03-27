import org.amshove.kluent.`should equal`
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it

/*
--- Day 10: Balance Bots ---

You come upon a factory in which many robots are zooming around handing small microchips to each other.

Upon closer examination, you notice that each bot only proceeds when it has two microchips, and once it does,
it gives each one to a different bot or puts it in a marked "output" bin.
Sometimes, bots take microchips from "input" bins, too.

Inspecting one of the microchips, it seems like they each contain a single number;
the bots must use some logic to decide what to do with each chip.
You access the local control computer and download the bots' instructions (your puzzle input).

Some of the instructions specify that a specific-valued microchip should be given to a specific bot;
the rest of the instructions indicate what a given bot should do with its lower-value or higher-value chip.

For example, consider the following instructions:

value 5 goes to bot 2
bot 2 gives low to bot 1 and high to bot 0
value 3 goes to bot 1
bot 1 gives low to output 1 and high to bot 0
bot 0 gives low to output 2 and high to output 0
value 2 goes to bot 2

Initially, bot 1 starts with a value-3 chip, and bot 2 starts with a value-2 chip and a value-5 chip.

Because bot 2 has two microchips, it gives its lower one (2) to bot 1 and its higher one (5) to bot 0.
Then, bot 1 has two microchips; it puts the value-2 chip in output 1 and gives the value-3 chip to bot 0.
Finally, bot 0 has two microchips; it puts the 3 in output 2 and the 5 in output 0.

In the end, output bin 0 contains a value-5 microchip, output bin 1 contains a value-2 microchip,
and output bin 2 contains a value-3 microchip.
In this configuration, bot number 2 is responsible for comparing value-5 microchips with value-2 microchips.

Based on your instructions, what is the number of the bot that is responsible
for comparing value-61 microchips with value-17 microchips?

--- Part Two ---

What do you get if you multiply together the values of one chip in each of outputs 0, 1, and 2?

 */

interface Storage {
    fun put(chip: Int)
}
class Bot(val nr: Int, val botFactory: BotFactory) : Storage {
    val chips = mutableListOf<Int>()
    var low: Storage? = null
    var high: Storage? = null
    override fun put(chip: Int) {
        chips += chip
        process()
    }
    fun process() {
        if (chips.size >= 2 && low != null && high != null) {
            val sortedChips = chips.sorted()
            low?.put(sortedChips[0])
            high?.put(sortedChips[1])
            chips.clear()
            botFactory.tracer?.invoke(this, sortedChips[0], sortedChips[1])
        }
    }
}
class OutputBin(val nr: Int) : Storage {
    var chip: Int? = null
    override fun put(chip: Int) {
        this.chip = chip
    }
}

typealias Tracer = (bot: Bot, lowValue: Int, highValue: Int) -> Unit
class BotFactory {
    val bots = mutableMapOf<Int, Bot>()
    val outputBins = mutableMapOf<Int, OutputBin>()
    var tracer: Tracer? = null

    fun valueGoes(value: Int, storage: Storage) {
        storage.put(value)
    }
    fun botGives(bot: Bot, low: Storage, high: Storage) {
        bot.low = low
        bot.high = high
        bot.process()
    }
    fun bot(nr: Int): Bot =
            with(bots[nr]) {
                if (this != null) this
                else {
                    val bot = Bot(nr, this@BotFactory)
                    bots[nr] = bot
                    bot
                }
            }
    fun output(nr: Int): OutputBin =
            with(outputBins[nr]) {
                if (this != null) this
                else {
                    val outputBin = OutputBin(nr)
                    outputBins[nr] = outputBin
                    outputBin
                }
            }

    fun init(input: String) {
        val inputList = parseTrimedLines(input)
        inputList.forEach {
            val parts = it.split(" ")
            when(parts[0]) {
                "value" -> valueGoes(parts[1].toInt(), initStorage(parts[4], parts[5].toInt()))
                "bot" -> botGives(bot(parts[1].toInt()), initStorage(parts[5], parts[6].toInt()), initStorage(parts[10], parts[11].toInt()))
                else -> throw IllegalArgumentException("input line $it")
            }
        }
    }
    private fun initStorage(storageType: String, nr: Int) =
            when(storageType) {
                "bot" -> bot(nr)
                "output" -> output(nr)
                else -> throw IllegalArgumentException("unkown storage type $storageType")
            }
}

class Day10Spec : Spek({

    describe("part 1 & 2") {
        describe("example") {
            given("the bot factory") {
                val factory = BotFactory()
                var responsibleBot: Bot? = null
                factory.tracer = { bot, lowValue, highValue -> if (lowValue == 2 && highValue == 5) responsibleBot = bot }
                with(factory) {
                    valueGoes(5, bot(2))
                    botGives(bot(2), bot(1), bot(0))
                    valueGoes(3, bot(1))
                    botGives(bot(1), output(1), bot(0))
                    botGives(bot(0), output(2), output(0))
                    valueGoes(2, bot(2))
                }
                it("should have put the correct values into the output bins") {
                    factory.outputBins[0]!!.chip `should equal` 5
                    factory.outputBins[1]!!.chip `should equal` 2
                    factory.outputBins[2]!!.chip `should equal` 3
                }
                it("should find the correct bot") {
                    responsibleBot?.nr `should equal` 2
                }
            }
            given("the bot factory initalized from string input") {
                val factory = BotFactory()
                var responsibleBot: Bot? = null
                factory.tracer = { bot, lowValue, highValue -> if (lowValue == 2 && highValue == 5) responsibleBot = bot }
                factory.init("""
                    value 5 goes to bot 2
                    bot 2 gives low to bot 1 and high to bot 0
                    value 3 goes to bot 1
                    bot 1 gives low to output 1 and high to bot 0
                    bot 0 gives low to output 2 and high to output 0
                    value 2 goes to bot 2
                    """)
                it("should have put the correct values into the output bins") {
                    factory.outputBins[0]!!.chip `should equal` 5
                    factory.outputBins[1]!!.chip `should equal` 2
                    factory.outputBins[2]!!.chip `should equal` 3
                }
                it("should find the correct bot") {
                    responsibleBot?.nr `should equal` 2
                }
            }
        }
        describe("exercise") {
            given("exercise input") {
                val input = readResource("day10Input.txt")
                val factory = BotFactory()
                var responsibleBot: Bot? = null
                factory.tracer = { bot, lowValue, highValue -> if (lowValue == 17 && highValue == 61) responsibleBot = bot }
                factory.init(input)
                println(responsibleBot?.nr)
                it("should find the correct bot") {
                    responsibleBot?.nr `should equal` 56
                }
                it("should have put the correct values into the output bins (part 2)") {
                    val result = factory.outputBins[0]!!.chip!! * factory.outputBins[1]!!.chip!! * factory.outputBins[2]!!.chip!!
                    println(result)
                    result `should equal` 7847
                }
            }
        }
    }
})
