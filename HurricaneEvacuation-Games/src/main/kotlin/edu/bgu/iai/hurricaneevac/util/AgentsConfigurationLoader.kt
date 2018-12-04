package edu.bgu.iai.hurricaneevac.util

import edu.bgu.iai.hurricaneevac.agent.HEAgent.AgentType

fun selectAgentFrom(agentTypes: List<AgentType>): AgentType {
    val options = agentTypes.mapIndexed { index, agentType -> "(${index+1}) $agentType" }
    println("Select an agent-type out of the following (by it's number):\n" +
        options.joinToString("\n"))

    var chosenType: Int
    while (true) {
        try {
            chosenType = (readLine() ?: "").toInt()
            break
        } catch (e: NumberFormatException) {
            println("Invalid number")
        }
    }
    return agentTypes[chosenType-1]
}

fun loadAgentConfig(agentType: AgentType, agentId: Int): HEAgentConfig {
    var position: Long
    while (true) {
        try {
            println("Enter initial position of $agentType-Agent #$agentId: ")
            position = (readLine() ?: "").toLong()
            break
        } catch (e: NumberFormatException) {
            println("Invalid number")
        }
    }

    return HEAgentConfig(agentType, agentId, position)
}

data class HEAgentConfig(
  val agentType: AgentType,
  val agentId: Int,
  val pos: Long
)