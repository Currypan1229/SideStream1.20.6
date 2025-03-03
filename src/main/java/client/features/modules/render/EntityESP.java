package client.features.modules.render;

import client.event.Event;
import client.event.listeners.EventRender3D;
import client.features.modules.Module;
import client.features.modules.ModuleManager;
import client.features.modules.combat.HitBoxes;
import client.settings.BooleanSetting;
import client.settings.ModeSetting;
import client.settings.NumberSetting;
import client.utils.*;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.Camera;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.awt.*;
import java.util.Objects;

public class EntityESP extends Module
{
	static ModeSetting mode;
	public ModeSetting colorMode;
	public static BooleanSetting mobs;
	public NumberSetting alpha;
	public EntityESP()
	{
		
		super("EntityESP", 0, Category.RENDER);
		
	}
	
	@Override
	public void init()
	{
		super.init();
		colorMode = new ModeSetting("Color Mode", "HurtTime",
			new String[]{"HurtTime", "Team"});
		alpha = new NumberSetting("Alpha", 0.5, 0 , 1,0.1);
		mode = new ModeSetting("Mode ", "BoundingBox",
			new String[]{"BoundingBox"});
		mobs = new BooleanSetting("Render Mobs", true);
		addSetting(mode, colorMode,mobs,alpha);
	}
	
	@Override
	public void onEvent(Event<?> event)
	{
		if(event instanceof EventRender3D)
		{
			MatrixStack matrixStack = ((EventRender3D)event).getMatrix();
			float partialTicks = ((EventRender3D)event).getPartialTicks();
			
			for(Entity entity : Objects.requireNonNull(mc.world).getEntities())
			{
				if(entity instanceof LivingEntity) {
if(!(entity instanceof PlayerEntity) && !mobs.isEnabled()){
	return;
}
					Camera camera = mc.gameRenderer.getCamera();
					Vec3d cameraPosition = camera.getPos();
					if (mc.getEntityRenderDispatcher().shouldRender(entity,
							((EventRender3D) event).getFrustum(), cameraPosition.getX(),
							cameraPosition.getY(), cameraPosition.getZ())) {
						if (entity != mc.player) {
							int color = 0;

							if (colorMode.getMode().equalsIgnoreCase("Team")) {
								color = entity.getTeamColorValue();
							} else if (colorMode.getMode()
									.equalsIgnoreCase("HurtTime")) {
								color = (((LivingEntity) entity).hurtTime == 0)
										? new Color(0, 200, 0, 0).getRGB()
										: new Color(239, 235, 41,   0).getRGB();
							}

							switch (mode.getMode()) {
								case "BoundingBox":
									double interpolatedX = MathHelper.lerp(partialTicks,
											entity.prevX, entity.getX());
									double interpolatedY = MathHelper.lerp(partialTicks,
											entity.prevY, entity.getY());
									double interpolatedZ = MathHelper.lerp(partialTicks,
											entity.prevZ, entity.getZ());
									Box boundingBox;
if(ModuleManager.getModulebyClass(HitBoxes.class).isEnabled()){
	 boundingBox = entity.getBoundingBox().offset(
			interpolatedX - entity.getX(),
			interpolatedY - entity.getY(),
			interpolatedZ - entity.getZ()).expand(HitBoxes.getSize(entity));
} else {
	boundingBox = entity.getBoundingBox().offset(
			interpolatedX - entity.getX(),
			interpolatedY - entity.getY(),
			interpolatedZ - entity.getZ());
}
									RenderingUtils.draw3DBox2(
											matrixStack.peek().getPositionMatrix(),
											boundingBox, Colors.reAlpha(color, (float) alpha.getValue()));
									break;
								case "Lines":
									// RenderingUtils.drawEntityModel(matrixStack,
									// partialTicks, entity, color,
									// lineThickness.getValue());
									break;
							}
						}
					}
				}
			}
		}
	}
}
