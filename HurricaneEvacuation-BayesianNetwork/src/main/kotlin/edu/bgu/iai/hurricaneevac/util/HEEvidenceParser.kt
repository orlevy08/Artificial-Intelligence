package edu.bgu.iai.hurricaneevac.util

import edu.bgu.iai.hurricaneevac.bn.Evidence

fun parseEvidence(input: String): Evidence<String> {
    val splitted = input.split(" ")
    var id: String
    var value = true
    when (splitted.size) {
        2 -> {
            val type =  splitted[0][0].toUpperCase()
            val graphElementId = splitted[1]
            id = type + graphElementId
        }
        3 -> {
            value = if(splitted[0] == "not") false else throw IllegalArgumentException(input)
            val type = splitted[1][0].toUpperCase()
            val graphElementId = splitted[2]
            id = type + graphElementId
        }
        else ->
            throw IllegalArgumentException(input)
    }
    return Evidence(id, value)
}