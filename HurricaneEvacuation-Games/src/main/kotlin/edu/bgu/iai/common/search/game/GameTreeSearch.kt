package edu.bgu.iai.common.search.game

import kotlin.math.max
import kotlin.math.min

typealias EvalFun<S> = (S) -> Pair<Int,Int>

typealias ZeroSumEvalFun<S> = (S) -> Int

interface GameProblem<A, S> {
    val initialState: S

    fun nextStates(state: S, player: Int): Collection<Pair<A, S>>

    fun isTerminalState(state: S, depth: Int): Boolean
}

fun <A, S> miniMaxWithPruning(problem: GameProblem<A, S>, default: A, evaluationFun: ZeroSumEvalFun<S>): A {
    val initialState = problem.initialState
    if (problem.isTerminalState(initialState, 0)) return default
    return problem.nextStates(initialState, 0)
            .map { (action, state) -> action to minValueWithPruning(state, 1, Int.MIN_VALUE, Int.MAX_VALUE, problem, evaluationFun) }
            .maxBy { it.second }
            ?.first ?: default
}

fun <A, S> maxValueWithPruning(state: S, depth: Int, alpha: Int, beta: Int, problem: GameProblem<A, S>, evaluationFun: ZeroSumEvalFun<S>): Int {
    if (problem.isTerminalState(state, depth))
        return evaluationFun(state)
    var alpha = alpha
    var beta = beta
    var v = Int.MIN_VALUE
    problem.nextStates(state, 0).forEach { (_, nextState) ->
        v = max(v, minValueWithPruning(nextState, depth+1, alpha, beta, problem, evaluationFun))
        if(v >= beta) {
            return v
        }
        alpha = max(alpha, v)
    }
    return v
}

fun <A, S> minValueWithPruning(state: S, depth: Int, alpha: Int, beta: Int, problem: GameProblem<A, S>, evaluationFun: ZeroSumEvalFun<S>): Int {
    if (problem.isTerminalState(state, depth))
        return evaluationFun(state)
    var alpha = alpha
    var beta = beta
    var v = Int.MAX_VALUE
    problem.nextStates(state,1).forEach { (_, nextState) ->
        v = min(v, maxValueWithPruning(nextState, depth+1, alpha, beta, problem, evaluationFun))
        if(v <= alpha) {
            return v
        }
        beta = min(beta, v)
    }
    return v
}

fun <A, S> maxiMax(problem: GameProblem<A, S>, default: A, evaluationFun: EvalFun<S>): A {
    val initialState = problem.initialState
    if (problem.isTerminalState(initialState, 0)) return default
    return problem.nextStates(initialState, 0)
            .map { (action, state) -> action to maxValue(state, 1, 1, problem, evaluationFun) }
            .maxBy { it.second.first }
            ?.first ?: default
}

fun <A, S> maxValue(state: S, player: Int, depth: Int, problem: GameProblem<A, S>, evaluationFun: EvalFun<S>): Pair<Int, Int> {
    if (problem.isTerminalState(state, depth))
        return evaluationFun(state)
    return problem.nextStates(state, player).map { (_, nextState) ->
        maxValue(nextState, 1-player, depth+1, problem, evaluationFun)
    }
    .maxWith(compareBy({ if (player == 0) it.first else it.second },
                       { if (player == 0) it.second else it.first }))
    ?: Int.MIN_VALUE to Int.MIN_VALUE
}
