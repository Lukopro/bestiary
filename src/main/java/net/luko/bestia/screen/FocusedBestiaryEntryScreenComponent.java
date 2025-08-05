package net.luko.bestia.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.luko.bestia.Bestia;
import net.luko.bestia.data.BestiaryData;
import net.luko.bestia.data.buff.special.SpecialBuff;
import net.luko.bestia.data.buff.special.SpecialBuffRegistry;
import net.luko.bestia.util.RomanUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;

import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class FocusedBestiaryEntryScreenComponent extends BestiarySideScreenComponent{
    protected final ResourceLocation mobId;
    protected final BestiaryData data;
    protected final EntityType<?> entityType;
    protected final Font FONT;
    protected static final ResourceLocation LEVEL_BAR_COMPLETED_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/level_completed_light.png");
    protected static final ResourceLocation LEVEL_BAR_BACKGROUND_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/level_background_light.png");

    protected static final int LEVEL_BAR_WIDTH = 212;
    protected static final int LEVEL_BAR_LEFT_BLIT = 8;
    protected static final int LEVEL_BAR_RIGHT_BLIT = 8;
    protected static final int LEVEL_BAR_MIDDLE_BLIT = LEVEL_BAR_WIDTH - LEVEL_BAR_LEFT_BLIT - LEVEL_BAR_RIGHT_BLIT;
    protected static final int LEVEL_BAR_HEIGHT = 8;
    protected final BestiaryEntryScreenComponent entry;

    protected final int BUTTON_UV_DIMENSIONS = 12;
    protected final int BUTTON_BLIT_DIMENSIONS = 24;
    protected final float BUTTON_SCALE = (float)BUTTON_BLIT_DIMENSIONS / (float)BUTTON_UV_DIMENSIONS;
    protected final float BUFF_TITLE_SCALE = 2.0F;

    protected Map<SpecialBuff<?>, UnfocusableButton> specialBuffButtons = new HashMap<>();

    protected ResourceLocation DEFAULT_BUFF_ICON_UNHOVERED =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/buffs/default.png");
    protected ResourceLocation DEFAULT_BUFF_ICON_HOVERED =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/buffs/default_hovered.png");

    public FocusedBestiaryEntryScreenComponent(int x, int y, int width, BestiaryScreen parentScreen, ResourceLocation mobId, BestiaryData data) {
        super(x, y, width, parentScreen);

        this.mobId = mobId;
        this.data = data;
        this.entry = new BestiaryEntryScreenComponent(this.mobId, this.data, null);
        this.entityType = BuiltInRegistries.ENTITY_TYPE.get(mobId);

        this.FONT = Minecraft.getInstance().font;

        this.finalizeLayout();
        this.initializeButtons();
    }

    public void initializeButtons(){
        if(this.data.level() >= 10){
            for(var buff : getBuffs().entrySet()){
                ResourceLocation buffId = buff.getKey().getId();

                String iconUnhoveredPath = "textures/gui/bestiary/buff/" + buffId.getPath() + ".png";
                ResourceLocation iconUnhoveredTexture = ResourceLocation.fromNamespaceAndPath(
                        buffId.getNamespace(), iconUnhoveredPath);

                String iconHoveredPath = "textures/gui/bestiary/buff/" + buffId.getPath() + "_hovered.png";
                ResourceLocation iconHoveredTexture = ResourceLocation.fromNamespaceAndPath(
                        buffId.getNamespace(), iconHoveredPath);
                try {
                    Minecraft.getInstance().getResourceManager().getResource(iconUnhoveredTexture);
                }catch(Exception e){
                    Bestia.LOGGER.warn("Missing texture for {}", iconUnhoveredTexture);
                    iconUnhoveredTexture = DEFAULT_BUFF_ICON_UNHOVERED;
                    iconHoveredTexture = DEFAULT_BUFF_ICON_HOVERED;
                }

                try {
                    Minecraft.getInstance().getResourceManager().getResource(iconHoveredTexture);
                }catch(Exception e){
                    Bestia.LOGGER.warn("Missing texture for {}", iconHoveredTexture);
                    iconUnhoveredTexture = DEFAULT_BUFF_ICON_UNHOVERED;
                    iconHoveredTexture = DEFAULT_BUFF_ICON_HOVERED;
                }

                this.specialBuffButtons.put(buff.getKey(),
                        new UnfocusableButton(0, 0,
                                BUTTON_BLIT_DIMENSIONS, BUTTON_BLIT_DIMENSIONS,
                                btn -> {}/*ModPackets.CHANNEL.sendToServer(new SpendPointPacket(this.mobId, buff.getKey().getId()))*/,
                                iconUnhoveredTexture, iconHoveredTexture,
                                BUTTON_SCALE)
                );
            }
        }
    }

    @Override
    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY){
        PoseStack poseStack = guiGraphics.pose();

        int adjustedX = this.x + BORDER_SIZE + PADDING;
        int adjustedY = this.y + BORDER_SIZE + PADDING;
        float headerScale = (float)this.availableWidth / (float)BestiaryEntryScreenComponent.ENTRY_WIDTH;
        poseStack.pushPose();
        poseStack.scale(headerScale, headerScale, headerScale);
        this.entry.render(guiGraphics, scaled(adjustedX, headerScale) + 1, scaled(adjustedY, headerScale));
        poseStack.popPose();

        renderPointSection(guiGraphics,
                adjustedX, adjustedY + (int)((float)BestiaryEntryScreenComponent.ENTRY_HEIGHT * headerScale),
                mouseX, mouseY);
    }

    private int renderPointSection(GuiGraphics guiGraphics, int x, int y, int mouseX, int mouseY){
        int rightX = x + availableWidth;
        int nextY = y + 4; // magic padding #1

        if(data.level() >= 10){
            nextY = drawCenteredComponentWrapped(guiGraphics,
                    Component.literal(String.format("%d point%s to spend", this.data.remainingPoints(), this.data.remainingPoints() == 1 ? "" : "s")),
                    x, rightX, nextY,
                    0xAAAAAA);
            nextY += 4; // magic padding #2

            Map<SpecialBuff<?>, Integer> buffList = getBuffs();
            for(var entry : buffList.entrySet()){
                PoseStack poseStack = guiGraphics.pose();
                UnfocusableButton button = specialBuffButtons.get(entry.getKey());

                button.setX(x + 2);
                button.setY(nextY + 14 - (int)this.scrollAmount);

                poseStack.pushPose();
                poseStack.scale(BUFF_TITLE_SCALE, BUFF_TITLE_SCALE, BUFF_TITLE_SCALE);

                int wrapY = drawComponentWrapped(guiGraphics,
                        Component.literal(String.format(
                                "%s %s", entry.getKey().getDisplayName(), RomanUtil.toRoman(entry.getValue()))),
                        scaled(x + (int)(BUTTON_UV_DIMENSIONS * BUFF_TITLE_SCALE + 6), BUFF_TITLE_SCALE),
                        scaled(rightX, BUFF_TITLE_SCALE),
                        scaled(nextY, BUFF_TITLE_SCALE));

                nextY = Math.max((nextY + BUTTON_BLIT_DIMENSIONS + 4),
                        scaled(wrapY, BUFF_TITLE_SCALE));

                nextY += 5; // magic padding #3

                poseStack.popPose();

                nextY = drawComponentWrapped(guiGraphics,
                        Component.literal(entry.getKey().getInfo(entry.getValue())),
                        x, rightX, nextY, 0xAAAAAA);
            }
            nextY += 12; // magic padding #4
        }

        nextY += 4; // magic padding #5

        int lastTenth = Mth.floor((float)this.data.level() / 10F) * 10;
        int nextTenth = lastTenth + 10;

        int lastTenthWidth = FONT.width(String.valueOf(lastTenth));
        guiGraphics.drawString(FONT, String.valueOf(lastTenth),
                x, nextY, 0xFFFFFF);

        int nextTenthWidth = FONT.width(String.valueOf(nextTenth));
        guiGraphics.drawString(FONT, String.valueOf(nextTenth),
                rightX - nextTenthWidth, nextY, 0xFFFFFF);

        float splitFactor = ((float)this.data.kills() - (float)BestiaryData.totalNeededForLevel(lastTenth))
                / ((float)BestiaryData.totalNeededForLevel(nextTenth) - (float)BestiaryData.totalNeededForLevel(lastTenth));
        nextY = drawLevelBar(guiGraphics, x + lastTenthWidth + 2, rightX - nextTenthWidth - 2, nextY, splitFactor);
        return nextY;
    }

    @Override
    public void renderContentButtons(GuiGraphics guiGraphics, int mouseX, int mouseY){
        for(UnfocusableButton button : this.specialBuffButtons.values()){
            button.render(guiGraphics, mouseX, mouseY, 0F);
        }
    }

    public Collection<UnfocusableButton> getButtons(){
        return specialBuffButtons.values();
    }

    private int scaled(int coordinate, float scale){
        return (int)((float)coordinate / scale);
    }

    private Map<SpecialBuff<?>, Integer> getBuffs(){
        Map<SpecialBuff<?>, Integer> existingUnorderedBuffs = new LinkedHashMap<>();
        for(var entry : this.data.spentPoints().entrySet()){
            SpecialBuff<?> buff = SpecialBuffRegistry.get(entry.getKey());
            if(buff != null) existingUnorderedBuffs.put(buff, entry.getValue());
        }
        Map<SpecialBuff<?>, Integer> allOrderedBuffs = new LinkedHashMap<>(existingUnorderedBuffs.entrySet().stream()
                .sorted((a, b) -> b.getValue() - a.getValue())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                )));
        for(SpecialBuff<?> registered : SpecialBuffRegistry.all()){
            if(!allOrderedBuffs.containsKey(registered)) allOrderedBuffs.put(registered, 0);
        }
        return allOrderedBuffs;
    }

    private int drawCenteredComponentWrapped(GuiGraphics guiGraphics, FormattedText text, int minX, int maxX, int y){
        List<FormattedCharSequence> wrappedText = FONT.split(text, maxX - minX);
        int nextY = y;
        for(FormattedCharSequence line : wrappedText){
            guiGraphics.drawCenteredString(
                    FONT, line, (minX + maxX) / 2, nextY += FONT.lineHeight, 0xFFFFFF);
        }
        return nextY + 2;
    }

    private int drawCenteredComponentWrapped(GuiGraphics guiGraphics, FormattedText text, int minX, int maxX, int y, int color){
        List<FormattedCharSequence> wrappedText = FONT.split(text, maxX - minX);
        int nextY = y;
        for(FormattedCharSequence line : wrappedText){
            guiGraphics.drawCenteredString(
                    FONT, line, (minX + maxX) / 2, nextY += FONT.lineHeight, color);
        }
        return nextY + 2;
    }

    private int drawComponentWrapped(GuiGraphics guiGraphics, FormattedText text, int minX, int maxX, int y){
        List<FormattedCharSequence> wrappedText = FONT.split(text, maxX - minX);
        int nextY = y;
        for(FormattedCharSequence line : wrappedText){
            guiGraphics.drawString(
                    FONT, line, minX, nextY += FONT.lineHeight, 0xFFFFFF);
        }
        return nextY + 2;
    }

    private int drawComponentWrapped(GuiGraphics guiGraphics, FormattedText text, int minX, int maxX, int y, int color){
        List<FormattedCharSequence> wrappedText = FONT.split(text, maxX - minX);
        int nextY = y;
        for(FormattedCharSequence line : wrappedText){
            guiGraphics.drawString(
                    FONT, line, minX, nextY += FONT.lineHeight, color);
        }
        return nextY + 2;
    }

    private int getComponentWrappedHeight(FormattedText text, int width, float scale){
        List<FormattedCharSequence> wrappedText = FONT.split(text, (int)((float)width / scale));
        return (int)((float)(wrappedText.size() * FONT.lineHeight + 2) * scale);
    }

    private int drawLevelBar(GuiGraphics guiGraphics, int x1, int x2, int y, float splitFactor){
        int leftMiddleBlit = x1 + LEVEL_BAR_LEFT_BLIT;
        int rightMiddleBlit = x2 - LEVEL_BAR_RIGHT_BLIT;

        int barWidth = x2 - x1;

        int blitSplit = x1 + Math.round((float)barWidth * splitFactor);
        int uvSplit = Math.round((float)(blitSplit - x1) / barWidth * LEVEL_BAR_WIDTH);
        if(blitSplit < leftMiddleBlit){
            guiGraphics.blit(LEVEL_BAR_COMPLETED_TEXTURE, x1, y,
                    0, 0,
                    uvSplit, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);
            guiGraphics.blit(LEVEL_BAR_BACKGROUND_TEXTURE, blitSplit - 1, y,
                    uvSplit, 0,
                    leftMiddleBlit - blitSplit + 1, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);

            guiGraphics.blitRepeating(LEVEL_BAR_BACKGROUND_TEXTURE, leftMiddleBlit, y,
                    rightMiddleBlit - leftMiddleBlit, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_LEFT_BLIT, 0,
                    LEVEL_BAR_MIDDLE_BLIT, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);

            guiGraphics.blit(LEVEL_BAR_BACKGROUND_TEXTURE, rightMiddleBlit, y,
                    LEVEL_BAR_WIDTH - LEVEL_BAR_RIGHT_BLIT, 0,
                    LEVEL_BAR_RIGHT_BLIT, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);
        } else if(blitSplit < rightMiddleBlit){
            guiGraphics.blit(LEVEL_BAR_COMPLETED_TEXTURE, x1, y,
                    0, 0,
                    LEVEL_BAR_LEFT_BLIT, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);

            guiGraphics.blitRepeating(LEVEL_BAR_COMPLETED_TEXTURE, leftMiddleBlit, y,
                    blitSplit - leftMiddleBlit, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_LEFT_BLIT, 0,
                    LEVEL_BAR_MIDDLE_BLIT, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);
            guiGraphics.blitRepeating(LEVEL_BAR_BACKGROUND_TEXTURE, blitSplit - 1, y,
                    rightMiddleBlit - blitSplit + 1, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_LEFT_BLIT, 0,
                    LEVEL_BAR_MIDDLE_BLIT, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);

            guiGraphics.blit(LEVEL_BAR_BACKGROUND_TEXTURE, rightMiddleBlit, y,
                    LEVEL_BAR_WIDTH - LEVEL_BAR_RIGHT_BLIT, 0,
                    LEVEL_BAR_RIGHT_BLIT, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);
        } else {
            guiGraphics.blit(LEVEL_BAR_COMPLETED_TEXTURE, x1, y,
                    0, 0,
                    LEVEL_BAR_LEFT_BLIT, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);

            guiGraphics.blitRepeating(LEVEL_BAR_COMPLETED_TEXTURE, leftMiddleBlit, y,
                    rightMiddleBlit - leftMiddleBlit, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_LEFT_BLIT, 0,
                    LEVEL_BAR_MIDDLE_BLIT, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);

            guiGraphics.blit(LEVEL_BAR_COMPLETED_TEXTURE, rightMiddleBlit, y,
                    LEVEL_BAR_WIDTH - LEVEL_BAR_RIGHT_BLIT, 0,
                    blitSplit - rightMiddleBlit, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);
            guiGraphics.blit(LEVEL_BAR_BACKGROUND_TEXTURE, blitSplit - 1, y,
                    uvSplit, 0,
                    x2 - blitSplit + 1, LEVEL_BAR_HEIGHT,
                    LEVEL_BAR_WIDTH, LEVEL_BAR_HEIGHT);
        }

        return y + getLevelBarHeight();
    }

    private int getLevelBarHeight(){
        return 8;
    }

    @Override
    public int getNeededHeight() {
        int y = 0;
        y += BestiaryEntryScreenComponent.ENTRY_HEIGHT * ((float)this.availableWidth / (float)BestiaryEntryScreenComponent.ENTRY_WIDTH);
        y += 4; // magic padding #1
        if(data.level() >= 10){
            y += getComponentWrappedHeight(Component.literal(String.format("%d point%s to spend",
                            this.data.remainingPoints(), this.data.remainingPoints() == 1 ? "" : "s")),
                    this.availableWidth, 1.0F);
            y += 4; // magic padding #2
            for(var entry : getBuffs().entrySet()){
                y += Math.max((BUTTON_BLIT_DIMENSIONS + 4),
                        getComponentWrappedHeight(Component.literal(String.format(
                                "%s %s", entry.getKey().getDisplayName(), RomanUtil.toRoman(entry.getValue()))),
                                this.availableWidth, BUFF_TITLE_SCALE));
                y += 5; // magic padding #3
                y += getComponentWrappedHeight(Component.literal(entry.getKey().getInfo(entry.getValue())), this.availableWidth, 1.0F);
            }
            y += 12; // magic padding #4
        }

        y += 4; // magic padding #5
        y += getLevelBarHeight();

        return y + 1; // +1 to account for slight rounding error
    }

    @Override
    public boolean handleContentClick(double mouseX, double mouseY, int button){
        for(UnfocusableButton btn : this.specialBuffButtons.values()){
            if(btn.mouseClicked(mouseX, mouseY, button)) return true;
        }

        return false;
    }
}
