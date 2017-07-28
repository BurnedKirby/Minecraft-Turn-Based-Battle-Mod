package burnedkirby.TurnBasedMinecraft.helpers;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityTippedArrow;
import net.minecraft.world.World;

public class BattleArrowHelper extends EntityTippedArrow {

	public BattleArrowHelper(World worldIn, EntityLivingBase shooter) {
		super(worldIn, shooter);
	}

	public void arrowHitHelper(EntityLivingBase entityLivingBase)
	{
		this.arrowHit(entityLivingBase);
	}
}
