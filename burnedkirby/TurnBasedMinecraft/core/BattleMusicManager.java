package burnedkirby.TurnBasedMinecraft.core;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Random;
import java.util.Vector;

import burnedkirby.TurnBasedMinecraft.ModMain;

import paulscode.sound.SoundSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundManager;

public class BattleMusicManager {
	
	protected static final String battleMusicFolder = ModMain.modFolder + "/BattleMusic";

	protected Random random;
	
	protected Vector<File> musicFiles = null;
	protected boolean battleMusicPlaying;
	protected Thread faderThread;
	
	public BattleMusicManager()
	{
		battleMusicPlaying = false;
		faderThread = null;
		
		File musicFolder = new File(battleMusicFolder);

		if(!musicFolder.exists())
		{
			if(!musicFolder.mkdir())
			{
				new IOException("Failed to create battle music directory.").printStackTrace();
				return;
			}
			try {
				FileWriter readme = new FileWriter(battleMusicFolder + "/README");
				readme.write("This folder and this file was auto-generated by the TurnBasedMinecraft mod!" +
						"\nPlace .ogg or .wav files in here to be used as battle music!" +
						"\n\nIf there are no music files here, then the mod will not halt any music playing by Minecraft.");
				readme.close();
			} catch(IOException e) {}
		}
		
		File[] musicFiles = musicFolder.listFiles(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				int ext;
				if((ext = name.lastIndexOf(".")) == -1)
					return false;
				String extension = name.substring(ext + 1);
				if(extension.equals("ogg") || extension.equals("wav"))
					return true;
				return false;
			}
		});
		
		if(musicFiles.length == 0)
			this.musicFiles = null;
		else
		{
			this.musicFiles = new Vector<File>(musicFiles.length);
			for(File music : musicFiles)
				this.musicFiles.add(music);
		}
		
		random = new Random(System.currentTimeMillis());
	}
	
	public boolean isPlaying()
	{
		return battleMusicPlaying;
	}
	
	public void playRandomBattleMusic()
	{
		if(musicFiles != null && Minecraft.getMinecraft().gameSettings.musicVolume != 0.0f)
		{
			SoundManager mcSoundManager = Minecraft.getMinecraft().sndManager;
			mcSoundManager.sndSystem.stop("BgMusic");
			File randMusic = getRandomMusicFile();
			try {
				mcSoundManager.sndSystem.backgroundMusic("BgMusic", randMusic.toURI().toURL(), randMusic.getPath(), true);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			mcSoundManager.sndSystem.setVolume("BgMusic", Minecraft.getMinecraft().gameSettings.musicVolume);
			mcSoundManager.sndSystem.play("BgMusic");
			
			battleMusicPlaying = true;
		}
	}
	
	public void stopBattleMusic()
	{
		if(battleMusicPlaying && faderThread == null)
		{
			faderThread = new Thread(new BattleMusicFader());
			faderThread.start();
		}
	}
	
	private File getRandomMusicFile()
	{
		return musicFiles.get(random.nextInt(musicFiles.size()));
	}
	
	private class BattleMusicFader implements Runnable {

		@Override
		public void run() {
			float currentVol = Minecraft.getMinecraft().sndManager.sndSystem.getVolume("BgMusic");
			float interval = currentVol / 10.0f;
			for(int i=1; i <= 10; i++)
			{
				Minecraft.getMinecraft().sndManager.sndSystem.setVolume("BgMusic", currentVol - interval*i);
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {}
			}
			Minecraft.getMinecraft().sndManager.sndSystem.stop("BgMusic");
			faderThread = null;
			battleMusicPlaying = false;
		}
		
	}
}
