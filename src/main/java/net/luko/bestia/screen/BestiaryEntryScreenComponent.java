package net.luko.bestia.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.luko.bestia.Bestia;
import net.luko.bestia.data.BestiaryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
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
    private static final int LEVEL_BAR_HEIGHT = 8;
    private static final int LEVEL_BAR_WIDTH = 212;
    private static final int LEVEL_BAR_PADDING = 1;

    public static final int ENTRY_HEIGHT = COMPONENT_TEXTURE_HEIGHT + TITLE_TEXTURE_HEIGHT + LEVEL_BAR_HEIGHT + LEVEL_BAR_PADDING;
    public static final int ENTRY_WIDTH = 212;

    private static final ResourceLocation COMPONENT_TEXTURE_DARK =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/component_dark.png");
    private static final ResourceLocation COMPONENT_TEXTURE_LIGHT =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/component_light.png");

    private static final ResourceLocation TITLE_TEXTURE_DARK =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/title_dark.png");
    private static final ResourceLocation TITLE_TEXTURE_LIGHT =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/title_light.png");

    private static final ResourceLocation LEVEL_COMPLETED_DARK =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/level_completed_dark.png");
    private static final ResourceLocation LEVEL_COMPLETED_LIGHT =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/level_completed_light.png");
    private static final ResourceLocation LEVEL_BACKGROUND_DARK =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/level_background_dark.png");
    private static final ResourceLocation LEVEL_BACKGROUND_LIGHT =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/level_background_light.png");


    private final ResourceLocation mobId;
    private final BestiaryData data;
    private final EntityType<?> entityType;
    private final @Nullable BestiaryScreen parentScreen;

    private Set<BestiaryTooltip> tooltips = new HashSet<>();

    private static final Font FONT = Minecraft.getInstance().font;

    public boolean mouseIsHovering = false;

    public BestiaryEntryScreenComponent(ResourceLocation mobId, BestiaryData data, @Nullable BestiaryScreen parentScreen){
        this.mobId = mobId;
        this.data = data;
        this.entityType = BuiltInRegistries.ENTITY_TYPE.get(mobId);
        this.parentScreen = parentScreen;
    }

    public String getDisplayName(){
        return entityType != null ? entityType.getDescription().getString() : mobId.toString();
    }

    public void checkMouse(int x, int y, int mouseX, int mouseY){
        // Check if it's over the title
        if(mouseX >= x + 2 && mouseX < x + 2 + this.getTitleBlitWidth(this.getDisplayName())
        && mouseY >= y && mouseY < y + TITLE_TEXTURE_HEIGHT){
            this.mouseIsHovering = true;
            return;
        }

        // Check if it's over the main component or level bar
        if(mouseX >= x && mouseX < x + ENTRY_WIDTH
        && mouseY >= y + TITLE_TEXTURE_HEIGHT && mouseY < y + ENTRY_HEIGHT){
            this.mouseIsHovering = true;
            return;
        }

        this.mouseIsHovering = false;
    }

    public void render(GuiGraphics guiGraphics, int x, int y){
        this.tooltips.clear();

        drawTitle(guiGraphics, x + 2, y, this.getDisplayName());

        drawKills(guiGraphics, x + ENTRY_WIDTH - 2, y, String.format("%d kill%s", this.data.kills(), this.data.kills() == 1 ? "" : "s"));

        drawComponent(guiGraphics, x, y + TITLE_TEXTURE_HEIGHT);

        drawLevelBar(guiGraphics, x + ENTRY_WIDTH / 2, y + ENTRY_HEIGHT - LEVEL_BAR_HEIGHT);

        drawLevelText(guiGraphics, x, y);

        if(entityType != null) drawMobIcon(guiGraphics, x + 8, y + TITLE_TEXTURE_HEIGHT + 12, entityType);

        guiGraphics.drawString(FONT,
                String.format("x%.2f damage dealt",
                        data.mobBuff().damageFactor()),
                x + 52, y + 24, 0xAAAAAA);
        guiGraphics.drawString(FONT,
                String.format("x%.3f damage taken",
                        data.mobBuff().resistanceFactor()),
                        x + 52, y + 40, 0xAAAAAA);

        this.tooltips.add(new BestiaryTooltip(
                x, x + ENTRY_WIDTH,
                y + ENTRY_HEIGHT - LEVEL_BAR_HEIGHT, y + ENTRY_HEIGHT,
                List.of(Component.literal(String.format(
                        "%.1f%% (%d/%d kills)",
                        (1F - ((float)this.data.remainingKills() / (float)this.data.neededForNextLevel())) * 100F,
                        this.data.neededForNextLevel() - this.data.remainingKills(),
                        this.data.neededForNextLevel())))
        ));

        this.mouseIsHovering = false;
    }

    public Set<BestiaryTooltip> getTooltips(){
        return this.tooltips;
    }

    private final int xPadding = 3;

    private int getTitleBlitWidth(String name){
        return TITLE_TEXTURE_LEFT_WIDTH + xPadding * 2 + FONT.width(name) + TITLE_TEXTURE_RIGHT_WIDTH;
    }

    private void drawTitle(GuiGraphics guiGraphics, int x, int y, String name) {
        int textWidth = FONT.width(name);
        ResourceLocation backgroundTexture = mouseIsHovering ? TITLE_TEXTURE_LIGHT : TITLE_TEXTURE_DARK;

        guiGraphics.blit(backgroundTexture, x, y,
                0, 0,
                TITLE_TEXTURE_LEFT_WIDTH, TITLE_TEXTURE_HEIGHT,
                TITLE_TEXTURE_WIDTH, TITLE_TEXTURE_HEIGHT);

        int middleXEnd = x + TITLE_TEXTURE_LEFT_WIDTH + xPadding * 2 + textWidth;

        for(int middleXBlit = x + TITLE_TEXTURE_LEFT_WIDTH; middleXBlit < middleXEnd; middleXBlit += TITLE_TEXTURE_MIDDLE_WIDTH){
            int blitWidth = Math.min(TITLE_TEXTURE_MIDDLE_WIDTH, middleXEnd - middleXBlit);
            guiGraphics.blit(backgroundTexture, middleXBlit, y,
                    TITLE_TEXTURE_LEFT_WIDTH, 0,
                    blitWidth, TITLE_TEXTURE_HEIGHT,
                    TITLE_TEXTURE_WIDTH, TITLE_TEXTURE_HEIGHT);
        }

        guiGraphics.blit(backgroundTexture, middleXEnd, y,
                TITLE_TEXTURE_WIDTH - TITLE_TEXTURE_RIGHT_WIDTH, 0,
                TITLE_TEXTURE_RIGHT_WIDTH, TITLE_TEXTURE_HEIGHT,
                TITLE_TEXTURE_WIDTH, TITLE_TEXTURE_HEIGHT);

        guiGraphics.drawString(FONT, name,
                x + TITLE_TEXTURE_LEFT_WIDTH + xPadding, y + 3, 0xFFFFFF);
    }

    private void drawKills(GuiGraphics guiGraphics, int maxX, int y, String kills){
        int textWidth = FONT.width(kills);
        ResourceLocation titleTexture = mouseIsHovering ? TITLE_TEXTURE_LIGHT : TITLE_TEXTURE_DARK;
        int x = maxX - xPadding * 2 - TITLE_TEXTURE_RIGHT_WIDTH - TITLE_TEXTURE_LEFT_WIDTH - textWidth;

        guiGraphics.blit(titleTexture, x, y,
                0, 0,
                TITLE_TEXTURE_LEFT_WIDTH, TITLE_TEXTURE_HEIGHT,
                TITLE_TEXTURE_WIDTH, TITLE_TEXTURE_HEIGHT);

        int middleXEnd = x + TITLE_TEXTURE_LEFT_WIDTH + xPadding * 2 + textWidth;

        for(int middleXBlit = x + TITLE_TEXTURE_LEFT_WIDTH; middleXBlit < middleXEnd; middleXBlit += TITLE_TEXTURE_MIDDLE_WIDTH){
            int blitWidth = Math.min(TITLE_TEXTURE_MIDDLE_WIDTH, middleXEnd - middleXBlit);
            guiGraphics.blit(titleTexture, middleXBlit, y,
                    TITLE_TEXTURE_LEFT_WIDTH, 0,
                    blitWidth, TITLE_TEXTURE_HEIGHT,
                    TITLE_TEXTURE_WIDTH, TITLE_TEXTURE_HEIGHT);
        }

        guiGraphics.blit(titleTexture, middleXEnd, y,
                TITLE_TEXTURE_WIDTH - TITLE_TEXTURE_RIGHT_WIDTH, 0,
                TITLE_TEXTURE_RIGHT_WIDTH, TITLE_TEXTURE_HEIGHT,
                TITLE_TEXTURE_WIDTH, TITLE_TEXTURE_HEIGHT);

        guiGraphics.drawString(FONT, kills,
                x + TITLE_TEXTURE_LEFT_WIDTH + xPadding, y + 3, 0xFFFFFF);
    }

    private void drawComponent(GuiGraphics guiGraphics, int x, int y) {
        ResourceLocation texture = this.mouseIsHovering ? COMPONENT_TEXTURE_LIGHT : COMPONENT_TEXTURE_DARK;
        guiGraphics.blit(texture, x, y,
                0, 0,
                ENTRY_WIDTH, COMPONENT_TEXTURE_HEIGHT,
                ENTRY_WIDTH, COMPONENT_TEXTURE_HEIGHT);
    }

    private void drawLevelBar(GuiGraphics guiGraphics, int middleX, int y) {
        ResourceLocation completedTexture = this.mouseIsHovering ? LEVEL_COMPLETED_LIGHT : LEVEL_COMPLETED_DARK;
        ResourceLocation backgroundTexture = this.mouseIsHovering ? LEVEL_BACKGROUND_LIGHT : LEVEL_BACKGROUND_DARK;

        int x = middleX - LEVEL_BAR_WIDTH / 2;
        int split = Math.round((float)ENTRY_WIDTH * (float)(data.neededForNextLevel() - data.remainingKills()) / (float)data.neededForNextLevel());

        guiGraphics.blit(completedTexture, x, y,
                0, 0,
                split, LEVEL_BAR_HEIGHT,
                LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);
        guiGraphics.blit(backgroundTexture, x + split, y,
                split, 0,
                LEVEL_BAR_WIDTH - split, LEVEL_BAR_HEIGHT,
                LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);
    }

    private void drawLevelText(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(2.5F, 2.5F, 1.0F);

        String levelText = String.format("%d", data.level());
        int levelWidth = FONT.width(levelText);
        float levelTextX = ((float)x + (float)ENTRY_WIDTH - 23F) / 2.5F - (float)levelWidth / 2F;
        float levelTextY = ((float)y + 27F) / 2.5F;


        guiGraphics.drawString(FONT, levelText,
                levelTextX, levelTextY, -1, false);

        guiGraphics.pose().popPose();
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

            if(this.mouseIsHovering){
                RenderSystem.enableBlend();
                RenderSystem.setShaderColor(1.2F, 1.2F, 1.2F, 1.0F);
            }

            // In some sort of freak case that renderEntityInInventory throws, RenderSystem is reset and disabled.
            try {
                InventoryScreen.renderEntityInInventory(guiGraphics,
                        x + 16, y + 32 + computeEntityYOffset(living, poseXRot), computeEntityScale(living), pose, camera, living);
            } finally {
                if(this.mouseIsHovering){
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    RenderSystem.disableBlend();
                }
            }
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

    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(this.mouseIsHovering && button == 0){
            if(this.parentScreen != null) parentScreen.openFocusedEntryScreenComponent(this.mobId, this.data);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return false;
    }
}
