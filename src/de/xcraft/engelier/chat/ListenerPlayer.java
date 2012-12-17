package de.xcraft.engelier.chat;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ListenerPlayer implements Listener {
	private Chat chat;
	
	public ListenerPlayer (Chat chat) {
		this.chat = chat;
	}
	
	@EventHandler
	public void onPlayerChat(AsyncPlayerChatEvent event) {
		if (event.getPlayer().hasPermission("chat.deny")) {
			event.getPlayer().sendMessage(chat.getConfig().getString("config.denymessage").replaceAll("&([a-f0-9])", "\u00A7$1"));
			event.setCancelled(true);
			return;
		}
		
		event.setFormat(chat.getChatFormat(event.getPlayer()));
	}
}
