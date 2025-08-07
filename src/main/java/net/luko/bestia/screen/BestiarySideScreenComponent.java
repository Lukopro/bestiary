package net.luko.bestia.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.luko.bestia.Bestia;
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
    protected final BestiaryScreen parentScreen;
    protected final int availableWidth;

    protected static final int BORDER_SIZE = 9;
    protected static final int TEXTURE_WIDTH = 240;
    protected static final int TEXTURE_HEIGHT = 294;

    protected static final int OUTSIDE_BORDER_SIZE = 3;

    public static final int PADDING = 4;

    protected float scrollAmount = 0F;

    protected Set<BestiaryTooltip> tooltips = new HashSet<>();

    protected static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/side_panel.png");

    public BestiarySideScreenComponent(int x, int y, int width, BestiaryScreen parentScreen){
        this.x = x;
        this.y = y;

        this.parentScreen = parentScreen;

        this.width = width;
        this.availableWidth = this.width - (2 * PADDING) - (2 * BORDER_SIZE);

        this.closeButton = new CustomButton(this.x + this.width - 12, this.y,
                12, 12,
                Component.literal("X"),
                btn -> this.parentScreen.clearSideScreenComponent()
        );
    }

    public void finalizeLayout(){
        this.height = Math.min(
                PADDING * 2 + BORDER_SIZE * 2 + getNeededHeight(),
                parentScreen.height - PADDING * 2
        );

        if(parentScreen.height < (this.height + 2 * y)){
            this.y = (parentScreen.height - this.height) / 2;
            this.closeButton.setY(this.y);
        }
    }

    public abstract int getNeededHeight();

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY){
        this.tooltips.clear();
        blitPanel(guiGraphics);

        closeButton.render(guiGraphics, mouseX, mouseY, 0F);

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
        if(this.closeButton != null) this.closeButton.setX(this.x + this.width - 12);
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

        return this.handleContentClick(mouseX, mouseY, button);
    }

    public boolean containsMouse(int mouseX, int mouseY){
        return mouseX >= this.x + OUTSIDE_BORDER_SIZE && mouseX <= this.x + this.width - OUTSIDE_BORDER_SIZE
                && mouseY >= this.y + OUTSIDE_BORDER_SIZE && mouseY <= this.y + this.height - OUTSIDE_BORDER_SIZE;
    }

    public void onScroll(){}

    public boolean mouseScrolled(double mouseX, double mouseY, double delta){
        if(this.containsMouse((int)mouseX, (int)mouseY)){
            this.scrollAmount -= (float) delta * 20F;

            float maxScroll = Math.max(0, getNeededHeight() - (this.height - 2 * BORDER_SIZE - 2 * PADDING));
            this.scrollAmount = Mth.clamp(this.scrollAmount, 0, maxScroll);

            this.onScroll();

            return true;
        }
        return false;
    }
}
