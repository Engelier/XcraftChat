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
		event.setFormat(chat.getChatFormat(event.getPlayer()));
	}
}
