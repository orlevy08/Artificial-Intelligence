package edu.bgu.iai

import edu.bgu.iai.hurricaneevac.util.allPossibleCombinations
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class CombinationsTest {

    @Test
    fun `test all possible combinations`() {
        assertThat(allPossibleCombinations(3)).containsOnly(
                booleanArrayOf(false, false, false),
                booleanArrayOf(false, false, true),
                booleanArrayOf(false, true, false),
                booleanArrayOf(false, true, true),
                booleanArrayOf(true, false, false),
                booleanArrayOf(true, false, true),
                booleanArrayOf(true, true, false),
                booleanArrayOf(true, true, true)
        )
    }
}
