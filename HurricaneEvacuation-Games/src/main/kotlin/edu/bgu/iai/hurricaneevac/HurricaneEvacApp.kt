package edu.bgu.iai.hurricaneevac

import edu.bgu.iai.common.simulator.simulator
import edu.bgu.iai.hurricaneevac.agent.HEAgent.AgentType
import edu.bgu.iai.hurricaneevac.util.loadAgentConfig
import edu.bgu.iai.hurricaneevac.util.loadSimulatorConfiguration
import edu.bgu.iai.hurricaneevac.util.selectAgentFrom

fun main(args: Array<String>) {
    val simConfig = loadSimulatorConfiguration("src/test/resources/SimulatorTest.txt")
    val humanAgentConfig = loadAgentConfig(AgentType.HUMAN, 1)
    val gameType = selectAgentFrom(listOf(AgentType.ADVERSARIAL, AgentType.SEMI_COOPERATIVE, AgentType.FULLY_COOPERATIVE))
    val selectedAgentConfig = loadAgentConfig(gameType, 1)
    val agents = listOf(humanAgentConfig, selectedAgentConfig).map { it.agentType.factory(it.agentId) }
    simConfig.agentsConfiguration = listOf(humanAgentConfig, selectedAgentConfig)
    simulator(simConfig, agents)
}

