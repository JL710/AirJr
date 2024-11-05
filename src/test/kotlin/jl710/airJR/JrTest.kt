package jl710.airJR

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.util.Vector
import java.util.*

class JrTest {

    @Test
    fun nextBlock() {
        val world = mockk<World>()
        every { world.uid } returns UUID.randomUUID()
        val jr = Jr(0, Location(world, 1.0, 1.0, 1.0), Location(world, 20.0, 200.0, 20.0), "test")
        val blocks = mutableListOf(Vector(2, 5, 5), Vector(5, 5, 5), Vector(8, 5, 5))
        for (i in 0..1_000_000) {
            val nextBlock = jr.nextBlock(blocks) // check if an exception is thrown here
            blocks.removeFirst()
            blocks.add(nextBlock)
        }
    }
}