package jl710.airJR

import org.bukkit.Location
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random

data class Jr(val id: Int, val location1: Location, val location2: Location, val name: String) {
    init {
        if (location1.world.uid != location2.world.uid) {
            throw IllegalArgumentException("location1 and location2 have different worlds!")
        }
    }

    fun nextBlock(blocks: List<Vector>): Vector {
        if (blocks.count() == 1) {
            return blocks.first().clone().setX(blocks.first().x + 3)
        }

        val newBlock = blocks.last().clone()
        while (
            !inBounds(newBlock) ||
            newBlock.y in blocks.map { x -> x.y }.toSet() || // check if blocks are under / over each other
            newBlock.distance(blocks.last()) < 1 ||
            newBlock.distance(blocks.last()) >= 5 ||
            newBlock.distance(blocks.first()) < 5 || // the blocks need to be somewhat far away from each other and not stacked up
            minAngle(blocks.last().clone().subtract(newBlock), blocks.dropLast(1).last().clone().subtract(newBlock)) < 0.26) { // 0.27 = 15°
            newBlock.x = blocks.last().x + randomInt(-4, 4).toDouble()
            newBlock.y = blocks.last().y + randomInt(-1, 1).toDouble()
            newBlock.z = blocks.last().z + randomInt(-4, 4).toDouble()
        }

        return newBlock
    }

    private fun minAngle(vec1: Vector, vec2: Vector): Float {
        val angle = listOf(vec1.angle(vec2), vec1.multiply(-1).angle(vec2)).min()
        if (angle.isNaN()) {
            return 0f
        }
        return angle
    }

    private fun inBounds(vec: Vector): Boolean {
        val xRange = listOf(location1.x, location2.x)
        val yRange = listOf(location1.y, location2.y)
        val zRange = listOf(location1.z, location2.z)
        if (vec.x <= xRange.min() || vec.x > xRange.max() ) {
            return false
        }
        if (vec.y <= yRange.min() || vec.y > yRange.max() ) {
            return false
        }
        if (vec.z <= zRange.min() || vec.z > zRange.max() ) {
            return false
        }
        return true
    }

    private fun getRandomFirstBlock(): Vector {
        return Vector(
            randomInt(location1.x.toInt(), location2.x.toInt()),
            randomInt(location1.y.toInt(), location2.y.toInt()),
            randomInt(location1.z.toInt(), location2.z.toInt())
        )
    }

    fun getFirstBlocks(): List<Vector> {
        val blocks = mutableListOf(getRandomFirstBlock())
        blocks.add(nextBlock(blocks))
        blocks.add(nextBlock(blocks))
        return blocks
    }
}

private fun randomInt(start: Int, end: Int): Int {
    val rangeLength = abs(start - end)
    val randomNumber = min(start, end) + Random.nextInt(rangeLength)
    return randomNumber
}