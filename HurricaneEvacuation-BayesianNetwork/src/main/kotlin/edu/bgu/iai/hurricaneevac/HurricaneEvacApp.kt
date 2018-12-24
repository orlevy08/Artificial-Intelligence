package edu.bgu.iai.hurricaneevac

import edu.bgu.iai.common.graph.UndirectedWeightedGraph
import edu.bgu.iai.hurricaneevac.bn.HEBayesNetwork
import edu.bgu.iai.hurricaneevac.util.loadBayesNetworkConfiguration

fun main(args: Array<String>) {
    val bnConfig = loadBayesNetworkConfiguration("src/main/resources/hurricane_evacuation_BN_config.txt")
    val vertices = (1..bnConfig.nVertices).toSet()
    val edges = bnConfig.edgesConfig.map { IWEdge(it.id, it.vFrom, it.vTo, it.weight) }
    val graph = UndirectedWeightedGraph(vertices, edges)
    val bNetwork = HEBayesNetwork(bnConfig)
    bNetwork.print()
    menu(bNetwork, graph)
}



