package edu.bgu.iai.common.graph

open class DirectedGraph<V, E : Edge<V>>(vertices: Set<V>, edges: Collection<E>) : Graph<V, E> {
    protected val graph: MutableMap<V, MutableMap<V, E>> = mutableMapOf()

    override val vertices: Set<V>
        get() = graph.keys.toSet()

    override val edges: Collection<E>
        get() = graph.values.flatMap { it.values }

    init {
        vertices.forEach { addVertex(it) }
        edges.forEach { addEdge(it) }
    }

    protected fun addVertex(vId: V) {
        graph.putIfAbsent(vId, mutableMapOf())
    }

    protected fun addEdge(edge: E) {
        graph[edge.vFrom]?.takeIf { graph.containsKey(edge.vTo) }?.put(edge.vTo, edge)
    }

    override fun getNeighbours(vId: V): Collection<E> {
        return getNeighboursInternal(vId).values.toList()
    }

    private fun getNeighboursInternal(vId: V): Map<V, E> {
        return graph[vId] ?: mapOf()
    }

    override fun getEdge(vFrom: V, vTo: V): E? {
        return getNeighboursInternal(vFrom)[vTo]
    }
}


open class MutableDirectedGraph<V, E : Edge<V>>(vertices: Set<V>, edges: Collection<E>) :
                                    DirectedGraph<V, E>(vertices, edges), MutableGraph<V, E> {
    constructor() : this(setOf(), listOf())

    override fun addVertexIfNotExists(vId: V) {
        addVertex(vId)
    }

    override fun addOrModifyEdge(edge: E) {
        addEdge(edge)
    }

    override fun removeEdgeIfExists(vFrom: V, vTo: V) {
        graph[vFrom]?.remove(vTo)
    }
}


class DirectedWeightedGraph<V, E : WEdge<V>>(vertices: Set<V>, edges: Collection<E>) :
                                    DirectedGraph<V, E>(vertices, edges), WeightedGraph<V, E>


class MutableDirectedWeightedGraph<V, E : WEdge<V>>(vertices: Set<V>, edges: Collection<E>) :
                                    MutableDirectedGraph<V, E>(vertices, edges), MutableWeightedGraph<V, E> {
    constructor() : this(setOf(), listOf())
}