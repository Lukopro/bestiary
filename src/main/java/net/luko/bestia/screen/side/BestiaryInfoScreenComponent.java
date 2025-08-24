package net.luko.bestia.screen.side;

import net.luko.bestia.screen.BestiaryScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public class BestiaryInfoScreenComponent extends BestiarySideScreenComponent{
    public static final int TEXT_LINE_SPACING = 3;

    public static final List<Component> INFO_TEXT = List.of(
            Component.literal("Welcome to the Bestiary!"),
            Component.literal(""),
            Component.literal("Kill mobs to add them to the Bestiary."),
            Component.literal("Get more kills to acquire buffs."),
            Component.literal("Use the search bar to find mobs.")
    );

    private final List<FormattedCharSequence> wrappedLines;

    public BestiaryInfoScreenComponent(int x, int y, int width, BestiaryScreen parentScreen){
        super(x, y, width, parentScreen);
        this.wrappedLines = getWrappedLines();
        this.finalizeLayout();
    }

    @Override
    public int getNeededHeight() {
        return getWrappedLines().size() * (FONT.lineHeight + TEXT_LINE_SPACING) - TEXT_LINE_SPACING;
    }

    public List<FormattedCharSequence> getWrappedLines(){
        List<FormattedCharSequence> wrappedLines = new ArrayList<>();
        for(Component line : INFO_TEXT){
            List<FormattedCharSequence> wrap = FONT.split(
                    line, this.availableWidth);
            wrappedLines.addAll(wrap);
        }

        return wrappedLines;
    }

    @Override
    public void renderContent(GuiGraphics guiGraphics, int mouseX, int mouseY){
        this.drawText(guiGraphics,
                x + BORDER_SIZE + PADDING, y + BORDER_SIZE + PADDING);
    }

    private void drawText(GuiGraphics guiGraphics, int x, int y) {
        int color = 0xFFFFFF;

        for (int i = 0; i < this.wrappedLines.size(); i++) {
            int lineY = y + i * (FONT.lineHeight + TEXT_LINE_SPACING);
            FormattedCharSequence line = this.wrappedLines.get(i);

            guiGraphics.drawString(FONT, line, x, lineY, color);
        }
    }
}
