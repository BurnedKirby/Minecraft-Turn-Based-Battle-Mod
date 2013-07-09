package burnedkirby.TurnBasedBattleMod;

import burnedkirby.TurnBasedBattleMod.core.BattleQueryPacket;
import burnedkirby.TurnBasedBattleMod.core.InitiateBattlePacket;
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
	
	protected static int battleIDCounter = 0; //TODO maybe split this per world
	
	@ForgeSubscribe
	public void entityAttacked(LivingAttackEvent event) throws MinecraftException
	{
		if(event.entity.worldObj.isRemote)
			return;
		
		if(!(event.source.getEntity() instanceof EntityLiving) || !(event.entity instanceof EntityLiving))
			return;
		
		if(event.entity == event.source.getEntity())
			return;

		for(WorldServer world : MinecraftServer.getServer().worldServers)
		{
			System.out.println("DIMENSION " + world.getWorldInfo().getDimension());
		}

		int world = event.entity.dimension;
		
		System.out.println(event.source.getEntity().getEntityName() + "(" + event.source.getEntity().entityId
				+ ") hit " + event.entity.getEntityName() + "(" + event.entity.entityId + ") in world " + world);

//		Player player = null;
//		
//		if(event.entity instanceof EntityPlayer)
//			player = (Player) event.entity;
//		else if(event.source.getEntity() instanceof EntityPlayer)
//			player = (Player) event.source.getEntity();
//		else
//			return;
		
		
//		int[] sideOne = {event.entity.entityId};
//		int[] sideTwo = {event.source.getEntity().entityId};
//		
//		ModMain.bss.createNewBattle(battleIDCounter, sideOne, sideTwo);
		if(ModMain.bss.manageCombatants(event.source.getEntity(), event.entity))
			event.setCanceled(true);
		else if(ModMain.bss.isInBattle(event.source.getEntity().entityId) && ModMain.bss.isInBattle(event.entity.entityId))
			if(!Battle.playerAttacking && !(event.source.getEntity() instanceof EntityPlayer)) //TODO go over this again
				event.setCanceled(true);
//		for(int entityID : sideOne)
//			ModMain.bss.addCombatant(world, battleIDCounter, entityID, true);
//		for(int entityID : sideTwo)
//			ModMain.bss.addCombatant(world, battleIDCounter, entityID, false);
		
		//ModMain.instance.bss.createNewBattle(sideOne, sideTwo);
		
//		PacketDispatcher.sendPacketToServer(new InitiateBattlePacket(commandIDCounter).makePacket());
//		
//		for(int entityID : sideOne)
//			PacketDispatcher.sendPacketToServer(new AddCombatantPacket(commandIDCounter, true, entityID).makePacket());
//		for(int entityID : sideTwo)
//			PacketDispatcher.sendPacketToServer(new AddCombatantPacket(commandIDCounter, false, entityID).makePacket());
		
		
		//hitEntity.addPotionEffect(new PotionEffect(2, 2000000000, 127, false));
	}
	
	@ForgeSubscribe
	public void livingDeathEvent(LivingDeathEvent event)
	{
		ModMain.bss.combatantDeath(event.entity);
	}
}
