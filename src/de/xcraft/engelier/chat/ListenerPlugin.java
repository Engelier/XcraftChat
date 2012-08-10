package de.xcraft.engelier.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;

public class ListenerPlugin implements Listener {
	private Chat chat;
	
	public ListenerPlugin(Chat chat) {
		this.chat = chat;
	}
	
	@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		if (event.getPlugin().getDescription().getName().equals("Permissions")) {
			chat.checkPermissionPlugin(event.getPlugin());
		}
	}
	
	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		if (event.getPlugin().getDescription().getName().equals("Permissions")) {
			chat.checkPermissionPlugin(event.getPlugin());
		}
	}
}
