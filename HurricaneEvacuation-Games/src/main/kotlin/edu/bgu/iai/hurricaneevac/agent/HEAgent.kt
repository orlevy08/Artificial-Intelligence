package edu.bgu.iai.hurricaneevac.agent

import edu.bgu.iai.common.agent.Agent
import edu.bgu.iai.common.search.game.GameProblem
import edu.bgu.iai.common.search.game.maxiMax
import edu.bgu.iai.common.search.game.miniMaxWithPruning
import edu.bgu.iai.hurricaneevac.api.*

typealias AgentFactory = (Int) -> HEAgent

sealed class HEAgent(override val agentId: Int) : Agent<HEPercept, HEAction> {
    enum class AgentType(val toString: String, val factory: AgentFactory) {
        HUMAN("Human", { id -> HumanAgent(id) }),
        ADVERSARIAL("Adversarial", { id -> AdversarialAgent(id) }),
        SEMI_COOPERATIVE("Semi-Cooperative", { id -> SemiCooperativeAgent(id) }),
        FULLY_COOPERATIVE("Fully-Cooperative", { id -> FullyCooperativeAgent(id) });

        override fun toString(): String = toString
    }

    abstract val type: AgentType

    override fun nextAction(percept: HEPercept): HEAction {
        if (percept.deadline < 1) {
            return no_op
        }

        return doNextAction(percept)
    }

    abstract fun doNextAction(percept: HEPercept): HEAction

    protected val no_op: HEAction = { simConfig ->
        simConfig.deadline--
        println("""

            *** $type-Agent #$agentId performed no_op ***

        """.trimIndent())
    }

    protected fun traverse(edge: BWEdge<Long>, percept: HEPercept): HEAction {
        return { simConfig ->
            val agentMetaData = percept.agentsMetaData[type, agentId]!!
            val traverseTime = traverseTime(edge.weight, percept.slowDown, agentMetaData.nPeople)
            simConfig.deadline -= traverseTime
            val pos = edge.vTo
            var nPeople = agentMetaData.nPeople
            var score = agentMetaData.score
            val spot = percept.spots[pos]
            spot?.also {
                when(it.spotType) {
                    SpotType.SHELTER -> {
                        score += nPeople
                        nPeople = 0
                    }
                    SpotType.PERSON -> {
                        nPeople += it.amount
                        simConfig.spots -= pos
                    }

                }
            }
            simConfig.agentsMetaData[type, agentId] = AgentsMetaData.AgentMetaDataPatch(pos, nPeople, score)
            println("""

                *** $type-Agent #$agentId traversed from ${edge.vFrom} to ${edge.vTo} ***

            """.trimIndent())
        }
    }

    fun traverseTime(weight: Int, slowDown: Double, nPeople: Int): Double {
        return "%.2f".format((weight * (1 + slowDown * nPeople))).toDouble()
    }

    override fun printState(){

    }
}

class HumanAgent(agentId: Int) : HEAgent(agentId) {
    override val type
        get() = AgentType.HUMAN

    override fun doNextAction(percept: HEPercept): HEAction {
        val agentMetaData = percept.agentsMetaData[type, agentId]!!
        val possibleMoves = percept.graph.getNeighbours(agentMetaData.pos)
                .filter { percept.deadline >= traverseTime(it.weight, percept.slowDown, agentMetaData.nPeople) }
                .map { edge -> edge.vTo }

        println("""

            Agent Number: $agentId
            Agent Position: ${agentMetaData.pos}
            Number of people collected: ${agentMetaData.score}
            Number of people on board: ${agentMetaData.nPeople}
        """.trimIndent())

        if (possibleMoves.isEmpty()) {
            println("No possible moves. Performing no_op")
            return no_op
        } else {
            val chosenMove = selectMove(possibleMoves)
            val edge = percept.graph.getEdge(agentMetaData.pos, chosenMove)!!
            return traverse(edge, percept)
        }
    }

    fun selectMove(possibleMoves: List<Long>): Long {
        println("""
            Select one of the following positions for traverse:
            ${possibleMoves.joinToString(", ")}
        """.trimIndent())

        var chosenMove: Long
        while (true) {
            chosenMove = (readLine()?.takeUnless { it.isBlank() } ?: "-1").toLong()
            if (chosenMove in possibleMoves) {
                println()
                break
            }
            println("Invalid - please select again:")
        }
        return chosenMove
    }
}

