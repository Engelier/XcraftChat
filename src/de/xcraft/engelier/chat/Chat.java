package de.xcraft.engelier.chat;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import com.nijiko.permissions.PermissionHandler;
import com.nijikokun.bukkit.Permissions.Permissions;

import de.xcraft.engelier.utils.config.Configuration;

public class Chat extends JavaPlugin {
	private final static Logger log = Logger.getLogger("Minecraft");
	private final ListenerPlayer playerListener = new ListenerPlayer(this);
	private final ListenerPlugin pluginListener = new ListenerPlugin(this);
	private PluginManager pm = null;
	private Configuration config = null;
	private Set<String> toReplace = new HashSet<String>();
	private PermissionHandler permHandler = null;
		
	@Override
	public void onDisable() {
		log.info(getNameBrackets() + "disabled.");
	}

	@Override
	public void onEnable() {
		pm = getServer().getPluginManager();
		
		loadConfig();
		registerPermissions();
		
		Plugin permCheck;
		if ((permCheck = pm.getPlugin("Permissions")) != null) checkPermissionPlugin(permCheck);
		
		pm.registerEvent(Event.Type.PLAYER_CHAT, playerListener, Event.Priority.Lowest, this);
		pm.registerEvent(Event.Type.PLUGIN_ENABLE, pluginListener, Event.Priority.Normal, this);
		pm.registerEvent(Event.Type.PLUGIN_DISABLE, pluginListener, Event.Priority.Normal, this);
		
		log.info(getNameBrackets() + "enabled.");
	}

	public void checkPermissionPlugin(Plugin plugin) {
		if (plugin.getDescription().getName().equals("Permissions")) {
			try {
				if (plugin.isEnabled()) {
					permHandler = ((Permissions) plugin).getHandler();
					log.info(getNameBrackets() + "hooked into " + plugin.getDescription().getFullName());
				} else {
					permHandler = null;
					log.info(getNameBrackets() + "lost permissions plugin - falling back to SuperPerms");
				}
			} catch (Exception ex) {log.severe(getNameBrackets() + "failed to hook into Permissions");}
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String cmdLabel, String[] args) {
		if (sender instanceof Player) {
			if (!hasPermission((Player) sender, "XcraftChat.reload")) return true;
		}
		
		loadConfig();
		sender.sendMessage(ChatColor.DARK_PURPLE + getNameBrackets() + ChatColor.DARK_AQUA + "config reloaded.");
		
		return true;
	}
	
	private void loadConfig() {
		File configFile = new File(this.getDataFolder(), "config.yml");
		if (!configFile.exists()) {
			try {
				this.getDataFolder().mkdirs();
				this.getDataFolder().setReadable(true);
				this.getDataFolder().setExecutable(true);
				configFile.createNewFile();
				configFile.setReadable(true);
				configFile.setWritable(true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		config = new Configuration(configFile, "/");
		config.load();
		
		if (config.getString("config/chatformat") == null) {
			config.setProperty("config/chatformat", "[$tag$] $playername$: $message$");
			config.setProperty("config/timeformat", "HH:mm:ss");
			config.setProperty("permissions/some.permission.node/tag", "&cX&9craft&aChat&f");
			config.setProperty("users/Engelier/tag", "&cX&9craft&aDev&f");
			config.save();
		}
				
		parseChatFormat(config.getString("config/chatformat", "<$playername$> $message$"));
	}
	
	private void registerPermissions() {
		for (String permission : config.getKeys("permissions")) {
			Permission thisPerm = new Permission(permission, PermissionDefault.FALSE);
			if (pm.getPermission(permission) == null)
				pm.addPermission(thisPerm);
		}
	}
	
	public Boolean hasPermission(Player player, String permission) {
		if (permHandler != null) {
			return permHandler.has(player, permission);
		} else {
			return player.hasPermission(permission);
		}
	}
	
	private void parseChatFormat(String chatFormat) {
		int i = 0;
		int d1 = 0;
		int d2 = 0;
		
		while (i < chatFormat.length()) {
			if ((d1 = chatFormat.indexOf("$", i)) == -1) {
				break;
			}
			d1++;
			
			if ((d2 = chatFormat.indexOf("$", d1)) == -1) {
				break;
			}
			
			String tag = chatFormat.substring(d1, d2);
			toReplace.add(tag);
			i = ++d2;
		}		
	}
	
	public String getChatFormat(Player player) {
		Map<String, String> values = new HashMap<String, String>();
		DateFormat dateFormat = new SimpleDateFormat(config.getString("config/timeformat", "HH:mm:ss"));
		
		values.put("world", player.getWorld().getName());
		values.put("playername", "%1$s");
		values.put("message", "%2$s");
		values.put("health", "" + player.getHealth());
		values.put("time", dateFormat.format(new Date()));
		
		for (String permission : config.getKeys("permissions")) {
			if (hasPermission(player, permission)) {
				for (String var : config.getKeys("permissions/" + permission)) {
					values.put(var, config.getString("permissions/" + permission + "/" + var));
				}
				break;
			}
		}
		
		if (config.getNode("users/" + player.getName()) != null) {
			for (String userTag : config.getKeys("users/" + player.getName())) {
				values.put(userTag, config.getString("users/" + player.getName() + "/" + userTag));
			}
		}
		
		String chatFormat = config.getString("config/chatformat", "<$playername$> $message$");
		
		for (String replaceMe : toReplace) {
			if (values.get(replaceMe) != null) {
				chatFormat = chatFormat.replace("$" + replaceMe + "$", values.get(replaceMe));
			} else {
				chatFormat = chatFormat.replace("$" + replaceMe + "$", "");				
			}
		}
		
		int i = 0;
		while ((i = chatFormat.indexOf("&")) > -1) {
			String colorCode = chatFormat.substring(i, i + 2);
			chatFormat = chatFormat.replace(colorCode, ChatColor.getByCode(Integer.parseInt(colorCode.substring(1), 16)).toString());
		}
		
		return chatFormat;
	}
	
	public String getNameBrackets() {
		return "[" + getDescription().getFullName() + "] ";
	}
}
