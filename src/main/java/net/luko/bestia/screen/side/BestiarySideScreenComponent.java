package net.luko.bestia.screen.side;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.luko.bestia.Bestia;
import net.luko.bestia.screen.BestiaryScreen;
import net.luko.bestia.screen.BestiaryTooltip;
import net.luko.bestia.screen.widget.CustomButton;
import net.luko.bestia.screen.widget.ScrollBarWidget;
import net.luko.bestia.util.ResourceUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.HashSet;
import java.util.Set;

public abstract class BestiarySideScreenComponent {
    protected int x, y;
    protected int width, height;
    protected CustomButton closeButton;
    protected ScrollBarWidget scrollBar;
    protected final BestiaryScreen parentScreen;
    protected final int availableWidth;

    protected static final int BORDER_SIZE = 9;
    protected static final int TEXTURE_WIDTH = 240;
    protected static final int TEXTURE_HEIGHT = 294;

    protected static final int OUTSIDE_BORDER_SIZE = 3;

    protected static final int BUTTON_SIZE = 12;

    public static final int PADDING = 4;

    protected float scrollAmount = 0F;

    protected Set<BestiaryTooltip> tooltips = new HashSet<>();

    protected static final ResourceLocation TEXTURE =
            ResourceUtil.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/side_panel.png");

    public BestiarySideScreenComponent(int x, int y, int width, BestiaryScreen parentScreen){
        this.x = x;
        this.y = y;

        this.parentScreen = parentScreen;

        this.width = width;
        this.availableWidth = this.width - (2 * PADDING) - (2 * BORDER_SIZE);

        // Call finalizeLayout() later, in case getNeededHeight() needs inheritor-specific data.
    }

    public int getScrollBarThumbHeight(){
        return (int)(((float)this.getVisibleHeight() / this.getNeededHeight()) * this.getScrollBarTrackHeight());
    }

    public int getScrollBarTrackHeight(){
        return this.height - BUTTON_SIZE - OUTSIDE_BORDER_SIZE;
    }

    public void finalizeLayout(){
        this.height = Math.min(
                PADDING * 2 + BORDER_SIZE * 2 + getNeededHeight(),
                parentScreen.height - PADDING * 2
        );

        if(parentScreen.height < (this.height + 2 * y)){
            this.y = (parentScreen.height - this.height) / 2;
        }

        this.closeButton = new CustomButton(this.x + this.width - BUTTON_SIZE, this.y,
                BUTTON_SIZE, BUTTON_SIZE,
                Component.literal("X"),
                btn -> this.parentScreen.clearSideScreenComponent()
        );

        this.scrollBar = new ScrollBarWidget(this.x + this.width - BORDER_SIZE + 1, this.y + BUTTON_SIZE,
                4, this.getScrollBarTrackHeight(),
                Component.literal("Scroll"), getScrollBarThumbHeight(),
                (normalized) -> this.scrollAmount = normalized * getMaxScroll());
        this.scrollBar.visible = this.getMaxScroll() > 0;
    }

