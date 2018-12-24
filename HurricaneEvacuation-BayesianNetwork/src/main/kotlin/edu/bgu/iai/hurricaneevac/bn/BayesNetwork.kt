package edu.bgu.iai.hurricaneevac.bn

import edu.bgu.iai.common.graph.Edge
import edu.bgu.iai.common.graph.MutableDirectedGraph
import edu.bgu.iai.hurricaneevac.util.allPossibleCombinations

class CPT<ID>(vars: Array<ID>) {
    val vars = vars
    private val table: MutableMap<String, Double> = mutableMapOf()

    init {
        allPossibleCombinations(vars.size).map { calcKey(it) }.forEach { key -> table[key] = 0.0 }
    }

    fun updateConditionalProb(prob: Double, vararg given: Boolean) {
        val key = calcKey(given)
        table.computeIfPresent(key) { _, _ -> "%.3f".format(prob).toDouble() }
    }

    fun getConditionalProb(vararg given: Boolean): Double? {
        val key = calcKey(given)
        return table[key]
    }

    private fun calcKey(given: BooleanArray): String {
        return given.map { if (it) "T" else "F" }.joinToString()
    }
}

data class BNNode<ID>(
        val id: ID,
        val cpt: CPT<ID>
)

data class Evidence<ID>(
        val nodeId: ID,
        val value: Boolean
)

class BayesNetwork<ID> {
    private val bNetwork: MutableDirectedGraph<ID, Edge<ID>> = MutableDirectedGraph()
    private val complementBN: MutableDirectedGraph<ID, Edge<ID>> = MutableDirectedGraph()
    private val nodes: MutableMap<ID, BNNode<ID>> = mutableMapOf()

    fun addNode(node: BNNode<ID>, vararg parents: ID) {
        nodes[node.id] = node
        bNetwork.addVertexIfNotExists(node.id)
        complementBN.addVertexIfNotExists(node.id)
        parents.forEach { parent ->
            bNetwork.addOrModifyEdge(Edge(parent, node.id))
            complementBN.addOrModifyEdge(Edge(node.id, parent))
        }
    }

    fun getNode(nodeId: ID): BNNode<ID>? {
        return nodes[nodeId]
    }

    private fun getAllNodeIds(): List<ID> {
        return bNetwork.vertices.toList()
    }

    fun enumerationAsk(target: ID, evidence: List<Evidence<ID>>): Double {
        val evidenceByTarget = evidence.filter { it.nodeId == target }
        if (evidenceByTarget.isEmpty()) {
            val unnormalizedProbForTrue = enumerationAll(getAllNodeIds(), evidence + Evidence(target, true))
            val unnormalizedProbForFalse = enumerationAll(getAllNodeIds(), evidence + Evidence(target, false))
            return "%.3f".format(unnormalizedProbForTrue / (unnormalizedProbForTrue + unnormalizedProbForFalse)).toDouble()
        }
        val targetEvidence = evidenceByTarget[0]
        return if(targetEvidence.value) 1.0 else 0.0
    }

    private fun enumerationAll(vars: List<ID>, evidence: List<Evidence<ID>>): Double {
        if (vars.isEmpty()) return 1.0
        val nodeId = vars[0]
        val evidenceByNodeId = evidence.filter { it.nodeId == nodeId }

        val node = getNode(nodeId)!!
        val parents = node.cpt.vars
        val combination: BooleanArray = parents.map { parent -> evidence.filter { it.nodeId == parent }[0].value }.toBooleanArray()
        val probForTrue = node.cpt.getConditionalProb(*combination)!!

        if(evidenceByNodeId.isNotEmpty()) {
            val nodeEvidence = evidenceByNodeId[0]
            val prob = if(nodeEvidence.value) probForTrue else complementProb(probForTrue)
            return prob * enumerationAll(vars.subList(1,vars.size), evidence)
        }
        var totalProb = 0.0
        for(b in listOf(true, false)) {
            val prob = if(b) probForTrue else complementProb(probForTrue)
            totalProb += prob * enumerationAll(vars.subList(1, vars.size), evidence+Evidence(nodeId, b))
        }
        return totalProb
    }
}



