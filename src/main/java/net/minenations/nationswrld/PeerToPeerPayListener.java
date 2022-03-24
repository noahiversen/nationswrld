package net.minenations.nationswrld;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import com.nftworlds.wallet.event.PeerToPeerPayEvent;

import net.md_5.bungee.api.ChatColor;

public class PeerToPeerPayListener implements Listener {
	
	@EventHandler
	public void onPeerToPeerPay(PeerToPeerPayEvent event) {
		event.getTo().sendMessage(ChatColor.GREEN + "You received " + event.getAmount() + " $WRLD from " + event.getFrom().getName());
		event.getFrom().sendMessage(ChatColor.GREEN + "You sent " + event.getAmount() + " $WRLD to " + event.getTo().getName());
	}
}
