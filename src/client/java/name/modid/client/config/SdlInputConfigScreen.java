package name.modid.client.config;

import name.modid.client.SdlInputClient;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class SdlInputConfigScreen extends Screen {
    private final Screen parent;
    private boolean enabled;
    private boolean changed;

    protected SdlInputConfigScreen(Screen parent) {
        super(Component.literal("SDL Input Config"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        enabled = SdlInputClient.getConfig().isEnabled();
        changed = false;
        int cx = width / 2;

        addRenderableWidget(Button.builder(
                toggleText(enabled), button -> {
                    enabled = !enabled;
                    button.setMessage(toggleText(enabled));
                    changed = true;
                }
        ).bounds(cx - 100, 50, 200, 20).build());

        addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE, button -> saveAndClose()
        ).bounds(cx - 100, height - 28, 200, 20).build());
    }

    public void close() {
        if (changed) save();
        if (minecraft != null) minecraft.setScreenAndShow(parent);
    }

    private void saveAndClose() {
        save();
        if (minecraft != null) minecraft.setScreenAndShow(parent);
    }

    private void save() {
        SdlInputClient.getConfig().setEnabled(enabled);
        SdlInputClient.getConfig().save();
    }

    private static Component toggleText(boolean value) {
        return Component.literal("SDL Direct Input: " + (value ? "ON" : "OFF"));
    }
}