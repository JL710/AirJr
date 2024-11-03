package jl710.airJR

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin

interface SubPlugin: CommandExecutor {
    fun onEnable()

    fun onDisable()

    fun commands(): Set<String>
}

class AirJR : JavaPlugin(), CommandExecutor {

    private val dao = JRDao("airJR.sqlite3")

    private val subPlugins: List<SubPlugin> = listOf(JrManage(dao))

    override fun onEnable() {
        for (subPlug in subPlugins) {
            subPlug.onEnable()
            for (cmd in subPlug.commands()) {
                getCommand(cmd)!!.setExecutor(subPlug)
            }
        }
        logger.info("Plugin AirJR Loaded")
    }

    override fun onDisable() {
        for (subPlug in subPlugins) {
            subPlug.onDisable()
        }
    }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        when (command.name) {
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
