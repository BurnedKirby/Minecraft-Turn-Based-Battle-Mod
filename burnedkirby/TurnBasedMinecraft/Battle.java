package burnedkirby.TurnBasedMinecraft;

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import net.minecraft.entity.EntityCreature;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.scoreboard.ScorePlayerTeam;
import burnedkirby.TurnBasedMinecraft.CombatantInfo.Type;
import burnedkirby.TurnBasedMinecraft.core.network.BattleCombatantPacket;
import burnedkirby.TurnBasedMinecraft.core.network.BattleMessagePacket;
import burnedkirby.TurnBasedMinecraft.core.network.BattleStatusPacket;
import burnedkirby.TurnBasedMinecraft.core.network.CombatantHealthPacket;
import burnedkirby.TurnBasedMinecraft.core.network.InitiateBattlePacket;

public class Battle{
	private Map<Integer,CombatantInfo> combatants;
	private Stack<CombatantInfo> newCombatantQueue;
//	private Stack<CombatantInfo> removeCombatantQueue;
	private int battleID;

	protected enum BattleStatus {
		PLAYER_PHASE, CALCULATIONS_PHASE, END_CHECK_PHASE
	}
	
	private BattleStatus status;
	
//	private boolean phaseInProgress;
	
	protected boolean battleEnded;
	
	private short healthUpdateTick;
	private final short healthUpdateTime = 4;
	
	private final short turnTickTime = 30;
	
	public Battle(int id)
	{
		battleID = id;
	}
	
	public Battle(int id, Stack<CombatantInfo> newCombatants)
	{
		battleID = id;
		combatants = new TreeMap<Integer,CombatantInfo>();
		newCombatantQueue = new Stack<CombatantInfo>();
//		removeCombatantQueue = new Stack<CombatantInfo>();
		
		CombatantInfo combatant;
		while(!newCombatants.isEmpty())
		{
			combatant = newCombatants.pop();
			System.out.println("Initializing battle with combatant " + combatant.name);
			if(combatant.isPlayer)
			{
				ModMain.pp.sendTo(new InitiateBattlePacket(battleID,combatant), (EntityPlayerMP)combatant.entityReference);
				combatant.setTurnTickTimer(turnTickTime);
			}
			else if(isFightingEntity(combatant.entityReference))
			{
				combatant.type = Type.ATTACK;
			}
			else if(combatant.entityReference instanceof EntityAnimal)
			{
				combatant.type = Type.FLEE;
			}
			combatants.put(combatant.id, combatant);
			ModMain.bss.inBattle.add(combatant);
		}
		
		status = BattleStatus.PLAYER_PHASE;
		battleEnded = false;
//		phaseInProgress = false;
		healthUpdateTick = healthUpdateTime;
	}
	
	public void addCombatant(CombatantInfo newCombatant)
	{
		if(status == BattleStatus.PLAYER_PHASE)
		{
			if(isFightingEntity(newCombatant.entityReference))
			{
				newCombatant.type = Type.ATTACK;
			}
			
			combatants.put(newCombatant.id, newCombatant);
			ModMain.bss.inBattle.add(newCombatant);

			if(newCombatant.isPlayer)
			{
				ModMain.pp.sendTo(new InitiateBattlePacket(battleID,newCombatant), (EntityPlayerMP)newCombatant.entityReference);
				newCombatant.setTurnTickTimer(turnTickTime);
			}
			
			notifyPlayers(true);
			String name = ScorePlayerTeam.formatPlayerName(newCombatant.entityReference.worldObj.getScoreboard().getPlayersTeam(newCombatant.name), newCombatant.name);
			notifyPlayersWithMessage(name + " has entered battle!");
		}
		else
		{
			newCombatantQueue.push(newCombatant);
		}
	}
	
//	public void manageDeath(int id)
//	{
//		if(status == BattleStatus.PLAYER_PHASE)
//		{
//			CombatantInfo removed = combatants.remove(id);
//			
//			if(removed.isPlayer)
//				PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(false, false, combatants.size()).makePacket(), (Player)removed.entityReference);
//			
//			notifyPlayers(true);
//		}
//		else
//		{
//			removeCombatantQueue.add(combatants.get(id));
//		}
//	}
	
	public CombatantInfo getCombatant(int combatantID)
	{
		return combatants.get(combatantID);
	}
	
