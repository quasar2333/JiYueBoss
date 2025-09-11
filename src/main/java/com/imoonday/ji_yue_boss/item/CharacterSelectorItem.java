package com.imoonday.ji_yue_boss.item;

import com.imoonday.ji_yue_boss.character.Character;
import com.imoonday.ji_yue_boss.character.CharacterManager;
import com.imoonday.ji_yue_boss.character.CharacterReceiver;
import com.imoonday.ji_yue_boss.data.CharacterData;
import com.imoonday.ji_yue_boss.init.ModItems;
import com.imoonday.ji_yue_boss.network.Network;
import com.imoonday.ji_yue_boss.network.OpenCharacterSelectionS2CPacket;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;

import java.util.List;

public class CharacterSelectorItem extends Item {

    private static final Logger LOGGER = LogUtils.getLogger();

    public CharacterSelectorItem(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            MinecraftServer server = serverPlayer.getServer();
            if (server != null) {
                List<Character> characters = CharacterManager.getInstance().getCharacters();
                if (!characters.isEmpty()) {
                    Character character = characters.get(0);
                    CharacterData data = CharacterData.fromServer(server);
                    Network.sendToClient(new OpenCharacterSelectionS2CPacket(CharacterReceiver.Context.of(data, character, serverPlayer, true)), serverPlayer);
                } else {
                    serverPlayer.sendSystemMessage(Component.translatable("msg.ji_yue_boss.no_characters").withStyle(ChatFormatting.DARK_RED), true);
                }
            } else {
                LOGGER.error("Server is null");
            }
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    public static void consumeItem(Player player) {
        if (player.getAbilities().instabuild) return;
        ItemStack itemStack = player.getMainHandItem();
        if (itemStack.is(ModItems.CHARACTER_SELECTOR.get())) {
            itemStack.shrink(1);
        } else {
            player.getInventory().hasAnyMatching(stack -> {
                if (stack.is(ModItems.CHARACTER_SELECTOR.get())) {
                    stack.shrink(1);
                    return true;
                }
                return false;
            });
        }
    }
}
