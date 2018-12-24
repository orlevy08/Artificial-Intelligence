package edu.bgu.iai.hurricaneevac.util

import edu.bgu.iai.hurricaneevac.util.Constants.FILTER_COMMENTS_REGEX
import java.io.File

object Constants { val FILTER_COMMENTS_REGEX = ";.*$".toRegex() }

fun loadBayesNetworkConfiguration(path: String): HEBayesNetworkConfig {
    var nVertices: Long = 0
    val verticesConfig: MutableList<VertexConfig> = mutableListOf()
    val edgesConfig: MutableList<EdgeConfig> = mutableListOf()

    val bufferedReader = File(path).bufferedReader()

    bufferedReader.useLines { lines ->
        lines.filter { line ->  line.isNotBlank() }
            .forEachIndexed { index, line ->
                val filteredLine = line.replace(FILTER_COMMENTS_REGEX, "").trim()
                val splitted = filteredLine.split(' ')
                val identifier = splitted[0]
                try {
                    when {
                         identifier.matches("#E\\d+".toRegex())-> {
                            if (splitted.size != 4)
                                throw IllegalArgumentException("Input file is invalid - at line $index: $line")
                            val id = splitted[0].removePrefix("#E").toLong()
                            val vFrom = splitted[1].toLong()
                            val vTo = splitted[2].toLong()
                            val weight = splitted[3].removePrefix("W").toInt()
                            edgesConfig += EdgeConfig(id, vFrom, vTo, weight)
                        }
                        identifier.matches("#V\\d+".toRegex()) -> {
                            if (splitted.size != 3)
                                throw IllegalArgumentException("Input file is invalid - at line $index: $line")
                            val id = splitted[0].removePrefix("#V").toLong()
                            val fProb = splitted[2].toDouble()
                            verticesConfig += VertexConfig(id, fProb)
                        }
                        identifier.equals("#V") -> {
                            if (splitted.size != 2)
                                throw IllegalArgumentException("Input file is invalid - at line $index: $line")
                            nVertices = splitted[1].toLong()
                        }
                    }
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("Input file is invalid - at line $index: $line")
                }
            }
    }

    return HEBayesNetworkConfig(nVertices, verticesConfig, edgesConfig)
}

data class HEBayesNetworkConfig(
        val nVertices: Long,
        val verticesConfig: List<VertexConfig>,
        val edgesConfig: List<EdgeConfig>
)

data class VertexConfig(
        val id: Long,
        val fProb: Double
)

data class EdgeConfig(
        val id: Long,
        val vFrom: Long,
        val vTo: Long,
        val weight: Int
)