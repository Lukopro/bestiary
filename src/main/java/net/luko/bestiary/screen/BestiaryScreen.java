package net.luko.bestiary.screen;

import net.luko.bestiary.data.BestiaryData;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.network.PacketDistributor;

import java.util.Map;

public class BestiaryScreen extends Screen {
    private Map<ResourceLocation, BestiaryData> entries;

    public BestiaryScreen(Map<ResourceLocation, BestiaryData> entries) {
        super(Component.literal("Bestiary"));
        this.entries = entries;
    }

    @Override
    protected void init(){
        super.init();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick){
        renderBackground(guiGraphics);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);

        int y = 30;
        for(var entry : this.entries.entrySet()){
            String mobInfo = getMobInfo(entry.getKey(), entry.getValue());
            guiGraphics.drawString(this.font, mobInfo, 20, y, 0xAAAAAA);
            y += 12;
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    private String getMobInfo(ResourceLocation mobId, BestiaryData data){
        return String.format("%s: Level %d, %d kills. %d left...",
                getMobDisplayName(mobId),
                data.level(),
                data.kills(),
                data.nextLevelKills());
    }

    private String getMobDisplayName(ResourceLocation mobId){
        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(mobId);
        if(type != null){
            return type.getDescriptionId();
        } else {
            return mobId.toString();
        }
    }
}
