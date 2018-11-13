package edu.bgu.iai.hurricaneevac.api

import edu.bgu.iai.common.graph.MutableWeightedGraph
import edu.bgu.iai.common.graph.WEdge
import edu.bgu.iai.common.graph.WeightedGraph
import edu.bgu.iai.common.simulator.SimulatorWorld
import edu.bgu.iai.hurricaneevac.agent.HEAgent
import java.lang.Double.max

class BWEdge<V> : WEdge<V> {
    constructor(vFrom: V, vTo: V, weight: Int, isBlocked: Boolean = false) :
            super(vFrom, vTo, weight) {
        this.isBlocked = isBlocked
    }

    constructor(other: BWEdge<V>) : super(other) {
        this.isBlocked = other.isBlocked
    }

    val isBlocked: Boolean

    override fun attrs(): String = super.attrs() + if (isBlocked) ":B" else ""
}


typealias HEAction = (HESimulatorConfiguration) -> Unit


interface HEPercept {
    val graph: WeightedGraph<Long, BWEdge<Long>>
    val spots: Map<Long, Spot>
    val nVertices: Long
    val deadline: Double
    val slowDown: Double
}


class HESimulatorConfiguration(
        override val graph: MutableWeightedGraph<Long, BWEdge<Long>>,
        override val spots: MutableMap<Long, Spot>,
        override val nVertices: Long,
        override val slowDown: Double,
        deadline: Double
) : SimulatorWorld<HEPercept, HEAction, HEAgent>, HEPercept {

    override var deadline: Double = deadline
        set(value) { field = max(0.0, "%.2f".format(value).toDouble()) }

    override fun init() {
        println()
    }

    override fun end(): Boolean {
        return deadline == 0.0
    }

    override fun percept(agent: HEAgent): HEPercept {
        return this
    }

    override fun simulate(action: HEAction) {
        action(this)
    }

    override fun printState() {
        fun printWorld() {
            for (vId in graph.vertices) {
                val spot = spots[vId]?.toString() ?: "None"
                val neighbours = graph.getNeighbours(vId)
                println("#V$vId - $spot : $neighbours")
            }
        }
        println("""
            Number of Vertices: $nVertices
            Slow-down constant: $slowDown

            Time to end of simulation: $deadline units

        """.trimIndent())
        printWorld()
    }

}