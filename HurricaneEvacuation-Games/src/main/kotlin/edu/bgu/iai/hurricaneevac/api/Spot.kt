package edu.bgu.iai.hurricaneevac.api

import edu.bgu.iai.hurricaneevac.api.SpotType.PERSON
import edu.bgu.iai.hurricaneevac.api.SpotType.SHELTER

enum class SpotType {
    PERSON, SHELTER
}

data class Spot(val spotType: SpotType, val amount: Int) {

    override fun toString(): String {
        return when(spotType) {
            PERSON -> when(amount) {
                1 -> "1 Person"
                else -> "$amount People"
            }
            SHELTER -> "Shelter"
        }
    }
}
