package edu.bgu.iai.hurricaneevac

import edu.bgu.iai.common.graph.MutableDirectedWeightedGraph
import edu.bgu.iai.common.graph.MutableWeightedGraph
import edu.bgu.iai.common.graph.WEdge
import edu.bgu.iai.hurricaneevac.graph.dijkstraShortestPath
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.entry
import kotlin.test.Test

class DijkstraTest {

    @Test
    fun testDijkstra() {
        val graph: MutableWeightedGraph<Long, WEdge<Long>> = MutableDirectedWeightedGraph(
                (1L..6L).toSet(),
                listOf(
                        WEdge(1L, 2L, 1),
                        WEdge(1L, 3L, 5),
                        WEdge(2L, 5L, 5),
                        WEdge(2L, 4L, 1),
                        WEdge(2L, 6L, 3),
                        WEdge(4L, 5L, 2),
                        WEdge(5L, 6L, 1)))
        val (dist, backtrack) = dijkstraShortestPath(graph, 1L)
        assertThat(dist).containsOnly(
                entry(2L, 1), entry(3L, 5), entry(4L, 2),
                entry(5L, 4), entry(6L, 4)
        )
        assertThat(backtrack).containsOnly(
                entry(2L, 1L), entry(3L, 1L), entry(4L, 2L),
                entry(5L, 4L), entry(6L, 2L)
        )
    }
}