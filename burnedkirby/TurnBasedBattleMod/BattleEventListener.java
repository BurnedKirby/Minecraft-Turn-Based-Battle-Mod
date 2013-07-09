package burnedkirby.TurnBasedBattleMod;

import burnedkirby.TurnBasedBattleMod.core.network.BattleQueryPacket;
import burnedkirby.TurnBasedBattleMod.core.network.InitiateBattlePacket;
import cpw.mods.fml.common.network.PacketDispatcher;
import cpw.mods.fml.common.network.Player;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public class BattleEventListener {
	
	/**
	 * LivingAttackEvent handler that calls BattleSystemServer's manageCombatants() method
	 * after checking several conditions. Cancels the event if the entities were added to
	 * battle or if they are already in battle.
	 * @param event The LivingAtttackEvent this method handles.
	 */
	@ForgeSubscribe
	public void entityAttacked(LivingAttackEvent event)
	{
		if(event.entity.worldObj.isRemote)
			return;
		
		if(!(event.source.getEntity() instanceof EntityLiving) || !(event.entity instanceof EntityLiving))
			return;
		
		if(event.entity == event.source.getEntity())
			return;
		
		System.out.println(event.source.getEntity().getEntityName() + "(" + event.source.getEntity().entityId
				+ ") hit " + event.entity.getEntityName() + "(" + event.entity.entityId + ").");

		if(ModMain.bss.manageCombatants(event.source.getEntity(), event.entity))
			event.setCanceled(true);
		else if(ModMain.bss.isInBattle(event.source.getEntity().entityId) && ModMain.bss.isInBattle(event.entity.entityId))
			if(!Battle.playerAttacking && !(event.source.getEntity() instanceof EntityPlayer)) //TODO go over this again
				event.setCanceled(true);
	}
	
	/**
	 * LivingDeathEvent handler that alls BattleSystemServer's combatantDeath
	 * method with the entity that has perished.
	 * @param event The LivingDeathEvent this method handles.
	 */
	@ForgeSubscribe
	public void livingDeathEvent(LivingDeathEvent event)
	{
		ModMain.bss.combatantDeath(event.entity);
	}
}
