package com.falcon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

/**
 * Coordinate helper for planning big builds (e.g. an orbital strike cannon):
 * type a target X and Z, see the distance and the compass bearing from where you stand.
 * Pure maths on numbers you type in. It does not touch the game or the server.
 */
public class OscScreen extends Screen {
    private EditBox targetX;
    private EditBox targetZ;

    public OscScreen() {
        super(Component.literal("Koordinat hesaplayici"));
    }

    @Override
    protected void init() {
        int x = this.width / 2 - 100;
        int y = this.height / 2 - 50;

        targetX = new EditBox(this.font, x, y, 200, 20, Component.literal("Hedef X"));
        targetZ = new EditBox(this.font, x, y + 36, 200, 20, Component.literal("Hedef Z"));
        targetX.setMaxLength(10);
        targetZ.setMaxLength(10);
        addRenderableWidget(targetX);
        addRenderableWidget(targetZ);

        addRenderableWidget(Button.builder(Component.literal("Geri"),
                b -> this.minecraft.setScreen(new FalconMenuScreen())).bounds(x, y + 120, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        int cx = this.width / 2;
        int y = this.height / 2 - 50;

        g.drawCenteredString(this.font, "Koordinat hesaplayici", cx, y - 28, 0xFFFF3030);
        g.drawString(this.font, "Hedef X", cx - 100, y - 11, 0xFFFFFFFF, true);
        g.drawString(this.font, "Hedef Z", cx - 100, y + 25, 0xFFFFFFFF, true);

        LocalPlayer player = this.minecraft.player;
        if (player == null) return;

        try {
            double tx = Double.parseDouble(targetX.getValue().trim());
            double tz = Double.parseDouble(targetZ.getValue().trim());
            double dx = tx - player.getX();
            double dz = tz - player.getZ();
            double dist = Math.sqrt(dx * dx + dz * dz);
            // Minecraft yaw: 0 = south (+Z), 90 = west (-X)
            double yaw = Math.toDegrees(Math.atan2(-dx, dz));
            double turn = wrap(yaw - player.getYRot());

            g.drawString(this.font, String.format("Mesafe: %.1f blok", dist), cx - 100, y + 66, 0xFFFFFFFF, true);
            g.drawString(this.font, String.format("Hedef yonu (yaw): %.1f", yaw), cx - 100, y + 78, 0xFFFFFFFF, true);
            g.drawString(this.font, String.format("Su an bakisina gore: %+.1f derece", turn), cx - 100, y + 90, 0xFFFFFFFF, true);
        } catch (NumberFormatException e) {
            g.drawString(this.font, "Sayi gir (ornek: 1200 ve -350)", cx - 100, y + 66, 0xFF888888, true);
        }
    }

    private static double wrap(double deg) {
        deg %= 360.0;
        if (deg > 180.0) deg -= 360.0;
        if (deg < -180.0) deg += 360.0;
        return deg;
    }
}
