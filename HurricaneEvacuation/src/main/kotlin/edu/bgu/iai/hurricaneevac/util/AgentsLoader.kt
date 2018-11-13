package edu.bgu.iai.hurricaneevac.util

import edu.bgu.iai.hurricaneevac.agent.HEAgent
import edu.bgu.iai.hurricaneevac.agent.HEAgent.AgentType
import edu.bgu.iai.hurricaneevac.agent.RTAStarSearchAgent

fun loadAgents(nAgents: MutableMap<AgentType, Int>): List<HEAgent> {
    val agents = mutableListOf<HEAgent>()
    val chosenAgentType = selectAgentFrom(nAgents.keys.toList())
    nAgents.computeIfPresent(chosenAgentType) { _ , nAgent -> nAgent + 1 }
    nAgents.forEach { agentType, nAgent ->
        for(id in 1..nAgent) {
            agents += loadAgent(agentType, id)
        }
    }
    return agents.apply { shuffle() }
}

private fun selectAgentFrom(agentTypes: List<AgentType>): AgentType {
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

private fun loadAgent(agentType: AgentType, agentId: Int): HEAgent{
    var position: Long
    while (true) {
        try {
            println("Enter initial position of $agentType agent #$agentId: ")
            position = (readLine() ?: "").toLong()
            break
        } catch (e: NumberFormatException) {
            println("Invalid number")
        }
    }
    val agent: HEAgent = agentType.factory(agentId, position)
    if(agent.type === AgentType.RTASTARSEARCH) {
        var nExpandsLimit: Int?
        while (true) {
            try {
                println("Enter a limit for number of tree expansions: ")
                nExpandsLimit = (readLine() ?: "").toInt()
                break
            } catch (e: NumberFormatException) {
                println("Invalid number")
            }
        }
        (agent as RTAStarSearchAgent).nExpandsLimit = nExpandsLimit!!
    }
    return agent
}