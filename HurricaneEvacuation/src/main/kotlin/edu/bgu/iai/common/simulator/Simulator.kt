package edu.bgu.iai.common.simulator

import edu.bgu.iai.common.agent.Agent

fun <P, A, G : Agent<P, A>> simulator(simWorld: SimulatorWorld<P, A, G>, agents: List<G>) {

    simWorld.init()
    printState(simWorld,agents)

    while (true) {
        for (agent in agents) {
            val percept = simWorld.percept(agent)
            val action = agent.nextAction(percept)
            simWorld.simulate(action)
            printState(simWorld,agents)
            if (simWorld.end()) {
                return
            }
        }
    }

}

fun <P, A, G : Agent<P, A>> printState (simWorld: SimulatorWorld<P, A, G>, agents: List<G>){
    println("------------- CURRENT STATE -----------------")
    simWorld.printState()
    println()
    agents.forEach { agent -> agent.printState() }
    println("---------------------------------------------")
}

interface SimulatorWorld<P, A, G : Agent<P, A>> {
    fun init()

    fun end(): Boolean

    fun percept(agent: G): P

    fun simulate(action: A)

    fun printState()
}