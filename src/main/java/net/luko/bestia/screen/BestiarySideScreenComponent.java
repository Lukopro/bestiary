package net.luko.bestia.screen;

import net.luko.bestia.Bestia;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public abstract class BestiarySideScreenComponent {
    protected int x, y;
    protected final int width, height;
    protected final UnfocusableButton closeButton;
    protected final BestiaryScreen parentScreen;
    protected final int availableWidth;

    protected static final int BORDER_SIZE = 9;
    protected static final int TEXTURE_WIDTH = 240;
    protected static final int TEXTURE_HEIGHT = 294;

    public static final int PADDING = 4;

    protected static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/side_panel.png");

    public BestiarySideScreenComponent(int x, int y, int width, BestiaryScreen parentScreen){
        this.x = x;
        this.y = y;

        this.parentScreen = parentScreen;

        this.width = width;
        this.height = PADDING * 2 + BORDER_SIZE * 2 + getNeededHeight();

        this.availableWidth = this.width - 2 * PADDING - 2 * BORDER_SIZE;

        if(parentScreen.height < this.height + 2 * y){
            this.y = (parentScreen.height - this.height) / 2;
        }

        this.closeButton = new UnfocusableButton(this.x + this.width - 12, this.y,
                12, 12,
                Component.literal("X"),
                btn -> this.parentScreen.clearSideScreenComponent());
    }

    public abstract int getNeededHeight();

    public void setX(int x){
        this.x = x;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY){
        blitPanel(guiGraphics);

        closeButton.render(guiGraphics, mouseX, mouseY, 0F);
    }

    public void moveX(int x){
        this.setX(x);
        this.closeButton.setX(this.x + this.width - 12);
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

    public boolean mouseClicked(double mouseX, double mouseY, int button){
        return closeButton.mouseClicked(mouseX, mouseY, button);
    }
}
