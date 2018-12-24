package edu.bgu.iai.common.graph

open class UndirectedGraph<V, E: Edge<V>>(vertices: Set<V>, edges: Collection<E>) : Graph<V, E> {
    protected val graph: MutableDirectedGraph<V, E> = MutableDirectedGraph()

    override val vertices: Set<V>
        get() = graph.vertices

    override val edges: Collection<E>
        get() = graph.edges

    init {
        vertices.forEach { graph.addVertexIfNotExists(it) }
        edges.forEach { addEdge(it) }
    }

    protected fun addEdge(edge: E) {
        val oppositeEdge = Edge.opposite(edge)
        graph.addOrModifyEdge(edge)
        graph.addOrModifyEdge(oppositeEdge)
    }

    override fun getNeighbours(vId: V): Collection<E> {
        return graph.getNeighbours(vId)
    }

    override fun getEdge(vFrom: V, vTo: V): E? {
        return graph.getEdge(vFrom, vTo)
    }
}


open class MutableUndirectedGraph<V, E: Edge<V>>(vertices: Set<V>, edges: Collection<E>) :
                                    UndirectedGraph<V, E>(vertices, edges), MutableGraph<V, E> {
    constructor() : this(setOf(), listOf())

    override fun addVertexIfNotExists(vId: V) {
        graph.addVertexIfNotExists(vId)
    }

    override fun addOrModifyEdge(edge: E) {
        addEdge(edge)
    }

    override fun removeEdgeIfExists(vFrom: V, vTo: V) {
        graph.removeEdgeIfExists(vFrom = vFrom, vTo = vTo)
        graph.removeEdgeIfExists(vFrom = vTo, vTo = vFrom)
    }
}


class UndirectedWeightedGraph<V, E: WEdge<V>>(vertices: Set<V>, edges: Collection<E>) :
                                    UndirectedGraph<V, E>(vertices, edges), WeightedGraph<V, E>


class MutableUndirectedWeightedGraph<V, E: WEdge<V>>(vertices: Set<V>, edges: Collection<E>) :
                                    MutableUndirectedGraph<V, E>(vertices, edges), MutableWeightedGraph<V, E> {
    constructor() : this(setOf(), listOf())
}