package burnedkirby.TurnBasedMinecraft.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import burnedkirby.TurnBasedMinecraft.CombatantInfo;
import burnedkirby.TurnBasedMinecraft.ModMain;

public class CommonProxy{

	public static final String battleSettingsElementName = "TurnBasedMinecraft_BattleSettings";

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
		
		File theXML = new File(ModMain.battleSettingsFile);
		
		if(!theXML.exists())
		{
			try {
				FileOutputStream fileOutputStream = new FileOutputStream(theXML);
				XMLStreamWriter xmlWriter = XMLOutputFactory.newInstance().createXMLStreamWriter(fileOutputStream);
				
				xmlWriter.writeStartDocument();
				
				xmlWriter.writeComment("\nAll values are percentages.\n" +
						"\nevasionRate: default evasion rate for a combatant." +
						"\ncriticalRate: default critical hit rate for a combatant." +
						"\nonDodgeEvasionRate: evasion rate when dodging an attack from a combatant that wasn't selected." +
						"\nonCorrectDodgeEvasionRate: evasion rate when dodging an attack from a combatant that was selected." +
						"\ncounterRateAfterHit: counter-attack rate after being hit by a combatant." +
						"\ncounterRateAfterMiss: counter-attack rate after dodging an attack by a combatant." +
						"\nonCorrectDodgeHitBonus: hit bonus that increases the chances of hitting a combatant on the next turn." +
						"\nonCorrectDodgeCriticalBonus: critical bonus that increases the chances of a critical hit on a combatant on the next turn.\n");
				xmlWriter.writeStartElement(battleSettingsElementName);
				xmlWriter.writeAttribute("\nevasionRate", default_evasionRate.toString());
				xmlWriter.writeAttribute("\ncriticalRate", default_criticalRate.toString());
				xmlWriter.writeAttribute("\nonDodgeEvasionRate", default_onDodgeEvasionRate.toString());
				xmlWriter.writeAttribute("\nonCorrectDodgeEvasionRate", default_onCorrectDodgeEvasionRate.toString());
				xmlWriter.writeAttribute("\ncounterRateAfterHit", default_counterRateAfterHit.toString());
				xmlWriter.writeAttribute("\ncounterRateAfterMiss", default_counterRateAfterMiss.toString());
				xmlWriter.writeAttribute("\nonCorrectDodgeHitBonus", default_onCorrectDodgeHitBonus.toString());
				xmlWriter.writeAttribute("\nonCorrectDodgeCriticalBonus", default_onCorrectDodgeCriticalBonus.toString());
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
		}
		
		try {
			FileInputStream fileInputStream = new FileInputStream(theXML);
			XMLStreamReader xmlReader = XMLInputFactory.newInstance().createXMLStreamReader(fileInputStream);

			//TODO rewrite this for possible other settings
			while(!xmlReader.isStartElement() || !xmlReader.getLocalName().equals(battleSettingsElementName))
			{
				xmlReader.next();
			}
			
			CombatantInfo.evasionRate = Float.parseFloat(xmlReader.getAttributeValue(null, "evasionRate"));
			CombatantInfo.criticalRate = Float.parseFloat(xmlReader.getAttributeValue(null, "criticalRate"));
			CombatantInfo.onDodgeEvasionRate = Float.parseFloat(xmlReader.getAttributeValue(null, "onDodgeEvasionRate"));
			CombatantInfo.onCorrectDodgeEvasionRate = Float.parseFloat(xmlReader.getAttributeValue(null, "onCorrectDodgeEvasionRate"));
			CombatantInfo.counterRateAfterHit = Float.parseFloat(xmlReader.getAttributeValue(null, "counterRateAfterHit"));
			CombatantInfo.counterRateAfterMiss = Float.parseFloat(xmlReader.getAttributeValue(null, "counterRateAfterMiss"));
			CombatantInfo.onCorrectDodgeHitBonus = Float.parseFloat(xmlReader.getAttributeValue(null, "onCorrectDodgeHitBonus"));
			CombatantInfo.onCorrectDodgeCriticalBonus = Float.parseFloat(xmlReader.getAttributeValue(null, "onCorrectDodgeCriticalBonus"));
			
			xmlReader.close();
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (FactoryConfigurationError e) {
			e.printStackTrace();
		}
	}
}
