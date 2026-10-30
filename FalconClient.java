package com.falcon;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * FalconClient: red-themed, client-side only PvP HUD.
 * Every feature can be switched on or off in the menu (default key: Right Shift).
 * It only displays information the vanilla client already has; it sends nothing
 * to the server and changes no gameplay.
 */
public class FalconClient implements ClientModInitializer {
    private static final int RED = 0xFFFF3030;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int GRAY = 0xFF888888;
    private static final int YELLOW = 0xFFFFFF55;
    private static final int GREEN = 0xFF55FF55;

    private static final EquipmentSlot[] ARMOR = {
            EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET
    };
    private static final String[] ARMOR_NAMES = {"Kask", "Gogus", "Pantolon", "Bot"};

    private final Deque<Long> leftClicks = new ArrayDeque<>();
    private final Deque<Long> rightClicks = new ArrayDeque<>();
    private boolean leftWasDown;
    private boolean rightWasDown;
    private long joinTime = System.currentTimeMillis();
    private KeyMapping menuKey;

    @Override
    public void onInitializeClient() {
        FalconConfig.load();

        KeyMapping.Category category = KeyMapping.Category.register(
                Identifier.fromNamespaceAndPath("falconclient", "main"));
        menuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.falconclient.menu", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, category));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (menuKey.consumeClick()) {
                mc.setScreen(new FalconMenuScreen());
            }
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, mc) -> joinTime = System.currentTimeMillis());

        HudRenderCallback.EVENT.register((graphics, tickCounter) -> render(graphics));
    }

    private void render(GuiGraphics g) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui || mc.screen != null) {
            leftWasDown = false;
            rightWasDown = false;
            return;
        }

        long now = System.currentTimeMillis();
        boolean leftDown = mc.options.keyAttack.isDown();
        boolean rightDown = mc.options.keyUse.isDown();
        if (leftDown && !leftWasDown) leftClicks.addLast(now);
        if (rightDown && !rightWasDown) rightClicks.addLast(now);
        leftWasDown = leftDown;
        rightWasDown = rightDown;
        prune(leftClicks, now);
        prune(rightClicks, now);

        int x = 6;
        int y = 6;
        int line = 11;

        g.drawString(mc.font, "FalconClient", x, y, RED, true);
        y += line + 2;

        if (FalconConfig.showCps) {
            g.drawString(mc.font, "CPS: " + leftClicks.size() + " | " + rightClicks.size(), x, y, RED, true);
            y += line;
        }
        if (FalconConfig.showFps) {
            g.drawString(mc.font, "FPS: " + mc.getFps(), x, y, WHITE, true);
            y += line;
        }
        if (FalconConfig.showPing && mc.getConnection() != null) {
            PlayerInfo info = mc.getConnection().getPlayerInfo(player.getUUID());
            if (info != null) {
                g.drawString(mc.font, "Ping: " + info.getLatency() + " ms", x, y, WHITE, true);
                y += line;
            }
        }
        if (FalconConfig.showPlaytime) {
            g.drawString(mc.font, "Sure: " + formatTime(now - joinTime), x, y, WHITE, true);
            y += line;
        }
        if (FalconConfig.showTotems) {
            g.drawString(mc.font, "Totem: " + countTotems(player), x, y, RED, true);
            y += line;
        }
        if (FalconConfig.showArmor) {
            for (int i = 0; i < ARMOR.length; i++) {
                ItemStack stack = player.getItemBySlot(ARMOR[i]);
                if (stack.isEmpty()) {
                    g.drawString(mc.font, ARMOR_NAMES[i] + ": -", x, y, GRAY, true);
                } else if (stack.getMaxDamage() > 0) {
                    int left = stack.getMaxDamage() - stack.getDamageValue();
                    int percent = Math.round(100f * left / stack.getMaxDamage());
                    g.drawString(mc.font, ARMOR_NAMES[i] + ": " + left + " (" + percent + "%)",
                            x, y, colorFor(percent), true);
                } else {
                    g.drawString(mc.font, ARMOR_NAMES[i] + ": ~", x, y, WHITE, true);
                }
                y += line;
            }
        }
    }

    private static String formatTime(long ms) {
        long s = ms / 1000;
        return String.format("%02d:%02d:%02d", s / 3600, (s / 60) % 60, s % 60);
    }

    private static void prune(Deque<Long> clicks, long now) {
        while (!clicks.isEmpty() && now - clicks.peekFirst() > 1000L) {
            clicks.removeFirst();
        }
    }

    private static int countTotems(LocalPlayer player) {
        int total = 0;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.TOTEM_OF_UNDYING)) total += stack.getCount();
        }
        return total;
    }

    private static int colorFor(int percent) {
        if (percent > 60) return GREEN;
        if (percent > 25) return YELLOW;
        return RED;
    }
}