	public int getBattleID()
	{
		return battleID;
	}
	
	public void updatePlayerStatus(CombatantInfo combatant)
	{
		if(this.status != BattleStatus.PLAYER_PHASE || !combatants.containsValue(combatant))
		{
			System.out.println("WARNING: Battle " + battleID + " failed updatePlayerStatus."); //TODO debug
			return;
		}
		
		if(combatants.containsKey(combatant.id) && combatants.containsKey(combatant.target))
		{
			combatants.get(combatant.id).updateBattleInformation(combatant);
		}
		else
			notifyPlayers(false);
	}
	
	public boolean isInBattle(int id)
	{
		return combatants.containsKey(id);
	}
	
	public boolean isFightingEntity(EntityLivingBase entity)
	{
		return entity instanceof EntityMob
				|| entity instanceof EntityGolem
				|| entity instanceof EntityDragon
				|| entity instanceof EntitySlime
				|| entity instanceof EntityGhast;
	}
	
	private boolean getPlayersReady()
	{
		for(CombatantInfo combatant : combatants.values())
		{
			if(combatant.isPlayer && !combatant.ready)
				return false;
		}
		return true;
	}
	
	public synchronized boolean update()
	{
		if(!battleEnded)
			switch(status)
			{
			case PLAYER_PHASE:
				playerPhase();
				break;
			case CALCULATIONS_PHASE:
				calculationsPhase();
				break;
			case END_CHECK_PHASE:
				endCheckPhase();
				break;
			}
		else
		{
			notifyPlayers(false);
		}
		
		return battleEnded;
	}

	private void playerPhase()
	{
//		if(phaseInProgress)
//			return;
//		phaseInProgress = true;
		
		boolean forceUpdate = false;
		String name = "";
		if(!newCombatantQueue.isEmpty())
			forceUpdate = true;
		while(!newCombatantQueue.isEmpty())
		{
			CombatantInfo combatant = newCombatantQueue.pop();
			combatants.put(combatant.id, combatant);
			ModMain.bss.inBattle.add(combatant);
			name = ScorePlayerTeam.formatPlayerName(combatant.entityReference.worldObj.getScoreboard().getPlayersTeam(combatant.name), combatant.name);
			notifyPlayersWithMessage(name + " has entered battle!");
		}
		
		Iterator<CombatantInfo> iter = combatants.values().iterator();
		Stack<CombatantInfo> messageQueue = new Stack<CombatantInfo>();
		CombatantInfo combatant;
		while(iter.hasNext())
		{
			combatant = iter.next();
			if(!combatant.entityReference.isEntityAlive())
			{
				iter.remove();
				if(!combatant.isPlayer)
					messageQueue.push(combatant);
				
				combatant.removeEntityReference();
				combatant.setTarget(BattleSystemServer.exitCooldownTime);
				synchronized(ModMain.bss.inBattle) {
					ModMain.bss.inBattle.remove(combatant);
				}
				synchronized(ModMain.bss.exitedBattle) {
					ModMain.bss.exitedBattle.add(combatant);
				}
			}
			else if(combatant.isPlayer)
			{
				if(combatant.decrementTimer() <= 0)
				{
					//TODO end turn for player
					combatant.target = combatant.id;
					combatant.type = Type.DO_NOTHING;
					combatant.ready = true;
				}
			}
		}
		
		while(!messageQueue.isEmpty())
			notifyPlayersWithMessage(messageQueue.pop().name + " has died!");

		notifyPlayers(forceUpdate);
		
		checkIfBattleEnded();
		
		
		if(getPlayersReady())
		{
			status = BattleStatus.CALCULATIONS_PHASE;
			System.out.println("PlayerPhase ended.");
		}
		
//		phaseInProgress = false;
		
		if(--healthUpdateTick == 0)
		{
			notifyPlayersHealthInformation();
			healthUpdateTick = healthUpdateTime;
		}
	}
	
