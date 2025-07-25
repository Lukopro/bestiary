package net.luko.bestia.screen;

import net.luko.bestia.Bestia;
import net.luko.bestia.data.BestiaryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BestiaryEntryScreenComponent {
    private static final int COMPONENT_TEXTURE_HEIGHT = 48;
    private static final int TITLE_TEXTURE_HEIGHT = 12;
    private static final int TITLE_TEXTURE_WIDTH = 20;
    private static final int TITLE_TEXTURE_LEFT_WIDTH = 1;
    private static final int TITLE_TEXTURE_MIDDLE_WIDTH = 18;
    private static final int TITLE_TEXTURE_RIGHT_WIDTH = 1;

    public static final int ENTRY_HEIGHT = COMPONENT_TEXTURE_HEIGHT + TITLE_TEXTURE_HEIGHT;
    public static final int ENTRY_WIDTH = 212;

    private static final ResourceLocation COMPONENT_TEXTURE_DARK =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/component_dark.png");
    private static final ResourceLocation COMPONENT_TEXTURE_LIGHT =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/component_light.png");
    private static final ResourceLocation TITLE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/component_title.png");

    private final ResourceLocation mobId;
    private final BestiaryData data;
    private final EntityType<?> entityType;

    private Set<BestiaryTooltip> tooltips = new HashSet<>();

    private static final Font FONT = Minecraft.getInstance().font;

    public BestiaryEntryScreenComponent(ResourceLocation mobId, BestiaryData data){
        this.mobId = mobId;
        this.data = data;
        this.entityType = BuiltInRegistries.ENTITY_TYPE.get(mobId);
    }

    public String getDisplayName(){
        return entityType != null ? entityType.getDescription().getString() : mobId.toString();
    }

    public void render(GuiGraphics guiGraphics, int x, int y){
        this.tooltips.clear();

        drawTitle(guiGraphics, x + 2, y, this.getDisplayName());

        drawComponent(guiGraphics, x , y + TITLE_TEXTURE_HEIGHT);

        if(entityType != null){
            drawMobIcon(guiGraphics, x + 8, y + TITLE_TEXTURE_HEIGHT + 12, entityType);
        }

        this.tooltips.add(new BestiaryTooltip(
                x + 168, x + ENTRY_WIDTH - 4,
                y + TITLE_TEXTURE_HEIGHT + 4, y + ENTRY_HEIGHT - 4,
                List.of(Component.literal(String.format("%d kills", data.kills())),
                        Component.literal(String.format("%d needed, %d remaining", (data.level() + 1) * 2, data.remaining())))
        ));

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(2.5F, 2.5F, 1.0F);

        String levelText = String.format("%d", data.level());
        int levelWidth = FONT.width(levelText);
        float levelTextX = ((float)x + (float)ENTRY_WIDTH - 23F) / 2.5F - (float)levelWidth / 2F;
        float levelTextY = ((float)y + 27F) / 2.5F;


        guiGraphics.drawString(FONT, levelText,
                levelTextX, levelTextY, -1, false);

        guiGraphics.pose().popPose();

        guiGraphics.drawString(FONT,
                String.format("x%.2f damage dealt",
                        data.mobBuff().damageFactor()),
                x + 52, y + 24, 0xAAAAAA);
        guiGraphics.drawString(FONT,
                String.format("x%.3f damage taken",
                        data.mobBuff().resistanceFactor()),
                        x + 52, y + 40, 0xAAAAAA);
    }

    public Set<BestiaryTooltip> getTooltips(){
        return this.tooltips;
    }

    private void drawTitle(GuiGraphics guiGraphics, int x, int y, String name) {
        int titleWidth = FONT.width(name);

        guiGraphics.blit(TITLE_TEXTURE, x, y,
                0, 0,
                TITLE_TEXTURE_LEFT_WIDTH, TITLE_TEXTURE_HEIGHT,
                TITLE_TEXTURE_WIDTH, TITLE_TEXTURE_HEIGHT);

        int xPadding = 3;
        int middleXEnd = x + TITLE_TEXTURE_LEFT_WIDTH + xPadding * 2 + titleWidth;

        for(int middleXBlit = x + TITLE_TEXTURE_LEFT_WIDTH; middleXBlit < middleXEnd; middleXBlit += TITLE_TEXTURE_MIDDLE_WIDTH){
            int blitWidth = Math.min(TITLE_TEXTURE_MIDDLE_WIDTH, middleXEnd - middleXBlit);
            guiGraphics.blit(TITLE_TEXTURE, middleXBlit, y,
                    TITLE_TEXTURE_LEFT_WIDTH, 0,
                    blitWidth, TITLE_TEXTURE_HEIGHT,
                    TITLE_TEXTURE_WIDTH, TITLE_TEXTURE_HEIGHT);
        }

        guiGraphics.blit(TITLE_TEXTURE, middleXEnd, y,
                TITLE_TEXTURE_WIDTH - TITLE_TEXTURE_RIGHT_WIDTH, 0,
                TITLE_TEXTURE_RIGHT_WIDTH, TITLE_TEXTURE_HEIGHT,
                TITLE_TEXTURE_WIDTH, TITLE_TEXTURE_HEIGHT);

        guiGraphics.drawString(FONT, name,
                x + TITLE_TEXTURE_LEFT_WIDTH + xPadding, y + 3, 0xFFFFFF);
    }

    private void drawComponent(GuiGraphics guiGraphics, int x, int y) {
        int split = Math.round((float)ENTRY_WIDTH * (float)((data.level() + 1) * 2 - data.remaining()) / (float)((data.level() + 1) * 2));
        guiGraphics.blit(COMPONENT_TEXTURE_LIGHT, x, y,
                0, 0,
                split, COMPONENT_TEXTURE_HEIGHT,
                ENTRY_WIDTH, COMPONENT_TEXTURE_HEIGHT);
        guiGraphics.blit(COMPONENT_TEXTURE_DARK, x + split, y,
                split, 0,
                ENTRY_WIDTH - split, COMPONENT_TEXTURE_HEIGHT,
                ENTRY_WIDTH, COMPONENT_TEXTURE_HEIGHT);
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
