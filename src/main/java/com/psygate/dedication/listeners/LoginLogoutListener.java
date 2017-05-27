package com.psygate.dedication.listeners;

import com.psygate.dedication.Dedication;
import com.psygate.dedication.data.PlayerData;
import com.psygate.dedication.data.TimeTarget;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 *
 * @author psygate (https://github.com/psygate)
 */
public class LoginLogoutListener implements Listener {

	private Map<UUID, Long> loginTimes = new HashMap<>();
	private Map<UUID, Long> lastUpdateTimes = new HashMap<>();
	private final Map<UUID, Set<TimeTarget>> incrementOn = new HashMap<>();

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogin(PlayerJoinEvent ev) {
		PlayerData data = Dedication.initPlayer(ev.getPlayer().getUniqueId());
		if (data.isDedicated()) {
			ev.getPlayer().awardAchievement(Achievement.END_PORTAL);
			ev.getPlayer().sendMessage("You have earned dedicated status.");
		}
		loginTimes.put(ev.getPlayer().getUniqueId(), System.currentTimeMillis());
		lastUpdateTimes.put(ev.getPlayer().getUniqueId(), System.currentTimeMillis());
		data.getPlayerNames().add(ev.getPlayer().getName());
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLogout(PlayerQuitEvent ev) {
		onUpdate(ev.getPlayer().getUniqueId());

		Dedication.initPlayer(ev.getPlayer().getUniqueId()).setTimestamp(new Date(System.currentTimeMillis()));

		incrementOn.remove(ev.getPlayer().getUniqueId());
		loginTimes.remove(ev.getPlayer().getUniqueId());
		lastUpdateTimes.remove(ev.getPlayer().getUniqueId());
		Dedication.saveAndRemovePlayer(ev.getPlayer().getUniqueId());
	}

	public void updateAll() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			PlayerData data = Dedication.initPlayer(player.getUniqueId());
			if (data.isDedicated()) continue;
			
			onUpdate(player.getUniqueId());
			
			if (data.isDedicated()) {
				player.awardAchievement(Achievement.END_PORTAL);
				player.sendMessage("You have earned dedicated status.");
			}
		}
	}

	public void onUpdate(UUID player) {
		if (incrementOn.containsKey(player)) {
			for (TimeTarget tgt : incrementOn.get(player)) {
				if (tgt == null) {
					continue;
				}
				tgt.increment(System.currentTimeMillis() - lastUpdateTimes.get(player));
			}
		}

		lastUpdateTimes.put(player, System.currentTimeMillis());
	}

	public void addTarget(TimeTarget tgt) {
		incrementOn.putIfAbsent(tgt.getUUID(), new HashSet<TimeTarget>());
		incrementOn.get(tgt.getUUID()).add(tgt);
	}

	public void removeTarget(TimeTarget tgt) {
		incrementOn.remove(tgt.getUUID());
	}

	public void removeTarget(UUID uuid) {
		incrementOn.remove(uuid);
	}
}
