package burnedkirby.TurnBasedMinecraft;

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

import burnedkirby.TurnBasedMinecraft.CombatantInfo.Type;
import burnedkirby.TurnBasedMinecraft.core.Utility;
import burnedkirby.TurnBasedMinecraft.core.network.BattleCombatantPacket;
import burnedkirby.TurnBasedMinecraft.core.network.BattleMessagePacket;
import burnedkirby.TurnBasedMinecraft.core.network.BattleStatusPacket;
import burnedkirby.TurnBasedMinecraft.core.network.CombatantHealthPacket;
import burnedkirby.TurnBasedMinecraft.core.network.InitiateBattlePacket;
import burnedkirby.TurnBasedMinecraft.helpers.BattleArrowHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityGolem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Enchantments;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArrow;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

public class Battle {
	private Map<Integer, CombatantInfo> combatants;
	private Stack<CombatantInfo> newCombatantQueue;
	// private Stack<CombatantInfo> removeCombatantQueue;
	private int battleID;

	protected enum BattleStatus {
		PLAYER_PHASE, CALCULATIONS_PHASE, END_CHECK_PHASE
	}

	private BattleStatus status;

	// private boolean phaseInProgress;

	protected boolean battleEnded;

	private short healthUpdateTick;
	private final short healthUpdateTime = 4;

	private final short turnTickTime = 15 * 1000 / BattleSystemServer.UPDATE_TIME_MILLISECONDS;

	private boolean silly;

	public Battle(int id) {
		battleID = id;
	}

	public Battle(int id, Stack<CombatantInfo> newCombatants, boolean silly) {
		battleID = id;
		combatants = new TreeMap<Integer, CombatantInfo>();
		newCombatantQueue = new Stack<CombatantInfo>();
		// removeCombatantQueue = new Stack<CombatantInfo>();

		this.silly = silly;

		CombatantInfo combatant;
		while (!newCombatants.isEmpty()) {
			combatant = newCombatants.pop();
			Utility.log("Initializing battle with combatant " + combatant.name);
			if (combatant.isPlayer) {
				ModMain.network.sendTo(new InitiateBattlePacket(battleID, combatant, silly),
						(EntityPlayerMP) combatant.entityReference);
				combatant.setTurnTickTimer(turnTickTime);
			} else if (isFightingEntity(combatant.entityReference)) {
				combatant.type = Type.ATTACK;
			} else if (combatant.entityReference instanceof EntityAnimal) {
				combatant.type = Type.FLEE;
			}
			combatants.put(combatant.id, combatant);
			ModMain.bss.inBattle.add(combatant);
		}

		status = BattleStatus.PLAYER_PHASE;
		battleEnded = false;
		// phaseInProgress = false;
		healthUpdateTick = healthUpdateTime;
	}

	public void addCombatant(CombatantInfo newCombatant) {
		if (status == BattleStatus.PLAYER_PHASE) {
			if (isFightingEntity(newCombatant.entityReference)) {
				newCombatant.type = Type.ATTACK;
			}
			
			for(CombatantInfo combatant : combatants.values())
			{
				if(combatant.isPlayer)
				{
					combatant.setYaw(Utility.yawDirection(combatant.posX, combatant.posZ, newCombatant.posX, newCombatant.posZ));
					combatant.setPitch(Utility.pitchDirection(combatant.posX, combatant.posY, combatant.posZ, newCombatant.posX, newCombatant.posY, newCombatant.posZ) + Utility.PITCH_OFFSET);
				}
			}

			combatants.put(newCombatant.id, newCombatant);
			ModMain.bss.inBattle.add(newCombatant);

			if (newCombatant.isPlayer) {
				ModMain.network.sendTo(new InitiateBattlePacket(battleID, newCombatant, silly),
						(EntityPlayerMP) newCombatant.entityReference);
				newCombatant.setTurnTickTimer(turnTickTime);
			}

			notifyPlayers(true);
			String name = ScorePlayerTeam.formatPlayerName(
					newCombatant.entityReference.world.getScoreboard().getPlayersTeam(newCombatant.name),
					newCombatant.name);
			notifyPlayersWithMessage(name + " has entered battle!");
		} else {
			newCombatantQueue.push(newCombatant);
		}
	}

