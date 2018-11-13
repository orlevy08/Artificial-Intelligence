package edu.bgu.iai.hurricaneevac.agent

import edu.bgu.iai.common.agent.Agent
import edu.bgu.iai.common.search.Problem
import edu.bgu.iai.common.search.Strategy
import edu.bgu.iai.common.search.treeSearch
import edu.bgu.iai.hurricaneevac.api.BWEdge
import edu.bgu.iai.hurricaneevac.api.HEAction
import edu.bgu.iai.hurricaneevac.api.HEPercept
import edu.bgu.iai.hurricaneevac.api.SpotType
import edu.bgu.iai.hurricaneevac.graph.dijkstraShortestPath
import java.util.*

typealias AgentFactory = (Int, Long) -> HEAgent

sealed class HEAgent(override val agentId: Int, protected var pos: Long) : Agent<HEPercept, HEAction> {
    companion object {
        val EDGE_FILTER: (BWEdge<Long>) -> Boolean = { edge -> !edge.isBlocked }
    }

    enum class AgentType(val toString: String, val factory: AgentFactory) {
        HUMAN("Human", { id, pos -> HumanAgent(id, pos) }),
        GREEDY("Greedy", { id, pos -> GreedyAgent(id, pos) }),
        VANDAL("Vandal", { id, pos -> VandalAgent(id, pos) }),
        GREEDYSEARCH("Greedy-Search", { id, pos -> GreedySearchAgent(id, pos) }),
        ASTARSEARCH("A*", { id, pos -> AStarSearchAgent(id, pos) }),
        RTASTARSEARCH("RT-A*", { id, pos -> RTAStarSearchAgent(id, pos) });

        override fun toString(): String = toString
    }

    override var score: Int = 0
        protected set

    protected var numOfPeople: Int = 0

    abstract val type: AgentType

    override fun nextAction(percept: HEPercept): HEAction {
        if (percept.deadline < 1) {
            return no_op
        }
        percept.spots[pos]?.takeIf { this.type !== AgentType.VANDAL && it.spotType === SpotType.PERSON }?.also {
            numOfPeople += it.amount
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
            assert(pos == edge.vFrom && !edge.isBlocked)
            var thisSpot = percept.spots[pos]
            thisSpot?.takeIf { this.type !== AgentType.VANDAL && it.spotType === SpotType.PERSON }?.also {
                simConfig.spots -= pos
            }
            val traverseTime = traverseTime(edge.weight, percept.slowDown)
            simConfig.deadline -= traverseTime
            pos = edge.vTo
            thisSpot = percept.spots[pos]
            thisSpot?.takeIf { this.type !== AgentType.VANDAL }?.also {
                if (it.spotType === SpotType.SHELTER) {
                    score += numOfPeople
                    numOfPeople = 0
                }
            }
            println("""

                *** $type-Agent #$agentId traversed from ${edge.vFrom} to ${edge.vTo} ***

            """.trimIndent())
        }
    }

    fun traverseTime(weight: Int, slowDown: Double, nPeople: Int = numOfPeople): Double {
        return "%.2f".format((weight * (1 + slowDown * nPeople))).toDouble()
    }

    override fun printState(){
        println( """
            ${this.type}-Agent #$agentId - Position: $pos  Score: $score
        """.trimIndent())
    }
}

class HumanAgent(agentId: Int, pos: Long) : HEAgent(agentId, pos) {
    override val type
        get() = AgentType.HUMAN

    override fun doNextAction(percept: HEPercept): HEAction {
        val possibleMoves = percept.graph.getNeighbours(pos)
                .filter(EDGE_FILTER)
                .filter { percept.deadline >= traverseTime(it.weight, percept.slowDown) }
                .map { edge -> edge.vTo }

        println("""

            Agent Number: $agentId
            Agent Position: $pos
            Number of people collected: $score
            Number of people on board: $numOfPeople
        """.trimIndent())

        if (possibleMoves.isEmpty()) {
            println("No possible moves. Performing no_op")
            return no_op
        } else {
            val chosenMove = selectMove(possibleMoves)
            val edge = percept.graph.getEdge(pos, chosenMove)!!
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
            chosenMove = (readLine() ?: "-1").toLong()
            if (chosenMove in possibleMoves) {
                println()
                break
            }
            println("Invalid - please select again:")
        }
        return chosenMove
    }
}


class GreedyAgent(agentId: Int, pos: Long) : HEAgent(agentId, pos) {
    override val type
        get() = AgentType.GREEDY

    var movesSequence: LinkedList<Long> = LinkedList()

