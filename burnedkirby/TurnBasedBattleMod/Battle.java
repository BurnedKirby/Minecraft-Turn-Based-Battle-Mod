package burnedkirby.TurnBasedBattleMod;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import burnedkirby.TurnBasedBattleMod.core.BattlePhaseEndedPacket;
import burnedkirby.TurnBasedBattleMod.core.InitiateBattlePacket;
import burnedkirby.TurnBasedBattleMod.core.Utility;

import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.DamageSource;
import net.minecraft.world.MinecraftException;

public class Battle {
	private Vector<Integer> sideOne;
	private Vector<Integer> sideTwo;
	private int battleID;

	private Map<Integer,PlayerStatus> playerStatus;
	
	private boolean battleInProgress;
	private boolean playerPhase;
	
	protected static Boolean playerAttacking = false;
	
	public Battle(int battleID)
	{
		this.battleID = battleID;
		sideOne = new Vector<Integer>();
		sideTwo = new Vector<Integer>();
		playerStatus = new TreeMap<Integer,PlayerStatus>();
		
		battleInProgress = true;
		playerPhase = true;
	}
	
	public void addCombatant(int entityID, boolean sideOne) throws MinecraftException
	{
		if(sideOne)
			this.sideOne.add(entityID);
		else
			this.sideTwo.add(entityID);

		//EntityLiving combatant = (EntityLiving) Minecraft.getMinecraft().theWorld.getEntityByID(entityID);
		//System.out.println("Server has " + MinecraftServer.getServer().worldServers.length + " worlds."); //TODO debug
		//int index = -1;
		EntityLiving combatant = null;
		for(int i=0; i < MinecraftServer.getServer().worldServers.length; i++)
		{
			//System.out.println("Checking world " + i + ", has dimension " + MinecraftServer.getServer().worldServers[i].getWorldInfo().getDimension()); //TODO debug
//			if(MinecraftServer.getServer().worldServers[i].getWorldInfo().getDimension() == world)
//			{				
//				index = i;
//				break;
//			}
			combatant = (EntityLiving) MinecraftServer.getServer().worldServers[i].getEntityByID(entityID);
			if(combatant != null)
				break;
		}
		
		if(combatant == null)
			throw new MinecraftException("Combatant not found!!");
		
		//if(index == -1)
		//	throw new MinecraftException("World not found!!");
		
		//EntityLiving combatant = (EntityLiving) MinecraftServer.getServer().worldServers[index].getEntityByID(entityID);
				
		//TODO find a better way of getting the entity because this crashes the game
		
		if(!(combatant instanceof EntityPlayer))
		{
			combatant.clearActivePotions(); //TODO keep track of potion effects before clearing them
//			combatant.addPotionEffect(new PotionEffect(2, 2000000, 127, false));
			combatant.addPotionEffect(new PotionEffect(2, 2000000, 100, false));
			System.out.println("Set potion effect slow on " + combatant.getEntityName()); //TODO debug
			//TODO possibly find a way to keep mobs persistent
		}
		else
		{
			playerStatus.put(entityID, new PlayerStatus());
			PacketDispatcher.sendPacketToPlayer(new InitiateBattlePacket(battleID).makePacket(), (Player)combatant);
		}
	}
	
	public void removeCombatant(int entityID, boolean isPlayer)
	{
		if(sideOne.contains(entityID))
			sideOne.removeElement(entityID);
		else if(sideTwo.contains(entityID))
			sideTwo.removeElement(entityID);
		
		if(isPlayer)
		{
			playerStatus.remove(entityID);
		}
		
		if(playerStatus.isEmpty() || sideOne.isEmpty() || sideTwo.isEmpty())
			endBattle();
	}
	
	public int getID()
	{
		return battleID;
	}
	
	public void endBattle()
	{
		battleInProgress = false;
	}
	
	public Vector<Integer> getSideOne()
	{
		return sideOne;
	}
	
	public Vector<Integer> getSideTwo()
	{
		return sideTwo;
	}
	
	public boolean isBattleInProgress()
	{
		return battleInProgress;
	}
	
	public void updatePlayer(int ID, int type, int targetID)
	{
		synchronized(playerStatus)
		{
			if(!playerStatus.containsKey(ID))
				return;
		}
		
		PlayerStatus ps = new PlayerStatus(true,type,targetID);

		synchronized(playerStatus)
		{
			playerStatus.put(ID, ps);
		}
		
		battlePhase();
	}
	
	public Collection<Integer> getListOfPlayers()
	{
		return playerStatus.keySet();
	}
	
	private boolean allPlayersReady()
	{
		synchronized(playerStatus)
		{
			for(PlayerStatus ps : playerStatus.values())
				if(!ps.ready)
					return false;
		}
		return true;
	}
	
	public synchronized void battlePhase()
	{
		if(!allPlayersReady() || !battleInProgress)
			return;
		
		//TODO player and mob actions
		
		synchronized(playerStatus)
		{
			PlayerStatus ps = null;
			Set<Integer> keys = playerStatus.keySet();
			EntityPlayer player;
			Entity target;
			int damage;
			Collection<Integer> fleeing = new Vector<Integer>();
			
			//player attack phase
			for(Integer key : keys)
			{
				ps = playerStatus.get(key);
				player = (EntityPlayer) Utility.getEntityByID(key);
				target = Utility.getEntityByID(ps.target);
				if(target == null)
				{
					System.out.println("Player attempted to attack null entity!"); //TODO debug
					continue;
				}
				switch(ps.type)
				{
				case 1: //attack
					//TODO next line causes null pointer exception
					//damage = player.getItemInUse().getItem().getDamageVsEntity(target, player.getItemInUse());
					synchronized(playerAttacking){
						playerAttacking = true;
						System.out.println(player.getEntityName() + "is turn-based attacking" + target.getEntityName()); //TODO debug
						player.attackTargetEntityWithCurrentItem(target);
						playerAttacking = false;
					}
//					((EntityLiving)target).attackEntityFrom(DamageSource.causePlayerDamage(player), damage);
					break;
				case 2: //flee
					if(fleeCheck(key))
					{
						
					}
					break;
				default:
					break;
				}
			}
			
			//end turn phase
			for(Integer key : keys)
			{
				ps = playerStatus.get(key);
				ps.ready = false;
				playerStatus.put(key, ps);
				PacketDispatcher.sendPacketToPlayer(new BattlePhaseEndedPacket().makePacket(), (Player)Utility.getEntityByID(key));
			}
		}
	}
	
	private boolean fleeCheck(int entityID)
	{
		boolean sideOne = this.sideOne.contains(entityID);
		int enemySide = sideOne ? sideTwo.size() : this.sideOne.size();
		int ownSide = sideOne ? this.sideOne.size() : sideTwo.size();
		int diff;
		double rand;
		
		if(enemySide > ownSide)
		{
			diff = enemySide - ownSide;
			rand = (BattleSystemServer.random.nextDouble() + Math.log((double)(diff + 1)) )/ (double)(diff + 1) ;
			System.out.println("FleeCheck: Enemy side larger, rand is " + rand); //TODO debug
			return rand > 0.67d;
		}
		else if(enemySide == ownSide)
		{
			return BattleSystemServer.random.nextBoolean();
		}
		else
		{
			diff = ownSide - enemySide;
			rand = (BattleSystemServer.random.nextDouble() + Math.log((double)(diff + 1)) )/ (double)(diff + 1) ;
			System.out.println("FleeCheck: Player side larger, rand is " + rand); //TODO debug
			return rand < 0.67d;
		}
	}
}