	// public void manageDeath(int id)
	// {
	// if(status == BattleStatus.PLAYER_PHASE)
	// {
	// CombatantInfo removed = combatants.remove(id);
	//
	// if(removed.isPlayer)
	// PacketDispatcher.sendPacketToPlayer(new BattleStatusPacket(false, false,
	// combatants.size()).makePacket(), (Player)removed.entityReference);
	//
	// notifyPlayers(true);
	// }
	// else
	// {
	// removeCombatantQueue.add(combatants.get(id));
	// }
	// }

	public CombatantInfo getCombatant(int combatantID) {
		return combatants.get(combatantID);
	}

	public int getBattleID() {
		return battleID;
	}

	public void updatePlayerStatus(CombatantInfo combatant) {
		if (this.status != BattleStatus.PLAYER_PHASE || !combatants.containsValue(combatant)) {
			Utility.log("WARNING: Battle " + battleID + " failed updatePlayerStatus."); // TODO debug
			return;
		}

		if (combatants.containsKey(combatant.id) && combatants.containsKey(combatant.target)) {
			combatants.get(combatant.id).updateBattleInformation(combatant);
		} else
			notifyPlayers(false);
	}

	public boolean isInBattle(int id) {
		return combatants.containsKey(id);
	}

	public boolean isFightingEntity(EntityLivingBase entity) {
		return entity instanceof EntityMob || entity instanceof EntityGolem || entity instanceof EntityDragon
				|| entity instanceof EntitySlime || entity instanceof EntityGhast;
	}

	public int getNumberOfCombatants() {
		return combatants.size();
	}

	private boolean getPlayersReady() {
		for (CombatantInfo combatant : combatants.values()) {
			if (combatant.isPlayer && !combatant.ready)
				return false;
		}
		return true;
	}