    override fun doNextAction(percept: HEPercept): HEAction {
        checkMovesSequence(percept)
        if (movesSequence.isEmpty()) {
            val thereAreNoPeopleToSave = numOfPeople == 0 &&
                    percept.spots.filterValues { it.spotType === SpotType.PERSON }.isEmpty()
            if (thereAreNoPeopleToSave)
                return no_op

            if (numOfPeople > 0)
                search(SpotType.SHELTER, percept)
            else
                search(SpotType.PERSON, percept)
        }
        return movesSequence
                .takeUnless { it.isEmpty() }
                ?.removeFirst()
                ?.let { doNextAction(percept, it) } ?: no_op
    }

    private fun doNextAction(percept: HEPercept, chosenMove: Long): HEAction {
        val edge = percept.graph.getEdge(pos, chosenMove)!!
        val traverseTime = traverseTime(edge.weight, percept.slowDown)
        if (percept.deadline < traverseTime) {
            movesSequence.clear()
            return no_op
        }
        return traverse(edge, percept)
    }

    fun search(type: SpotType, percept: HEPercept) {
        val (dist, backtrack) =
                dijkstraShortestPath(percept.graph, pos, EDGE_FILTER)

        val distFilteredBySpotType = dist.filterKeys { v -> percept.spots[v]?.spotType === type }
        val minDistEntry = distFilteredBySpotType.minBy { it.value }

        var nextPos = minDistEntry?.key
        var prevPos = backtrack[nextPos]

        if (nextPos == null || prevPos == null) return

        while (nextPos != pos) {
            movesSequence.addFirst(nextPos)
            nextPos = prevPos
            prevPos = backtrack[nextPos]
        }
    }

    private fun checkMovesSequence(percept: HEPercept) {
        var currentPos = pos
        var shouldClear = movesSequence.takeUnless { it.isEmpty() }?.last?.let { percept.spots[it] == null } ?: false
        for (nextPos in movesSequence) {
            if (shouldClear) break
            shouldClear = percept.graph.getEdge(currentPos, nextPos)?.isBlocked ?: true
            currentPos = nextPos
        }
        if (shouldClear) movesSequence.clear()
    }
}


class VandalAgent(agentId: Int, pos: Long) : HEAgent(agentId, pos) {
    override val type
        get() = AgentType.VANDAL

    private var timeToAction = -1L

    private var didBlock = false

    override fun doNextAction(percept: HEPercept): HEAction {
        if(timeToAction == -1L)
            timeToAction = percept.nVertices

        if (timeToAction > 0) {
            timeToAction--
            return no_op
        }

        timeToAction = percept.nVertices

        val currentDidBlock = didBlock
        didBlock = !didBlock

        val minEdge = percept.graph.getNeighbours(pos)
                .filter(EDGE_FILTER)
                .minWith(compareBy({ it.weight }, { it.vTo }))

        return if (currentDidBlock) {
            minEdge?.let { traverse(it, percept) } ?: no_op

        } else {
            minEdge?.let { block(it) } ?: no_op
        }
    }

    private fun block(edge: BWEdge<Long>): HEAction {
        return { simConfig ->
            assert(pos == edge.vFrom && !edge.isBlocked)
            val blocked = BWEdge(edge.vFrom, edge.vTo, edge.weight, true)
            simConfig.graph.addOrModifyEdge(blocked)
            simConfig.deadline--
            didBlock = true

            println("""

                *** $type-Agent #$agentId blocked edge ${edge.vFrom}--${edge.vTo} ***

            """.trimIndent())
        }
    }
}

sealed class HESearchAgent(agentId: Int, pos: Long) : HEAgent(agentId, pos) {

    var movesSequence: LinkedList<Long>? = null
    var nExpands: Int = 0

    override fun doNextAction(percept: HEPercept): HEAction {
        if (movesSequence == null) {
            movesSequence = initMovesSequence(percept)
        }
        return movesSequence
                ?.takeUnless { it.isEmpty() }
                ?.removeFirst()
                ?.let { doNextAction(percept, it) } ?: no_op
    }

    open protected fun initMovesSequence(percept: HEPercept): LinkedList<Long> {
        val (goalNode, nExpands) = treeSearch(HEProblem(percept), heuristicFunc(percept))!!
        this.nExpands = nExpands
        val list = LinkedList<Long>()
        var node = goalNode
        while (node.parent != null) {
            list.addFirst(node.state.vertex)
            node = node.parent!!
        }
        return list
    }

    private fun doNextAction(percept: HEPercept, chosenMove: Long): HEAction {
        val edge = percept.graph.getEdge(pos, chosenMove)!!
        val traverseTime = traverseTime(edge.weight, percept.slowDown)
        if (percept.deadline < traverseTime) {
            movesSequence?.clear()
            return no_op
        }
        return traverse(edge, percept)
    }

