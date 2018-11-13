//package edu.bgu.iai.hurricaneevac
//
//import edu.bgu.iai.common.graph.Edge
//import edu.bgu.iai.common.graph.WEdge
//import edu.bgu.iai.hurricaneevac.model.*
//import edu.bgu.iai.hurricaneevac.agent.AIAgent.*
//import edu.bgu.iai.hurricaneevac.simulator.HurricaneEvacuationAISimulator
//import edu.bgu.iai.hurricaneevac.simulator.HurricaneEvacuationSimulator
//import edu.bgu.iai.hurricaneevac.util.loadSimulatorConfiguration
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.Ignore
//import java.io.*
//import kotlin.test.Test
//
//
//class Test {
//
//    @Test
//    fun `create graph from config`() {
//        val byteInputStream = ByteArrayInputStream("0.1".toByteArray())
//        System.setIn(byteInputStream)
//        val path = "src/test/resources/SimulatorTest.txt"
//        val simulator = HurricaneEvacuationSimulator(loadSimulatorConfiguration(path))
//        assertThat(simulator.simConfig.deadline).isEqualTo(7.2)
//        assertThat(simulator.simConfig.slowDown).isEqualTo(0.1)
//        assertThat(simulator.simConfig.nVertices).isEqualTo(4)
//        assertThat(simulator.simConfig.graph.getNeighbours(1))
//                .extracting("vTo")
//                .containsExactlyInAnyOrder(2L, 3L)
//    }
//
//    @Test
//    @Ignore
//    fun `create opposite edge`() {
//        val edge = WEdge(1, 2, 3)
//        val opposite = Edge.opposite(edge)
//        println("edge: $edge")
//        println("opposite: $opposite")
//    }
//
//    @Test
//fun `search tree`() {
//        val byteInputStream = ByteArrayInputStream("0.1".toByteArray())
//        System.setIn(byteInputStream)
//        val path = "src/test/resources/SimulatorTest.txt"
//        val simulator = HurricaneEvacuationAISimulator(loadSimulatorConfiguration(path))
//        val agent = GreedySearchAgent(simulator, 1, 1L)
//        println(movesSequence(simulator, agent))
//    }
//}
