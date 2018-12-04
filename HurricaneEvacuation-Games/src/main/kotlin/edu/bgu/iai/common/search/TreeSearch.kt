package edu.bgu.iai.common.search

import java.util.*

data class Node<A, S>(
        val state: S,
        val depth: Int,
        var price: Int = Int.MAX_VALUE,
        val action: A? = null,
        val parent: Node<A, S>? = null,
        val children: MutableList<Node<A, S>> = mutableListOf()
)

interface Problem<A, S> {
    val initialState: S

    fun nextStates(state: S): Collection<Pair<A, S>>

    fun isGoal(state: S): Boolean
}

typealias Strategy<A, S> = (Node<A, S>) -> Int

data class TreeSearchResult<A, S>(val node: Node<A, S>, val nExpands: Int)

fun <A, S> treeSearch(problem: Problem<A, S>, strategy: Strategy<A, S>, limit: Int = Int.MAX_VALUE): TreeSearchResult<A, S>? {
    val root = Node<A, S>(problem.initialState, 0)
    root.price = strategy(root)
    val fringe = PriorityQueue<Node<A, S>>(compareBy { it.price }).apply { add(root) }
    var nExpands = 0

    fun expandNode(node: Node<A, S>) {
        nExpands++
        val states = problem.nextStates(node.state)
        states.forEach {
            val action = it.first
            val state = it.second
            val childNode = Node(state, depth = node.depth + 1, action = action, parent = node)
            childNode.price = strategy(childNode)
            node.children += childNode
        }
    }

    while (fringe.isNotEmpty()) {
        val node = fringe.poll()
        if (problem.isGoal(node.state) || nExpands == limit) {
            return TreeSearchResult(node, nExpands)
        }
        expandNode(node)
        node.children.forEach { fringe.add(it) }
    }

    return null
}