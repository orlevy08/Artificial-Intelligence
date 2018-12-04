package edu.bgu.iai.common.agent

interface Agent<P, A> {
    val agentId: Int

    fun nextAction(percept: P): A

    fun printState()
}