package net.karoll.jesusmod.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.karoll.jesusmod.JesusMod;
import net.karoll.jesusmod.SoundRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class JesusHudOverlay implements HudRenderCallback {
    // Textures loaded here
    // if you wanna add textures, it's here
    public static final List<Identifier> JESUS_IMAGES = new ArrayList<>() {{
        add(Identifier.of(JesusMod.MOD_ID, "textures/jesusimages/jesus1.png"));
        add(Identifier.of(JesusMod.MOD_ID, "textures/jesusimages/jesus2.png"));
        add(Identifier.of(JesusMod.MOD_ID, "textures/jesusimages/jesus3.png"));
        add(Identifier.of(JesusMod.MOD_ID, "textures/jesusimages/jesus4.png"));
        add(Identifier.of(JesusMod.MOD_ID, "textures/jesusimages/jesus5.png"));
        add(Identifier.of(JesusMod.MOD_ID, "textures/jesusimages/jesus6.png"));
        add(Identifier.of(JesusMod.MOD_ID, "textures/jesusimages/jesus7.png"));
    }};

    // mask + image for transparency
    private static final Identifier BLOOD_IMAGE = Identifier.of(JesusMod.MOD_ID,
            "textures/blood.png");
    private static final Identifier BLOOD_MASK = Identifier.of(JesusMod.MOD_ID,
            "textures/bloodmask.png");

    private float elapsedTime = 0.0f; // Time elapsed for fade effect

    private static final float FADE_DURATION = 20.0f; // Duration for fade in/out (in ticks)
    private static final float FADE_IN_DURATION = 2.0f; // Duration for fade in (in ticks)
    private static final float FADE_OUT_DURATION = 20.0f; // Duration for fade out (in ticks)
    private static final float DELAY_DURATION = 25.0f; // Duration for fade out delay (in ticks)
    private static final float CYCLE_DURATION = FADE_IN_DURATION + FADE_OUT_DURATION + DELAY_DURATION;

    private float lastFadeInStartTime = -FADE_IN_DURATION; // Initialize to a time before any fade-in would start

    // calculates what the alpha of jesus image should be given the elapsed time (in ticks)
    private float calculateAlpha() {
        float timeInCycle = elapsedTime % CYCLE_DURATION; // Time within the current cycle

        if (timeInCycle < FADE_IN_DURATION) {
            // Fade in
            return timeInCycle / FADE_IN_DURATION;
        } else if (timeInCycle < FADE_IN_DURATION + FADE_OUT_DURATION) {
            // Fade out
            float fadeOutTime = timeInCycle - FADE_IN_DURATION;
            return 1.0f - (fadeOutTime / FADE_OUT_DURATION);
        } else {
            // Fully faded out during delay
            return 0.0f;
        }
    }

    // this stuff is the hud i think
    public JesusHudOverlay() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                elapsedTime += 1.0f; // Increment elapsed time on each tick
                updateRenderState(client);
                //client.player.sendMessage(Text.literal("Elasped: " + String.valueOf(elapsedTime)));
            }
        });
    }

    private float alpha = 0.0f;
    private boolean shouldRender = false;
    private RandomNumberGenerator rng = new RandomNumberGenerator(JESUS_IMAGES.size());
    private int RandomNumber = 0;

    // this runs every tick. It checks if player hp is low and, if so, starts the jesus hud.
    private void updateRenderState(MinecraftClient client) {
        float health = client.player.getHealth();
        shouldRender = health < 8;
        alpha = calculateAlpha();

        // Detect the start of a new fade-in phase
        float cyclePosition = elapsedTime % CYCLE_DURATION;
        boolean isFadingIn = cyclePosition < FADE_IN_DURATION;
        boolean wasFadingIn = lastFadeInStartTime >= 0 && cyclePosition < FADE_IN_DURATION;

        if (isFadingIn && !wasFadingIn && shouldRender) {
            // Fade-in just started, play the sound

            RandomNumber = rng.getNextRandom();
            client.getSoundManager().play(PositionedSoundInstance.master(SoundRegistry.CHURCHBELLS, 1.0f));
            lastFadeInStartTime = elapsedTime; // Update the timestamp of the last fade-in start
        }

        // Reset soundPlayed flag at the end of the fade-out phase
        if (cyclePosition >= FADE_IN_DURATION + FADE_OUT_DURATION && cyclePosition < CYCLE_DURATION && shouldRender) {
            lastFadeInStartTime = -FADE_IN_DURATION; // Reset the fade-in start timestamp
        }
    }

    // runs every tick i think
    @Override
    public void onHudRender(DrawContext drawContext, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        Identifier JESUS_IMAGE = JESUS_IMAGES.get(RandomNumber); // grab random jesus image

        if (client != null && client.player != null) {
            // get screen dimensions
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();
            if (shouldRender) {
                // set image dimensions to match screen dimensions
                int imageWidth = screenWidth;
                int imageHeight = screenHeight;
                // calculate the top-left corner coordinates to position the image
                int x = 0;
                int y = 0;

                // Bind the texture (dunno what that means)
                client.getTextureManager().bindTexture(JESUS_IMAGE);
                // Start drawing the jesus part of the hud
                MatrixStack matrices = drawContext.getMatrices();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

                // Draw the texture
                drawContext.drawTexture(JESUS_IMAGE, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

                // End drawing
                RenderSystem.disableBlend();

                // blood
                // Bind and draw the mask texture
                client.getTextureManager().bindTexture(BLOOD_MASK);
                MatrixStack bloodmatrices = drawContext.getMatrices();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // Fully opaque mask

                // Draw the mask texture (fully opaque mask)
                drawContext.drawTexture(BLOOD_MASK, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

                // Bind and draw the image texture with the mask applied
                client.getTextureManager().bindTexture(BLOOD_IMAGE);
                // Set the shader to use the mask texture (adjusting alpha)
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f); // Fully opaque image
                drawContext.drawTexture(BLOOD_IMAGE, x, y, 0, 0, imageWidth, imageHeight, imageWidth, imageHeight);

                // End blending
                RenderSystem.disableBlend();
            }

        }
    }
}


// made my own random number generator to get random number & no repeats.
class RandomNumberGenerator {
    private List<Integer> numbers;
    private int currentIndex;

    public RandomNumberGenerator(int length) {
        initializeNumbers(length - 1);
    }

    private void initializeNumbers(int length) {
        numbers = new ArrayList<>();
        for (int i = 0; i <= length; i++) {
            numbers.add(i);
        }
        Collections.shuffle(numbers);
        currentIndex = 0;
    }

    public int getNextRandom() {
        if (currentIndex >= numbers.size()) {
            Collections.shuffle(numbers);
            currentIndex = 0;
        }
        return numbers.get(currentIndex++);
    }
}
