package com.imoonday.ji_yue_boss.command;

import com.imoonday.ji_yue_boss.character.Character;
import com.imoonday.ji_yue_boss.character.CharacterManager;
import com.imoonday.ji_yue_boss.data.CharacterData;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.UuidArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@Mod.EventBusSubscriber
public class CommandHandler {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
        registerCommands(dispatcher);
    }

    private static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(literal("jiyue").requires(stack -> stack.hasPermission(2))
                                    .then(literal("get")
                                                  .executes(CommandHandler::get)
                                                  .then(literal("player")
                                                                .then(argument("player", EntityArgument.player())
                                                                              .executes(context -> {
                                                                                  ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                                                  return get(context, player.getUUID(), player.getName());
                                                                              })
                                                                )
                                                                .then(argument("uuid", UuidArgument.uuid())
                                                                              .suggests(CommandHandler::suggestUUIDs)
                                                                              .executes(context -> {
                                                                                  UUID uuid = UuidArgument.getUuid(context, "uuid");
                                                                                  return get(context, uuid, Component.literal(uuid.toString()));
                                                                              })
                                                                )
                                                  )
                                                  .then(literal("character")
                                                                .then(argument("id", StringArgumentType.string())
                                                                              .suggests((context, builder) -> suggestCharacters(builder))
                                                                              .executes(context -> {
                                                                                  String id = StringArgumentType.getString(context, "id");
                                                                                  return get(context, id);
                                                                              })
                                                                )
                                                  )
                                    )
                                    .then(literal("set")
                                                  .then(argument("player", EntityArgument.player())
                                                                .then(argument("id", StringArgumentType.string())
                                                                              .suggests((context, builder) -> suggestCharacters(builder))
                                                                              .executes(context -> {
                                                                                  ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                                                  String id = StringArgumentType.getString(context, "id");
                                                                                  return set(context, player.getUUID(), id, player.getName());
                                                                              })
                                                                )
                                                  )
                                                  .then(argument("uuid", UuidArgument.uuid())
                                                                .suggests(CommandHandler::suggestUUIDs)
                                                                .then(argument("id", StringArgumentType.string())
                                                                              .suggests((context, builder) -> suggestCharacters(builder))
                                                                              .executes(context -> {
                                                                                  UUID uuid = UuidArgument.getUuid(context, "uuid");
                                                                                  String id = StringArgumentType.getString(context, "id");
                                                                                  return set(context, uuid, id, Component.literal(uuid.toString()));
                                                                              })
                                                                )
                                                  )
                                    )
                                    .then(literal("reset")
                                                  .executes(CommandHandler::resetAll)
                                                  .then(literal("player")
                                                                .then(argument("player", EntityArgument.player())
                                                                              .executes(context -> {
                                                                                  ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                                                                  return reset(context, player.getUUID(), player.getName());
                                                                              })
                                                                )
                                                                .then(argument("uuid", UuidArgument.uuid())
                                                                              .suggests(CommandHandler::suggestUUIDs)
                                                                              .executes(context -> {
                                                                                  UUID uuid = UuidArgument.getUuid(context, "uuid");
                                                                                  return reset(context, uuid, Component.literal(uuid.toString()));
                                                                              })
                                                                )
                                                  )
                                                  .then(literal("character")
                                                                .then(argument("id", StringArgumentType.string())
                                                                              .suggests((context, builder) -> suggestCharacters(builder))
                                                                              .executes(context -> {
                                                                                  String id = StringArgumentType.getString(context, "id");
                                                                                  return reset(context, id);
                                                                              })
                                                                )
                                                  )
                                    )
        );
    }

    private static CompletableFuture<Suggestions> suggestUUIDs(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        MinecraftServer server = context.getSource().getServer();
        CharacterData data = CharacterData.fromServer(server);
        for (UUID uuid : data.getSelectedCharacters().values()) {
            String uuidStr = uuid.toString();
            ServerPlayer player = server.getPlayerList().getPlayer(uuid);
            if (player != null) {
                builder.suggest(uuidStr, player.getName());
            } else {
                builder.suggest(uuidStr);
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestCharacters(SuggestionsBuilder builder) {
        for (Character character : CharacterManager.getInstance().getCharacters()) {
            String id = character.id();
            Component name = character.name();
            if (name != null) {
                builder.suggest(id, name);
            } else {
                builder.suggest(id);
            }
        }
        return builder.buildFuture();
    }

    private static int get(CommandContext<CommandSourceStack> context, UUID uuid, Component displayName) {
        MinecraftServer server = context.getSource().getServer();
        CharacterData data = CharacterData.fromServer(server);
        Character character = data.getCharacter(uuid);
        if (character != null) {
            Component name = getNameOrDefault(character, character.id());
            context.getSource().sendSuccess(() -> Component.translatable("msg.ji_yue_boss.character_selected", displayName, name), false);
        } else {
            String id = data.getCharacterId(uuid);
            if (id != null) {
                context.getSource().sendSuccess(() -> Component.translatable("msg.ji_yue_boss.invalid_character_selected", displayName, id), false);
            } else {
                context.getSource().sendSuccess(() -> Component.translatable("msg.ji_yue_boss.no_character_selected", displayName), false);
            }
        }
        return 1;
    }

    private static int get(CommandContext<CommandSourceStack> context, String id) {
        Character character = CharacterManager.getInstance().getCharacter(id);
        Component name = getNameOrDefault(character, id);
        if (character != null) {
            MinecraftServer server = context.getSource().getServer();
            CharacterData data = CharacterData.fromServer(server);
            UUID uuid = data.getSelectedCharacters().get(id);
            if (uuid != null) {
                context.getSource().sendSuccess(() -> Component.translatable("msg.ji_yue_boss.character_selected_by", name, uuid), false);
            } else {
                context.getSource().sendSuccess(() -> Component.translatable("msg.ji_yue_boss.character_not_selected_by", name), false);
            }
            return 1;
        }
        context.getSource().sendFailure(Component.translatable("msg.ji_yue_boss.character_not_found", name));
        return 0;
    }

    private static int get(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        CharacterData data = CharacterData.fromServer(server);
        MutableComponent text = Component.empty();
        for (Iterator<Map.Entry<String, UUID>> iterator = data.getSelectedCharacters().entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, UUID> entry = iterator.next();
            String id = entry.getKey();
            UUID uuid = entry.getValue();
            Character character = CharacterManager.getInstance().getCharacter(id);
            Component name = getNameOrDefault(character, id);
            text = text.append("[").append(name).append(" - " + uuid + "]");
            if (iterator.hasNext()) {
                text.append(" | ");
            }
        }
        MutableComponent finalText = text;
        context.getSource().sendSuccess(() -> finalText, false);
        return 0;
    }

    private static int set(CommandContext<CommandSourceStack> context, UUID uuid, String id, Component displayName) {
        MinecraftServer server = context.getSource().getServer();
        CharacterData data = CharacterData.fromServer(server);
        Character character = CharacterManager.getInstance().getCharacter(id);
        Component name = getNameOrDefault(character, id);
        if (data.setSelected(id, uuid)) {
            context.getSource().sendSuccess(() -> Component.translatable("msg.ji_yue_boss.character_set", displayName, name), false);
            return 1;
        }
        context.getSource().sendFailure(Component.translatable("msg.ji_yue_boss.character_set_failed", displayName, name));
        return 0;
    }

    private static int reset(CommandContext<CommandSourceStack> context, UUID uuid, Component displayName) {
        MinecraftServer server = context.getSource().getServer();
        CharacterData data = CharacterData.fromServer(server);
        if (data.removeSelectedBy(uuid)) {
            context.getSource().sendSuccess(() -> Component.translatable("msg.ji_yue_boss.character_reset", displayName), false);
            return 1;
        }
        context.getSource().sendSuccess(() -> Component.translatable("msg.ji_yue_boss.no_character_selected", displayName), false);
        return 0;
    }

    private static int reset(CommandContext<CommandSourceStack> context, String id) {
        MinecraftServer server = context.getSource().getServer();
        Character character = CharacterManager.getInstance().getCharacter(id);
        Component name = getNameOrDefault(character, id);
        CharacterData data = CharacterData.fromServer(server);
        if (data.removeSelected(id)) {
            context.getSource().sendSuccess(() -> Component.translatable("msg.ji_yue_boss.character_reset_by_id", name), false);
            return 1;
        }
        context.getSource().sendSuccess(() -> Component.translatable("msg.ji_yue_boss.not_selected", name), false);
        return 0;
    }

    private static int resetAll(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        CharacterData data = CharacterData.fromServer(server);
        data.clear();
        context.getSource().sendSuccess(() -> Component.translatable("msg.ji_yue_boss.all_characters_reset"), false);
        return 1;
    }

    private static Component getNameOrDefault(Character character, String id) {
        return character != null ? character.getNameOrDefault(id) : Component.literal(id);
    }
}
