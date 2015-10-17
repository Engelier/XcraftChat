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
		getServer().getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				registerPermissions();
				
			}
		});
		
		Plugin permCheck;
		if ((permCheck = pm.getPlugin("Permissions")) != null) checkPermissionPlugin(permCheck);
		
		pm.registerEvents(playerListener, this);
		pm.registerEvents(pluginListener, this);
				
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
		if (cmd.getName().equalsIgnoreCase("xchat")) {
			if (sender instanceof Player) {
				if (!hasPermission((Player) sender, "XcraftChat.reload")) return true;
			}
		
			loadConfig();
			sender.sendMessage(ChatColor.DARK_PURPLE + getNameBrackets() + ChatColor.DARK_AQUA + "config reloaded.");
		}
		
		if (cmd.getName().equalsIgnoreCase("a")) {
			if (!sender.hasPermission("XcraftChat.admin"))
				return true;
						
			String joinedArgs = "";
			for (int i = 0; i < args.length; i++) {
				joinedArgs += args[i] + " ";
			}
			
			String chatFormat = ChatColor.RED + "[AdminChat] " + ChatColor.WHITE + getChatFormat(sender);
			String message = chatFormat.replace("%1$s", sender.getName());
			message = message.replace("%2$s", joinedArgs);
			getServer().getConsoleSender().sendMessage(message);
			for (Player player : getServer().getOnlinePlayers()) {
				if (player.hasPermission("XcraftChat.admin")) {
					player.sendMessage(message);
				}
			}
		}
		if (cmd.getName().equalsIgnoreCase("c")) {
			if (!sender.hasPermission("XcraftChat.team"))
				return true;
						
			String joinedArgs = "";
			for (int i = 0; i < args.length; i++) {
				joinedArgs += args[i] + " ";
			}
			
			String chatFormat = ChatColor.GREEN + "[TeamChat] " + ChatColor.WHITE + getChatFormat(sender);
			String message = chatFormat.replace("%1$s", sender.getName());
			message = message.replace("%2$s", joinedArgs);
			getServer().getConsoleSender().sendMessage(message);
			for (Player player : getServer().getOnlinePlayers()) {
				if (player.hasPermission("XcraftChat.team")) {
					player.sendMessage(message);
				}
			}
		}
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
	
	public String getChatFormat(CommandSender sender) {
		Map<String, String> values = new HashMap<String, String>();
		DateFormat dateFormat = new SimpleDateFormat(config.getString("config/timeformat", "HH:mm:ss"));
		
		if (sender instanceof Player) {
			values.put("world", ((Player) sender).getWorld().getName());
			values.put("health", "" + ((Player) sender).getHealth());
			values.put("xp", "" + ((Player) sender).getLevel());
		} else {
			values.put("world", "CONSOLE");
			values.put("health", "0");
			values.put("xp", "")";
		}
		
		values.put("playername", "%1$s");
		values.put("message", "%2$s");
		values.put("time", dateFormat.format(new Date()));
		
		for (String permission : config.getKeys("permissions")) {
			if (sender.hasPermission(permission)) {
				for (String var : config.getKeys("permissions/" + permission)) {
					values.put(var, config.getString("permissions/" + permission + "/" + var));
				}
				break;
			}
		}
		
		if (config.getNode("users/" + sender.getName()) != null) {
			for (String userTag : config.getKeys("users/" + sender.getName())) {
				values.put(userTag, config.getString("users/" + sender.getName() + "/" + userTag));
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
		
		chatFormat = ChatColor.translateAlternateColorCodes('&', chatFormat);
		
		return chatFormat;
	}
	
	public String getNameBrackets() {
		return "[" + getDescription().getFullName() + "] ";
	}
}
