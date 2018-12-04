package edu.bgu.iai.hurricaneevac.util

import edu.bgu.iai.common.graph.MutableUndirectedWeightedGraph
import edu.bgu.iai.common.graph.MutableWeightedGraph
import edu.bgu.iai.hurricaneevac.api.BWEdge
import edu.bgu.iai.hurricaneevac.api.HESimulatorConfiguration
import edu.bgu.iai.hurricaneevac.api.Spot
import edu.bgu.iai.hurricaneevac.api.SpotType
import edu.bgu.iai.hurricaneevac.util.Constants.FILTER_COMMENTS_REGEX
import java.io.File

object Constants { val FILTER_COMMENTS_REGEX = ";.*$".toRegex() }

fun loadSimulatorConfiguration(path: String): HESimulatorConfiguration {
    var nVertices: Long = 0
    var deadline: Double = Double.MAX_VALUE
    val edges: MutableList<BWEdge<Long>> = mutableListOf()
    val spots: MutableMap<Long, Spot> = mutableMapOf()
    var slowDown: Double

    val bufferedReader = File(path).bufferedReader()

    bufferedReader.useLines { lines ->
        lines.filter { line ->  line.isNotBlank() }
            .forEachIndexed { index, line ->
                val filteredLine = line.replace(FILTER_COMMENTS_REGEX, "").trim()
                val splitted = filteredLine.split(' ')
                val identifier = splitted[0]
                try {
                    when (identifier) {
                        "#E" -> {
                            if (splitted.size != 4)
                                throw IllegalArgumentException("Input file is invalid - at line $index: $line")
                            val vFrom = splitted[1].toLong()
                            val vTo = splitted[2].toLong()
                            val weight = splitted[3].removePrefix("W").toInt()
                            edges += BWEdge(vFrom, vTo, weight)
                        }
                        "#V" -> {
                            when (splitted.size) {
                                2 -> nVertices = splitted[1].toLong()
                                3 -> spots.put(splitted[1].toLong(), Spot(SpotType.SHELTER, 0))
                                4 -> spots.put(splitted[1].toLong(), Spot(SpotType.PERSON, splitted[3].toInt()))
                                else -> throw IllegalArgumentException("Input file is invalid - at line $index: $line")
                            }
                        }
                        "#D" -> deadline = splitted[1].toDouble()
                        else -> throw IllegalArgumentException("Input file is invalid - at line $index: $line")
                    }
                } catch (e: NumberFormatException) {
                    throw IllegalArgumentException("Input file is invalid - at line $index: $line")
                }
            }


    }

    val graph: MutableWeightedGraph<Long, BWEdge<Long>> =
                    MutableUndirectedWeightedGraph((1L..nVertices).toSet(), edges)

    while (true) {
        try {
            println("Enter a slow-down constant between 0 and 1: ")
            slowDown = (readLine() ?: "").toDouble()
            if(slowDown in 0.0..1.0) {
                break
            }
            println("Constant is not in the valid range")
        } catch (e: NumberFormatException) {
            println("Invalid number")
        }
    }

    return HESimulatorConfiguration(graph, spots, nVertices, slowDown, deadline)
}