    open fun heuristicFunc(percept: HEPercept): Strategy<HEAction, State> {
        return { node ->
            val state = node.state
            val (dist, _) = dijkstraShortestPath(percept.graph, state.vertex)
            val distFilteredByPerson = dist.filterKeys { it in state.hasPeople }
            val pathsWeight = distFilteredByPerson.mapValues {
                val pplVertex = it.key
                val distanceToPpl = it.value
                val nPeopleAtVertex = percept.spots[pplVertex]!!.amount
                val traverseTimeToPeople = traverseTime(distanceToPpl, percept.slowDown, state.nPeople)

                val (dist2, _) = dijkstraShortestPath(percept.graph, pplVertex)
                val closestShelter = dist2.filterKeys { percept.spots[it]?.spotType === SpotType.SHELTER }.minBy { it.value }
                val distanceToClosestShelter = closestShelter?.value ?: Int.MAX_VALUE
                val traverseTimeToShelter = traverseTime(distanceToClosestShelter, percept.slowDown, state.nPeople + nPeopleAtVertex)
                "%.2f".format((traverseTimeToPeople + traverseTimeToShelter)).toDouble()
            }
            val closestShelter = dist.filterKeys { percept.spots[it]?.spotType === SpotType.SHELTER }.minBy { it.value }
            val distanceToClosestShelter = closestShelter?.value ?: Int.MAX_VALUE
            val traverseTimeToShelter = traverseTime(distanceToClosestShelter, percept.slowDown, state.nPeople)
            val canRescueMyPeople = traverseTimeToShelter <= state.time

            var unrescued = pathsWeight.filterValues { it > state.time }.map {
                percept.spots[it.key]?.amount ?: 0
            }.sum()
            unrescued += if (canRescueMyPeople) 0 else state.nPeople

            val edgeWeight = node.parent?.state?.vertex?.let { percept.graph.getEdge(it, state.vertex)!!.weight } ?: 0
            unrescued * 100 + edgeWeight
        }
    }

    data class State(
            val vertex: Long,
            val hasPeople: Set<Long>,
            val nPeople: Int,
            val time: Double
    )

    inner class HEProblem(val percept: HEPercept) : Problem<HEAction, State> {

        override val initialState: State

        init {
            val vertex = pos
            val hasPeople = percept.spots.filterValues { it.spotType === SpotType.PERSON }.keys
            val time = percept.deadline
            initialState = State(vertex, hasPeople, numOfPeople, time)
        }

        override fun nextStates(state: State): Collection<Pair<HEAction, State>> {
            return percept.graph.getNeighbours(state.vertex).map { edge ->
                val action = traverse(edge, percept)
                val vertex = edge.vTo
                val nPeople = percept.spots[vertex]?.let {
                    when (it.spotType) {
                        SpotType.SHELTER -> 0
                        SpotType.PERSON -> state.nPeople + (it.amount.takeIf { vertex in state.hasPeople } ?: 0)
                    }
                } ?: state.nPeople
                val hasPeople = state.hasPeople.toMutableSet().apply { remove(vertex) }
                val traverseTime = traverseTime(edge.weight, percept.slowDown, state.nPeople)
                val time = "%.2f".format((state.time - traverseTime)).toDouble()
                val nextState = State(vertex, hasPeople, nPeople, time)

                action to nextState
            }
        }

        override fun isGoal(state: State): Boolean {
            return state.time < 0 ||
                    (state.hasPeople.isEmpty() && state.nPeople == 0)
        }

    }
}

class GreedySearchAgent(agentId: Int, pos: Long) : HESearchAgent(agentId, pos) {
    override val type: AgentType
        get() = AgentType.GREEDYSEARCH

}

open class AStarSearchAgent(agentId: Int, pos: Long) : HESearchAgent(agentId, pos) {
    override val type: AgentType
        get() = AgentType.ASTARSEARCH

    override fun heuristicFunc(percept: HEPercept): Strategy<HEAction, State> {
        val h = super.heuristicFunc(percept)
        val g: Strategy<HEAction, State> = { node ->
            var currNode = node
            var weight = 0
            while (currNode.parent != null) {
                weight += percept.graph.getEdge(currNode.parent!!.state.vertex, currNode.state.vertex)!!.weight
                currNode = currNode.parent!!
            }
            weight
        }
        return { node -> g(node) + h(node) }
    }
}

class RTAStarSearchAgent(agentId: Int, pos: Long) : AStarSearchAgent(agentId, pos) {
    override val type: AgentType
        get() = AgentType.RTASTARSEARCH

    var nExpandsLimit: Int = Int.MAX_VALUE

    override fun doNextAction(percept: HEPercept): HEAction {
        if (movesSequence?.isEmpty() == true) {
            movesSequence = null
        }
        return super.doNextAction(percept)
    }

    override fun initMovesSequence(percept: HEPercept): LinkedList<Long> {
        val (goalNode, nExpands) = treeSearch(HEProblem(percept), heuristicFunc(percept), nExpandsLimit)!!
        this.nExpands += nExpands
        val list = LinkedList<Long>()
        var node = goalNode
        while (node.parent != null) {
            list.addFirst(node.state.vertex)
            node = node.parent!!
        }
        return list
    }
}
