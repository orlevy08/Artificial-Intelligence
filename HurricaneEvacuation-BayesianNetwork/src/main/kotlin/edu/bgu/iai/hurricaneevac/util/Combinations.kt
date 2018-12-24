package edu.bgu.iai.hurricaneevac.util

fun allPossibleCombinations(n: Int): List<BooleanArray> {
    fun allPossibleCombinations(n: Int, combination: BooleanArray): List<BooleanArray> {
        if(combination.size == n) {
            return listOf(combination)
        }
        return listOf(
                    allPossibleCombinations(n, combination+false),
                    allPossibleCombinations(n, combination+true)
                ).flatten()
    }
    return allPossibleCombinations(n, booleanArrayOf())
}