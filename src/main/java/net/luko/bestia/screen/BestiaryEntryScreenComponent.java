package net.luko.bestia.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.luko.bestia.Bestia;
import net.luko.bestia.config.BestiaClientConfig;
import net.luko.bestia.config.BestiaCommonConfig;
import net.luko.bestia.data.BestiaryData;
import net.luko.bestia.data.buff.special.SpecialBuffRegistry;
import net.luko.bestia.util.RomanUtil;
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

import java.util.*;
import java.util.stream.Collectors;

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
    private final boolean focused;

    private Set<BestiaryTooltip> tooltips = new HashSet<>();

    private static final Font FONT = Minecraft.getInstance().font;

    public boolean mouseIsHovering = false;

    public BestiaryEntryScreenComponent(ResourceLocation mobId, BestiaryData data, @Nullable BestiaryScreen parentScreen, boolean focused){
        this.mobId = mobId;
        this.data = data;
        this.entityType = BuiltInRegistries.ENTITY_TYPE.get(mobId);
        this.parentScreen = parentScreen;
        this.focused = focused;
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

        int rightTitleX = this.drawTitle(guiGraphics, x + 2, y, this.getDisplayName());

        this.drawKills(guiGraphics, x + ENTRY_WIDTH - 2, y, String.format("%d kill%s", this.data.kills(), this.data.kills() == 1 ? "" : "s"));

        this.drawComponent(guiGraphics, x, y + TITLE_TEXTURE_HEIGHT);

        this.drawLevelBar(guiGraphics, x + ENTRY_WIDTH / 2, y + ENTRY_HEIGHT - LEVEL_BAR_HEIGHT);

        this.drawLevelText(guiGraphics, x, y);

        if(this.entityType != null) this.drawMobIcon(guiGraphics, x + 8, y + TITLE_TEXTURE_HEIGHT + 12, entityType);

        this.drawContent(guiGraphics, x + 48, 116, y + TITLE_TEXTURE_HEIGHT + 5, 38);

        if(!this.focused && BestiaClientConfig.SHOW_NOTIFICATION_BADGES.get() && BestiaCommonConfig.ENABLE_SPECIAL_BUFFS.get()) this.drawNotificationBadge(guiGraphics, rightTitleX - 4, y - 4);

        this.tooltips.add(new BestiaryTooltip(
                x, x + ENTRY_WIDTH,
                y + ENTRY_HEIGHT - LEVEL_BAR_HEIGHT, y + ENTRY_HEIGHT,
                List.of(Component.literal(this.data.level() >= BestiaCommonConfig.MAX_LEVEL.get()
                        ? "MAX LEVEL!"
                        : String.format(
                        "%.1f%% (%d/%d kills)",
                        (1F - ((float)this.data.remainingKills() / (float)this.data.neededForNextLevel())) * 100F,
                        this.data.neededForNextLevel() - this.data.remainingKills(),
                        this.data.neededForNextLevel())))
        ));

        this.mouseIsHovering = false;
    }

    private final int xPadding = 3;
    private final int verticalPadding = 5;

    private void drawContent(GuiGraphics guiGraphics, int x, int width, int y, int height) {
        String fullDamageText = String.format("x%.2f damage dealt",
                data.mobBuff().damageFactor());

        String fullResistanceText = String.format("x%.3f damage taken",
                data.mobBuff().resistanceFactor());
        if(!focused && BestiaCommonConfig.ENABLE_SPECIAL_BUFFS.get()){
            ResourceLocation damageTexture = ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/buff/damage.png");
            ResourceLocation resistanceTexture = ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/buff/resistance.png");
            String damageText = String.format("x%.2f", data.mobBuff().damageFactor());
            String resistanceText = String.format("x%.3f", data.mobBuff().resistanceFactor());

            int iconSize = 12;
            int damageTextWidth = FONT.width(damageText);
            int resistanceTextWidth = FONT.width(resistanceText);
            int damageWidth = iconSize + xPadding + damageTextWidth;
            int resistanceWidth = iconSize + xPadding + resistanceTextWidth;

            int totalAllowedPadding = width - damageWidth - resistanceWidth;

            // To ensure rounding doesn't offcenter the damage and resistance,
            // modular division is used to see how much can be given to left, middle, and right padding.

            int leftPad = totalAllowedPadding % 3 == 2 ? totalAllowedPadding / 3 + 1 : totalAllowedPadding / 3;
            int midPad = totalAllowedPadding % 3 == 1 ? totalAllowedPadding / 3 + 1 : totalAllowedPadding / 3;

            int nextX = x + leftPad;

            guiGraphics.blit(damageTexture, nextX, y + verticalPadding,
                    iconSize, iconSize,
                    0, 0,
                    iconSize, iconSize,
                    iconSize, iconSize);
            this.tooltips.add(new BestiaryTooltip(nextX, nextX + iconSize,
                    y + verticalPadding, y + verticalPadding + iconSize,
                    List.of(Component.literal(fullDamageText))));
            nextX += iconSize + xPadding;

            guiGraphics.drawString(FONT, damageText, nextX, y + verticalPadding + 2, 0xFFFFFF);
            nextX += damageTextWidth + midPad;
            guiGraphics.blit(resistanceTexture, nextX, y + verticalPadding,
                    iconSize, iconSize,
                    0, 0,
                    iconSize, iconSize,
                    iconSize, iconSize);
            this.tooltips.add(new BestiaryTooltip(nextX, nextX + iconSize,
                    y + verticalPadding, y + verticalPadding + iconSize,
                    List.of(Component.literal(fullDamageText))));
            nextX += iconSize + xPadding;
            guiGraphics.drawString(FONT, resistanceText, nextX, y + verticalPadding + 2, 0xFFFFFF);

            if(data.totalPoints() == data.remainingPoints()){
                String sbText = "No special buffs.";
                int sbTextWidth = FONT.width(sbText);
                int sbTextX = x + (width / 2) - (sbTextWidth / 2);
                guiGraphics.drawString(FONT, sbText, sbTextX, y + height - verticalPadding - FONT.lineHeight - 2, 0xAAAAAA);
            } else {
                this.drawIconsAndRoman(guiGraphics, x, width, y + height - verticalPadding - iconSize);
            }

        } else {
            guiGraphics.drawString(FONT,
                    fullDamageText,
                    x + xPadding, y + verticalPadding + 2, 0xAAAAAA);
            guiGraphics.drawString(FONT,
                    fullResistanceText,
                    x + xPadding, y + height - verticalPadding - FONT.lineHeight - 2, 0xAAAAAA);
        }
    }

    private void drawIconsAndRoman(GuiGraphics guiGraphics, int x, int width, int y){
        Map<ResourceLocation, Integer> applicableSpecialBuffs = new LinkedHashMap<>(data.spentPoints().entrySet().stream()
                .filter(entry -> SpecialBuffRegistry.get(entry.getKey()) != null)
                .filter(entry -> entry.getValue() > 0)
                .sorted((a, b) -> b.getValue() - a.getValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                )));

        List<RenderableSpecialBuff> toBlit = new ArrayList<>();
        int totalToBlitWidth = 0;
        int additionalBuffs = 0;
        String additionalBuffsText = "";
        for(var entry : applicableSpecialBuffs.entrySet()){
            RenderableSpecialBuff ir = new RenderableSpecialBuff(entry.getKey(), entry.getValue());
            totalToBlitWidth += ir.width() + xPadding;

            if(totalToBlitWidth >= width){
                int buffsLeftOver = applicableSpecialBuffs.size() - toBlit.size();
                if(buffsLeftOver > 0 && !toBlit.isEmpty()){
                    RenderableSpecialBuff last = toBlit.get(toBlit.size() - 1);
                    totalToBlitWidth -= last.width();
                    toBlit.remove(toBlit.size() - 1);
                    additionalBuffs = buffsLeftOver + 1;
                    additionalBuffsText = "+" + additionalBuffs;
                    while(totalToBlitWidth >= width - FONT.width(additionalBuffsText)){
                        last = toBlit.get(toBlit.size() - 1);
                        totalToBlitWidth -= last.width();
                        toBlit.remove(toBlit.size() - 1);
                        additionalBuffs++;
                        additionalBuffsText = "+" + additionalBuffs;
                    }
                }
                break;
            }

            toBlit.add(ir);
        }

        int totalWidth = FONT.width(additionalBuffsText);
        for(RenderableSpecialBuff ir : toBlit){
            totalWidth += ir.width() + xPadding;
        }

        int nextX = x + (width / 2) - (totalWidth / 2);
        for(RenderableSpecialBuff ir : toBlit) nextX = ir.blitAndAddTooltip(guiGraphics, nextX, y, this.tooltips) + xPadding;
        if(additionalBuffs > 0) guiGraphics.drawString(FONT, additionalBuffsText, nextX, y + 2, 0xAAAAAA);
    }

    private record RenderableSpecialBuff(ResourceLocation buff, Integer level){
        private static int ICON_SIZE = 12;
        private static float ROMAN_SCALE = 0.6F;

        private String roman(){
            return RomanUtil.toRoman(level);
        }

        private ResourceLocation icon(){
            return ResourceLocation.fromNamespaceAndPath(
                    buff().getNamespace(), "textures/gui/bestiary/buff/" + buff().getPath() + ".png");
        }

        private int width(){
            return ICON_SIZE + (int)Math.ceil((float)FONT.width(this.roman()) * ROMAN_SCALE);
        }

        // x: left, y: center
        private int blitAndAddTooltip(GuiGraphics guiGraphics, int x, int y, Set<BestiaryTooltip> tooltips){
            guiGraphics.blit(this.icon(), x, y,
                    ICON_SIZE, ICON_SIZE,
                    0, 0,
                    ICON_SIZE, ICON_SIZE,
                    ICON_SIZE, ICON_SIZE);
            tooltips.add(new BestiaryTooltip(x, x + ICON_SIZE, y, y + ICON_SIZE,
                    List.of(Component.literal(String.format("%s %s",
                            Component.translatable("buff.special." + this.buff.getNamespace() + "." + this.buff.getPath()).getString(), this.roman())))));

            PoseStack poseStack = guiGraphics.pose();
            poseStack.pushPose();
            poseStack.scale(ROMAN_SCALE, ROMAN_SCALE, 1F);
            poseStack.translate((float)(x + ICON_SIZE) / ROMAN_SCALE, (float)y / ROMAN_SCALE, 0);

            String roman = this.roman();
            int romanWidth = FONT.width(roman);
            guiGraphics.drawString(FONT, roman, (-romanWidth) / 2, 0, 0xFFFFFF);

            poseStack.popPose();

            return x + this.width();
        }
    }

    public Set<BestiaryTooltip> getTooltips(){
        return this.tooltips;
    }

    private int getTitleBlitWidth(String name){
        return TITLE_TEXTURE_LEFT_WIDTH + xPadding * 2 + FONT.width(name) + TITLE_TEXTURE_RIGHT_WIDTH;
    }

    private int drawTitle(GuiGraphics guiGraphics, int x, int y, String name) {
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

        return middleXEnd + TITLE_TEXTURE_RIGHT_WIDTH;
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
        int needed = data.neededForNextLevel();
        int split = needed == 0
        ? ENTRY_WIDTH
        : Math.round((float)ENTRY_WIDTH * (float)(needed - data.remainingKills()) / (float)needed);

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
        String levelText = String.format("%d", data.level());
        int levelWidth = FONT.width(levelText);
        float levelScale = Math.min(2.5F, 34F / levelWidth);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(levelScale, levelScale, 1.0F);


        float centerX = x + ENTRY_WIDTH - 23F;
        float levelTextX = centerX / levelScale - (float)levelWidth / 2F;

        float levelTextY = (y + 20F) / levelScale + 12F / (float)(Math.pow(levelScale, 1.5));


        guiGraphics.drawString(FONT, levelText,
                levelTextX, levelTextY, -1, false);

        guiGraphics.pose().popPose();
    }

    private void drawNotificationBadge(GuiGraphics guiGraphics, int x, int y){
        int unspentPoints = this.data.remainingPoints();
        if(unspentPoints > 0){
            final ResourceLocation WIDGETS = ResourceLocation.fromNamespaceAndPath("bestia", "textures/gui/bestiary/badges.png");
            int widgetX = switch (unspentPoints) {
                case 1 -> 0;
                case 2 -> 11;
                case 3 -> 22;
                case 4 -> 33;
                case 5 -> 44;
                default -> 55;
            };
            int widgetY = 22;
            int widgetWidth = 11;
            int widgetHeight = 11;

            guiGraphics.blit(WIDGETS, x, y,
                    widgetWidth, widgetHeight,
                    widgetX, widgetY,
                    widgetWidth, widgetHeight,
                    66, 11);

            this.tooltips.add(new BestiaryTooltip(x, x + widgetWidth, y, y + widgetHeight,
                    List.of(Component.literal(String.format("%d unspent points", unspentPoints)))));
        }
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

    public int kills(){
        return this.data.kills();
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(this.mouseIsHovering && button == 0 && BestiaCommonConfig.ENABLE_SPECIAL_BUFFS.get()){
            if(this.parentScreen != null) parentScreen.openFocusedEntryScreenComponent(this.mobId, this.data);
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            return true;
        }
        return false;
    }
}
