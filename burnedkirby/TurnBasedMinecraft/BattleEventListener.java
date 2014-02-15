package burnedkirby.TurnBasedMinecraft;

import burnedkirby.TurnBasedMinecraft.core.Utility;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class BattleEventListener {
	
	/**
	 * LivingAttackEvent handler that calls BattleSystemServer's manageCombatants() method
	 * after checking several conditions. Cancels the event if the entities were added to
	 * battle or if they are already in battle.
	 * @param event The LivingAtttackEvent this method handles.
	 */
	@SubscribeEvent
	public void entityAttacked(LivingAttackEvent event)
	{
		if(event.entity.worldObj.isRemote)
			return;
		
		if(!(event.source.getEntity() instanceof EntityLivingBase) || !(event.entity instanceof EntityLivingBase))
			return;
		
		if(event.entity == event.source.getEntity())
			return;
		
		String sName = null;
		String name = null;
		
		if((sName = EntityList.getEntityString(event.source.getEntity())) == null)
			sName = ((EntityPlayer)event.source.getEntity()).getDisplayName();

		if((name = EntityList.getEntityString(event.entity)) == null)
			name = ((EntityPlayer)event.entity).getDisplayName();
		
		Utility.log(sName + "(" + event.source.getEntity().getEntityId()
				+ ") hit " + name + "(" + event.entity.getEntityId() + ").");
		
		if(ModMain.bss.attackingEntity == null)
			name = "null";
		else
		{
			if((name = EntityList.getEntityString(ModMain.bss.attackingEntity)) == null)
				name = ((EntityPlayer)ModMain.bss.attackingEntity).getDisplayName();
		}
		
		Utility.log("Battle Attacker is currently " + name);

		if(ModMain.bss.manageCombatants((EntityLivingBase)event.source.getEntity(), (EntityLivingBase)event.entity))
			event.setCanceled(true);
//		else if(ModMain.bss.isInBattle(event.source.getEntity().entityId) && ModMain.bss.isInBattle(event.entity.entityId))
//			if(!Battle.playerAttacking && !(event.source.getEntity() instanceof EntityPlayer)) //TODO go over this again
//				event.setCanceled(true);
	}
}
