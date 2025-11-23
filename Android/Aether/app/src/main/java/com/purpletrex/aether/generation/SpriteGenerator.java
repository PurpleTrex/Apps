package com.purpletrex.aether.generation;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import com.purpletrex.aether.entities.ElementType;
import com.purpletrex.aether.entities.Ethereal;
import java.util.Random;

/**
 * Procedurally generates sprites for Ethereals
 */
public class SpriteGenerator {
    private static final int SPRITE_SIZE = 64;
    private Random random;

    public SpriteGenerator() {
        this.random = new Random();
    }

    /**
     * Generate a sprite for an Ethereal
     */
    public Bitmap generateEtherealSprite(Ethereal ethereal) {
        return generateEtherealSprite(ethereal.getSpeciesId(), ethereal.getType(), ethereal.isShiny());
    }

    /**
     * Generate a sprite based on species and type
     */
    public Bitmap generateEtherealSprite(int speciesId, ElementType type, boolean isShiny) {
        Bitmap bitmap = Bitmap.createBitmap(SPRITE_SIZE, SPRITE_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Seed random with species ID for consistency
        random.setSeed(speciesId);

        // Get color palette
        int primaryColor = type.getColor();
        int secondaryColor = adjustColor(primaryColor, 0.7f);
        int accentColor = adjustColor(primaryColor, 1.3f);

        // Shiny variant has different colors
        if (isShiny) {
            primaryColor = Color.rgb(255, 215, 0); // Gold
            secondaryColor = Color.rgb(218, 165, 32);
        }

        // Determine creature archetype based on species ID
        int archetype = speciesId % 5;

        switch (archetype) {
            case 0: // Quadruped
                drawQuadruped(canvas, paint, primaryColor, secondaryColor, accentColor);
                break;
            case 1: // Bird
                drawBird(canvas, paint, primaryColor, secondaryColor, accentColor);
                break;
            case 2: // Serpent
                drawSerpent(canvas, paint, primaryColor, secondaryColor, accentColor);
                break;
            case 3: // Humanoid
                drawHumanoid(canvas, paint, primaryColor, secondaryColor, accentColor);
                break;
            case 4: // Blob
                drawBlob(canvas, paint, primaryColor, secondaryColor, accentColor);
                break;
        }

        // Add outline
        addOutline(bitmap);

        return bitmap;
    }

    /**
     * Draw a quadruped creature (4 legs)
     */
    private void drawQuadruped(Canvas canvas, Paint paint, int primary, int secondary, int accent) {
        // Body
        paint.setColor(primary);
        canvas.drawOval(16, 24, 48, 44, paint);

        // Head
        canvas.drawCircle(32, 16, 12, paint);

        // Legs
        paint.setColor(secondary);
        canvas.drawRect(20, 40, 26, 54, paint); // Front left
        canvas.drawRect(38, 40, 44, 54, paint); // Front right
        canvas.drawRect(22, 42, 28, 56, paint); // Back left
        canvas.drawRect(36, 42, 42, 56, paint); // Back right

        // Eyes
        paint.setColor(accent);
        canvas.drawCircle(28, 14, 2, paint);
        canvas.drawCircle(36, 14, 2, paint);
    }

    /**
     * Draw a bird creature
     */
    private void drawBird(Canvas canvas, Paint paint, int primary, int secondary, int accent) {
        // Body
        paint.setColor(primary);
        canvas.drawOval(24, 28, 40, 44, paint);

        // Head
        canvas.drawCircle(32, 20, 10, paint);

        // Wings
        paint.setColor(secondary);
        canvas.drawOval(12, 28, 28, 40, paint); // Left wing
        canvas.drawOval(36, 28, 52, 40, paint); // Right wing

        // Beak
        paint.setColor(accent);
        canvas.drawCircle(32, 24, 3, paint);

        // Eyes
        canvas.drawCircle(28, 18, 2, paint);
        canvas.drawCircle(36, 18, 2, paint);

        // Tail feathers
        paint.setColor(secondary);
        canvas.drawOval(28, 42, 36, 54, paint);
    }

    /**
     * Draw a serpent creature
     */
    private void drawSerpent(Canvas canvas, Paint paint, int primary, int secondary, int accent) {
        // Body segments
        paint.setColor(primary);
        for (int i = 0; i < 5; i++) {
            int y = 12 + i * 8;
            int x = 32 + (int)(Math.sin(i * 0.5) * 8);
            canvas.drawCircle(x, y, 8 - i, paint);
        }

        // Head
        canvas.drawCircle(32, 12, 10, paint);

        // Eyes
        paint.setColor(accent);
        canvas.drawCircle(28, 10, 2, paint);
        canvas.drawCircle(36, 10, 2, paint);

        // Pattern
        paint.setColor(secondary);
        for (int i = 0; i < 5; i++) {
            int y = 12 + i * 8;
            int x = 32 + (int)(Math.sin(i * 0.5) * 8);
            canvas.drawCircle(x, y, 3, paint);
        }
    }

    /**
     * Draw a humanoid creature
     */
    private void drawHumanoid(Canvas canvas, Paint paint, int primary, int secondary, int accent) {
        // Body
        paint.setColor(primary);
        canvas.drawRect(24, 28, 40, 48, paint);

        // Head
        canvas.drawCircle(32, 18, 12, paint);

        // Arms
        paint.setColor(secondary);
        canvas.drawRect(16, 28, 24, 44, paint); // Left arm
        canvas.drawRect(40, 28, 48, 44, paint); // Right arm

        // Legs
        canvas.drawRect(26, 48, 32, 58, paint); // Left leg
        canvas.drawRect(32, 48, 38, 58, paint); // Right leg

        // Eyes
        paint.setColor(accent);
        canvas.drawCircle(28, 16, 2, paint);
        canvas.drawCircle(36, 16, 2, paint);
    }

    /**
     * Draw a blob creature
     */
    private void drawBlob(Canvas canvas, Paint paint, int primary, int secondary, int accent) {
        // Main body
        paint.setColor(primary);
        canvas.drawCircle(32, 32, 20, paint);

        // Smaller blobs
        paint.setColor(secondary);
        canvas.drawCircle(20, 40, 8, paint);
        canvas.drawCircle(44, 40, 8, paint);

        // Eyes
        paint.setColor(accent);
        canvas.drawCircle(26, 28, 3, paint);
        canvas.drawCircle(38, 28, 3, paint);

        // Mouth
        canvas.drawCircle(32, 36, 2, paint);
    }

    /**
     * Add black outline to sprite
     */
    private void addOutline(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int index = y * width + x;
                if (Color.alpha(pixels[index]) > 0) {
                    // Check neighbors
                    boolean hasTransparentNeighbor = false;
                    if (Color.alpha(pixels[index - 1]) == 0 ||
                        Color.alpha(pixels[index + 1]) == 0 ||
                        Color.alpha(pixels[index - width]) == 0 ||
                        Color.alpha(pixels[index + width]) == 0) {
                        hasTransparentNeighbor = true;
                    }
                    if (hasTransparentNeighbor) {
                        pixels[index] = Color.BLACK;
                    }
                }
            }
        }

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
    }

    /**
     * Adjust color brightness
     */
    private int adjustColor(int color, float factor) {
        int r = (int)(Color.red(color) * factor);
        int g = (int)(Color.green(color) * factor);
        int b = (int)(Color.blue(color) * factor);
        return Color.rgb(
            Math.min(255, Math.max(0, r)),
            Math.min(255, Math.max(0, g)),
            Math.min(255, Math.max(0, b))
        );
    }

    /**
     * Generate icon for app
     */
    public Bitmap generateAppIcon() {
        Bitmap bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint();
        paint.setAntiAlias(true);

        // Background
        paint.setColor(Color.parseColor("#4B0082")); // Void color
        canvas.drawRect(0, 0, 512, 512, paint);

        // Gauntlet shape
        paint.setColor(Color.parseColor("#FFD700")); // Gold
        canvas.drawCircle(256, 256, 200, paint);

        // Inner detail
        paint.setColor(Color.parseColor("#4B0082"));
        canvas.drawCircle(256, 256, 150, paint);

        // Symbol
        paint.setColor(Color.parseColor("#FFD700"));
        canvas.drawCircle(256, 256, 100, paint);

        return bitmap;
    }
}
