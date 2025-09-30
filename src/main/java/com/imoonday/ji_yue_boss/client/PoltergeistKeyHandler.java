package com.imoonday.ji_yue_boss.client;

import com.imoonday.ji_yue_boss.JiYueBoss;
import com.imoonday.ji_yue_boss.network.Network;
import com.imoonday.ji_yue_boss.network.PoltergeistSkillC2SPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端按键处理 - 监听中键按下
 */
@Mod.EventBusSubscriber(modid = JiYueBoss.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class PoltergeistKeyHandler {

    private static boolean wasMiddleMousePressed = false;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) {
            return;
        }

        // 检测中键按下
        boolean isMiddleMousePressed = GLFW.glfwGetMouseButton(mc.getWindow().getWindow(), GLFW.GLFW_MOUSE_BUTTON_MIDDLE) == GLFW.GLFW_PRESS;

        // 边沿检测：从未按下到按下
        if (isMiddleMousePressed && !wasMiddleMousePressed) {
            // 发送包到服务器
            Network.sendToServer(new PoltergeistSkillC2SPacket());
        }

        wasMiddleMousePressed = isMiddleMousePressed;
    }
}

