package net.luko.bestia.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.luko.bestia.Bestia;
import net.luko.bestia.data.BestiaryData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.luko.bestia.screen.BestiaryEntryScreenComponent.ENTRY_HEIGHT;

public class BestiaryScreen extends Screen {
    private static final ResourceLocation PANEL_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Bestia.MODID, "textures/gui/bestiary/panel.png");

    private static final int PANEL_TEXTURE_WIDTH = 336;
    private static final int PANEL_TEXTURE_HEIGHT = 384;

    private static final int PANEL_BLIT_WIDTH = 336;
    private static final int PANEL_BLIT_HEIGHT = 384;

    private static final int PADDING = 8;
    private static final int LEFT_BLIT_MARGIN =
            (PANEL_BLIT_WIDTH - BestiaryEntryScreenComponent.ENTRY_WIDTH) / 2;

    private static final float SCALE = (float)PANEL_BLIT_HEIGHT / (float)PANEL_TEXTURE_HEIGHT;

    private static final int PANEL_TOP_UV_HEIGHT = 54;
    private static final int PANEL_BOTTOM_UV_HEIGHT = 54;
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

    private int leftPosMoveTo;

    private List<BestiaryEntryScreenComponent> bestiaryEntryScreenComponents = new ArrayList<>();
    private List<BestiaryEntryScreenComponent> filteredEntries;

    private EditBox searchBox;

    private BestiaryInfoScreenComponent infoScreenComponent;
    private UnfocusableButton infoToggleButton;
    private static boolean shownBefore = false;

    private static int MARGIN = 4;

    private float scrollAmount = 0F;
    private int totalContentHeight = 0;

    public BestiaryScreen(Map<ResourceLocation, BestiaryData> entries) {
        super(Component.literal("Bestiary"));

        for(var entry : entries.entrySet()){
            this.bestiaryEntryScreenComponents.add(
                    new BestiaryEntryScreenComponent(entry.getKey(), entry.getValue()));
        }
        this.filteredEntries = List.copyOf(this.bestiaryEntryScreenComponents);
    }

    @Override
    protected void init(){
        super.init();

        totalContentHeight = bestiaryEntryScreenComponents.size() * (ENTRY_HEIGHT + PADDING);

        this.leftPos = Math.max(!shownBefore
                ? (this.width - PANEL_BLIT_WIDTH - this.getInfoScreenComponentWidth()) / 2 - 2
                : (this.width - PANEL_BLIT_WIDTH) / 2,
                MARGIN);
        this.topPos = 16;

        this.leftPosMoveTo = this.leftPos;

        this.searchBox = new EditBox(
                this.font,
                this.leftPos + (PANEL_BLIT_WIDTH / 2) - 90,
                this.topPos + 13,
                180, 21,
                Component.literal("Search")
        );

        this.searchBox.setMaxLength(50);
        this.searchBox.setResponder(this::onSearchChanged);
        this.searchBox.setBordered(true);
        this.searchBox.setVisible(true);
        this.addRenderableWidget(this.searchBox);

        this.infoScreenComponent = new BestiaryInfoScreenComponent(
                this.leftPos + PANEL_BLIT_WIDTH + MARGIN,
                this.topPos + PANEL_TOP_BLIT_HEIGHT - 6,
                this.getInfoScreenComponentWidth(),
                this
        );

        this.infoToggleButton = new UnfocusableButton(
                this.leftPos + PANEL_BLIT_WIDTH - 72, this.topPos + 13, 21, 21,
                Component.literal("i"),
                btn -> {
                    this.infoScreenComponent.setVisible(!this.infoScreenComponent.isVisible());
                    shownBefore = true;
                }
        );

        this.addRenderableWidget(infoToggleButton);

        this.infoScreenComponent.setVisible(!shownBefore);
    }

    private int getInfoScreenComponentWidth(){
        int maxPanelWidth = 240;

        int availableWidth = this.width - PANEL_BLIT_WIDTH - 3 * MARGIN;
        return Math.min(maxPanelWidth, availableWidth);
    }

    private int leftPosPrev;

    public void updatePositions(){
        this.leftPosMoveTo = Math.max(infoScreenComponent.isVisible()
                ? (this.width - PANEL_BLIT_WIDTH - this.getInfoScreenComponentWidth()) / 2 - 2
                : (this.width - PANEL_BLIT_WIDTH) / 2,
                MARGIN);

        this.leftPosPrev = leftPos;
        this.leftPos += (int) ((double)(this.leftPosMoveTo - this.leftPos) * 0.4);

        infoScreenComponent.moveX(this.leftPos + PANEL_BLIT_WIDTH + MARGIN);
        searchBox.setX(this.leftPos + (PANEL_BLIT_WIDTH / 2) - 90);
        infoToggleButton.setX(this.leftPos + PANEL_BLIT_WIDTH - 72);
    }

    private void onSearchChanged(String newText){
        String query = newText.trim().toLowerCase();

        this.filteredEntries = this.bestiaryEntryScreenComponents.stream()
                .filter(entry -> entry.getDisplayName().toLowerCase().contains(query))
                .toList();
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers){
        if(this.searchBox != null && this.searchBox.keyPressed(keyCode, scanCode, modifiers)){
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers){
        if(this.searchBox != null && this.searchBox.charTyped(codePoint, modifiers)){
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick){
        if(this.leftPos != this.leftPosMoveTo) updatePositions();

        // I built everything off of leftPos, which is not interpolated between partial ticks. Instead of rewriting everything, I interpolate it now and set it back.
        int uninterpolatedLeftPos = this.leftPos;
        this.leftPos = (int) (this.leftPosPrev + (this.leftPos - leftPosPrev) * partialTick);

        renderBackground(guiGraphics);

        drawPanel(guiGraphics);

        renderEntries(guiGraphics, mouseX, mouseY);

        this.infoScreenComponent.render(guiGraphics, mouseX, mouseY);

        this.leftPos = uninterpolatedLeftPos;

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

    private void renderEntries(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int scissorX = this.leftPos;
        int scissorY = this.topPos + PANEL_TOP_BLIT_HEIGHT - 6;
        int scissorWidth = PANEL_BLIT_WIDTH;
        int scissorHeight = getPanelContentHeight() - PANEL_TOP_BLIT_HEIGHT - PANEL_BOTTOM_BLIT_HEIGHT + 12;
        int scaleFactor = (int) Minecraft.getInstance().getWindow().getGuiScale();
        int windowHeight = Minecraft.getInstance().getWindow().getHeight();

        RenderSystem.enableScissor(
                scissorX * scaleFactor,
                (windowHeight - (scissorY + scissorHeight) * scaleFactor),
                scissorWidth * scaleFactor,
                scissorHeight * scaleFactor
        );

        int yOffset = this.topPos + PANEL_TOP_BLIT_HEIGHT + PADDING - (int)scrollAmount;
        for(var entry : filteredEntries){
            if(yOffset > -ENTRY_HEIGHT && yOffset < this.height){
                entry.render(guiGraphics, this.leftPos + LEFT_BLIT_MARGIN, yOffset);
            }
            yOffset += ENTRY_HEIGHT + PADDING;
        }

        RenderSystem.disableScissor();

        List<Component> tooltipToRender = getTooltip(mouseX, mouseY);

        if(tooltipToRender != null) guiGraphics.renderTooltip(
                Minecraft.getInstance().font, tooltipToRender, Optional.empty(), mouseX, mouseY
        );
    }

    private @Nullable List<Component> getTooltip(int mouseX, int mouseY) {
        List<Component> tooltipToRender = null;
        if(mouseX >= leftPos + LEFT_BLIT_MARGIN && mouseX <= leftPos + PANEL_BLIT_WIDTH - LEFT_BLIT_MARGIN
        && mouseY >= topPos + PANEL_TOP_BLIT_HEIGHT && mouseY <= topPos - PANEL_TOP_BLIT_HEIGHT + getPanelContentHeight()){
            for(var component : bestiaryEntryScreenComponents){
                for(var tooltip : component.getTooltips()){
                    if(tooltip.contains(mouseX, mouseY)){
                        tooltipToRender = tooltip.tooltip();
                        break;
                    }
                }
            }
        }
        return tooltipToRender;
    }

    private int getPanelContentHeight(){
        return this.height - this.topPos * 2;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta){
        this.scrollAmount -= (float)delta * (ENTRY_HEIGHT / 2f);

        int visibleHeight = (this.height - PADDING - 2 * this.topPos - PANEL_TOP_BLIT_HEIGHT - PANEL_BOTTOM_BLIT_HEIGHT);
        float maxScroll = Math.max(0, totalContentHeight - visibleHeight);

        this.scrollAmount = Mth.clamp(this.scrollAmount, 0, maxScroll);
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(infoScreenComponent.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
