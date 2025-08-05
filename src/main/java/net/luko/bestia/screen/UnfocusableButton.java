package net.luko.bestia.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class UnfocusableButton extends Button {
    @Nullable private ResourceLocation texture;
    @Nullable private ResourceLocation hoveredTexture;
    private int uvX;
    private int uvY;
    float scale = 1.0F;
    boolean scaled = false;

    protected UnfocusableButton(int pX, int pY, int pWidth, int pHeight, OnPress pOnPress,
                                ResourceLocation texture, ResourceLocation hoveredTexture,
                                float scale) {
        super(pX, pY, pWidth, pHeight, Component.literal(""), pOnPress, Button.DEFAULT_NARRATION);
        this.texture = texture;
        this.hoveredTexture = hoveredTexture;
        this.scale = scale;
        this.scaled = true;
        this.uvX = (int)(this.width / scale);
        this.uvY = (int)(this.height / scale);
    }

    protected UnfocusableButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, Button.DEFAULT_NARRATION);
        this.texture = null;
        this.hoveredTexture = null;
    }

    @Override
    public void setFocused(boolean f){}

    @Override
    public boolean isFocused(){return false;}

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
        if(this.texture == null){
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
            return;
        }

        PoseStack poseStack = guiGraphics.pose();
        int x = this.getX();
        int y = this.getY();
        if(this.scaled){
            poseStack.pushPose();
            poseStack.scale(this.scale, this.scale, this.scale);
            x = ((int)((float)this.getX() / scale));
            y = ((int)((float)this.getY() / scale));
        }

        ResourceLocation activeTexture;
        if(this.isHovered && this.hoveredTexture != null) activeTexture = this.hoveredTexture;
        else activeTexture = this.texture;

        guiGraphics.blit(activeTexture,
                x, y,
                0, 0,
                this.uvX, this.uvY,
                this.uvX, this.uvY);

        if(scaled) poseStack.popPose();
    }
}
