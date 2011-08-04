package de.xcraft.engelier.chat;

import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.event.server.ServerListener;

public class ListenerPlugin extends ServerListener {
	private Chat chat;
	
	public ListenerPlugin(Chat chat) {
		this.chat = chat;
	}
	
	public void onPluginEnable(PluginEnableEvent event) {
		if (event.getPlugin().getDescription().getName().equals("Permissions")) {
			chat.checkPermissionPlugin(event.getPlugin());
		}
	}
	
	public void onPluginDisable(PluginDisableEvent event) {
		if (event.getPlugin().getDescription().getName().equals("Permissions")) {
			chat.checkPermissionPlugin(event.getPlugin());
		}
	}
}
