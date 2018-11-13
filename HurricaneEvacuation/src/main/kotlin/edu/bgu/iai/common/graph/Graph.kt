package edu.bgu.iai.common.graph

interface Graph<V, E : Edge<V>> {
    val vertices: Set<V>

    val edges: Collection<E>

    fun getNeighbours(vId: V): Collection<E>

    fun getEdge(vFrom: V, vTo: V): E?
}


interface MutableGraph<V, E : Edge<V>> : Graph<V, E> {
    fun addVertexIfNotExists(vId: V)

    fun addOrModifyEdge(edge: E)

    fun addAllEdges(edges: Collection<E>) {
        edges.forEach(this::addOrModifyEdge)
    }

    fun removeEdgeIfExists(vFrom: V, vTo: V)
}


open class Edge<V>(vFrom: V, vTo: V) {
    companion object {
        /*
         * requires copy constructor
         */
        fun <V, E : Edge<V>> opposite(edge: E): E {
            val dup = edge.javaClass.getConstructor(edge.javaClass).newInstance(edge)
            return dup.apply { reverse() }
        }
    }

    constructor(other: Edge<V>) : this(other.vFrom, other.vTo)

    var vFrom: V = vFrom
        private set

    var vTo: V = vTo
        private set

    private fun reverse() {
        val temp = this.vFrom
        this.vFrom = this.vTo
        this.vTo = temp
    }

    override fun toString(): String = "($vFrom-->$vTo${attrs()})"

    open fun attrs(): String = ""
}


interface WeightedGraph<V, E : WEdge<V>> : Graph<V, E>


interface MutableWeightedGraph<V, E : WEdge<V>> : WeightedGraph<V, E>, MutableGraph<V, E>


open class WEdge<V> : Edge<V> {
    constructor(vFrom: V, vTo: V, weight: Int) : super(vFrom, vTo) {
        this.weight = weight
    }

    constructor(other: WEdge<V>) : super(other) {
        this.weight = other.weight
    }

    val weight: Int

    override fun attrs(): String = ":W$weight"
}
