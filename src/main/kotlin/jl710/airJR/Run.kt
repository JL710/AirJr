package jl710.airJR

import java.util.UUID
import kotlin.math.abs
import org.bukkit.util.Vector

data class Run(
        val player: UUID,
        private val _blocks: MutableList<Vector>,
        var _alreadyJumped: Int,
        val jr: Jr
) {
    val blocks: List<Vector>
        get() = _blocks
    val alreadyJumped
        get() = _alreadyJumped

    fun outOfBounce(position: Vector): Boolean {
        val distances: MutableList<Double> = mutableListOf()

        for (block in _blocks) {
            distances.add(abs(block.distance(position)))
        }

        if (distances.max() > 10) {
            return true
        }
        return false
    }

    fun newBlocksNeeded(playerPosition: Vector): Boolean {
        val offset = Vector(0.5, 1.0, 0.5)
        if (listOf(
                                playerPosition.distance(blocks[1].clone().add(offset)),
                                playerPosition.distance(blocks[2].clone().add(offset))
                        )
                        .min() <= 0.25
        ) {
            return true
        }
        return false
    }

    fun nextBlock() {
        _blocks.removeFirst()
        _blocks.add(jr.nextBlock(blocks))
        _alreadyJumped++
    }
}
