package de.xcraft.engelier.chat;

import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerListener;

public class ListenerPlayer extends PlayerListener {
	private Chat chat;
	
	public ListenerPlayer (Chat chat) {
		this.chat = chat;
	}
	
	public void onPlayerChat(PlayerChatEvent event) {
		event.setFormat(chat.getChatFormat(event.getPlayer()));
	}
}
