package net.luko.bestia.screen.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

public class CustomButton extends Button {
    @Nullable private ResourceLocation texture;
    @Nullable private ResourceLocation hoveredTexture;
    private int uvX;
    private int uvY;
    float scale = 1.0F;

    public CustomButton(int pX, int pY, int pWidth, int pHeight, OnPress pOnPress,
                        ResourceLocation texture, ResourceLocation hoveredTexture,
                        float scale) {
        super(pX, pY, pWidth, pHeight, Component.literal(""), pOnPress, Button.DEFAULT_NARRATION);
        this.texture = texture;
        this.hoveredTexture = hoveredTexture;
        this.scale = scale;
        this.uvX = (int)(this.width / scale);
        this.uvY = (int)(this.height / scale);
    }

    public CustomButton(int pX, int pY, int pWidth, int pHeight, Component pMessage, OnPress pOnPress) {
        super(pX, pY, pWidth, pHeight, pMessage, pOnPress, Button.DEFAULT_NARRATION);
        this.texture = null;
        this.hoveredTexture = null;
    }

    @Override
    public void setFocused(boolean f){}

    @Override
    public boolean isFocused(){return false;}

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
        if (this.visible) {
            this.isHovered = this.isMouseOver(mouseX, mouseY);

            this.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
        }
    }

    public void setActive(boolean active){
        this.active = active;
    }



    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
        if(this.texture == null){
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
            return;
        }

        PoseStack poseStack = guiGraphics.pose();

        poseStack.pushPose();
        poseStack.scale(this.scale, this.scale, 1.0F);
        int x = ((int)((float)this.getX() / scale));
        int y = ((int)((float)this.getY() / scale));

        ResourceLocation activeTexture;
        if(this.isHovered && this.hoveredTexture != null) activeTexture = this.hoveredTexture;
        else activeTexture = this.texture;

        guiGraphics.blit(activeTexture,
                x, y,
                0, 0,
                this.uvX, this.uvY,
                this.uvX, this.uvY);

        poseStack.popPose();
    }

    private int minY = 0;
    private int maxY = Integer.MAX_VALUE;

    public void setYClip(int minY, int maxY){
        this.minY = minY;
        this.maxY = maxY;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY){
        if(mouseY >= this.minY && mouseY <= maxY) return super.isMouseOver(mouseX, mouseY);
        return false;
    }
}