	private void calculationsPhase()
	{
//		if(phaseInProgress)
//			return;
//		phaseInProgress = true;
		
		//Combatant flee phase
		Iterator<CombatantInfo> iter = combatants.values().iterator();
		Stack<CombatantInfo> messageQueue = new Stack<CombatantInfo>();
		CombatantInfo combatant;
		while(iter.hasNext())
		{
			combatant = iter.next();
			if(combatant.type != Type.FLEE)
				continue;
			
			if(fleeCheck(combatant))
			{
				iter.remove();
				if(combatant.isPlayer)
					notifyPlayer(false, combatant, true);
				messageQueue.push(combatant);

				combatant.setTarget(BattleSystemServer.exitCooldownTime);
				synchronized(ModMain.bss.inBattle) {
					ModMain.bss.inBattle.remove(combatant);
				}
				synchronized(ModMain.bss.exitedBattle) {
					ModMain.bss.exitedBattle.add(combatant);
				}
			}
		}
		
		String name = "";
		String targetName = "";
		
		while(!messageQueue.isEmpty())
		{
			combatant = messageQueue.pop();
			name = ScorePlayerTeam.formatPlayerName(combatant.entityReference.worldObj.getScoreboard().getPlayersTeam(combatant.name), combatant.name);
			notifyPlayersWithMessage(name + " has fled battle!");
		}
		
		//Combatant attack phase
		EntityLivingBase combatantEntity = null;
		EntityLivingBase targetEntity = null;
		CombatantInfo[] combatantArray = combatants.values().toArray(new CombatantInfo[0]);
		int rand;
		for(int i=0; i < combatantArray.length; i++)
		{
			combatant = combatantArray[i];
			if(combatant.type != Type.ATTACK)
				continue;
			
			combatantEntity = combatant.entityReference;
			if(!combatantEntity.isEntityAlive())
				continue;
			
			synchronized(ModMain.bss.attackingLock)
			{
				if(combatant.isPlayer)
				{
					if(combatants.get(combatant.target) != null)
						targetEntity = combatants.get(combatant.target).entityReference;
					else
						targetEntity = null;

					if(targetEntity == null || !targetEntity.isEntityAlive() || !combatants.containsKey(targetEntity.func_145782_y()))
						continue;
					
					if(missCheck(combatant, combatants.get(combatant.target)))
					{
						name = ((EntityPlayer)combatantEntity).getDisplayName();
						name = ScorePlayerTeam.formatPlayerName(combatantEntity.worldObj.getScoreboard().getPlayersTeam(name), name);

						if((targetName = EntityList.getEntityString(targetEntity)) == null)
						{
							targetName = ((EntityPlayer)targetEntity).getDisplayName();
							targetName = ScorePlayerTeam.formatPlayerName(targetEntity.worldObj.getScoreboard().getPlayersTeam(targetName), targetName);
						}
						notifyPlayersWithMessage(name + " attacks " + targetName + " but missed!");
					}
					else
					{
						targetEntity.hurtResistantTime = 0;

						name = ((EntityPlayer)combatantEntity).getDisplayName();
						name = ScorePlayerTeam.formatPlayerName(combatantEntity.worldObj.getScoreboard().getPlayersTeam(name), name);

						if((targetName = EntityList.getEntityString(targetEntity)) == null)
						{
							targetName = ((EntityPlayer)targetEntity).getDisplayName();
							targetName = ScorePlayerTeam.formatPlayerName(targetEntity.worldObj.getScoreboard().getPlayersTeam(targetName), targetName);
						}
						
						if(criticalCheck(combatant))
						{
							combatantEntity.fallDistance = 0.1f;
							combatantEntity.onGround = false; //critical hit
							notifyPlayersWithMessage(name + " attacks " + targetName + " with a critical hit!!");
						}
						else
							notifyPlayersWithMessage(name + " attacks " + targetName + "!");
						ModMain.bss.attackingEntity = combatantEntity;
						((EntityPlayer)combatantEntity).attackTargetEntityWithCurrentItem(targetEntity);
						ModMain.bss.attackingEntity = null;
					}
				}
				else if(combatantEntity instanceof EntityMob || combatantEntity instanceof EntityGolem)
				{
					if(((EntityCreature)combatantEntity).getEntityToAttack() instanceof EntityLivingBase && combatants.containsKey(((EntityMob) combatantEntity).getEntityToAttack().func_145782_y()))
						targetEntity = (EntityLivingBase) ((EntityCreature)combatantEntity).getEntityToAttack();
					else
						targetEntity = null;
					

					if((targetName = EntityList.getEntityString(targetEntity)) == null)
						targetName = ((EntityPlayer)targetEntity).getDisplayName();
					
					System.out.println(EntityList.getEntityString(combatantEntity) + " targeting " + (targetEntity != null ? targetName : "null"));
					
					if(targetEntity == null)
					{
						rand = ModMain.bss.random.nextInt(combatants.size()) + combatants.size();
						int k=0,j=0,picked=0;
						boolean nonPlayersExist = true;
						for(; j<rand; k++)
						{
							if(combatantEntity instanceof EntityGolem && !combatantArray[k % combatantArray.length].isPlayer
									&& combatantArray[k % combatantArray.length].id != combatantEntity.func_145782_y() && nonPlayersExist)
							{
								j++;
								picked = k % combatantArray.length;
							}
							else if(combatantArray[k % combatantArray.length].isPlayer)
							{
								j++;
								picked = k % combatantArray.length;
							}
							if(k > combatants.size() && j == 0)
							{
								if(combatantEntity instanceof EntityGolem)
								{
									nonPlayersExist = false;
									k=0;
									j=0;
									continue;
								}
								picked = -1;
								break;
							}
						}
						if(picked == -1)
							continue;
						targetEntity = combatantArray[picked].entityReference;
					}
					
					if(missCheck(combatant, combatants.get(targetEntity.func_145782_y())))
					{
						name = EntityList.getEntityString(combatantEntity);
						
						if((targetName = EntityList.getEntityString(targetEntity)) == null)
						{
							targetName = ((EntityPlayer)targetEntity).getDisplayName();
							targetName = ScorePlayerTeam.formatPlayerName(targetEntity.worldObj.getScoreboard().getPlayersTeam(targetName), targetName);
						}
						
						notifyPlayersWithMessage(name + " attacks " + targetName + " but missed!");
					}
					else
					{
						targetEntity.hurtResistantTime = 0;
						
						name = EntityList.getEntityString(combatantEntity);
						
						if((targetName = EntityList.getEntityString(targetEntity)) == null)
						{
							targetName = ((EntityPlayer)targetEntity).getDisplayName();
							targetName = ScorePlayerTeam.formatPlayerName(targetEntity.worldObj.getScoreboard().getPlayersTeam(targetName), targetName);
						}
						
						notifyPlayersWithMessage(name + " attacks " + targetName + "!");
						ModMain.bss.attackingEntity = combatantEntity;
						(combatantEntity).attackEntityAsMob(targetEntity);
						ModMain.bss.attackingEntity = null;
					}
				}
				else if(combatantEntity instanceof EntitySlime)
				{
					targetEntity = ((EntitySlime)combatantEntity).worldObj.getClosestVulnerablePlayerToEntity(combatantEntity, 16.0);
					if(targetEntity == null || !combatants.containsKey(targetEntity.func_145782_y()))
					{

						rand = ModMain.bss.random.nextInt(combatants.size()) + combatants.size();
						int k=0,j=0,picked=0;
						for(; j<rand; k++)
						{
							if(combatantArray[k % combatantArray.length].isPlayer)
							{
								j++;
								picked = k % combatantArray.length;
							}
							if(k > combatants.size() && j == 0)
							{
								picked = -1;
								break;
							}
						}
						if(picked == -1)
							continue;
						targetEntity = combatantArray[picked].entityReference;
					}
					
					if(missCheck(combatant, combatants.get(targetEntity.func_145782_y())))
					{
						name = EntityList.getEntityString(combatantEntity);
						
						if((targetName = EntityList.getEntityString(targetEntity)) == null)
						{
							targetName = ((EntityPlayer)targetEntity).getDisplayName();
							targetName = ScorePlayerTeam.formatPlayerName(targetEntity.worldObj.getScoreboard().getPlayersTeam(targetName), targetName);
						}
						
						notifyPlayersWithMessage(name + " attacks " + targetName + " but missed!");
					}
					else
					{
						targetEntity.hurtResistantTime = 0;
						
						name = EntityList.getEntityString(combatantEntity);
						
						if((targetName = EntityList.getEntityString(targetEntity)) == null)
						{
							targetName = ((EntityPlayer)targetEntity).getDisplayName();
							targetName = ScorePlayerTeam.formatPlayerName(targetEntity.worldObj.getScoreboard().getPlayersTeam(targetName), targetName);
						}
						
						notifyPlayersWithMessage(name + " attacks " + targetName + "!");
						ModMain.bss.attackingEntity = combatantEntity;
						((EntitySlime)combatantEntity).onCollideWithPlayer((EntityPlayer)targetEntity);
						ModMain.bss.attackingEntity = null;
					}
				}
				else
					System.out.println("Else triggered");//TODO implement non-mob entities or don't enter battle with them???
				
				if(targetEntity.isEntityAlive() && counterCheck(combatants.get(targetEntity.func_145782_y())))
				{
					notifyPlayersWithMessage(targetName + " countered " + name + "!");
					targetEntity.hurtResistantTime = 0;
					ModMain.bss.attackingEntity = targetEntity;
					((EntityPlayer)targetEntity).attackTargetEntityWithCurrentItem(combatant.entityReference);
					ModMain.bss.attackingEntity = null;
				}
				
			}
		}
		
		status = BattleStatus.END_CHECK_PHASE;
		System.out.println("Calculations phase ended.");
		
//		phaseInProgress = false;
	}
	
