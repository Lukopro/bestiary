package net.luko.bestia.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.luko.bestia.Bestia;
import net.luko.bestia.data.BestiaryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static net.luko.bestia.screen.BestiaryEntryScreenComponent.ENTRY_HEIGHT;

public class BestiaryScreen extends Screen {
    private static final ResourceLocation PANEL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/panel.png");

    private static final int PANEL_TEXTURE_WIDTH = 1742;
    private static final int PANEL_TEXTURE_HEIGHT = 2048;

    private static final int PANEL_BLIT_WIDTH = 336;
    private static final int PANEL_BLIT_HEIGHT = 384;

    private static final int PADDING = 8;
    private static final int LEFT_BLIT_MARGIN =
            (PANEL_BLIT_WIDTH - BestiaryEntryScreenComponent.ENTRY_WIDTH) / 2;

    private static final float SCALE = (float)PANEL_BLIT_HEIGHT / (float)PANEL_TEXTURE_HEIGHT;

    private static final int PANEL_TOP_UV_HEIGHT = 296;
    private static final int PANEL_BOTTOM_UV_HEIGHT = 295;
    private static final int PANEL_MIDDLE_UV_HEIGHT =
            PANEL_TEXTURE_HEIGHT - PANEL_TOP_UV_HEIGHT - PANEL_BOTTOM_UV_HEIGHT;

    private static final int PANEL_TOP_BLIT_HEIGHT =
            Math.round(PANEL_TOP_UV_HEIGHT * SCALE);
    private static final int PANEL_BOTTOM_BLIT_HEIGHT =
            Math.round(PANEL_BOTTOM_UV_HEIGHT * SCALE);
    private static final int PANEL_MIDDLE_BLIT_HEIGHT =
            PANEL_BLIT_HEIGHT - PANEL_TOP_BLIT_HEIGHT - PANEL_BOTTOM_BLIT_HEIGHT;

    private int leftPos;
    private int topPos;

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
        this.leftPos = (this.width - PANEL_BLIT_WIDTH) / 2;
        this.topPos = 16;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick){
        renderBackground(guiGraphics);

        drawPanel(guiGraphics);

        renderEntries(guiGraphics, mouseX, mouseY, partialTick);

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private void drawPanel(GuiGraphics guiGraphics){
        guiGraphics.blit(PANEL_TEXTURE,
                this.leftPos, this.topPos,
                PANEL_BLIT_WIDTH, PANEL_TOP_BLIT_HEIGHT,
                0, 0,
                PANEL_TEXTURE_WIDTH, PANEL_TOP_UV_HEIGHT,
                PANEL_TEXTURE_WIDTH, PANEL_TEXTURE_HEIGHT);

        int middleTopPos = this.topPos + PANEL_TOP_BLIT_HEIGHT;
        int availableMiddleHeight = getPanelContentHeight() - PANEL_TOP_BLIT_HEIGHT - PANEL_BOTTOM_BLIT_HEIGHT;
        if(availableMiddleHeight <= 0) return;
        for(int i = 0; i < availableMiddleHeight; i += PANEL_MIDDLE_BLIT_HEIGHT){
            int drawHeight = Math.min(PANEL_MIDDLE_BLIT_HEIGHT, availableMiddleHeight - i);
            guiGraphics.blit(PANEL_TEXTURE,
                    this.leftPos, middleTopPos + i,
                    PANEL_BLIT_WIDTH, drawHeight,
                    0, PANEL_TOP_UV_HEIGHT,
                    PANEL_TEXTURE_WIDTH, PANEL_MIDDLE_UV_HEIGHT,
                    PANEL_TEXTURE_WIDTH, PANEL_TEXTURE_HEIGHT);
        }

        guiGraphics.blit(PANEL_TEXTURE,
                this.leftPos, middleTopPos + availableMiddleHeight,
                PANEL_BLIT_WIDTH, PANEL_BOTTOM_BLIT_HEIGHT,
                0, PANEL_TEXTURE_HEIGHT - PANEL_BOTTOM_UV_HEIGHT,
                PANEL_TEXTURE_WIDTH, PANEL_BOTTOM_UV_HEIGHT,
                PANEL_TEXTURE_WIDTH, PANEL_TEXTURE_HEIGHT);
    }

    private void renderEntries(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int scissorX = this.leftPos;
        int scissorY = this.topPos + PANEL_TOP_BLIT_HEIGHT;
        int scissorWidth = PANEL_BLIT_WIDTH;
        int scissorHeight = getPanelContentHeight() - PANEL_TOP_BLIT_HEIGHT - PANEL_BOTTOM_BLIT_HEIGHT;
        int scaleFactor = (int) Minecraft.getInstance().getWindow().getGuiScale();
        int windowHeight = Minecraft.getInstance().getWindow().getHeight();

        RenderSystem.enableScissor(
                scissorX * scaleFactor,
                (windowHeight - (scissorY + scissorHeight) * scaleFactor),
                scissorWidth * scaleFactor,
                scissorHeight * scaleFactor
        );

        int yOffset = this.topPos + PANEL_TOP_BLIT_HEIGHT + 4 - (int)scrollAmount;
        for(var entry : bestiaryEntryScreenComponents){
            if(yOffset > -ENTRY_HEIGHT && yOffset < this.height){
                entry.render(guiGraphics, this.leftPos + LEFT_BLIT_MARGIN, yOffset);
            }
            yOffset += ENTRY_HEIGHT + PADDING;
        }

        RenderSystem.disableScissor();
    }

    private int getPanelContentHeight(){
        return this.height - this.topPos * 2;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta){
        this.scrollAmount -= (float)delta * (ENTRY_HEIGHT / 2f);
        this.scrollAmount = Mth.clamp(this.scrollAmount, 0, Math.max(0, totalContentHeight - this.height + 40));
        return true;
    }
}
