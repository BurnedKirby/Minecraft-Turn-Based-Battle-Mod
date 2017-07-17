package burnedkirby.TurnBasedMinecraft;

import burnedkirby.TurnBasedMinecraft.core.Utility;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
		if(event.getEntity().world.isRemote)
			return;
		if(!(event.getSource().getTrueSource() instanceof EntityLivingBase) || !(event.getEntity() instanceof EntityLivingBase))
			return;
		
		if(event.getEntity() == event.getSource().getTrueSource())
			return;
		
		String sName = null;
		String name = null;
		
		if((sName = EntityList.getEntityString(event.getSource().getTrueSource())) == null)
			sName = event.getSource().getTrueSource().getName();

		if((name = EntityList.getEntityString(event.getEntity())) == null)
			name = event.getEntity().getName();
		
		Utility.log(sName + "(" + event.getSource().getTrueSource().getEntityId()
				+ ") hit " + name + "(" + event.getEntity().getEntityId() + ").");
		
		if(BattleSystemServer.attackingEntity == null)
			name = "null";
		else
		{
			if((name = EntityList.getEntityString(BattleSystemServer.attackingEntity)) == null)
				name = BattleSystemServer.attackingEntity.getName();
		}
		
		Utility.log("Battle Attacker is currently " + name);

		if(ModMain.bss.manageCombatants((EntityLivingBase)event.getSource().getTrueSource(), (EntityLivingBase)event.getEntity()))
			event.setCanceled(true);
//		else if(ModMain.bss.isInBattle(event.getSource().getEntity().entityId) && ModMain.bss.isInBattle(event.getEntity().entityId))
//			if(!Battle.playerAttacking && !(event.getSource().getEntity() instanceof EntityPlayer)) //TODO go over this again
//				event.setCanceled(true);
	}
}
