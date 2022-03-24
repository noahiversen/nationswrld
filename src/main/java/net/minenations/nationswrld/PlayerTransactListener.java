package net.minenations.nationswrld;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.nftworlds.wallet.event.PlayerTransactEvent;

import net.md_5.bungee.api.ChatColor;

public class PlayerTransactListener implements Listener {

	@EventHandler
	public void onPlayerTransact(PlayerTransactEvent<?> event) {
		event.getPlayer().sendMessage(ChatColor.GREEN + "You received " + event.getAmount() + "$WRLD");
	}
}
