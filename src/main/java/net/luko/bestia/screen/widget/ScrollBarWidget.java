package net.luko.bestia.screen.widget;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import java.util.function.Consumer;

public class ScrollBarWidget extends AbstractWidget {
    private final int trackHeight;
    private int thumbHeight;
    private float scrollAmount; // 0.0 - 1.0 normalized
    private boolean dragging = false;
    private boolean canDrag = true;
    private final Consumer<Float> onScrollChanged;
    private final int trackColor;
    private final int trackColorHovered;
    private final int thumbColor;
    private final int thumbColorHovered;

    public ScrollBarWidget(int x, int y, int width, int height, Component message,
                           int thumbHeight, Consumer<Float> onScrollChanged, int trackColor, int trackColorHovered, int thumbColor, int thumbColorHovered) {
        super(x, y, width, height, message);
        this.trackHeight = height;
        this.thumbHeight = thumbHeight;
        this.scrollAmount = 0F;
        this.onScrollChanged = onScrollChanged;
        this.trackColor = trackColor;
        this.trackColorHovered = trackColorHovered;
        this.thumbColor = thumbColor;
        this.thumbColorHovered = thumbColorHovered;
    }

    public ScrollBarWidget(int x, int y, int width, int height, Component message,
                           int thumbHeight, Consumer<Float> onScrollChanged) {
        super(x, y, width, height, message);
        this.trackHeight = height;
        this.thumbHeight = thumbHeight;
        this.scrollAmount = 0F;
        this.onScrollChanged = onScrollChanged;
        this.trackColor = 0x00000000;
        this.trackColorHovered = 0x44FFFFFF;
        this.thumbColor = 0x88FFFFFF;
        this.thumbColorHovered = 0xDDFFFFFF;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks){
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int youseY, float partialTick) {
        guiGraphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, (this.isHovered || this.dragging) ? this.trackColorHovered : this.trackColor);

        int thumbY = this.thumbTopY();

        guiGraphics.fill(this.getX(), thumbY, this.getX() + this.width, thumbY + this.thumbHeight, (this.isHovered || this.dragging) ? this.thumbColorHovered : this.thumbColor);
    }

    public int thumbTopY(){
        return this.getY() + (int)((this.trackHeight - this.thumbHeight) * this.scrollAmount);
    }

    public int thumbBottomY(){
        return this.thumbTopY() + thumbHeight;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput pNarrationElementOutput) {

    }

    public void setThumbHeight(int thumbHeight){
        this.thumbHeight = thumbHeight;
    }

    public void setScrollAmount(float normalized){
        this.scrollAmount = Mth.clamp(normalized, 0F, 1F);
        this.onScrollChanged.accept(this.scrollAmount);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(!this.visible) return false;
        if(isMouseOver(mouseX, mouseY) && button == 0){
            float perFrameStaticScroll = ((float)this.thumbHeight / (float)this.trackHeight) / 15F;
            if(mouseY > thumbBottomY()){
                this.setScrollAmount(this.scrollAmount + perFrameStaticScroll);
                this.canDrag = false;
            } else if(mouseY < thumbTopY()){
                this.setScrollAmount(this.scrollAmount - perFrameStaticScroll);
                this.canDrag = false;
            } else {
                this.dragging = true;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button){
        if(!this.visible) return false;
        if (button == 0){
            this.dragging = false;
            this.canDrag = true;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY){
        if(!this.visible || this.trackHeight == this.thumbHeight) return false;
        if(this.dragging && this.canDrag){
            double normalizedDrag = dragY / (this.trackHeight - this.thumbHeight);
            this.setScrollAmount(this.scrollAmount + (float)normalizedDrag);
            return true;
        }
        return false;
    }
}