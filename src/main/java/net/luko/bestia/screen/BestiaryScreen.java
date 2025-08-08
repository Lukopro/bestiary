package net.luko.bestia.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.luko.bestia.Bestia;
import net.luko.bestia.config.BestiaConfig;
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

    private final List<BestiaryEntryScreenComponent> bestiaryEntryScreenComponents = new ArrayList<>();
    private List<BestiaryEntryScreenComponent> filteredEntries;

    private EditBox searchBox;

    private BestiarySideScreenComponent activeSideScreenComponent = null;
    private CustomButton infoToggleButton;
    private static boolean shownBefore = false;

    public static final int MARGIN = 4;

    private float scrollAmount = 0F;
    private int totalContentHeight = 0;

    private int leftPosPrev;

    private boolean onlySideScreen = false;
    private int sideScreenWidth = 0;

    public BestiaryScreen(Map<ResourceLocation, BestiaryData> entries) {
        super(Component.literal("Bestiary"));
        for(var entry : entries.entrySet()){
            this.bestiaryEntryScreenComponents.add(
                    new BestiaryEntryScreenComponent(entry.getKey(), entry.getValue(), this));
        }
        this.filteredEntries = List.copyOf(this.bestiaryEntryScreenComponents);
    }

    @Override
    protected void init(){
        super.init();

        totalContentHeight = bestiaryEntryScreenComponents.size() * (ENTRY_HEIGHT + PADDING);

        this.leftPos = (this.width - PANEL_BLIT_WIDTH) / 2;
        this.leftPosPrev = this.leftPos;
        this.topPos = 16;

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

        this.infoToggleButton = new CustomButton(
                this.leftPos + PANEL_BLIT_WIDTH - 72, this.topPos + 13, 21, 21,
                Component.literal("i"),
                btn -> {
                    if(this.activeSideScreenComponent instanceof BestiaryInfoScreenComponent) this.clearSideScreenComponent();
                    else this.openInfoScreenComponent();
                    shownBefore = true;
                });

        this.addRenderableWidget(infoToggleButton);

        if(!shownBefore) openInfoScreenComponent();
        else clearSideScreenComponent();
    }

    public void rebuiltEntries(Map<ResourceLocation, BestiaryData> entries){
        this.bestiaryEntryScreenComponents.clear();
        for(var entry : entries.entrySet()){
            this.bestiaryEntryScreenComponents.add(
                    new BestiaryEntryScreenComponent(entry.getKey(), entry.getValue(), this));
        }
        this.updateSearch();
    }

    public void openInfoScreenComponent(){
        this.clearSideScreenComponent();
        int maxWidth = 200;
        this.sideScreenWidth = this.getSideScreenComponentWidth(maxWidth, true);

        int maxWidthToRemoveMainPanel = 40;
        if(this.sideScreenWidth < maxWidthToRemoveMainPanel){
            this.sideScreenWidth = this.getSideScreenComponentWidth(maxWidth, false);
            this.onlySideScreen = true;
        }

        int mainPanelWidthAdjustment = this.onlySideScreen ? 0 : PANEL_BLIT_WIDTH;

        this.activeSideScreenComponent = new BestiaryInfoScreenComponent(
                this.leftPos + mainPanelWidthAdjustment + MARGIN,
                this.topPos + PANEL_TOP_BLIT_HEIGHT - 6,
                this.sideScreenWidth,
                this
        );
        this.updateLeftPosMoveTo();
    }

    public void openFocusedEntryScreenComponent(ResourceLocation mobId, BestiaryData data){
        if(!BestiaConfig.ENABLE_SPECIAL_BUFFS.get()) return;
        this.clearSideScreenComponent();
        int max = BestiaryEntryScreenComponent.ENTRY_WIDTH * 2;
        this.sideScreenWidth = this.getSideScreenComponentWidth(max, true);

        int maxWidthToRemoveMainPanel = BestiaryEntryScreenComponent.ENTRY_WIDTH;
        if(this.sideScreenWidth < maxWidthToRemoveMainPanel){
            this.sideScreenWidth = this.getSideScreenComponentWidth(max, false);
            this.onlySideScreen = true;
        }

        int mainPanelWidthAdjustment = this.onlySideScreen ? 0 : PANEL_BLIT_WIDTH;

        this.activeSideScreenComponent = new FocusedBestiaryEntryScreenComponent(
                this.leftPos + mainPanelWidthAdjustment + MARGIN,
                this.topPos + PANEL_TOP_BLIT_HEIGHT - 6,
                this.sideScreenWidth,
                this,
                mobId, data
        );
        this.updateLeftPosMoveTo();
    }

    public void clearSideScreenComponent(){
        this.activeSideScreenComponent = null;
        this.onlySideScreen = false;
        this.sideScreenWidth = 0;
        this.updateLeftPosMoveTo();
        shownBefore = true;
    }

    public BestiarySideScreenComponent getActiveSideScreenComponent(){
        return this.activeSideScreenComponent;
    }

    private int getSideScreenComponentWidth(int max, boolean withMain){
        int availableWidth = withMain
                ? this.width - PANEL_BLIT_WIDTH - 3 * MARGIN
                : this.width - 3 * MARGIN;
        return Math.min(max, availableWidth);
    }

    public void updateLeftPosMoveTo(){

        this.leftPosMoveTo = this.onlySideScreen
                ? (this.width - this.sideScreenWidth - 3 * MARGIN) / 2
                : Math.max(activeSideScreenComponent != null
                        ? (this.width - PANEL_BLIT_WIDTH - this.sideScreenWidth) / 2 - 2
                        : (this.width - PANEL_BLIT_WIDTH) / 2,
                MARGIN);
    }

    public void updateLeftPos(){
        this.leftPosPrev = leftPos;
        this.leftPos += (int) ((double)(this.leftPosMoveTo - this.leftPos) * 0.4);
    }

    public void updatePositions(){
        int mainPanelWidthAdjustment = this.onlySideScreen ? 0 : PANEL_BLIT_WIDTH;
        if(activeSideScreenComponent != null){
            activeSideScreenComponent.moveX(
                    this.leftPos + mainPanelWidthAdjustment + MARGIN);
        }
        if(onlySideScreen) {
            this.searchBox.visible = false;
            this.infoToggleButton.visible = false;
        } else {
            this.searchBox.visible = true;
            this.infoToggleButton.visible = true;
            this.searchBox.setX(this.leftPos + (PANEL_BLIT_WIDTH / 2) - 90);
            this.infoToggleButton.setX(this.leftPos + PANEL_BLIT_WIDTH - 72);
        }
    }

    private void updateSearch(){
        this.onSearchChanged(this.searchBox.getValue());
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
        if(this.leftPos != this.leftPosMoveTo) updateLeftPos();
        renderBackground(guiGraphics);

        // I built everything off of leftPos, which is not interpolated between partial ticks. Instead of rewriting everything, I interpolate it now and set it back.
        int uninterpolatedLeftPos = this.leftPos;
        this.leftPos = (int) (this.leftPosPrev + (this.leftPos - leftPosPrev) * partialTick);

        if(this.activeSideScreenComponent != null) this.activeSideScreenComponent.render(guiGraphics, mouseX, mouseY);


        updatePositions();

        if(!this.onlySideScreen){
            drawPanel(guiGraphics);

            renderEntries(guiGraphics, mouseX, mouseY);
        }

        List<Component> tooltipToRender = getTooltip(mouseX, mouseY);

        if(tooltipToRender != null) guiGraphics.renderTooltip(
                Minecraft.getInstance().font, tooltipToRender, Optional.empty(), mouseX, mouseY
        );

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
                int x = this.leftPos + LEFT_BLIT_MARGIN;
                int y = yOffset;
                entry.render(guiGraphics, x, y);
                if(mouseInScissor(mouseX, mouseY)) entry.checkMouse(x, y, mouseX, mouseY);
            }
            yOffset += ENTRY_HEIGHT + PADDING;
        }

        RenderSystem.disableScissor();
    }

    private boolean mouseInScissor(int mouseX, int mouseY){
        return mouseX >= leftPos + LEFT_BLIT_MARGIN && mouseX <= leftPos + PANEL_BLIT_WIDTH - LEFT_BLIT_MARGIN
                && mouseY >= topPos + PANEL_TOP_BLIT_HEIGHT && mouseY <= topPos - PANEL_TOP_BLIT_HEIGHT + getPanelContentHeight();
    }

    private @Nullable List<Component> getTooltip(int mouseX, int mouseY) {
        List<Component> tooltipToRender = null;
        if(!this.onlySideScreen && mouseInScissor(mouseX, mouseY)){
            for(var component : bestiaryEntryScreenComponents){
                for(var tooltip : component.getTooltips()){
                    if(tooltip.contains(mouseX, mouseY)){
                        tooltipToRender = tooltip.tooltip();
                        break;
                    }
                }
            }
        }

        if(this.activeSideScreenComponent != null){
            for(var tooltip : this.activeSideScreenComponent.getTooltips()){
                if(tooltip.contains(mouseX, mouseY)){
                    tooltipToRender = tooltip.tooltip();
                    break;
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
        if(!onlySideScreen && mouseInScissor((int)mouseX, (int)mouseY)) {
            this.scrollAmount -= (float) delta * 30F;

            int visibleHeight = (this.height - PADDING - 2 * this.topPos - PANEL_TOP_BLIT_HEIGHT - PANEL_BOTTOM_BLIT_HEIGHT);
            float maxScroll = Math.max(0, totalContentHeight - visibleHeight);

            this.scrollAmount = Mth.clamp(this.scrollAmount, 0, maxScroll);
            return true;
        }

        if(this.activeSideScreenComponent instanceof FocusedBestiaryEntryScreenComponent entryScreen
                && entryScreen.mouseScrolled(mouseX, mouseY, delta)) return true;

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button){
        if(!onlySideScreen) for(var entry : filteredEntries) if(entry.mouseClicked(mouseX, mouseY, button)) return true;
        if(this.activeSideScreenComponent != null && activeSideScreenComponent.mouseClicked(mouseX, mouseY, button)) return true;
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
