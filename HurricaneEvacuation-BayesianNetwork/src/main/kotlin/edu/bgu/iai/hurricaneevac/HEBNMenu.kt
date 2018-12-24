package edu.bgu.iai.hurricaneevac

import edu.bgu.iai.common.graph.Graph
import edu.bgu.iai.common.graph.WEdge
import edu.bgu.iai.hurricaneevac.bn.Evidence
import edu.bgu.iai.hurricaneevac.bn.HEBayesNetwork
import edu.bgu.iai.hurricaneevac.bn.complementProb
import edu.bgu.iai.hurricaneevac.util.parseEvidence

class IWEdge<V> : WEdge<V> {
    val id: Long

    constructor(id: Long, vFrom: V, vTo: V, weight: Int) : super(vFrom, vTo, weight) {
        this.id = id
    }

    constructor(other: IWEdge<V>) : super(other) {
        this.id = other.id
    }

    override fun attrs(): String = "ID:$id," + super.attrs()
}

data class Path(val path: List<Long>)

fun allPossiblePaths(src: Int, dest: Int, graph: Graph<Long, IWEdge<Long>>): List<Path> {
    fun recPossiblePaths(src: Int, dest: Int, graph: Graph<Long, IWEdge<Long>>, currPath: List<Long>, isVisited: BooleanArray): List<Path> {
        if (src == dest) {
            return listOf(Path(currPath))
        }
        val paths = mutableListOf<Path>()
        graph.getNeighbours(src.toLong()).forEach { edge ->
            if (!isVisited[edge.vTo.toInt() - 1]) {
                paths.addAll(recPossiblePaths(edge.vTo.toInt(), dest, graph, currPath + edge.id, booleanArrayOf(*isVisited).apply { set(edge.vTo.toInt() - 1, true) }))
            }
        }
        return paths
    }

    val isVisited = BooleanArray(graph.vertices.size)
    return recPossiblePaths(src, dest, graph, mutableListOf(), isVisited.apply { set(src - 1, true) })
}


fun menu(bn: HEBayesNetwork, heGraph: Graph<Long, IWEdge<Long>>) {
    val evidenceList = mutableListOf<Evidence<String>>()
    var quit = false

    fun addEvidence() {
        println("Enter evidence to add: ")
        val evidence = parseEvidence(readLine() ?: "")
        evidenceList.add(evidence)
    }

    fun quit() {
        quit = true
    }

    fun calcProbReasoning() {
        fun calcProbOfPath(edgesIds: List<Long>): Double {
            var prob = 1.0
            val evidence = evidenceList.toMutableList()
            edgesIds.forEach { edgeId ->
                val uid = "B$edgeId"
                prob *= complementProb(bn.probReasoning(uid, evidence))
                if (evidence.filter { it.nodeId == uid }.isEmpty())
                    evidence += Evidence(uid, false)
            }
            return "%.3f".format(prob).toDouble()
        }

        (1L..bn.nVertices).forEach { vertexId ->
            println("P(Evacuees $vertexId) = ${bn.probReasoning("E$vertexId", evidenceList)}")
        }
        println()
        (1L..bn.nVertices).forEach { vertexId ->
            println("P(Flooding $vertexId) = ${bn.probReasoning("F$vertexId", evidenceList)}")
        }
        println()
        (1L..bn.nEdges).forEach { edgeId ->
            println("P(Blockage $edgeId) = ${bn.probReasoning("B$edgeId", evidenceList)}")
        }
        println()
        println("Enter set of edges: ")
        val edgesIds: List<Long> = (readLine() ?: "").split(" ").map { it.toLong() }
        println("P(not Blockage ${edgesIds.joinToString(", ")}) = ${calcProbOfPath(edgesIds)}")
        println()
        println("Enter source and goal vertices: ")
        val (src, dest) = (readLine() ?: "").split(" ").map { it.toInt() }
        val paths = allPossiblePaths(src, dest, heGraph)
        val maxProbPath = paths.maxBy { calcProbOfPath(it.path) }
        println("P(not Blockage ${maxProbPath?.path?.joinToString(", ")}) = ${calcProbOfPath(maxProbPath?.path
                ?: listOf())}")
    }

    val options: Map<Int, () -> Unit> = mapOf(
            1 to evidenceList::clear,
            2 to ::addEvidence,
            3 to ::calcProbReasoning,
            4 to ::quit
    )

    println("---------MENU---------")
    while (!quit) {
        println("""

            1) Reset evidence list
            2) Add evidence
            3) Do probabilistic reasoning
            4) Quit
        """.trimIndent())

        var option = readLine()?.toInt() ?: 4
        options[option]?.invoke()
    }
}