	private void endCheckPhase()
	{
//		if(phaseInProgress)
//			return;
//		phaseInProgress = true;
		CombatantInfo combatantRef;
		Iterator<CombatantInfo> iter = combatants.values().iterator();
		CombatantInfo defaultInfo = new CombatantInfo();
		defaultInfo.ready = false;
		defaultInfo.type = Type.DO_NOTHING;
		Stack<CombatantInfo> messageQueue = new Stack<CombatantInfo>();
		
		while(iter.hasNext())
		{
			combatantRef = iter.next();
			if(!combatantRef.entityReference.isEntityAlive())
			{
				System.out.println("Entity is dead, removing");
				if(combatantRef.isPlayer)
					notifyPlayer(false, combatantRef, false);
				else
					messageQueue.push(combatantRef);
				
				iter.remove();

				combatantRef.removeEntityReference();
				combatantRef.setTarget(BattleSystemServer.exitCooldownTime);
				synchronized(ModMain.bss.inBattle) {
					ModMain.bss.inBattle.remove(combatantRef);
				}
				synchronized(ModMain.bss.exitedBattle) {
					ModMain.bss.exitedBattle.add(combatantRef);
				}
				
				continue;
			}

			if(combatantRef.isPlayer)
			{
				defaultInfo.target = combatantRef.target;
				defaultInfo.id = combatantRef.id;
				combatantRef.updateBattleInformation(defaultInfo);
				combatantRef.setTurnTickTimer(turnTickTime);
			}

			combatantRef.resetBonuses();
			if(combatantRef.counterSelectionSuccess)
			{
				combatantRef.criticalBonus += CombatantInfo.onCorrectDodgeCriticalBonus;
				combatantRef.hitBonus += CombatantInfo.onCorrectDodgeHitBonus;
				combatantRef.counterSelectionSuccess = false;
			}
		}
		
		while(!messageQueue.isEmpty())
		{
			notifyPlayersWithMessage(messageQueue.pop().name + " has died!");
		}
		
		checkIfBattleEnded();
		
//		notifyPlayersTurnEnded();
		
		status = BattleStatus.PLAYER_PHASE;
		System.out.println("End phase ended.");

		notifyPlayers(false);
		
//		phaseInProgress = false;
	}
	
