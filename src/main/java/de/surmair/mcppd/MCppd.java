package de.surmair.mcppd;

import java.io.StringBufferInputStream;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.Location;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;

import com.google.gson.Gson;

public class MCppd extends JavaPlugin {
	private int writerTaskID;
	private String access_key_id;
	private String secret_key;
	private String bucket;
	private String object_name;

	@Override
	public void onEnable(){
		// Create default configuration file, if there's none
		this.saveDefaultConfig();

		access_key_id = this.getConfig().getString("s3.access_key_id");
		secret_key = this.getConfig().getString("s3.secret_key");
		bucket = this.getConfig().getString("s3.bucket");
		object_name = this.getConfig().getString("s3.object_name");
		System.out.printf("%s %s %s %s", access_key_id, secret_key, bucket, object_name);

		writerTaskID = this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, new PosLogThread(this), 60L, this.getConfig().getLong("interval")*20L);
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


			AWSCredentials awscred = new BasicAWSCredentials(access_key_id, secret_key);
			AmazonS3Client as3c = new AmazonS3Client(awscred);
			try {
				as3c.putObject(bucket, object_name, new StringBufferInputStream(json), new com.amazonaws.services.s3.model.ObjectMetadata());
			} catch(AmazonClientException ae) {
				this.parent.getLogger().severe("Uploading failed: " + ae.getMessage());
			}
		}
	}
}
