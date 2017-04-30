package science.amberfall.dumbo

import org.bukkit.ChatColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.InputStreamReader
import java.net.URL
import java.util.*

class Dumbo : JavaPlugin(), Listener {

    override fun onEnable() {
        createConfig()
        server.pluginManager.registerEvents(this, this)

        if(server.version.contains("git-Spigot")) {
            logger.info("We highly recommend that you use Paper over Spigot, as they have been hostile to me personally and the development of this plugin. Paper offers many performance improvements and optimizations over Spigot. Please download and install Paper from https://paperci.emc.gs to use this plugin.".red())
            server.pluginManager.disablePlugin(this)
        }
    }

    private fun createConfig() {
        if(!dataFolder.exists()) {
            dataFolder.mkdirs()
        }
        val file = File(dataFolder, "config.yml")
        if(!file.exists()) {
            logger.info("Fetching quotes from GitHub...")

            val stream = URL("https://raw.githubusercontent.com/sweepyoface/dumbo/master/quotes.yml")
                    .openStream()
            val yaml = YamlConfiguration.loadConfiguration(InputStreamReader(stream))

            val colour = "&6"

            config.addDefault("color", colour)
            config.addDefault("quotes", yaml.getStringList("quotes"))

            config.options().copyDefaults(true)
            saveConfig()
            reloadConfig()

            logger.info("Done!")
        }
    }

    fun randomQuote(): String {
        val rand = SplittableRandom()
        val quotes = config.getStringList("quotes")
        val quote = rand.nextInt(quotes.size)

        return quotes[quote]
    }

    override fun onCommand(sender: CommandSender, cmd: Command, label: String, args: Array<String>): Boolean {
        if(sender is Player) {
            if(label.equals("dumbo", true)) {
                if(args.isEmpty()) {
                    if(sender.hasPermission("dumbo.quote") || sender.isOp) {
                        server.broadcastMessage(
                                ChatColor.translateAlternateColorCodes('&', config.getString("color") + randomQuote()))

                    }else if(args[0].equals("reload", true)) {
                        if(sender.hasPermission("dumbo.reload") || sender.isOp) {
                            this.reloadConfig()
                            sender.sendMessage("Dumbo config reloaded".green())
                        }
                    }else {
                        sender.sendMessage("Unknown argument. See /help dumbo".red())
                    }
                }
            }
        }
        return true
    }

    @EventHandler
    fun onChat(ev: AsyncPlayerChatEvent) {
        if(!ev.isCancelled) {
            val msgArr = ev.message.trim().split("\\s+".toRegex())
            if(msgArr[0].equals(".dumbo", true)) {
                val p = ev.player
                if(p.hasPermission("dumbo.quote") || p.isOp) {
                    server.broadcastMessage(
                            ChatColor.translateAlternateColorCodes('&', config.getString("color") + randomQuote())
                    )
                }else {
                    p.sendMessage("You do not have access to that command.".darkRed())
                }
            }
        }
    }
}

fun String.red(): String = "${ChatColor.RED}$this"
fun String.green(): String = "${ChatColor.GREEN}$this"
fun String.darkRed(): String = "${ChatColor.DARK_RED}$this"
