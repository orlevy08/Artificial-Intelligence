package edu.bgu.iai.hurricaneevac

import edu.bgu.iai.common.simulator.simulator
import edu.bgu.iai.hurricaneevac.agent.HEAgent
import edu.bgu.iai.hurricaneevac.agent.HEAgent.AgentType
import edu.bgu.iai.hurricaneevac.agent.HESearchAgent
import edu.bgu.iai.hurricaneevac.util.loadAgents
import edu.bgu.iai.hurricaneevac.util.loadSimulatorConfiguration

fun main(args: Array<String>) {
    val partDictionary: Map<Int, () -> List<HEAgent>> = mapOf(
            1 to { loadAgents(mutableMapOf(
                    AgentType.HUMAN to 0,
                    AgentType.GREEDY to 1,
                    AgentType.VANDAL to 1
            )) },

            2 to { loadAgents(mutableMapOf(
                    AgentType.GREEDYSEARCH to 0,
                    AgentType.ASTARSEARCH to 0,
                    AgentType.RTASTARSEARCH to 0
            )) }
    )
    println("Select a part to run (1/2): ")
    val part = (readLine() ?: "1").toInt()

    val simConfig = loadSimulatorConfiguration("src/test/resources/SimulatorTest.txt")
    val agents = partDictionary[part]?.invoke() ?: partDictionary[1]!!.invoke()
    simulator(simConfig, agents)

    println()
    agents.forEach { agent ->
        println("""
            ${agent.type}-Agent #${agent.agentId} has scored: ${agent.score}
        """.trimIndent())

        if(part == 2) {
            var nExpands = (agent as HESearchAgent).nExpands
            val fs = listOf(1,100,10000)
            val performanceMeasure = fs.map{ f -> "With f = $f ---> ${(f * agent.score) + nExpands}" }

            println("Performance-measure:\n${performanceMeasure.joinToString("\n")}")
        }
    }
}