	public synchronized boolean update() {
		if (!battleEnded) {
			for (CombatantInfo combatant : combatants.values()) {
				if(combatant.isPlayer)
				{
					if(combatant.entityReference.posY < combatant.posY)
					{
						combatant.setPosY(combatant.entityReference.posY);
					}
					((EntityPlayerMP)combatant.entityReference).connection.setPlayerLocation(combatant.posX, combatant.posY, combatant.posZ, combatant.yaw, combatant.pitch);
				}
				else
				{
					combatant.entityReference.setPosition(combatant.posX, combatant.posY, combatant.posZ);
				}
			}
			switch (status) {
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
		} else {
			notifyPlayers(false);
		}

		return battleEnded;
	}

	private void playerPhase() {
		// if(phaseInProgress)
		// return;
		// phaseInProgress = true;

		boolean forceUpdate = false;
		String name = "";
		if (!newCombatantQueue.isEmpty())
			forceUpdate = true;
		while (!newCombatantQueue.isEmpty()) {
			CombatantInfo combatant = newCombatantQueue.pop();
			if(newCombatantQueue.isEmpty())
			{
				for(CombatantInfo battlingCombatant : combatants.values())
				{
					if(battlingCombatant.isPlayer)
					{
						battlingCombatant.setYaw(Utility.yawDirection(battlingCombatant.posX, battlingCombatant.posZ, combatant.posX, combatant.posZ));
						battlingCombatant.setPitch(Utility.pitchDirection(battlingCombatant.posX, battlingCombatant.posY, battlingCombatant.posZ, combatant.posX, combatant.posY, combatant.posZ) + Utility.PITCH_OFFSET);
					}
				}
			}
			combatants.put(combatant.id, combatant);
			ModMain.bss.inBattle.add(combatant);
			name = ScorePlayerTeam.formatPlayerName(
					combatant.entityReference.world.getScoreboard().getPlayersTeam(combatant.name), combatant.name);
			notifyPlayersWithMessage(name + " has entered battle!");
		}

		Iterator<CombatantInfo> iter = combatants.values().iterator();
		Stack<CombatantInfo> messageQueue = new Stack<CombatantInfo>();
		CombatantInfo combatant;
		while (iter.hasNext()) {
			combatant = iter.next();
			if (!combatant.entityReference.isEntityAlive()) {
				messageQueue.push(combatant);

				combatant.removeEntityReference();
				combatant.setTarget(BattleSystemServer.exitCooldownTime);
				synchronized (ModMain.bss.inBattle) {
					ModMain.bss.inBattle.remove(combatant);
				}
				synchronized (ModMain.bss.exitedBattle) {
					ModMain.bss.exitedBattle.add(combatant);
				}
			} else if (combatant.isPlayer) {
				if (combatant.decrementTimer() <= 0) {
					combatant.target = combatant.id;
					combatant.type = Type.DO_NOTHING;
					combatant.ready = true;
				}
			}
		}

		while (!messageQueue.isEmpty())
		{
			CombatantInfo deadCombatant = messageQueue.pop();
			notifyPlayersWithMessage(deadCombatant.name + " has died!");
			combatants.remove(deadCombatant.id);
		}

		notifyPlayers(forceUpdate);

		checkIfBattleEnded();

		if (getPlayersReady()) {
			status = BattleStatus.CALCULATIONS_PHASE;
			Utility.log("PlayerPhase ended.");
		}

		// phaseInProgress = false;

		if (--healthUpdateTick == 0) {
			notifyPlayersHealthInformation();
			healthUpdateTick = healthUpdateTime;
		}
	}

	private void calculationsPhase() {
		// if(phaseInProgress)
		// return;
		// phaseInProgress = true;

		// Combatant flee phase
		Iterator<CombatantInfo> iter = combatants.values().iterator();
		Stack<CombatantInfo> messageQueue = new Stack<CombatantInfo>();
		CombatantInfo combatant;
		while (iter.hasNext()) {
			combatant = iter.next();
			if (combatant.type != Type.FLEE)
				continue;

			if (fleeCheck(combatant)) {
				if (combatant.isPlayer)
					notifyPlayer(false, combatant, true);
				messageQueue.push(combatant);

				combatant.setTarget(BattleSystemServer.exitCooldownTime);
				synchronized (ModMain.bss.inBattle) {
					ModMain.bss.inBattle.remove(combatant);
				}
				synchronized (ModMain.bss.exitedBattle) {
					ModMain.bss.exitedBattle.add(combatant);
				}
			}
		}

		String name = "";
		String targetName = "";

		while (!messageQueue.isEmpty()) {
			combatant = messageQueue.pop();
			name = ScorePlayerTeam.formatPlayerName(
					combatant.entityReference.world.getScoreboard().getPlayersTeam(combatant.name), combatant.name);
			notifyPlayersWithMessage(name + " has fled battle!");
			combatants.remove(combatant.id);
		}

		// Combatant attack phase
		EntityLivingBase combatantEntity = null;
		EntityLivingBase targetEntity = null;
		CombatantInfo[] combatantArray = combatants.values().toArray(new CombatantInfo[0]);
		for (int i = 0; i < combatantArray.length; i++) {
			combatant = combatantArray[i];
			if (combatant.type != Type.ATTACK)
				continue;

			combatantEntity = combatant.entityReference;
			if (!combatantEntity.isEntityAlive())
				continue;

			synchronized (BattleSystemServer.attackingLock) {
				if (combatant.isPlayer) {
					if (combatants.get(combatant.target) != null)
						targetEntity = combatants.get(combatant.target).entityReference;
					else
						targetEntity = null;

					if (targetEntity == null || !targetEntity.isEntityAlive()
							|| !combatants.containsKey(targetEntity.getEntityId()))
						continue;

					targetEntity.hurtResistantTime = 0;

					name = combatantEntity.getName();
					name = ScorePlayerTeam.formatPlayerName(combatantEntity.world.getScoreboard().getPlayersTeam(name),
							name);

					if ((targetName = EntityList.getEntityString(targetEntity)) == null) {
						targetName = targetEntity.getName();
						targetName = ScorePlayerTeam.formatPlayerName(
								targetEntity.world.getScoreboard().getPlayersTeam(targetName), targetName);
					}
					
					// set player to look at target
					combatant.setYaw(Utility.yawDirection(combatant.posX, combatant.posZ, targetEntity.posX, targetEntity.posZ));
					combatant.setPitch(Utility.pitchDirection(combatant.posX, combatant.posY, combatant.posZ, targetEntity.posX, targetEntity.posY, targetEntity.posZ) + Utility.PITCH_OFFSET);
					((EntityPlayerMP)combatant.entityReference).connection.setPlayerLocation(combatant.posX, combatant.posY, combatant.posZ, combatant.yaw, combatant.pitch);

					if (combatantEntity.getHeldItemMainhand().getItem() instanceof ItemBow) {
						ItemStack arrows = null;
						for (int j = 0; j < ((EntityPlayer) combatantEntity).inventory.getSizeInventory(); ++j) {
							if (((EntityPlayer) combatantEntity).inventory.getStackInSlot(j)
									.getItem() instanceof ItemArrow) {
								arrows = ((EntityPlayer) combatantEntity).inventory.getStackInSlot(j);
								break;
							}
						}

						boolean isInfinityBow = EnchantmentHelper.getEnchantmentLevel(Enchantments.INFINITY,
								combatantEntity.getHeldItemMainhand()) > 0;
						if (arrows == null || arrows.isEmpty()) {
							notifyPlayersWithMessage(name + " tried to attack " + targetName
									+ " with bow and arrows but ran out of ammo!");
						} else if (missCheck(combatant, combatants.get(combatant.target))) {
							notifyPlayersWithMessage(name + " attacks " + targetName + " but missed!");

							BattleArrowHelper entityArrow = new BattleArrowHelper(combatantEntity.world,
									combatantEntity);
							float f;
							if (arrows != null && !arrows.isEmpty()) {
								entityArrow.setPotionEffect(arrows);
							}
							if (criticalCheck(combatant)) {
								entityArrow.setAim(combatantEntity,
										Utility.pitchDirection(combatantEntity.posX, combatantEntity.posY,
												combatantEntity.posZ, targetEntity.posX, targetEntity.posY,
												targetEntity.posZ),
										Utility.yawDirection(combatantEntity.posX, combatantEntity.posZ,
												targetEntity.posX, targetEntity.posZ),
										0, 3.0f, 1.0f);
								entityArrow.setIsCritical(true);
								f = 1.0f;
							} else {
								entityArrow.setAim(combatantEntity,
										Utility.pitchDirection(combatantEntity.posX, combatantEntity.posY,
												combatantEntity.posZ, targetEntity.posX, targetEntity.posY,
												targetEntity.posZ),
										Utility.yawDirection(combatantEntity.posX, combatantEntity.posZ,
												targetEntity.posX, targetEntity.posZ),
										0, 2.0f, 1.0f);
								f = 0.666f;
							}
							int powerEnchantlevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER,
									combatantEntity.getHeldItemMainhand());

							if (powerEnchantlevel > 0) {
								entityArrow
										.setDamage(entityArrow.getDamage() + (double) powerEnchantlevel * 0.5D + 0.5D);
							}

							int knockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH,
									combatantEntity.getHeldItemMainhand());

							if (knockbackLevel > 0) {
								entityArrow.setKnockbackStrength(knockbackLevel);
							}

							if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME,
									combatantEntity.getHeldItemMainhand()) > 0) {
								entityArrow.setFire(100);
							}
							if (!isInfinityBow) {
								entityArrow.pickupStatus = EntityArrow.PickupStatus.ALLOWED;
							} else {
								entityArrow.pickupStatus = EntityArrow.PickupStatus.DISALLOWED;
							}
							combatantEntity.world.spawnEntity(entityArrow);
							if (!isInfinityBow) {
								arrows.shrink(1);
							}

							combatantEntity.getHeldItemMainhand().damageItem(1, combatantEntity);
							
							combatantEntity.world.playSound((EntityPlayer)null, combatantEntity.posX, combatantEntity.posY, combatantEntity.posZ, SoundEvents.ENTITY_ARROW_SHOOT, SoundCategory.PLAYERS, 1.0F, 1.0F / (BattleSystemServer.random.nextFloat() * 0.4F + 1.2F) + f * 0.5F);
						} else {
							BattleArrowHelper entityArrow = new BattleArrowHelper(combatantEntity.world,
									combatantEntity);
							if (arrows != null) {
								entityArrow.setPotionEffect(arrows);
							}
							if (criticalCheck(combatant)) {
								entityArrow.setAim(combatantEntity, combatantEntity.rotationPitch,
										combatantEntity.rotationYaw, 0, 3.0f, 1.0f);
								entityArrow.setIsCritical(true);
								notifyPlayersWithMessage(name + " attacks " + targetName + " with a critical hit!!");
							} else {
								entityArrow.setAim(combatantEntity, combatantEntity.rotationPitch,
										combatantEntity.rotationYaw, 0, 2.0f, 1.0f);
								notifyPlayersWithMessage(name + " attacks " + targetName + "!");
							}

							// net.minecraft.item.ItemBow =================
							int powerEnchantlevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.POWER,
									combatantEntity.getHeldItemMainhand());

							if (powerEnchantlevel > 0) {
								entityArrow
										.setDamage(entityArrow.getDamage() + (double) powerEnchantlevel * 0.5D + 0.5D);
							}

							int knockbackLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PUNCH,
									combatantEntity.getHeldItemMainhand());

							if (knockbackLevel > 0) {
								entityArrow.setKnockbackStrength(knockbackLevel);
							}

							if (EnchantmentHelper.getEnchantmentLevel(Enchantments.FLAME,
									combatantEntity.getHeldItemMainhand()) > 0) {
								entityArrow.setFire(100);
							}

							combatantEntity.getHeldItemMainhand().damageItem(1, combatantEntity);

							if (!isInfinityBow) {
								arrows.shrink(1);
							}
							// end ItemBow ================================

							// net.minecraft.entity.projectile.EntityArrow
							float f = MathHelper.sqrt(entityArrow.motionX * entityArrow.motionX
									+ entityArrow.motionY * entityArrow.motionY
									+ entityArrow.motionZ * entityArrow.motionZ);
							int damage = MathHelper.ceil((double) f * entityArrow.getDamage());
							if (entityArrow.getIsCritical()) {
								damage += BattleSystemServer.random.nextInt(damage / 2 + 2);
							}
							DamageSource damageSource = DamageSource.causeArrowDamage(entityArrow, combatantEntity);
							if (entityArrow.isBurning() && !(targetEntity instanceof EntityEnderman)) {
								targetEntity.setFire(5);
							}
							BattleSystemServer.attackingEntity = combatantEntity;
							targetEntity.attackEntityFrom(damageSource, damage);
							BattleSystemServer.attackingEntity = null;
							targetEntity.setArrowCountInEntity(targetEntity.getArrowCountInEntity() + 1);
							if (knockbackLevel > 0) {
								float f1 = MathHelper.sqrt(entityArrow.motionX * entityArrow.motionX
										+ entityArrow.motionZ * entityArrow.motionZ);

								if (f1 > 0.0F) {
									targetEntity.addVelocity(
											entityArrow.motionX * (double) knockbackLevel * 0.6000000238418579D
													/ (double) f1,
											0.1D, entityArrow.motionZ * (double) knockbackLevel * 0.6000000238418579D
													/ (double) f1);
								}
							}
							EnchantmentHelper.applyThornEnchantments(targetEntity, combatantEntity);
							EnchantmentHelper.applyArthropodEnchantments(combatantEntity, targetEntity);
							entityArrow.arrowHitHelper(targetEntity);
							entityArrow.playSound(SoundEvents.ENTITY_ARROW_HIT, 1.0F,
									1.2F / (BattleSystemServer.random.nextFloat() * 0.2F + 0.9F));
							// end EntityArrow ============================
						}
					} else // not attacking with bow
					{
						if (missCheck(combatant, combatants.get(combatant.target))) {
							notifyPlayersWithMessage(name + " attacks " + targetName + " but missed!");
						} else {
							BattleSystemServer.attackingEntity = combatantEntity;
							((EntityPlayer) combatantEntity).attackTargetEntityWithCurrentItem(targetEntity);
							BattleSystemServer.attackingEntity = null;

							if (criticalCheck(combatant)) {
								notifyPlayersWithMessage(name + " attacks " + targetName + " with a critical hit!!");
							} else {
								notifyPlayersWithMessage(name + " attacks " + targetName + "!");
							}
						}
					}
				} else if (combatantEntity instanceof EntityMob || combatantEntity instanceof EntityGolem) {
					CombatantInfo targetInfo = null;
					targetEntity = ((EntityLiving)combatantEntity).getAttackTarget();
					if(targetEntity != null && combatants.containsKey(targetEntity.getEntityId()))
					{
						targetInfo = combatants.get(targetEntity.getEntityId());
					}
					else
					{
						targetEntity = null;
						targetInfo = getRandomPlayerTarget(combatant, combatantArray);
						if (targetInfo != null)
							targetEntity = targetInfo.entityReference;
					}
					if (targetEntity == null || !combatants.containsKey(targetEntity.getEntityId()))
						continue;
					else if ((targetName = EntityList.getEntityString(targetEntity)) == null)
						targetName = targetEntity.getName();

					Utility.log(EntityList.getEntityString(combatantEntity) + " targeting "
							+ (targetEntity != null ? targetName : "null"));

					if (missCheck(combatant, combatants.get(targetEntity.getEntityId()))) {
						name = EntityList.getEntityString(combatantEntity);

						if ((targetName = EntityList.getEntityString(targetEntity)) == null) {
							targetName = targetEntity.getName();
							targetName = ScorePlayerTeam.formatPlayerName(
									targetEntity.world.getScoreboard().getPlayersTeam(targetName), targetName);
						}

						notifyPlayersWithMessage(name + " attacks " + targetName + " but missed!");
					} else {
						targetEntity.hurtResistantTime = 0;

						name = EntityList.getEntityString(combatantEntity);

						if ((targetName = EntityList.getEntityString(targetEntity)) == null) {
							targetName = targetEntity.getName();
							targetName = ScorePlayerTeam.formatPlayerName(
									targetEntity.world.getScoreboard().getPlayersTeam(targetName), targetName);
						}

						notifyPlayersWithMessage(name + " attacks " + targetName + "!");
						BattleSystemServer.attackingEntity = combatantEntity;
						(combatantEntity).attackEntityAsMob(targetEntity);
						BattleSystemServer.attackingEntity = null;
					}
				} else if (combatantEntity instanceof EntitySlime) {
					targetEntity = ((EntitySlime) combatantEntity).world.getClosestPlayerToEntity(combatantEntity,
							16.0);
					if (targetEntity == null || !combatants.containsKey(targetEntity.getEntityId())) {
						// select player at random, if player exists
						CombatantInfo targetInfo = getRandomPlayerTarget(combatant, combatantArray);
						if (targetInfo != null)
							targetEntity = targetInfo.entityReference;
						else
							continue;
					}

					if (missCheck(combatant, combatants.get(targetEntity.getEntityId()))) {
						name = EntityList.getEntityString(combatantEntity);

						if ((targetName = EntityList.getEntityString(targetEntity)) == null) {
							targetName = targetEntity.getName();
							targetName = ScorePlayerTeam.formatPlayerName(
									targetEntity.world.getScoreboard().getPlayersTeam(targetName), targetName);
						}

						notifyPlayersWithMessage(name + " attacks " + targetName + " but missed!");
					} else {
						targetEntity.hurtResistantTime = 0;

						name = EntityList.getEntityString(combatantEntity);

						if ((targetName = EntityList.getEntityString(targetEntity)) == null) {
							targetName = targetEntity.getName();
							targetName = ScorePlayerTeam.formatPlayerName(
									targetEntity.world.getScoreboard().getPlayersTeam(targetName), targetName);
						}

						notifyPlayersWithMessage(name + " attacks " + targetName + "!");
						BattleSystemServer.attackingEntity = combatantEntity;
						((EntitySlime) combatantEntity).onCollideWithPlayer((EntityPlayer) targetEntity);
						BattleSystemServer.attackingEntity = null;
					}
				} else
					Utility.log("Warning: Entity failed to attack!");// TODO implement non-mob entities or don't enter
																		// battle with them???

				if (targetEntity.isEntityAlive() && counterCheck(combatants.get(targetEntity.getEntityId()))) {
					notifyPlayersWithMessage(targetName + " countered " + name + "!");
					targetEntity.hurtResistantTime = 0;
					BattleSystemServer.attackingEntity = targetEntity;
					((EntityPlayer) targetEntity).attackTargetEntityWithCurrentItem(combatant.entityReference);
					BattleSystemServer.attackingEntity = null;
				}

			}
		}

		// combatant heal phase
		for (int i = 0; i < combatantArray.length; ++i) {
			if (combatantArray[i].type != Type.ATTEMPT_HEAL || !combatantArray[i].isPlayer) {
				continue;
			}

			ItemStack targetItemStack = ((EntityPlayer) combatantArray[i].entityReference).inventory
					.getStackInSlot(combatantArray[i].useItemID);
			Item targetItem = targetItemStack.getItem();
			String targetItemName = targetItemStack.getDisplayName();
			if (targetItem == null) {
				Utility.log("ERROR: targetItem is null, cannot consume");
				notifyPlayersWithMessage(combatantArray[i].entityReference.getName() + " tried to eat a "
						+ targetItemName + " but failed!");
			}
			if (targetItem instanceof ItemFood) {
				((ItemFood) targetItem).onItemUseFinish(targetItemStack, combatantArray[i].entityReference.world,
						combatantArray[i].entityReference);
				notifyPlayersWithMessage(
						combatantArray[i].entityReference.getName() + " ate a " + targetItemName + "!");
			} else if (targetItem instanceof ItemPotion) {
				((ItemPotion) targetItem).onItemUseFinish(targetItemStack, combatantArray[i].entityReference.world,
						combatantArray[i].entityReference);
				ItemStack glassBottle = new ItemStack(Items.GLASS_BOTTLE);
				((EntityPlayer)combatantArray[i].entityReference).inventory.setInventorySlotContents(combatantArray[i].useItemID, glassBottle);
				notifyPlayersWithMessage(
						combatantArray[i].entityReference.getName() + " consumed a " + targetItemName + "!");
			} else {
				notifyPlayersWithMessage(combatantArray[i].entityReference.getName() + " tried to eat a "
						+ targetItemName + " but failed!");
			}
		}

		status = BattleStatus.END_CHECK_PHASE;
		Utility.log("Calculations phase ended.");

		// phaseInProgress = false;
	}

	private void endCheckPhase() {
		// if(phaseInProgress)
		// return;
		// phaseInProgress = true;
		CombatantInfo combatantRef;
		Iterator<CombatantInfo> iter = combatants.values().iterator();
		CombatantInfo defaultInfo = new CombatantInfo();
		defaultInfo.ready = false;
		defaultInfo.type = Type.DO_NOTHING;
		Stack<CombatantInfo> messageQueue = new Stack<CombatantInfo>();

		while (iter.hasNext()) {
			combatantRef = iter.next();
			if (!combatantRef.entityReference.isEntityAlive()) {
				Utility.log("Entity is dead, removing");
				if (combatantRef.isPlayer)
					notifyPlayer(false, combatantRef, false);
				else
					messageQueue.push(combatantRef);

				iter.remove();

				combatantRef.removeEntityReference();
				combatantRef.setTarget(BattleSystemServer.exitCooldownTime);
				synchronized (ModMain.bss.inBattle) {
					ModMain.bss.inBattle.remove(combatantRef);
				}
				synchronized (ModMain.bss.exitedBattle) {
					ModMain.bss.exitedBattle.add(combatantRef);
				}

				continue;
			}

			if (combatantRef.isPlayer) {
				defaultInfo.target = combatantRef.target;
				defaultInfo.id = combatantRef.id;
				combatantRef.updateBattleInformation(defaultInfo);
				combatantRef.setTurnTickTimer(turnTickTime);
			}

			combatantRef.resetBonuses();
			if (combatantRef.counterSelectionSuccess) {
				combatantRef.criticalBonus += CombatantInfo.onCorrectDodgeCriticalBonus;
				combatantRef.hitBonus += CombatantInfo.onCorrectDodgeHitBonus;
				combatantRef.counterSelectionSuccess = false;
			}
		}

		while (!messageQueue.isEmpty()) {
			notifyPlayersWithMessage(messageQueue.pop().name + " has died!");
		}

		checkIfBattleEnded();

		// notifyPlayersTurnEnded();

		status = BattleStatus.PLAYER_PHASE;
		Utility.log("End phase ended.");

		notifyPlayers(false);

		// phaseInProgress = false;
	}

	private void checkIfBattleEnded() {
		if (battleEnded)
			return;

		int players = 0;
		int sideOne = 0;
		int sideTwo = 0;
		for (CombatantInfo combatant : combatants.values()) {
			if (combatant.isSideOne)
				sideOne++;
			else
				sideTwo++;

			if (combatant.isPlayer)
				players++;
		}

		if (sideOne == 0 || sideTwo == 0 || players == 0) {
			battleEnded = true;
			Utility.log("Battle " + battleID + " ended.");
			synchronized (ModMain.bss.inBattle) {
				synchronized (ModMain.bss.exitedBattle) {
					for (CombatantInfo combatant : combatants.values()) {
						ModMain.bss.inBattle.remove(combatant);
						combatant.setTarget(BattleSystemServer.exitCooldownTime);
						ModMain.bss.exitedBattle.add(combatant);
					}
				}
			}
		}
	}

	private boolean fleeCheck(CombatantInfo fleeingCombatant) {
		int enemySide = 0;
		int ownSide = 0;
		int diff;
		double rand;

		if (enemySide == ownSide) {
			return BattleSystemServer.random.nextBoolean();
		} else {
			diff = Math.abs(ownSide - enemySide);
			rand = BattleSystemServer.random.nextDouble() / Math.log((double) (diff + Math.E));
			return ownSide > enemySide ? rand <= 0.67d : rand > 0.67d;
		}
	}

	private boolean missCheck(CombatantInfo attacker, CombatantInfo attacked) {
		if (attacked.type == Type.DODGE_COUNTER) {
			boolean miss = false;
			if (attacked.target == attacker.id) {
				attacked.counterSelectionSuccess = true;
				miss = BattleSystemServer.random.nextFloat() < CombatantInfo.evasionRate
						+ CombatantInfo.onCorrectDodgeEvasionRate + attacked.evasionBonus - attacker.hitBonus;
				attacked.counterBonus += miss ? CombatantInfo.counterRateAfterMiss : CombatantInfo.counterRateAfterHit;
				return miss;
			} else {
				return BattleSystemServer.random.nextFloat() < CombatantInfo.evasionRate
						+ CombatantInfo.onDodgeEvasionRate + attacked.evasionBonus - attacker.hitBonus;
			}
		} else
			return BattleSystemServer.random.nextFloat() < CombatantInfo.evasionRate + attacked.evasionBonus
					- attacker.hitBonus;
	}

	private boolean criticalCheck(CombatantInfo attacker) {
		return BattleSystemServer.random.nextFloat() < CombatantInfo.criticalRate + attacker.criticalBonus;
	}

	private boolean counterCheck(CombatantInfo attacked) {
		return BattleSystemServer.random.nextFloat() < attacked.counterBonus;
	}

	protected void notifyPlayers(boolean forceUpdate) {
		for (CombatantInfo combatant : combatants.values()) {
			if (combatant.isPlayer)
				ModMain.network
						.sendTo(new BattleStatusPacket(!battleEnded && (combatant.entityReference.isEntityAlive()),
								forceUpdate, combatants.size(), status == BattleStatus.PLAYER_PHASE, combatant.ready,
								combatant.turnTickTimer), (EntityPlayerMP) combatant.entityReference);
		}
	}

	protected void notifyPlayer(boolean forceUpdate, CombatantInfo player, boolean fledBattle) {
		ModMain.network
				.sendTo(new BattleStatusPacket(!battleEnded && (player.entityReference.isEntityAlive()) && !fledBattle,
						forceUpdate, combatants.size(), status == BattleStatus.PLAYER_PHASE, player.ready,
						player.turnTickTimer), (EntityPlayerMP) player.entityReference);
	}

	protected void notifyPlayersWithMessage(String message) {
		for (CombatantInfo combatant : combatants.values()) {
			if (combatant.isPlayer) {
				ModMain.network.sendTo(new BattleMessagePacket(message), (EntityPlayerMP) combatant.entityReference);
			}
		}
	}

	// protected void notifyPlayersTurnEnded()
	// {
	// for(CombatantInfo combatant : combatants.values())
	// {
	// if(combatant.isPlayer)
	// {
	// PacketDispatcher.sendPacketToPlayer(new
	// BattlePhaseEndedPacket().makePacket(), (Player)combatant.entityReference);
	// }
	// }
	// }

	protected void notifyPlayerOfCombatants(EntityLivingBase player) {
		for (CombatantInfo combatant : combatants.values()) {
			ModMain.network.sendTo(new BattleCombatantPacket(combatant), (EntityPlayerMP) player);
		}
	}

	protected void notifyPlayersHealthInformation() {
		CombatantInfo[] combatantListCopy = combatants.values().toArray(new CombatantInfo[0]);
		for (int i = 0; i < combatantListCopy.length; i++) {
			combatantListCopy[i].setHealth(combatantListCopy[i].entityReference.getHealth());
			// Utility.log("Possible health is " +
			// combatantListCopy[i].entityReference.func_110143_aJ());

		}

		for (int i = 0; i < combatantListCopy.length; i++) {
			if (combatantListCopy[i].isPlayer) {
				for (int j = 0; j < combatantListCopy.length; j++) {
					ModMain.network.sendTo(
							new CombatantHealthPacket(combatantListCopy[j].id, combatantListCopy[j].health),
							(EntityPlayerMP) combatantListCopy[i].entityReference);
				}
			}
		}
	}

	private CombatantInfo getRandomPlayerTarget(CombatantInfo combatant, CombatantInfo[] combatantArray) {
		int randomValue = BattleSystemServer.random.nextInt(combatantArray.length);
		CombatantInfo returnValue = null;
		int loopIter = 0;
		while (returnValue == null) {
			if (combatantArray[randomValue].isPlayer) {
				returnValue = combatantArray[randomValue];
			} else {
				randomValue = (randomValue + 1) % combatantArray.length;
			}
			if (loopIter++ > combatantArray.length) {
				break;
			}
		}

		return returnValue;
	}
}
