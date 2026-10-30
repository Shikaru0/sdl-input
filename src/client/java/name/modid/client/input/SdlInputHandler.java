package name.modid.client.input;

import com.mojang.blaze3d.platform.InputConstants;
import name.modid.client.SdlInputClient;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.component.SwingAnimation;
import org.lwjgl.sdl.SDLKeyboard;
import org.lwjgl.sdl.SDLMouse;

import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;

public class SdlInputHandler {
    private static SdlInputHandler INSTANCE;

    private static final int MAX_MOUSE = 6;

    private boolean active = true;

    private final BitSet currentKeyboard = new BitSet();
    private final BitSet prevKeyboard = new BitSet();
    private final BitSet currentMouse = new BitSet(MAX_MOUSE);
    private final BitSet prevMouse = new BitSet(MAX_MOUSE);

    private final Set<InputConstants.Key> newPresses = new HashSet<>();
    private int rightClickHeldTicks = 0;
    private Screen previousScreen = null;

    public static SdlInputHandler getInstance() {
        return INSTANCE;
    }

    public void register() {
        INSTANCE = this;
        ClientTickEvents.START_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(Minecraft mc) {
        if (!active) return;
        if (!SdlInputClient.getConfig().isEnabled()) return;

        Screen currentScreen = mc.gui.screen();
        boolean screenOpen = currentScreen != null;
        boolean screenChanged = currentScreen != previousScreen;
        previousScreen = currentScreen;

        boolean trackTransitions = !screenOpen && !screenChanged;

        if (trackTransitions) {
            newPresses.clear();
        }

        ByteBuffer keys = SDLKeyboard.SDL_GetKeyboardState();
        currentKeyboard.clear();

        if (keys != null) {
            for (int key = 0; key < keys.capacity(); key++) {
                boolean pressed = keys.get(key) != 0;
                currentKeyboard.set(key, pressed);
                if (trackTransitions && pressed && !prevKeyboard.get(key)) {
                    newPresses.add(InputConstants.Type.KEYBOARD.getOrCreate(key));
                }
            }
        }

        int buttons = SDLMouse.SDL_GetMouseState(null, null);
        currentMouse.clear();

        for (int btn = 1; btn < MAX_MOUSE; btn++) {
            boolean pressed = (buttons & (1 << (btn -1))) != 0;
            currentMouse.set(btn, pressed);
            if (trackTransitions && pressed && !prevMouse.get(btn)) {
                newPresses.add(InputConstants.Type.MOUSE.getOrCreate(btn));
            }
        }

        prevKeyboard.clear();
        prevKeyboard.or(currentKeyboard);
        prevMouse.clear();
        prevMouse.or(currentMouse);

        if (!screenOpen) {
            handleCooldownOverride(mc);
        }
    }

    private void handleCooldownOverride(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null || mc.gameMode == null) return;

        boolean rightHeld = currentMouse.get(SDLMouse.SDL_BUTTON_RIGHT);

        if (!rightHeld) {
            rightClickHeldTicks = 0;
            return;
        }

        rightClickHeldTicks++;
        if (rightClickHeldTicks <= 1) return;

        if (player.isUsingItem()) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        boolean mainOk = isThrowable(mainHand);
        boolean offOk = isThrowable(offHand);

        if (!mainOk && !offOk) return;

        InteractionHand hand = mainOk ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        ItemStack stack = hand == InteractionHand.MAIN_HAND ? mainHand : offHand;

        if (!player.getCooldowns().isOnCooldown(stack) && stack.has(DataComponents.USE_COOLDOWN)) {
            mc.gameMode.useItem(player, hand);
            player.swing(hand, SwingAnimation.DEFAULT, false);
        }
    }

    private static boolean isThrowable(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ProjectileItem;
    }

    public boolean isKeyDown(InputConstants.Key key) {
        int code = key.getValue();

        if (key.getType() == InputConstants.Type.KEYBOARD) {
            return currentKeyboard.get(code);
        }
        if (key.getType() == InputConstants.Type.MOUSE) {
            return currentMouse.get(code);
        }
        return false;
    }

    public boolean consumePress(InputConstants.Key key) {
        return newPresses.remove(key);
    }

    public boolean isActive() {
        if (!active) return false;
        if (Minecraft.getInstance().gui.screen() != null) return false;
        try {
            return SdlInputClient.getConfig() != null && SdlInputClient.getConfig().isEnabled();
        } catch (Exception e) {
            return true;
        }
    }

    @SuppressWarnings("unused")
    public void setActive(boolean active) {
        this.active = active;
    }
}