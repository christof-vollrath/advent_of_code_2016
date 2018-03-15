fun readResource(name: String) = ClassLoader.getSystemClassLoader().getResource(name).readText()

fun readTrimedLinesFromResource(name: String) = parseTrimedLines(readResource(name))
fun parseTrimedLines(input: String) =
        input.split("\n")
            .filter { ! it.isBlank() }
            .map { it.trim() }

// https://github.com/dkandalov/kotlin-99/blob/master/src/org/kotlin99/common/collections.kt
fun <E> List<List<E>>.transpose(): List<List<E>> {
    if (isEmpty()) return this

    val width = first().size
    if (any { it.size != width }) {
        throw IllegalArgumentException("All nested lists must have the same size, but sizes were ${map { it.size }}")
    }

    return (0 until width).map { col ->
        (0 until size).map { row -> this[row][col] }
    }
}