sealed class GameTreeSearchAgent(agentId: Int): HEAgent(agentId) {

    data class State(
            val myMetaData: AgentsMetaData.AgentMetaData,
            val otherMetaData: AgentsMetaData.AgentMetaData,
            val hasPeople: Set<Long>,
            val time: Double
    )

    inner class HEProblem(val percept: HEPercept): GameProblem<HEAction, State> {

        override val initialState: State

        init {
            val allAgents = percept.agentsMetaData.all()
            assert(allAgents.size == 2)
            var myMetaData: AgentsMetaData.AgentMetaData? = null
            var otherMetaData: AgentsMetaData.AgentMetaData? = null
            allAgents.forEach { if(it.agentType === type && it.agentId == agentId) myMetaData = it else otherMetaData = it}
            val hasPeople = percept.spots.filterValues { it.spotType === SpotType.PERSON }.keys
            initialState = State(myMetaData!!, otherMetaData!!, hasPeople, percept.deadline )
        }

        override fun nextStates(state: State, player: Int): Collection<Pair<HEAction, State>> {
            val playerMetaData = if (player == 0) state.myMetaData else state.otherMetaData
            return percept.graph.getNeighbours(playerMetaData.pos).map { edge ->
                val traverseTime = traverseTime(edge.weight, percept.slowDown, playerMetaData.nPeople)
                val time = "%.2f".format((state.time - traverseTime)).toDouble()
                val pos = edge.vTo
                val nPeople = percept.spots[pos]?.let {
                    when (it.spotType) {
                        SpotType.SHELTER ->
                            if(time < 0)playerMetaData.nPeople
                            else 0
                        SpotType.PERSON -> playerMetaData.nPeople + (it.amount.takeIf { pos in state.hasPeople } ?: 0)
                    }
                } ?: playerMetaData.nPeople
                val score = playerMetaData.score +
                        (percept.spots[pos]?.let {
                            if(it.spotType === SpotType.SHELTER && time >= 0) playerMetaData.nPeople else 0
                        } ?: 0)
                val hasPeople = state.hasPeople.toMutableSet().apply { remove(pos) }
                val newMetaData = AgentsMetaData.AgentMetaData(playerMetaData.agentType,
                                                               playerMetaData.agentId,
                                                               pos,
                                                               nPeople,
                                                               score)
                val nextState = if (player == 0)
                                    State(newMetaData, state.otherMetaData, hasPeople, time)
                                else
                                    State(state.myMetaData, newMetaData, hasPeople, time)

                val action = traverse(edge, percept)
                action to nextState
            }
        }

        override fun isTerminalState(state: State, depth: Int): Boolean {
            return  depth == 20 ||
                    state.time <= 0 ||
                    (state.hasPeople.isEmpty() && state.myMetaData.nPeople == 0 && state.otherMetaData.nPeople == 0)
        }
    }
}

class AdversarialAgent(agentId: Int): GameTreeSearchAgent(agentId) {
    override val type: AgentType
        get() = AgentType.ADVERSARIAL

    override fun doNextAction(percept: HEPercept): HEAction {
        return miniMaxWithPruning(HEProblem(percept), no_op) { state ->
            state.myMetaData.score - state.otherMetaData.score
        }
    }
}

class SemiCooperativeAgent(agentId: Int): GameTreeSearchAgent(agentId) {
    override val type: AgentType
        get() = AgentType.SEMI_COOPERATIVE

    override fun doNextAction(percept: HEPercept): HEAction {
        return maxiMax(HEProblem(percept), no_op) { state ->
            state.myMetaData.score to state.otherMetaData.score
        }
    }
}

class FullyCooperativeAgent(agentId: Int): GameTreeSearchAgent(agentId) {
    override val type: AgentType
        get() = AgentType.FULLY_COOPERATIVE

    override fun doNextAction(percept: HEPercept): HEAction {
        return maxiMax(HEProblem(percept), no_op) { state ->
            val score = 2 * state.myMetaData.score + state.otherMetaData.score
            score to score
        }
    }
}
