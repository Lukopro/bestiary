package net.luko.bestia.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.luko.bestia.Bestia;
import net.luko.bestia.config.BestiaCommonConfig;
import net.luko.bestia.data.BestiaryManager;
import net.luko.bestia.data.PlayerBestiaryStore;
import net.luko.bestia.util.MobIdUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = Bestia.MODID)
public class ModCommands {

    public static final SuggestionProvider<CommandSourceStack> ENTITY_SUGGESTIONS = (ctx, builder) ->
            SharedSuggestionProvider.suggestResource(
                    ForgeRegistries.ENTITY_TYPES.getKeys().stream()
                            .filter(id -> MobIdUtil.validBestiaryMob(id, LogicalSide.SERVER))
                            .toList(),
                    builder
            );

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event){
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        LiteralArgumentBuilder<CommandSourceStack> bestiaryCommand =
                Commands.literal("bestiary")
                                .requires(source -> source.hasPermission(2));

        LiteralArgumentBuilder<CommandSourceStack> setLevel = Commands.literal("set")
                .then(Commands.literal("level")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("mob", ResourceLocationArgument.id())
                                        .suggests(ENTITY_SUGGESTIONS)
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                    ResourceLocation mobId = ResourceLocationArgument.getId(ctx, "mob");
                                                    int level = IntegerArgumentType.getInteger(ctx, "value");

                                                    return setBestiaryLevel(ctx.getSource(), player, mobId, level);
                                                })))));

        LiteralArgumentBuilder<CommandSourceStack> setKills = Commands.literal("set")
                .then(Commands.literal("kills")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("mob", ResourceLocationArgument.id())
                                        .suggests(ENTITY_SUGGESTIONS)
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                    ResourceLocation mobId = ResourceLocationArgument.getId(ctx, "mob");
                                                    int kills = IntegerArgumentType.getInteger(ctx, "value");

                                                    return setBestiaryKills(ctx.getSource(), player, mobId, kills);
                                                })))));

        LiteralArgumentBuilder<CommandSourceStack> addLevels = Commands.literal("add")
                .then(Commands.literal("level")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("mob", ResourceLocationArgument.id())
                                        .suggests(ENTITY_SUGGESTIONS)
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                    ResourceLocation mobId = ResourceLocationArgument.getId(ctx, "mob");
                                                    int levels = IntegerArgumentType.getInteger(ctx, "value");

                                                    return addBestiaryLevels(ctx.getSource(), player, mobId, levels);
                                                })))));

        LiteralArgumentBuilder<CommandSourceStack> addKills = Commands.literal("add")
                .then(Commands.literal("kills")
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("mob", ResourceLocationArgument.id())
                                        .suggests(ENTITY_SUGGESTIONS)
                                        .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                                .executes(ctx -> {
                                                    ServerPlayer player = EntityArgument.getPlayer(ctx, "player");
                                                    ResourceLocation mobId = ResourceLocationArgument.getId(ctx, "mob");
                                                    int kills = IntegerArgumentType.getInteger(ctx, "value");

                                                    return addBestiaryKills(ctx.getSource(), player, mobId, kills);
                                                })))));

        bestiaryCommand.then(setLevel);
        bestiaryCommand.then(setKills);
        bestiaryCommand.then(addLevels);
        bestiaryCommand.then(addKills);

        dispatcher.register(bestiaryCommand);
    }

    private static int setBestiaryLevel(CommandSourceStack source, ServerPlayer player, ResourceLocation mobId, int value){
        if(value > BestiaCommonConfig.MAX_LEVEL.get()){
            source.sendFailure(Component.literal("Level cannot exceed max level!"));
            return 0;
        }

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

    private static int setBestiaryKills(CommandSourceStack source, ServerPlayer player, ResourceLocation mobId, int value){
        BestiaryManager manager = PlayerBestiaryStore.get(player);
        if(manager == null){
            source.sendFailure(Component.literal("Could not get bestiary data for player."));
            return 0;
        }

        manager.setKillsAndSync(player, mobId, value);

        source.sendSuccess(() -> Component.literal(
                "Set " + mobId + " kills to " + value + " for " + player.getName().getString()
        ), true);
        return 1;
    }

    private static int addBestiaryLevels(CommandSourceStack source, ServerPlayer player, ResourceLocation mobId, int value){
        if(value > BestiaCommonConfig.MAX_LEVEL.get()){
            source.sendFailure(Component.literal("Level cannot exceed max level!"));
            return 0;
        }

        BestiaryManager manager = PlayerBestiaryStore.get(player);
        if(manager == null){
            source.sendFailure(Component.literal("Could not get bestiary data for player."));
            return 0;
        }

        int current = manager.getData(mobId) == null ? 0 : manager.getData(mobId).level();
        if(current + value > BestiaCommonConfig.MAX_LEVEL.get()){
            source.sendFailure(Component.literal("Level cannot exceed max level!"));
            return 0;
        }

        manager.addLevelsAndSync(player, mobId, value);

        source.sendSuccess(() -> Component.literal(
                "Added " + value + " level" + (value == 1 ? "" : "s") + " to " + mobId + " for " + player.getName().getString()
        ), true);
        return 1;
    }

    private static int addBestiaryKills(CommandSourceStack source, ServerPlayer player, ResourceLocation mobId, int value){
        BestiaryManager manager = PlayerBestiaryStore.get(player);
        if(manager == null){
            source.sendFailure(Component.literal("Could not get bestiary data for player."));
            return 0;
        }

        manager.addKillsAndSync(player, mobId, value);

        source.sendSuccess(() -> Component.literal(
                "Added " + value + " kill" + (value == 1 ? "" : "s") + " to " + mobId + " for " + player.getName().getString()
        ), true);
        return 1;
    }
}
