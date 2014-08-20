package burnedkirby.TurnBasedMinecraft.core;

import java.io.File;
import java.io.IOException;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class BattleMusicPlayer {
	private boolean constructorError = false;
	
	private AudioInputStream ais = null;
	
	private Clip clip = null;
	
	public BattleMusicPlayer()
	{

		try {
			clip = AudioSystem.getClip();
		} catch (LineUnavailableException e) {
			Utility.log("Unable to get clip");
			constructorError = true;
		}
		
	}
	
	public int loadWav(File wav)
	{
		if(constructorError)
			return -2;
		
		if(ais != null)
		{
			try {
				ais.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		try {
			ais = AudioSystem.getAudioInputStream(wav);
		} catch (UnsupportedAudioFileException e) {
			Utility.log("Tried to open an audio file of an unsupported format");
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		
		synchronized(clip)
		{
			try {
				clip.open(ais);
			} catch (LineUnavailableException e) {
				e.printStackTrace();
				return -1;
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		
		return 0;
	}
	
	public int playWav(boolean loop)
	{
		if(constructorError)
			return -2;
		
		synchronized(clip)
		{
			if(loop)
			{
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			}
			
			if(clip != null)
			{
				clip.start();
			}
		}
		
		return 0;
	}
	
	public int stop(boolean fade)
	{
		if(constructorError)
			return -2;
		
		synchronized(clip)
		{
			if(clip.isActive())
			{
				if(fade)
				{
					new FaderThread().start();
				}
				else
				{
					clip.stop();
					clip.close();
				}
			}
		}
		
		return 0;
	}
	
	public void destroy()
	{
		synchronized(clip)
		{
			clip.close();
		}
		
		try {
			ais.close();
			ais = null;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private class FaderThread extends Thread
	{
		@Override
		public void run() {
			if(clip != null)
			{
				synchronized(clip)
				{
					Control[] controls = clip.getControls();
					for(Control c : controls)
					{
						if(c.getType() == FloatControl.Type.VOLUME)
						{
							float current = ((FloatControl)c).getValue();
							while(current > 0.0f)
							{
								current -= 0.1f;
								if(current < 0.0f)
								{
									current = 0.0f;
								}
								((FloatControl)c).setValue(current);
								try {
									sleep(100);
								} catch (InterruptedException e) {}
							}
						}
					}
					
					clip.stop();
					clip.close();
				}
			}
		}
	}
}
