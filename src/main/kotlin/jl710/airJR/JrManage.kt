package jl710.airJR

import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class JrManage(private val dao: JRDao): SubPlugin, CommandExecutor {
    override fun commands(): Set<String> {
        return setOf("jr_create", "jr_list", "jr_delete")
    }

    override fun onEnable() {}

    override fun onDisable() {}

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        when (command.name) {
            "jr_create" -> {
                if (args == null || args.count() != 7) {
                    sender.sendMessage("You did not provide the correct arguments.")
                    return false
                }
                val name = args[0]
                // check if name already exists
                val jrNames = dao.getJrs().map { x -> x.name }
                if (name in jrNames) {
                    sender.sendMessage("A Jump and run with that name already exists")
                    return true
                }
                try {
                    val x1 = args[1].toDouble()
                    val y1 = args[2].toDouble()
                    val z1 = args[3].toDouble()
                    val x2 = args[4].toDouble()
                    val y2 = args[5].toDouble()
                    val z2 = args[6].toDouble()
                    val world = (sender as Player).world
                    val jr = dao.createJr(Location(world, x1, y1, z1), Location(world, x2, y2, z2), name)
                } catch (_: NumberFormatException) {
                    sender.sendMessage("The arguments of the command could not be parsed")
                    return false
                }
                sender.sendMessage("Jump and Run created!")
            }
            "jr_list" -> {
                val jrs = dao.getJrs()
                val message = StringBuilder("List of Jump and Runs:")
                for (jr in jrs) {
                    with(jr) {
                        message.append("\n$name -> ${location1.x.toInt()} ${location1.y.toInt()} ${location1.z.toInt()} to ${location2.x.toInt()} ${location2.y.toInt()} ${location2.z.toInt()}")
                    }
                }
                sender.sendMessage(message.toString())
            }
            "jr_delete" -> {
                if (args == null || args.count() != 1) {
                    sender.sendMessage("Invalid arguments")
                    return false
                }
                val jrs = dao.getJrs()
                for (j in jrs) {
                    if (j.name == args[0]) {
                        if (!dao.deleteJr(j)) {
                            sender.sendMessage("Could not delete jump and run because of database issue.")
                        } else {
                            sender.sendMessage("Deleted jump and run ${j.name} successfully.")
                        }
                        return true
                    }
                }
                return true
            }
            "jr_start" -> {
                if (args == null || args.count() != 1) {
                    sender.sendMessage("Invalid arguments.")
                    return false
                }
                // TODO: to starting of j & r
            }
            else -> {
                sender.sendMessage("The Command could not be executed.")
            }
        }
        return true
    }
}