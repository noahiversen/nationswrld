package net.minenations.nationswrld;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;

import com.nftworlds.wallet.objects.NFTPlayer;

public class PlayerLoginListener implements Listener {

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		new BukkitRunnable() {
			@Override
			public void run() {
				new NFTPlayer(event.getPlayer().getUniqueId());
				new BukkitRunnable() {
					@Override
					public void run() {
		                if (!player.isOnline()) return;
		                if(!NationsWRLD.getWalletAPI().getNFTPlayer(player).isLinked()) {
			                player.sendMessage(ChatColor.translateAlternateColorCodes('&', " \n&f&lIMPORTANT: &cYou do not have a wallet linked!\n&7Link your wallet at &a&nhttps://nftworlds.com/login&r\n "));
			                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
		                }
					}
				}.runTaskLater(NationsWRLD.getInstance(), 20);
			}
		}.runTaskAsynchronously(NationsWRLD.getInstance());
	}
}
