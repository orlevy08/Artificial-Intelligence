package edu.bgu.iai.hurricaneevac.bn

import edu.bgu.iai.hurricaneevac.util.HEBayesNetworkConfig
import edu.bgu.iai.hurricaneevac.util.VertexConfig
import edu.bgu.iai.hurricaneevac.util.allPossibleCombinations
import java.util.*

class HEBayesNetwork(bnConfig: HEBayesNetworkConfig) {

    enum class NodeType(id: String) {
        FLOOD("F"), BLOCKAGE("B"), EVACUEES("E");

        val id = id
    }

    private val bNetwork: BayesNetwork<String> = BayesNetwork()
    val nVertices: Long = bnConfig.nVertices
    val nEdges: Long = bnConfig.edgesConfig.size.toLong()

    init {
        addFloodNodes(bnConfig)
        addBlockageNodes(bnConfig)
        addEvacNodes(bnConfig)
    }

    private fun calcUID(nodeType: NodeType, graphElementId: Long): String {
        return nodeType.id + graphElementId
    }

    private fun addFloodNodes(bnConfig: HEBayesNetworkConfig) {
        val verticesIdsFromConfig = bnConfig.verticesConfig.map { it.id }
        val allVerticesIds = (1..nVertices).toMutableSet()
        val verticesConfigToAdd = allVerticesIds.apply { removeAll(verticesIdsFromConfig) }.map { VertexConfig(it, 0.0) }
        val verticesConfig = bnConfig.verticesConfig.toMutableList().apply { addAll(verticesConfigToAdd) }
        verticesConfig.forEach { vertexConfig ->
            val uid = calcUID(NodeType.FLOOD, vertexConfig.id)
            val cpt = CPT(arrayOf<String>()).apply { updateConditionalProb(vertexConfig.fProb) }
            bNetwork.addNode(BNNode(uid, cpt))
        }
    }

    private fun addBlockageNodes(bnConfig: HEBayesNetworkConfig) {
        bnConfig.edgesConfig.forEach { edgeConfig ->
            val uid = calcUID(NodeType.BLOCKAGE, edgeConfig.id)
            val vFromFloodNodeId = calcUID(NodeType.FLOOD, edgeConfig.vFrom)
            val vToFloodNodeId = calcUID(NodeType.FLOOD, edgeConfig.vTo)
            val parentIds = arrayOf(vFromFloodNodeId, vToFloodNodeId)
            val cpt = CPT(parentIds).apply {
                val qi = complementProb(0.6 * (1.0 / edgeConfig.weight))
                updateConditionalProb(0.001, false, false)
                updateConditionalProb(complementProb(qi), false, true)
                updateConditionalProb(complementProb(qi), true, false)
                updateConditionalProb(complementProb(qi * qi), true, true)
            }
            bNetwork.addNode(BNNode(uid, cpt), *parentIds)
        }
    }

    private fun addEvacNodes(bnConfig: HEBayesNetworkConfig) {
        (1..nVertices).forEach { vertexId ->
            val uid = calcUID(NodeType.EVACUEES, vertexId)
            val incidentEdges = bnConfig.edgesConfig.filter { vertexId in listOf(it.vFrom, it.vTo) }
            val parentIds = incidentEdges.map { calcUID(NodeType.BLOCKAGE, it.id) }.toTypedArray()
            val cpt = CPT(parentIds).apply {
                val combinations: List<BooleanArray> = allPossibleCombinations(vars.size)
                val qis: List<Double> = incidentEdges.map { if (it.weight > 4) 0.2 else 0.6 }
                combinations.forEach { combination ->
                    var multQi = combination.foldIndexed(1.0) { index, acc, b -> acc * if (b) qis[index] else 1.0 }
                    multQi = if(multQi == 1.0) 0.999 else multQi
                    updateConditionalProb(complementProb(multQi), *combination)
                }
            }
            bNetwork.addNode(BNNode(uid, cpt), *parentIds)
        }
    }

    fun probReasoning(nodeId: String, evidence: List<Evidence<String>>): Double{
        return bNetwork.enumerationAsk(nodeId, evidence)
    }

    fun print() {
        fun conditionalProbAsString(varName: String, cpt: CPT<String>): String {
            fun varAssignmentAsString(combination: BooleanArray): String {
                val stringJoiner = StringJoiner(", ")
                cpt.vars.forEachIndexed { index, variable ->
                    stringJoiner.add((if(combination[index]) "" else "not ") + when {
                            variable.startsWith("F") -> "Flooding " + variable.removePrefix("F")
                            variable.startsWith("B") -> "Blockage " + variable.removePrefix("B")
                            else -> ""
                        }
                    )
                }
                return stringJoiner.toString()
            }

            val strBuilder = StringBuilder()
            allPossibleCombinations(cpt.vars.size).forEach { combination ->
                strBuilder.append("\tP($varName | ${varAssignmentAsString(combination)}) = ${cpt.getConditionalProb(*combination)}\n")
            }

            allPossibleCombinations(cpt.vars.size).forEach { combination ->
                strBuilder.append("\tP(not $varName | ${varAssignmentAsString(combination)}) = ${complementProb(cpt.getConditionalProb(*combination)!!)}\n")
            }

            return strBuilder.toString()
        }

        (1..nVertices).forEach { vertexId ->
            val floodNode = bNetwork.getNode(calcUID(NodeType.FLOOD, vertexId))!!
            val evacNode = bNetwork.getNode(calcUID(NodeType.EVACUEES, vertexId))!!
            println("""
                VERTEX $vertexId:
                    P(Flooding $vertexId) = ${floodNode.cpt.getConditionalProb()}
                    P(not Flooding $vertexId) = ${complementProb(floodNode.cpt.getConditionalProb()!!)}

            """.trimIndent())
            println(conditionalProbAsString("Evacuees $vertexId", evacNode.cpt))
        }

        (1..nEdges).forEach { edgeId ->
            val blockageNode = bNetwork.getNode(calcUID(NodeType.BLOCKAGE, edgeId))!!
            println("EDGE $edgeId:")
            println(conditionalProbAsString("Blockage $edgeId", blockageNode.cpt))
        }

    }
}

fun complementProb(prob: Double): Double {
    return "%.3f".format((1.0 - prob)).toDouble()
}