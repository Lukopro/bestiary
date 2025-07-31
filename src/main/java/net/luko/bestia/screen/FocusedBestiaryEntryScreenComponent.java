package net.luko.bestia.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.luko.bestia.data.BestiaryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Quaternionf;

import java.awt.*;
import java.util.List;

public class FocusedBestiaryEntryScreenComponent extends BestiarySideScreenComponent{
    protected final ResourceLocation mobId;
    protected final BestiaryData data;
    protected final EntityType<?> entityType;
    protected final Font FONT;

    public FocusedBestiaryEntryScreenComponent(int x, int y, int width, BestiaryScreen parentScreen, ResourceLocation mobId, BestiaryData data) {
        super(x, y, width, parentScreen);
        this.mobId = mobId;
        this.data = data;
        this.entityType = BuiltInRegistries.ENTITY_TYPE.get(mobId);
        this.FONT = Minecraft.getInstance().font;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY){
        super.render(guiGraphics, mouseX, mouseY);

        int adjustedX = this.x + BORDER_SIZE + PADDING;
        int adjustedY = this.x + BORDER_SIZE + PADDING;

        int nextY = renderTopHalf(guiGraphics, adjustedX, adjustedY);
        if(data.level() >= 10) renderBottomHalf(nextY);

    }

    private int renderTopHalf(GuiGraphics guiGraphics, int x, int y) {
        int middleX = (2 * x * availableWidth) / 2;
        int maxX = x + this.availableWidth;
        int maxExpectedMobWidth = 30;

        int leftY = y;

        drawMobIcon(guiGraphics, x, leftY += 30, this.entityType);
        leftY = drawComponentWrapped(guiGraphics,
                Component.literal(String.format("x%f damage dealt", this.data.mobBuff().damageFactor())),
                x, middleX, leftY);
        leftY = drawComponentWrapped(guiGraphics,
                Component.literal(String.format("x%f damage blocked", 1 - this.data.mobBuff().resistanceFactor())),
                x, middleX, leftY);

        int rightY = y;
        rightY = drawCenteredComponentWrapped(guiGraphics,
                Component.literal(this.getDisplayName()),
                x + maxExpectedMobWidth, maxX, rightY);
        rightY = drawCenteredComponentWrapped(guiGraphics,
                Component.literal(String.format("Level %d", this.data.level())),
                x + maxExpectedMobWidth, maxX, rightY);
        rightY = drawCenteredComponentWrapped(guiGraphics,
                Component.literal(String.format("%d kills", this.data.kills())),
                middleX, maxX, rightY);
        rightY = drawCenteredComponentWrapped(guiGraphics,
                Component.literal(String.format("%d kills needed", this.data.remainingKills())),
                middleX, maxX, rightY);

        rightY += 6;



        int maxY = Math.max(leftY, rightY);


        return ;
    }

    private int drawCenteredComponentWrapped(GuiGraphics guiGraphics, FormattedText text, int minX, int maxX, int y){
        List<FormattedCharSequence> wrappedText = FONT.split(text, maxX - minX);
        int nextY = y;
        for(FormattedCharSequence line : wrappedText){
            guiGraphics.drawCenteredString(
                    FONT, line, minX / 2, nextY += FONT.lineHeight, 0xFFFFFF);
        }
        return nextY;
    }

    private int drawComponentWrapped(GuiGraphics guiGraphics, FormattedText text, int minX, int maxX, int y){
        List<FormattedCharSequence> wrappedText = FONT.split(text, maxX - minX);
        int nextY = y;
        for(FormattedCharSequence line : wrappedText){
            guiGraphics.drawString(
                    FONT, line, (maxX + minX) / 2, nextY += FONT.lineHeight, 0xFFFFFF);
        }
        return nextY;
    }

    public String getDisplayName(){
        return entityType != null ? entityType.getDescription().getString() : mobId.toString();
    }

    @Override
    public int getNeededHeight() {

        return 50;
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
