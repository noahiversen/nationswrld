package net.minenations.nationswrld;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import com.nftworlds.wallet.event.PlayerWalletReadyEvent;

import net.luckperms.api.model.data.DataMutateResult;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;

public class PlayerJoinListener implements Listener {

	@EventHandler
	public void onPlayerWalletReady(PlayerWalletReadyEvent event) {
		Player player = event.getPlayer();
		if(player.isOnline()) {
			new BukkitRunnable() {
				@Override
				public void run() {
					User user = NationsWRLD.getInstance().getLuckPermsAPI().getUserManager().getUser(player.getUniqueId());
					InheritanceNode node = InheritanceNode.builder("champion").value(true).build();
					if(NationsWRLD.getInstance().getLuckPermsAPI().getGroupManager().getGroup("champion") != null) {
						NationsWRLDAPI.doesOwnNFT(player, "0x5B97353bB450c2CC0D91C681bf6102166A7465C9").thenAcceptAsync(doesOwn -> {
							if(doesOwn) {
								if(!player.hasPermission("group.champion")) {
									DataMutateResult result = user.data().add(node);
									NationsWRLD.getInstance().getLogger().info(player.getName() + " Champion's Role: " + result.toString());
									NationsWRLD.getInstance().getLuckPermsAPI().getUserManager().saveUser(user);
								}
							}
							else {
								if(player.hasPermission("group.champion")) {
									DataMutateResult result = user.data().remove(node);
									NationsWRLD.getInstance().getLogger().info(player.getName() + " Champion's Role Removal: " + result.toString());
									NationsWRLD.getInstance().getLuckPermsAPI().getUserManager().saveUser(user);
								}
							}
						});
					}
				}
			}.runTaskAsynchronously(NationsWRLD.getInstance());
		}
	}
}