	private void checkIfBattleEnded()
	{
		if(battleEnded)
			return;
		
		int players = 0;
		int sideOne = 0;
		int sideTwo = 0;
		for(CombatantInfo combatant : combatants.values())
		{
			if(combatant.isSideOne)
				sideOne++;
			else
				sideTwo++;
			
			if(combatant.isPlayer)
				players++;
		}
		
		if(sideOne == 0 || sideTwo == 0 || players == 0)
		{
			battleEnded = true;
			System.out.println("Battle " + battleID + " ended.");
			synchronized(ModMain.bss.inBattle) {
			synchronized(ModMain.bss.exitedBattle) {
				for(CombatantInfo combatant : combatants.values())
				{
					ModMain.bss.inBattle.remove(combatant);
					combatant.setTarget(BattleSystemServer.exitCooldownTime);
					ModMain.bss.exitedBattle.add(combatant);
				}
			}
			}
		}
	}
	
	private boolean fleeCheck(CombatantInfo fleeingCombatant)
	{
		int enemySide = 0;
		int ownSide = 0;
		int diff;
		double rand;

		if(enemySide == ownSide)
		{
			return BattleSystemServer.random.nextBoolean();
		}
		else
		{
			diff = Math.abs(ownSide - enemySide);
			rand = BattleSystemServer.random.nextDouble() / Math.log((double)(diff + Math.E));
			return ownSide > enemySide ? rand <= 0.67d : rand > 0.67d;
		}
	}
	
