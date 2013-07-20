package burnedkirby.TurnBasedMinecraft.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import com.google.common.io.Files;

import burnedkirby.TurnBasedMinecraft.CombatantInfo;
import burnedkirby.TurnBasedMinecraft.ModMain;

public class CommonProxy{
	
	public static final String rootElementName = "Settings";
	
	public static final String configVersionName = "ConfigVersion";
	
	public static final String configVersion = "1.0";
	
	public static final String battleDodgeCounterRatesName = "BattleDodgeCounterRates";
	
	public static final String battleMobIgnoreSystemName = "BattleMobIgnoreSystem";

	public static final Float default_evasionRate = 0.12f;
	
	public static final Float default_criticalRate = 0.08f;
	
	public static final Float default_onDodgeEvasionRate = 0.09f;
	
	public static final Float default_onCorrectDodgeEvasionRate = 0.35f;
	
	public static final Float default_counterRateAfterHit = 0.3f;
	
	public static final Float default_counterRateAfterMiss = 0.7f;
	
	public static final Float default_onCorrectDodgeHitBonus = 0.1f;
	
	public static final Float default_onCorrectDodgeCriticalBonus = 0.1f;
	
	public CommonProxy()
	{
	}

	public void newGui(int battleID, CombatantInfo player) {
	}
	
	public Object getGui()
	{
		return null;
	}
	
	public void setGui(Object gui)
	{}
	
	public void initializeMusicManager()
	{}
	
	public void playBattleMusic()
	{}
	
	public void stopBattleMusic()
	{}
	