    public abstract int getNeededHeight();

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY){
        this.tooltips.clear();
        blitPanel(guiGraphics);

        this.closeButton.render(guiGraphics, mouseX, mouseY, 0F);
        this.scrollBar.visible = this.getMaxScroll() > 0;
        this.scrollBar.render(guiGraphics, mouseX, mouseY, 0F);

        PoseStack poseStack = guiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, (int)-scrollAmount, 0);

        Minecraft mc = Minecraft.getInstance();
        int scissorX = this.x + BORDER_SIZE;
        int scissorY = this.y + OUTSIDE_BORDER_SIZE;
        int scissorWidth = this.width;
        int scissorHeight = this.height - 2 * OUTSIDE_BORDER_SIZE;
        float scaleFactor = (float) mc.getWindow().getScreenWidth() / (float) mc.getWindow().getGuiScaledWidth();
        int windowHeight = Minecraft.getInstance().getWindow().getHeight();

        RenderSystem.enableScissor(
                (int)(scissorX * scaleFactor),
                (int)(windowHeight - (scissorY + scissorHeight) * scaleFactor) - 1,
                (int)(scissorWidth * scaleFactor),
                (int)(scissorHeight * scaleFactor) + 2
        );

        renderContent(guiGraphics, mouseX, mouseY);

        poseStack.popPose();

        this.renderContentButtons(guiGraphics, mouseX, mouseY);
        RenderSystem.disableScissor();

    }

    public abstract void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY);

    public void renderContentButtons(GuiGraphics guiGraphics, int mouseX, int mouseY){}

    public Set<BestiaryTooltip> getTooltips(){
        return this.tooltips;
    }

    public void moveX(int x){
        this.x = x;
        // I don't think closeButton is ever null, but I did get an NPE with it once so I guard to be safe.
        if(this.closeButton != null) this.closeButton.setX(this.x + this.width - BUTTON_SIZE);
        if(this.scrollBar != null) this.scrollBar.setX(this.x + this.width - BORDER_SIZE + 1);
    }

    protected void blitPanel(GuiGraphics guiGraphics){
        /* Top-left corner */ guiGraphics.blit(TEXTURE,
                x, y,
                0, 0,
                BORDER_SIZE, BORDER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);
        /* Top border */ guiGraphics.blitRepeating(TEXTURE,
                x + BORDER_SIZE, y,
                width - BORDER_SIZE - BORDER_SIZE, BORDER_SIZE,
                BORDER_SIZE, 0,
                TEXTURE_WIDTH - BORDER_SIZE - BORDER_SIZE, BORDER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);
        /* Top-right corner */ guiGraphics.blit(TEXTURE,
                x + width - BORDER_SIZE, y,
                TEXTURE_WIDTH - BORDER_SIZE, 0,
                BORDER_SIZE, BORDER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);

        /* Left border */ guiGraphics.blitRepeating(TEXTURE,
                x, y + BORDER_SIZE,
                BORDER_SIZE, height - BORDER_SIZE - BORDER_SIZE,
                0, BORDER_SIZE,
                BORDER_SIZE, TEXTURE_HEIGHT - BORDER_SIZE - BORDER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);
        /* Middle */ guiGraphics.blitRepeating(TEXTURE,
                x + BORDER_SIZE, y + BORDER_SIZE,
                width - BORDER_SIZE - BORDER_SIZE, height - BORDER_SIZE - BORDER_SIZE,
                BORDER_SIZE, BORDER_SIZE,
                TEXTURE_WIDTH - BORDER_SIZE - BORDER_SIZE, TEXTURE_HEIGHT - BORDER_SIZE - BORDER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);
        /* Right border */ guiGraphics.blitRepeating(TEXTURE,
                x + width - BORDER_SIZE, y + BORDER_SIZE,
                BORDER_SIZE, height - BORDER_SIZE - BORDER_SIZE,
                TEXTURE_WIDTH - BORDER_SIZE, BORDER_SIZE,
                BORDER_SIZE, TEXTURE_HEIGHT - BORDER_SIZE - BORDER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);


        /* Bottom-left corner */ guiGraphics.blit(TEXTURE,
                x, y + height - BORDER_SIZE,
                0, TEXTURE_HEIGHT - BORDER_SIZE,
                BORDER_SIZE, BORDER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);
        /* Bottom border */ guiGraphics.blitRepeating(TEXTURE,
                x + BORDER_SIZE, y + height - BORDER_SIZE,
                width - BORDER_SIZE - BORDER_SIZE, BORDER_SIZE,
                BORDER_SIZE, TEXTURE_HEIGHT - BORDER_SIZE,
                TEXTURE_WIDTH - BORDER_SIZE - BORDER_SIZE, BORDER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);
        /* Bottom-right corner */ guiGraphics.blit(TEXTURE,
                x + width - BORDER_SIZE, y + height - BORDER_SIZE,
                TEXTURE_WIDTH - BORDER_SIZE, TEXTURE_HEIGHT - BORDER_SIZE,
                BORDER_SIZE, BORDER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public boolean handleContentClick(double mouseX, double mouseY, int button){
        return false;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(!containsMouse((int)mouseX, (int)mouseY)) return false;

        if(closeButton.mouseClicked(mouseX, mouseY, button)) return true;

        if(this.scrollBar.mouseClicked(mouseX, mouseY, button)) return true;

        return this.handleContentClick(mouseX, mouseY, button);
    }

    public boolean containsMouse(int mouseX, int mouseY){
        return mouseX >= this.x + OUTSIDE_BORDER_SIZE && mouseX <= this.x + this.width - OUTSIDE_BORDER_SIZE
                && mouseY >= this.y + OUTSIDE_BORDER_SIZE && mouseY <= this.y + this.height - OUTSIDE_BORDER_SIZE;
    }

    public int getVisibleHeight(){
        return this.height - 2 * BORDER_SIZE - 2 * PADDING;
    }

    public int getMaxScroll(){
        return Math.max(0, getNeededHeight() - getVisibleHeight());
    }

    public void updateScrollBar(){
        float maxScroll = this.getMaxScroll();
        if(maxScroll <= 0){
            this.scrollBar.visible = false;
            return;
        }
        this.scrollBar.setThumbHeight(this.getScrollBarThumbHeight());
        this.scrollBar.setScrollAmount(this.scrollAmount / this.getMaxScroll());
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta){
        if(this.containsMouse((int)mouseX, (int)mouseY)){
            this.scrollAmount -= (float) delta * 20F;

            float maxScroll = this.getMaxScroll();
            this.scrollAmount = Mth.clamp(this.scrollAmount, 0, maxScroll);

            this.updateScrollBar();

            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button){
        return this.scrollBar.mouseReleased(mouseX, mouseY, button);
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY){
        return this.scrollBar.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
}
