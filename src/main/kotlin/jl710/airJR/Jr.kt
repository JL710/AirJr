package jl710.airJR

import org.bukkit.Location

data class Jr(val id: Int, val location1: Location, val location2: Location, val name: String) {
    init {
        if (location1.world.uid != location2.world.uid) {
            throw IllegalArgumentException("location1 and location2 have different worlds!")
        }
    }
}