package de.surmair.mcppd;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;

import com.google.gson.Gson;

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

	private class Entry {
		public String name;
		public double[] position;

		public Entry(Player p) {
			name = p.getName();
			position = new double[3];
			Location l = p.getLocation();
			position[0] = l.getX();
			position[1] = l.getY();
			position[2] = l.getZ();
		}
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

			Entry[] entries = new Entry[players.length];
			for(int i = 0; i < players.length; i++) {
				entries[i] = new Entry(players[i]);
			}

			Gson gson = new Gson();
			String json = gson.toJson(entries);
			System.out.println(json);
			// String keyid = parent.getConfig().getString("s3.access_key_id");
			// String key = parent.getConfig().getString("s3.secret_key");
			// AWSCredential awscred = new BasicAWSCredentials(keyid, key);
			// AmazonS3Client as3c = new AmazonS3Client(awscred);
			// String bucket = parent.getConfig().getString("s3.bucket_url");
		}
	}
}