	private boolean missCheck(CombatantInfo attacker, CombatantInfo attacked)
	{
		if(attacked.type == Type.DODGE_COUNTER)
		{
			boolean miss = false;
			if(attacked.target == attacker.id)
			{
				attacked.counterSelectionSuccess = true;
				miss = BattleSystemServer.random.nextFloat() < CombatantInfo.evasionRate + CombatantInfo.onCorrectDodgeEvasionRate + attacked.evasionBonus - attacker.hitBonus;
				attacked.counterBonus += miss ? CombatantInfo.counterRateAfterMiss : CombatantInfo.counterRateAfterHit;
				return miss;
			}
			else
			{
				return BattleSystemServer.random.nextFloat() < CombatantInfo.evasionRate + CombatantInfo.onDodgeEvasionRate + attacked.evasionBonus - attacker.hitBonus;
			}
		}
		else
			return BattleSystemServer.random.nextFloat() < CombatantInfo.evasionRate + attacked.evasionBonus - attacker.hitBonus;
	}
	
	private boolean criticalCheck(CombatantInfo attacker)
	{
		return BattleSystemServer.random.nextFloat() < CombatantInfo.criticalRate + attacker.criticalBonus;
	}
	
	private boolean counterCheck(CombatantInfo attacked)
	{
		return BattleSystemServer.random.nextFloat() < attacked.counterBonus;
	}
	
	protected void notifyPlayers(boolean forceUpdate)
	{
		for(CombatantInfo combatant : combatants.values())
		{
			if(combatant.isPlayer)
				ModMain.pp.sendTo(new BattleStatusPacket(!battleEnded && (combatant.entityReference.isEntityAlive()), forceUpdate, combatants.size(), status == BattleStatus.PLAYER_PHASE, combatant.ready, combatant.turnTickTimer), (EntityPlayerMP)combatant.entityReference);
		}
	}
	
	protected void notifyPlayer(boolean forceUpdate, CombatantInfo player, boolean fledBattle)
	{
		ModMain.pp.sendTo(new BattleStatusPacket(!battleEnded && (player.entityReference.isEntityAlive()) && !fledBattle, forceUpdate, combatants.size(), status == BattleStatus.PLAYER_PHASE, player.ready, player.turnTickTimer), (EntityPlayerMP)player.entityReference);
	}
	
	protected void notifyPlayersWithMessage(String message)
	{
		for(CombatantInfo combatant : combatants.values())
		{
			if(combatant.isPlayer)
			{
				ModMain.pp.sendTo(new BattleMessagePacket(message), (EntityPlayerMP) combatant.entityReference);
			}
		}
	}
	
//	protected void notifyPlayersTurnEnded()
//	{
//		for(CombatantInfo combatant : combatants.values())
//		{
//			if(combatant.isPlayer)
//			{
//				PacketDispatcher.sendPacketToPlayer(new BattlePhaseEndedPacket().makePacket(), (Player)combatant.entityReference);
//			}
//		}
//	}
	
	protected void notifyPlayerOfCombatants(EntityLivingBase player)
	{
		for(CombatantInfo combatant : combatants.values())
		{
			ModMain.pp.sendTo(new BattleCombatantPacket(combatant), (EntityPlayerMP)player);
		}
	}
	
	protected void notifyPlayersHealthInformation()
	{
		CombatantInfo[] combatantListCopy = combatants.values().toArray(new CombatantInfo[0]);
		for(int i=0; i<combatantListCopy.length; i++)
		{
			combatantListCopy[i].setHealth(combatantListCopy[i].entityReference.getHealth());
//			System.out.println("Possible health is " + combatantListCopy[i].entityReference.func_110143_aJ());

		}
		
		for(int i=0; i<combatantListCopy.length; i++)
		{
			if(combatantListCopy[i].isPlayer)
			{
				for(int j=0; j<combatantListCopy.length; j++)
				{
					ModMain.pp.sendTo(new CombatantHealthPacket(combatantListCopy[j].id, combatantListCopy[j].health), (EntityPlayerMP)combatantListCopy[i].entityReference);
				}
			}
		}
	}
}