	public void initializeSettings()
	{
		File modFolder = new File(ModMain.modFolder);
		
		if(!modFolder.exists())
		{
			modFolder.mkdir();
		}
		
		File theREADME = new File(ModMain.modFolder + "/README");
		
		if(!theREADME.exists())
		{
			try {
				FileWriter writer = new FileWriter(theREADME);
				writer.write("This folder was autogenerated by the TurnBasedMinecraft mod!\n\n" +
						"You will find config files in this particular directory.\n");
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		boolean newXMLRequired = false;
		boolean configVersionExists = false;
		
		File theXML = new File(ModMain.battleSettingsFile);
		
		if(theXML.exists())
		{
			FileInputStream fileInputStream = null;
			try {
				fileInputStream = new FileInputStream(theXML);
				XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(fileInputStream);
				while(xmlReader.hasNext())
				{
					xmlReader.next();
					if(xmlReader.isStartElement() && xmlReader.getLocalName().equals(configVersionName))
					{
						configVersionExists = true;
						if(!xmlReader.getAttributeValue(null, "Version").equals(configVersion))
						{
							newXMLRequired = true;
						}
						continue;
					}
				}
				xmlReader.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				newXMLRequired = true;
			} catch (FactoryConfigurationError e) {
				e.printStackTrace();
			}
			
			try {
				fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(!configVersionExists)
				newXMLRequired = true;
		}
		
		if(newXMLRequired)
		{
			Calendar currentTime = Calendar.getInstance();
			String time = currentTime.get(Calendar.YEAR) + "." + currentTime.get(Calendar.MONTH) + "." + currentTime.get(Calendar.DAY_OF_MONTH) + ".." + currentTime.get(Calendar.HOUR_OF_DAY) + "." + currentTime.get(Calendar.MINUTE);
			
			File oldCopy = null;
			oldCopy = new File(ModMain.battleSettingsFile + ".replaced." + time + ".xml");

			try {
				Files.move(theXML, oldCopy);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if(!theXML.exists())
		{
			FileOutputStream fileOutputStream = null;
			try {
				fileOutputStream = new FileOutputStream(theXML);
				XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
				
				xmlWriter.writeStartDocument();
				
				xmlWriter.writeStartElement(rootElementName);
				
				xmlWriter.writeStartElement(configVersionName);
				xmlWriter.writeAttribute("Version", configVersion);
				xmlWriter.writeEndElement();
				
				xmlWriter.writeComment("All values are percentages.\n" +
						"\nevasionRate: default evasion rate for a combatant." +
						"\ncriticalRate: default critical hit rate for a combatant." +
						"\nonDodgeEvasionRate: evasion rate when dodging an attack from a combatant that wasn't selected." +
						"\nonCorrectDodgeEvasionRate: evasion rate when dodging an attack from a combatant that was selected." +
						"\ncounterRateAfterHit: counter-attack rate after being hit by a selected combatant." +
						"\ncounterRateAfterMiss: counter-attack rate after dodging an attack by a selected combatant." +
						"\nonCorrectDodgeHitBonus: hit bonus that increases the chances of hitting a combatant on the next turn." +
						"\nonCorrectDodgeCriticalBonus: critical bonus that increases the chances of a critical hit on a combatant on the next turn.");
				xmlWriter.writeStartElement(battleDodgeCounterRatesName);
				xmlWriter.writeAttribute("evasionRate", default_evasionRate.toString());
				xmlWriter.writeAttribute("criticalRate", default_criticalRate.toString());
				xmlWriter.writeAttribute("onDodgeEvasionRate", default_onDodgeEvasionRate.toString());
				xmlWriter.writeAttribute("onCorrectDodgeEvasionRate", default_onCorrectDodgeEvasionRate.toString());
				xmlWriter.writeAttribute("counterRateAfterHit", default_counterRateAfterHit.toString());
				xmlWriter.writeAttribute("counterRateAfterMiss", default_counterRateAfterMiss.toString());
				xmlWriter.writeAttribute("onCorrectDodgeHitBonus", default_onCorrectDodgeHitBonus.toString());
				xmlWriter.writeAttribute("onCorrectDodgeCriticalBonus", default_onCorrectDodgeCriticalBonus.toString());
				xmlWriter.writeEndElement();
				
				xmlWriter.writeStartElement(battleMobIgnoreSystemName);
				xmlWriter.writeAttribute("Bat", "false");
				xmlWriter.writeAttribute("Chicken", "false");
				xmlWriter.writeAttribute("Cow", "false");
				xmlWriter.writeAttribute("Horse", "false");
				xmlWriter.writeAttribute("Mooshroom", "false");
				xmlWriter.writeAttribute("Ocelot", "false");
				xmlWriter.writeAttribute("Pig", "false");
				xmlWriter.writeAttribute("Sheep", "false");
				xmlWriter.writeAttribute("Squid", "false");
				xmlWriter.writeAttribute("Villager", "false");
				xmlWriter.writeAttribute("Wolf","false");
				xmlWriter.writeAttribute("Blaze", "false");
				xmlWriter.writeAttribute("CaveSpider", "false");
				xmlWriter.writeAttribute("Creeper", "true");
				xmlWriter.writeAttribute("Enderman", "false");
				xmlWriter.writeAttribute("Ghast", "true");
				xmlWriter.writeAttribute("GiantZombie", "false");
				xmlWriter.writeAttribute("IronGolem", "false");
				xmlWriter.writeAttribute("MagmaCube", "false");
				xmlWriter.writeAttribute("PigZombie", "false");
				xmlWriter.writeAttribute("SilverFish", "true");
				xmlWriter.writeAttribute("Skeleton", "false");
				xmlWriter.writeAttribute("Slime", "false");
				xmlWriter.writeAttribute("Snowman", "false");
				xmlWriter.writeAttribute("Spider", "false");
				xmlWriter.writeAttribute("Witch", "false");
				xmlWriter.writeAttribute("WitherSkeleton", "false");
				xmlWriter.writeAttribute("Zombie", "false");
				xmlWriter.writeAttribute("Dragon", "true");
				xmlWriter.writeAttribute("WitherBoss", "true");
				xmlWriter.writeEndElement();
				
				xmlWriter.writeEndElement();
				
				xmlWriter.writeEndDocument();
				xmlWriter.close();
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			} catch (FactoryConfigurationError e) {
				e.printStackTrace();
			}
			
			try {
				fileOutputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(theXML);
			XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(fileInputStream);

			//TODO rewrite this for possible other settings
			while(xmlReader.hasNext())
			{
				xmlReader.next();
				if(xmlReader.isStartElement() && xmlReader.getLocalName().equals(battleDodgeCounterRatesName))
				{
					CombatantInfo.evasionRate = Float.parseFloat(xmlReader.getAttributeValue(null, "evasionRate"));
					CombatantInfo.criticalRate = Float.parseFloat(xmlReader.getAttributeValue(null, "criticalRate"));
					CombatantInfo.onDodgeEvasionRate = Float.parseFloat(xmlReader.getAttributeValue(null, "onDodgeEvasionRate"));
					CombatantInfo.onCorrectDodgeEvasionRate = Float.parseFloat(xmlReader.getAttributeValue(null, "onCorrectDodgeEvasionRate"));
					CombatantInfo.counterRateAfterHit = Float.parseFloat(xmlReader.getAttributeValue(null, "counterRateAfterHit"));
					CombatantInfo.counterRateAfterMiss = Float.parseFloat(xmlReader.getAttributeValue(null, "counterRateAfterMiss"));
					CombatantInfo.onCorrectDodgeHitBonus = Float.parseFloat(xmlReader.getAttributeValue(null, "onCorrectDodgeHitBonus"));
					CombatantInfo.onCorrectDodgeCriticalBonus = Float.parseFloat(xmlReader.getAttributeValue(null, "onCorrectDodgeCriticalBonus"));
				}
				else if(xmlReader.isStartElement() && xmlReader.getLocalName().equals(battleMobIgnoreSystemName))
				{
					ModMain.bss.ignoreSystemEntityMap.put("Bat", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Bat")));
					ModMain.bss.ignoreSystemEntityMap.put("Chicken", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Chicken")));
					ModMain.bss.ignoreSystemEntityMap.put("Cow", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Cow")));
					ModMain.bss.ignoreSystemEntityMap.put("Horse", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Horse")));
					ModMain.bss.ignoreSystemEntityMap.put("Mooshroom", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Mooshroom")));
					ModMain.bss.ignoreSystemEntityMap.put("Ocelot", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Ocelot")));
					ModMain.bss.ignoreSystemEntityMap.put("Pig", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Pig")));
					ModMain.bss.ignoreSystemEntityMap.put("Sheep", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Sheep")));
					ModMain.bss.ignoreSystemEntityMap.put("Squid", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Squid")));
					ModMain.bss.ignoreSystemEntityMap.put("Villager", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Villager")));
					ModMain.bss.ignoreSystemEntityMap.put("Wolf", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Wolf")));
					ModMain.bss.ignoreSystemEntityMap.put("Blaze", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Blaze")));
					ModMain.bss.ignoreSystemEntityMap.put("CaveSpider", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "CaveSpider")));
					ModMain.bss.ignoreSystemEntityMap.put("Creeper", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Creeper")));
					ModMain.bss.ignoreSystemEntityMap.put("Enderman", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Enderman")));
					ModMain.bss.ignoreSystemEntityMap.put("Ghast", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Ghast")));
					ModMain.bss.ignoreSystemEntityMap.put("GiantZombie", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "GiantZombie")));
					ModMain.bss.ignoreSystemEntityMap.put("IronGolem", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "IronGolem")));
					ModMain.bss.ignoreSystemEntityMap.put("MagmaCube", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "MagmaCube")));
					ModMain.bss.ignoreSystemEntityMap.put("PigZombie", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "PigZombie")));
					ModMain.bss.ignoreSystemEntityMap.put("Silverfish", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Silverfish")));
					ModMain.bss.ignoreSystemEntityMap.put("Skeleton", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Skeleton")));
					ModMain.bss.ignoreSystemEntityMap.put("Slime", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Slime")));
					ModMain.bss.ignoreSystemEntityMap.put("Snowman", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Snowman")));
					ModMain.bss.ignoreSystemEntityMap.put("Spider", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Spider")));
					ModMain.bss.ignoreSystemEntityMap.put("Witch", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Witch")));
					ModMain.bss.ignoreSystemEntityMap.put("WitherSkeleton", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "WitherSkeleton")));
					ModMain.bss.ignoreSystemEntityMap.put("Zombie", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Zombie")));
					ModMain.bss.ignoreSystemEntityMap.put("Dragon", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "Dragon")));
					ModMain.bss.ignoreSystemEntityMap.put("WitherBoss", Boolean.parseBoolean(xmlReader.getAttributeValue(null, "WitherBoss")));
				}
			}
			
			xmlReader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
		
		try {
			fileInputStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
