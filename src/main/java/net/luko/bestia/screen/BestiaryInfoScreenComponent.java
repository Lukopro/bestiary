package net.luko.bestia.screen;

import net.luko.bestia.Bestia;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class BestiaryInfoScreenComponent {
    private int x, y;
    private final int width, height;
    private final UnfocusableButton closeButton;
    private boolean visible = true;
    private final BestiaryScreen parentScreen;

    private static final int BORDER_SIZE = 9;
    private static final int TEXTURE_WIDTH = 240;
    private static final int TEXTURE_HEIGHT = 294;

    public static final int PADDING = 4;
    public static final int TEXT_LINE_SPACING = 3;

    public static final List<Component> INFO_TEXT = List.of(
            Component.literal("Welcome to the Bestiary!"),
            Component.literal(""),
            Component.literal("Kill mobs to add them to the Bestiary."),
            Component.literal("Get more kills to acquire buffs."),
            Component.literal("Use the search bar to find mobs.")
    );

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/info_panel.png");

    private List<FormattedCharSequence> wrappedLines;

    public BestiaryInfoScreenComponent(int x, int y, int width, BestiaryScreen parentScreen){
        this.x = x;
        this.y = y;

        this.parentScreen = parentScreen;

        this.width = width;
        this.height = this.getWrappedLines().size() * (Minecraft.getInstance().font.lineHeight + TEXT_LINE_SPACING)
                + PADDING * 2 - TEXT_LINE_SPACING + BORDER_SIZE * 2;

        if(parentScreen.height < this.height + 2 * y){
            this.y = (parentScreen.height - this.height) / 2;
        }

        this.closeButton = new UnfocusableButton(this.x + this.width - 12, this.y,
                12, 12,
                Component.literal("X"),
                btn -> this.setVisible(false));

        this.wrappedLines = getWrappedLines();
    }

    public void setX(int x){
        this.x = x;
    }

    public List<FormattedCharSequence> getWrappedLines(){
        Font font = Minecraft.getInstance().font;
        List<FormattedCharSequence> wrappedLines = new ArrayList<>();
        for(Component line : INFO_TEXT){
            List<FormattedCharSequence> wrap = font.split(
                    line, width - 2 * PADDING - 2 * BORDER_SIZE);
            wrappedLines.addAll(wrap);
        }

        return wrappedLines;
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY){
        if(!visible) return;

        blitPanel(guiGraphics);

        closeButton.render(guiGraphics, mouseX, mouseY, 0F);

        this.drawText(guiGraphics,
                x + BORDER_SIZE + PADDING, y + BORDER_SIZE + PADDING);
    }

    public void moveX(int x){
        this.setX(x);
        this.closeButton.setX(this.x + this.width - 12);
    }

    private void blitPanel(GuiGraphics guiGraphics){
        /* Top-left corner */ guiGraphics.blit(TEXTURE,
                x, y,
                0, -1,
                BORDER_SIZE, BORDER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);
        /* Top border */ guiGraphics.blitRepeating(TEXTURE,
                x + BORDER_SIZE, y,
                width - BORDER_SIZE - BORDER_SIZE, BORDER_SIZE,
                BORDER_SIZE, -1,
                TEXTURE_WIDTH - BORDER_SIZE - BORDER_SIZE, BORDER_SIZE,
                TEXTURE_WIDTH, TEXTURE_HEIGHT);
        /* Top-right corner */ guiGraphics.blit(TEXTURE,
                x + width - BORDER_SIZE, y,
                TEXTURE_WIDTH - BORDER_SIZE, -1,
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

    private void drawText(GuiGraphics guiGraphics, int x, int y) {
        Font font = Minecraft.getInstance().font;
        int color = 0xFFFFFF;
        int outlineColor = 0x26000000;


        for (int i = 0; i < this.wrappedLines.size(); i++) {
            int lineY = y + i * (font.lineHeight + TEXT_LINE_SPACING);
            FormattedCharSequence line = this.wrappedLines.get(i);

            guiGraphics.drawString(font, line, x + 1, lineY, outlineColor, false);
            guiGraphics.drawString(font, line, x - 1, lineY, outlineColor, false);
            guiGraphics.drawString(font, line, x, lineY + 1, outlineColor, false);
            guiGraphics.drawString(font, line, x, lineY - 1, outlineColor, false);

            guiGraphics.drawString(font, line, x, lineY, color);
        }
    }

    public boolean isVisible(){
        return visible;
    }

    public void setVisible(boolean visible){
        this.visible = visible;

        this.closeButton.visible = visible;
        this.closeButton.active = visible;

        parentScreen.updatePositions();

        closeButton.setX(x + width - 12);
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(!visible) return false;
        return closeButton.mouseClicked(mouseX, mouseY, button);
    }
}
