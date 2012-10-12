package de.surmair.mcppd;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.Location;

public class MCppd extends JavaPlugin {
	private int writerTaskID;

	@Override
	public void onEnable(){
		writerTaskID = this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new PosLogThread(this), 60L, 200L);
	}

	@Override
	public void onDisable() {
		this.getServer().getScheduler().cancelTask(writerTaskID);
		writerTaskID = 0;
	}

	private class PosLogThread implements Runnable {
		private JavaPlugin parent;

		public PosLogThread(JavaPlugin parent) {
			this.parent = parent;
		}

		public void run() {
			Player players[] = parent.getServer().getOnlinePlayers();
			if(players == null || players.length  <= 0){
				return;
			}
			for(Player p : players) {
				Location l = p.getLocation();
				System.out.printf("%s: %.1f %.1f %.1f\n", p.getName(), l.getX(), l.getY(), l.getZ());
			}
		}
	}
}
