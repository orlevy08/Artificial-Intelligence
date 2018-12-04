package edu.bgu.iai.hurricaneevac.graph

import edu.bgu.iai.common.graph.WEdge
import edu.bgu.iai.common.graph.WeightedGraph
import java.util.*

fun <V, E : WEdge<V>> dijkstraShortestPath(graph: WeightedGraph<V, E>, vSrc: V,
                                           edgeFilter: (E) -> Boolean = { true }): DijkstraResult<V> {

    val vertices = graph.vertices

    val dist: MutableMap<V, Int> = mutableMapOf(vSrc to 0)
    vertices.forEach { v ->
        if (v != vSrc) {
            dist[v] = Int.MAX_VALUE
        }
    }

    val backtrack: MutableMap<V, V> = mutableMapOf()

    val priorityQueue = PriorityQueue<V>(compareBy { dist[it]!! }).apply { add(vSrc) }

    val finalized: MutableMap<V, Boolean> = mutableMapOf()
    vertices.forEach { finalized[it] = false }

    fun relax(edge: E) {
        val newDist: Int = dist[edge.vFrom]!! + edge.weight
        if (newDist < dist[edge.vTo]!!) {
            dist[edge.vTo] = newDist
            backtrack[edge.vTo] = edge.vFrom
            priorityQueue.add(edge.vTo)
        }
    }

    while (priorityQueue.isNotEmpty()) {
        priorityQueue.poll().takeIf { !finalized[it]!! }?.also { vCurrent ->
            finalized[vCurrent] = true
            val neighbours = graph.getNeighbours(vCurrent)
            neighbours.filter(edgeFilter).forEach { edge -> relax(edge) }
        }

    }

    return DijkstraResult(dist.apply { remove(vSrc) }, backtrack)
}

data class DijkstraResult<V>(val dist: Map<V, Int>, val backtrack: Map<V, V>)