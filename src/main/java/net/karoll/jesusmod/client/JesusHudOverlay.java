package net.karoll.jesusmod.client;

import com.mojang.blaze3d.systems.RenderSystem;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.karoll.jesusmod.JesusMod;
import net.karoll.jesusmod.SoundRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.util.Identifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.gui.DrawContext;

public class JesusHudOverlay implements HudRenderCallback {
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
    private static final Identifier BLOOD_IMAGE = Identifier.of(JesusMod.MOD_ID, "textures/blood.png");

    private static final Identifier BLOOD_MASK = Identifier.of(JesusMod.MOD_ID, "textures/bloodmask.png");

    private static final float FADE_DURATION = 20.0F; // Duration for fade in/out (in ticks)

    private float elapsedTime = 0.0F; // Time elapsed for fade effect

    private static final float FADE_IN_DURATION = 2.0F; // Duration for fade in (in ticks)

    private static final float FADE_OUT_DURATION = 20.0F; // Duration for fade out (in ticks)

    private static final float DELAY_DURATION = 25.0F; // Duration for fade out delay (in ticks)

    private static final float CYCLE_DURATION = 47.0F;

    private float lastFadeInStartTime = -FADE_IN_DURATION;

    private float alpha;

    private boolean shouldRender;

    private RandomNumberGenerator rng;

    private int RandomNumber;

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

    private float calculateAlphaOld() {
        float time = this.elapsedTime % 40.0F;
        if (time < 20.0F)
            return time / 20.0F;
        return (40.0F - time) / 20.0F;
    }

    // this stuff is the hud i think
    public JesusHudOverlay() {
        this.alpha = 0.0F;
        this.shouldRender = false;
        this.rng = new RandomNumberGenerator(JESUS_IMAGES.size());
        this.RandomNumber = 0;
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player != null) {
                elapsedTime += 1.0f; // Increment elapsed time on each tick
                updateRenderState(client);
                //client.player.sendMessage(Text.literal("Elasped: " + String.valueOf(elapsedTime)));
            }
        });
    }

    // this runs every tick. It checks if player hp is low and, if so, starts the jesus hud.
    private void updateRenderState(MinecraftClient client) {
        float health = client.player.getHealth();
        this.shouldRender = (health < 8.0F);
        this.alpha = calculateAlpha();

        // Detect the start of a new fade-in phase
        float cyclePosition = this.elapsedTime % CYCLE_DURATION;
        boolean isFadingIn = (cyclePosition < FADE_IN_DURATION);
        boolean wasFadingIn = (this.lastFadeInStartTime >= 0.0F && cyclePosition < FADE_IN_DURATION);


        if (isFadingIn && !wasFadingIn && this.shouldRender) {
            // Fade-in just started, play the sound
            this.RandomNumber = this.rng.getNextRandom();
            client.getSoundManager().play(PositionedSoundInstance.master(SoundRegistry.CHURCHBELLS, 1.0F));
            this.lastFadeInStartTime = this.elapsedTime; // Update the timestamp of the last fade-in start
        }

        // Reset soundPlayed flag at the end of the fade-out phase
        if (cyclePosition >= FADE_IN_DURATION + FADE_OUT_DURATION && cyclePosition < CYCLE_DURATION && this.shouldRender)
            this.lastFadeInStartTime = -FADE_IN_DURATION; // Reset the fade-in start timestamp
    }

    // runs every tick i think
    @Override
    public void onHudRender(DrawContext drawContext, float tickDelta) {
        MinecraftClient client = MinecraftClient.getInstance();
        Identifier JESUS_IMAGE = JESUS_IMAGES.get(this.RandomNumber); // grab random jesus image


        if (client != null && client.player != null) {
            // get screen dimensions
            int screenWidth = client.getWindow().getScaledWidth();
            int screenHeight = client.getWindow().getScaledHeight();
            if (this.shouldRender) {
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
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, this.alpha);

                // Draw the texture
                drawContext.drawTexture(JESUS_IMAGE, x, y, 0.0F, 0.0F, imageWidth, imageHeight, imageWidth, imageHeight);

                // End drawing
                RenderSystem.disableBlend();

                // blood
                // Bind and draw the mask texture
                client.getTextureManager().bindTexture(BLOOD_MASK);
                MatrixStack bloodmatrices = drawContext.getMatrices();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

                // Draw the mask texture (fully opaque mask)
                drawContext.drawTexture(BLOOD_MASK, x, y, 0.0F, 0.0F, imageWidth, imageHeight, imageWidth, imageHeight);

                // Bind and draw the image texture with the mask applied
                client.getTextureManager().bindTexture(BLOOD_IMAGE);

                // Set the shader to use the mask texture (adjusting alpha)
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F); // Fully opaque image
                drawContext.drawTexture(BLOOD_IMAGE, x, y, 0.0F, 0.0F, imageWidth, imageHeight, imageWidth, imageHeight);

                // End blending
                RenderSystem.disableBlend();
            }
        }
    }
}
