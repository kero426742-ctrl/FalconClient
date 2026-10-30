package com.falcon;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** FalconClient settings: one button per feature, click to turn it on or off. */
public class FalconMenuScreen extends Screen {
    public FalconMenuScreen() {
        super(Component.literal("FalconClient"));
    }

    @Override
    protected void init() {
        int w = 200;
        int h = 20;
        int x = this.width / 2 - w / 2;
        int y = this.height / 2 - 82;

        addToggle(x, y, w, h, "CPS", () -> FalconConfig.showCps, v -> FalconConfig.showCps = v);
        addToggle(x, y + 24, w, h, "FPS", () -> FalconConfig.showFps, v -> FalconConfig.showFps = v);
        addToggle(x, y + 48, w, h, "Ping", () -> FalconConfig.showPing, v -> FalconConfig.showPing = v);
        addToggle(x, y + 72, w, h, "Oynama suresi", () -> FalconConfig.showPlaytime, v -> FalconConfig.showPlaytime = v);
        addToggle(x, y + 96, w, h, "Totem", () -> FalconConfig.showTotems, v -> FalconConfig.showTotems = v);
        addToggle(x, y + 120, w, h, "Zirh dayanikliligi", () -> FalconConfig.showArmor, v -> FalconConfig.showArmor = v);

        addRenderableWidget(Button.builder(Component.literal("Koordinat hesaplayici"),
                b -> this.minecraft.setScreen(new OscScreen())).bounds(x, y + 148, w, h).build());
        addRenderableWidget(Button.builder(Component.literal("Kapat"), b -> onClose())
                .bounds(x, y + 172, w, h).build());
    }

    private void addToggle(int x, int y, int w, int h, String name,
                           BooleanSupplier getter, Consumer<Boolean> setter) {
        addRenderableWidget(Button.builder(label(name, getter.getAsBoolean()), b -> {
            boolean now = !getter.getAsBoolean();
            setter.accept(now);
            b.setMessage(label(name, now));
            FalconConfig.save();
        }).bounds(x, y, w, h).build());
    }

    private static Component label(String name, boolean on) {
        return Component.literal(name + ": " + (on ? "ACIK" : "KAPALI"));
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float delta) {
        super.render(g, mouseX, mouseY, delta);
        g.drawCenteredString(this.font, "FalconClient", this.width / 2, this.height / 2 - 108, 0xFFFF3030);
    }
}
