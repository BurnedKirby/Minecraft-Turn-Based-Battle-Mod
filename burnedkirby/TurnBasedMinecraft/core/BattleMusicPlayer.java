package burnedkirby.TurnBasedMinecraft.core;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Sequencer;
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
	
	private FileInputStream fis = null;
	
	private Sequencer sequencer = null;
	
	public BattleMusicPlayer()
	{

		try {
			clip = AudioSystem.getClip();
		} catch (LineUnavailableException e) {
			Utility.log("Unable to get clip");
			constructorError = true;
		}
		
		try {
			sequencer = MidiSystem.getSequencer();
			sequencer.open();
		} catch (MidiUnavailableException e) {
			Utility.log("Unable to get midi sequencer");
			constructorError = true;
		}
	}
	
	public int loadMidi(File midi)
	{
		if(constructorError)
			return -2;
		
		if(fis != null)
		{
			try {
				fis.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			fis = new FileInputStream(midi);
			sequencer.setSequence(new BufferedInputStream(fis));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return -1;
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
			return -1;
		}
		
		return 0;
	}
	
	public int playMidi(boolean loop)
	{
		if(constructorError)
			return -2;
		
		if(loop)
			sequencer.setLoopCount(Sequencer.LOOP_CONTINUOUSLY);
		else
			sequencer.setLoopCount(0);
		
		sequencer.start();
		
		return 0;
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
			else
			{
				clip.loop(0);
			}
			
			if(clip != null)
			{
				clip.start();
			}
		}
		
		return 0;
	}
	
	public int stopMidi(boolean fade)
	{
		if(constructorError)
			return -2;
		
		if(sequencer.isRunning())
		{
			sequencer.stop();
		}
		
		return 0;
	}
	
	public int stopWav(boolean fade)
	{
		if(constructorError)
			return -2;
		
		synchronized(clip)
		{
			if(clip.isActive())
			{
				/*
				if(fade)
				{
					new FaderThread().start();
				}
				else
				{
				*/
					clip.stop();
					clip.close();
				/*
				}
				*/
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
		
		sequencer.close();
		
		try {
			if(fis != null)
				fis.close();
			if(ais != null)
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
