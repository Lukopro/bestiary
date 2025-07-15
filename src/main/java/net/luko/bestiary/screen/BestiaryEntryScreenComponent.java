package net.luko.bestiary.screen;

import net.luko.bestiary.data.BestiaryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

public class BestiaryEntryScreenComponent {
    public static int ENTRY_HEIGHT = 36;

    private final ResourceLocation mobId;
    private final BestiaryData data;
    private final EntityType<?> entityType;

    public BestiaryEntryScreenComponent(ResourceLocation mobId, BestiaryData data){
        this.mobId = mobId;
        this.data = data;
        this.entityType = BuiltInRegistries.ENTITY_TYPE.get(mobId);
    }

    public void render(GuiGraphics guiGraphics, int x, int y, int width){
        guiGraphics.fill(x, y, x + width, y + ENTRY_HEIGHT, 0xFF303030);
        if(entityType != null){
            drawMobIcon(guiGraphics, x + 4, y + 4, entityType);
        }

        String name = entityType != null ? entityType.getDescription().getString() : mobId.toString();
        guiGraphics.drawString(Minecraft.getInstance().font, name,
                x + 40, y + 6, 0xFFFFFF);
        guiGraphics.drawString(Minecraft.getInstance().font,
                String.format("Level %d, %d kills, %d left until next level. x%.2f damage dealt, x%.3f damage taken.",
                        data.level(),
                        data.kills(),
                        data.nextLevelKills(),
                        data.mobBuff().damageFactor(),
                        data.mobBuff().resistanceFactor()),
                x + 40, y + 20, 0xAAAAAA
        );
    }

    private void drawMobIcon(GuiGraphics guiGraphics, int x, int y, EntityType<?> type){
        Minecraft mc = Minecraft.getInstance();
        if(mc.level == null) return;

        Entity entity = type.create(mc.level);
        if(entity instanceof LivingEntity living){
            float poseXRot = (float)Math.toRadians(10);
            Quaternionf pose = new Quaternionf()
                    .rotateY((float)Math.toRadians(165))
                    .rotateZ((float)Math.toRadians(182))
                    .rotateX(poseXRot);

            Quaternionf camera = new Quaternionf();

            living.tickCount = 0;
            living.yBodyRot = 0;
            living.setYRot(0);
            living.setXRot(0);
            living.yHeadRot = 0;
            living.yHeadRotO = 0;

            InventoryScreen.renderEntityInInventory(guiGraphics,
                    x + 16, y + 32 + computeEntityYOffset(living, poseXRot), computeEntityScale(living), pose, camera, living);
        }
    }

    private int computeEntityScale(LivingEntity entity){
        //float size = (float)Math.sqrt(entity.getBbHeight() * entity.getBbHeight() + entity.getBbWidth() * entity.getBbWidth()) / 2F;
        float size = (entity.getBbHeight() + entity.getBbWidth()) / 2F;

        // Logistic function: output is between min and max
        float min = 14F;
        float max = 28F;
        float steepness = 8F;
        float midpoint = 1.2F;

        float logistic = 1F / (1F + (float)Math.exp(-steepness * (size - midpoint)));

        float scale = (min + (max - min) * logistic) / size;
        return Math.round(scale);
    }

    private int computeEntityYOffset(LivingEntity entity, float pitchRadians){
        float flatness = entity.getBbWidth() / entity.getBbHeight();
        float sinPitch = (float)Math.sin(pitchRadians);

        float factor = 20F;

        return -Math.round(factor * flatness * sinPitch);
    }
}
