package net.luko.bestiary.screen;

import net.luko.bestiary.data.BestiaryData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.luko.bestiary.screen.BestiaryEntryScreenComponent.ENTRY_HEIGHT;

public class BestiaryScreen extends Screen {
    private static final int PADDING = 8;
    private static final int LEFT_MARGIN = 20;

    private List<BestiaryEntryScreenComponent> bestiaryEntryScreenComponents = new ArrayList<>();
    private float scrollAmount = 0F;
    private int totalContentHeight = 0;

    public BestiaryScreen(Map<ResourceLocation, BestiaryData> entries) {
        super(Component.literal("Bestiary"));
        for(var entry : entries.entrySet()){
            this.bestiaryEntryScreenComponents.add(
                    new BestiaryEntryScreenComponent(entry.getKey(), entry.getValue()));
        }
    }

    @Override
    protected void init(){
        super.init();
        totalContentHeight = bestiaryEntryScreenComponents.size() * (ENTRY_HEIGHT + PADDING);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick){
        renderBackground(guiGraphics);

        guiGraphics.fill(0, 0, this.width, this.height, 0xFF202020);

        int yOffset = 20 - (int)scrollAmount;
        for(var entry : bestiaryEntryScreenComponents){
            if(yOffset > -ENTRY_HEIGHT && yOffset < this.height){
                entry.render(guiGraphics, LEFT_MARGIN, yOffset, this.width - LEFT_MARGIN * 2);
            }
            yOffset += ENTRY_HEIGHT + PADDING;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta){
        this.scrollAmount -= (float)delta * (ENTRY_HEIGHT / 2f);
        this.scrollAmount = Mth.clamp(this.scrollAmount, 0, Math.max(0, totalContentHeight - this.height + 40));
        return true;
    }
}
