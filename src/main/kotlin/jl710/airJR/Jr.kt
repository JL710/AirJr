package jl710.airJR

import kotlin.math.abs
import kotlin.math.min
import kotlin.random.Random
import org.bukkit.Location
import org.bukkit.util.Vector

data class Jr(val id: Int, val location1: Location, val location2: Location, val name: String) {

    companion object {
        private val possibleBlocks = nextBlockOffsets()
    }

    init {
        if (location1.world.uid != location2.world.uid) {
            throw IllegalArgumentException("location1 and location2 have different worlds!")
        }
    }

    fun nextBlock(blocks: List<Vector>): Vector {
        if (blocks.count() == 1) {
            return blocks.first().clone().setX(blocks.first().x + 3)
        }

        for (offset in possibleBlocks.shuffled()) {
            val newBlock = blocks.last().clone().add(offset)
            if (!inBounds(newBlock)) {
                continue
            }
            if (blocks.find { x -> x.x == newBlock.x && x.z == newBlock.z } != null) {
                continue
            }
            if (minAngle(
                            blocks.last().clone().subtract(newBlock),
                            blocks.dropLast(1).last().clone().subtract(newBlock)
                    ) < 0.26
            ) {
                continue
            }
            if (blocks.dropLast(1).last().distance(newBlock) < 3) {
                continue
            }
            return newBlock
        }
        throw RuntimeException("Could not find a good next block")
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
        if (vec.x <= xRange.min() || vec.x > xRange.max()) {
            return false
        }
        if (vec.y <= yRange.min() || vec.y > yRange.max()) {
            return false
        }
        if (vec.z <= zRange.min() || vec.z > zRange.max()) {
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
    val randomNumber =
            min(start, end) +
                    Random.nextInt(rangeLength + 1) // +1 because the end should be included
    return randomNumber
}

private fun nextBlockOffsets(): MutableList<Vector> {
    val offsets = mutableListOf<Vector>()

    val single_corner =
            listOf(
                    // Straight lines
                    Vector(2, 0, 0),
                    Vector(3, 0, 0),
                    Vector(4, 0, 0),
                    Vector(2, 1, 0),
                    Vector(3, 1, 0),
                    Vector(4, 1, 0),
                    Vector(2, -1, 0),
                    Vector(3, -1, 0),
                    Vector(4, -1, 0),
                    Vector(2, -2, 0),
                    Vector(3, -2, 0),
                    Vector(4, -2, 0),
                    // diagonal
                    Vector(2, 0, 2),
                    Vector(2, 1, 2),
                    Vector(2, -1, 2),
                    Vector(2, -2, 2),
                    Vector(3, 0, 2),
                    // other diagonals
                    Vector(3, 1, 2),
                    Vector(3, -1, 2),
                    Vector(3, -2, 2),
                    Vector(2, 0, 3),
                    Vector(2, 1, 3),
                    Vector(2, -1, 3),
                    Vector(2, -2, 3),
            )
    for (v in single_corner) {
        offsets.add(Vector(v.x * -1, v.y, v.z))
        offsets.add(Vector(v.z, v.y, v.x))
        offsets.add(Vector(v.z, v.y, v.x * -1))
    }
    offsets.addAll(single_corner)

    return offsets
}
