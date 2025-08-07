package net.luko.bestia.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.luko.bestia.Bestia;
import net.luko.bestia.data.BestiaryManager;
import net.luko.bestia.data.PlayerBestiaryStore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Bestia.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event){
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("bestiary")
                        .requires(source -> source.hasPermission(2))
                .then(Commands.literal("set")
                        .then(Commands.literal("level")
                                .then(Commands.argument("player", EntityArgument.player())
                                        .then(Commands.argument("mob", ResourceLocationArgument.id())
                                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                        .executes(ctx -> {
                                                            ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                            ResourceLocation mobId = ResourceLocationArgument.getId(ctx, "mob");
                                                            int level = IntegerArgumentType.getInteger(ctx, "value");

                                                            return setBestiaryLevel(ctx.getSource(), player, mobId, level);
                                                        })))))));
    }

    private static int setBestiaryLevel(CommandSourceStack source, ServerPlayer player, ResourceLocation mobId, int value){
        BestiaryManager manager = PlayerBestiaryStore.get(player);
        if(manager == null){
            source.sendFailure(Component.literal("Could not get bestiary data for player."));
            return 0;
        }

        manager.setLevelAndSync(player, mobId, value);

        source.sendSuccess(() -> Component.literal(
                "Set " + mobId + " level to " + value + " for " + player.getName().getString()
        ), true);
        return 1;
    }
}
