package jl710.airJR

import kotlin.collections.shuffle
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.util.Vector

class JrPlay(private val dao: JRDao) : SubPlugin, CommandExecutor {
    override fun commands(): Set<String> {
        return setOf("jr_start")
    }

    override fun onEnable() {}

    override fun onDisable() {}

    override fun onCommand(
            sender: CommandSender,
            command: Command,
            label: String,
            args: Array<out String>?
    ): Boolean {
        when (command.name) {
            "jr_start" -> {
                if (args == null || args.count() != 1) {
                    sender.sendMessage("Invalid arguments.")
                    return false
                }
                val jr = dao.getJr(args[0])
                if (jr == null) {
                    sender.sendMessage("Jump and Run '${args[0]}' does not exist")
                    return false
                }
                val first_blocks = jr.getFirstBlocks()
                val run =
                        dao.createRun(
                                (sender as Player).identity().uuid().toString(),
                                first_blocks,
                                jr.id
                        )

                sender.teleport(
                        Location(
                                jr.location1.world,
                                first_blocks[0].x,
                                first_blocks[0].y + 2,
                                first_blocks[0].z
                        )
                )

                placeBlocks(run)
            }
            else -> {
                sender.sendMessage("The Command could not be executed.")
            }
        }
        return true
    }

    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        val run = dao.getPlayerRun(event.player.identity().uuid().toString())
        if (run != null) {
            dao.deleteRun(run.player.toString())
            deleteBlocks(run)
        }
    }

    @EventHandler
    fun onPlayerMoveEvent(event: PlayerMoveEvent) {
        val run = dao.getPlayerRun(event.player.identity().uuid().toString()) ?: return
        if (run.outOfBounce(Vector(event.player.x, event.player.y, event.player.z))) {
            dao.deleteRun(event.player.identity().uuid().toString())
            deleteBlocks(run)
            return
        }
        if (run.newBlocksNeeded(Vector(event.player.x, event.player.y, event.player.z))) {
            setBlock(event.player.world, run.blocks.first(), "minecraft:air")
            run.nextBlock()
            setRandomBlock(event.player.world, run.blocks.last())
            dao.updateRun(run)
        }
    }
}

fun placeBlocks(run: Run) {
    for (block in run.blocks) {
        setRandomBlock(run.jr.location1.world, block)
    }
}

fun deleteBlocks(run: Run) {
    for (block in run.blocks) {
        setBlock(run.jr.location1.world, block, "minecraft:air")
    }
}

fun setRandomBlock(world: World, position: Vector) {
    val materials =
            mutableListOf(
                    "minecraft:terracotta",
                    "minecraft:white_terracotta",
                    "minecraft:orange_terracotta",
                    "minecraft:magenta_terracotta",
                    "minecraft:light_blue_terracotta",
                    "minecraft:yellow_terracotta",
                    "minecraft:lime_terracotta",
                    "minecraft:pink_terracotta",
                    "minecraft:gray_terracotta",
                    "minecraft:light_gray_terracotta",
                    "minecraft:cyan_terracotta",
                    "minecraft:purple_terracotta",
                    "minecraft:blue_terracotta",
                    "minecraft:brown_terracotta",
                    "minecraft:green_terracotta",
                    "minecraft:red_terracotta",
                    "minecraft:black_terracotta"
            )
    materials.shuffle()
    world.setBlockData(
            position.x.toInt(),
            position.y.toInt(),
            position.z.toInt(),
            Bukkit.createBlockData(materials.first())
    )
}

fun setBlock(world: World, position: Vector, block: String) {
    world.setBlockData(
            position.x.toInt(),
            position.y.toInt(),
            position.z.toInt(),
            Bukkit.createBlockData(block)
    )
}
