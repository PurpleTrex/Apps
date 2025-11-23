package com.purpletrex.aether.ui;

import android.content.Context;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import com.purpletrex.aether.core.GameEngine;
import com.purpletrex.aether.entities.Ethereal;
import com.purpletrex.aether.world.GameMap;
import com.purpletrex.aether.world.Tile;
import com.purpletrex.aether.battle.BattleSystem;
import com.purpletrex.aether.battle.Ability;

/**
 * Main game view that renders the game
 */
public class GameView extends View {
    private GameEngine gameEngine;
    private Paint paint;
    private ViewState viewState;
    private GameMap currentMap;
    private BattleSystem currentBattle;
    
    private static final int TILE_SIZE = 32;
    private static final int BUTTON_SIZE = 80;
    
    // UI Buttons
    private RectF upButton, downButton, leftButton, rightButton;
    private RectF aButton, bButton, startButton;
    private RectF ability1Button, ability2Button, ability3Button, ability4Button;
    private RectF glyphButton, etherealButton, fleeButton;
    
    public enum ViewState {
        OVERWORLD,
        BATTLE,
        MENU
    }
    
    public GameView(Context context, GameEngine engine) {
        super(context);
        this.gameEngine = engine;
        this.paint = new Paint();
        this.viewState = ViewState.OVERWORLD;
        
        paint.setAntiAlias(true);
        paint.setTextSize(20);
        
        // Initialize UI buttons
        initializeButtons();
        
        // Start game loop
        startGameLoop();
    }
    
    private void initializeButtons() {
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        
        // D-Pad
        int dpadX = 100;
        int dpadY = screenHeight - 250;
        upButton = new RectF(dpadX, dpadY - BUTTON_SIZE, dpadX + BUTTON_SIZE, dpadY);
        downButton = new RectF(dpadX, dpadY + BUTTON_SIZE, dpadX + BUTTON_SIZE, dpadY + BUTTON_SIZE * 2);
        leftButton = new RectF(dpadX - BUTTON_SIZE, dpadY, dpadX, dpadY + BUTTON_SIZE);
        rightButton = new RectF(dpadX + BUTTON_SIZE, dpadY, dpadX + BUTTON_SIZE * 2, dpadY + BUTTON_SIZE);
        
        // Action buttons
        int actX = screenWidth - 200;
        int actY = screenHeight - 250;
        aButton = new RectF(actX, actY, actX + BUTTON_SIZE, actY + BUTTON_SIZE);
        bButton = new RectF(actX - BUTTON_SIZE - 20, actY + 40, actX - 20, actY + 40 + BUTTON_SIZE);
        startButton = new RectF(screenWidth / 2 - 40, screenHeight - 120, screenWidth / 2 + 40, screenHeight - 40);
        
        // Battle buttons
        ability1Button = new RectF(20, screenHeight - 300, screenWidth / 2 - 20, screenHeight - 240);
        ability2Button = new RectF(screenWidth / 2 + 20, screenHeight - 300, screenWidth - 20, screenHeight - 240);
        ability3Button = new RectF(20, screenHeight - 220, screenWidth / 2 - 20, screenHeight - 160);
        ability4Button = new RectF(screenWidth / 2 + 20, screenHeight - 220, screenWidth - 20, screenHeight - 160);
        
        glyphButton = new RectF(20, screenHeight - 140, screenWidth / 3 - 10, screenHeight - 80);
        etherealButton = new RectF(screenWidth / 3 + 10, screenHeight - 140, screenWidth * 2 / 3 - 10, screenHeight - 80);
        fleeButton = new RectF(screenWidth * 2 / 3 + 10, screenHeight - 140, screenWidth - 20, screenHeight - 80);
    }
    
    public void setGameState(ViewState state) {
        this.viewState = state;
        invalidate();
    }
    
    public void setMap(GameMap map) {
        this.currentMap = map;
        invalidate();
    }
    
    public void setBattle(BattleSystem battle) {
        this.currentBattle = battle;
        invalidate();
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        // Clear screen
        canvas.drawColor(Color.BLACK);
        
        switch (viewState) {
            case OVERWORLD:
                drawOverworld(canvas);
                drawControls(canvas);
                break;
            case BATTLE:
                drawBattle(canvas);
                drawBattleUI(canvas);
                break;
            case MENU:
                drawMenu(canvas);
                break;
        }
    }
    
    private void drawOverworld(Canvas canvas) {
        if (currentMap == null || gameEngine.getPlayerData() == null) return;
        
        int playerX = gameEngine.getPlayerData().getPlayerX();
        int playerY = gameEngine.getPlayerData().getPlayerY();
        
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        int tilesX = screenWidth / TILE_SIZE + 2;
        int tilesY = screenHeight / TILE_SIZE + 2;
        
        int startX = Math.max(0, playerX - tilesX / 2);
        int startY = Math.max(0, playerY - tilesY / 2);
        
        // Draw tiles
        for (int y = 0; y < tilesY; y++) {
            for (int x = 0; x < tilesX; x++) {
                int worldX = startX + x;
                int worldY = startY + y;
                
                Tile tile = currentMap.getTile(worldX, worldY);
                if (tile != null) {
                    int screenX = x * TILE_SIZE - ((playerX - startX) * TILE_SIZE - screenWidth / 2);
                    int screenY = y * TILE_SIZE - ((playerY - startY) * TILE_SIZE - screenHeight / 2);
                    
                    drawTile(canvas, tile, screenX, screenY);
                }
            }
        }
        
        // Draw player
        paint.setColor(Color.YELLOW);
        canvas.drawCircle(screenWidth / 2, screenHeight / 2, 16, paint);
        
        // Draw HUD
        paint.setColor(Color.WHITE);
        paint.setTextSize(24);
        canvas.drawText("Aether", 20, 40, paint);
        canvas.drawText("Region: " + currentMap.getName(), 20, 70, paint);
        
        if (gameEngine.getPlayerData().getTeam().size() > 0) {
            Ethereal first = gameEngine.getPlayerData().getTeam().get(0);
            canvas.drawText(first.getNickname() + " Lv." + first.getLevel(), 20, 100, paint);
            
            // HP bar
            paint.setColor(Color.RED);
            canvas.drawRect(20, 110, 220, 130, paint);
            paint.setColor(Color.GREEN);
            float hpPercent = first.getStats().getHpPercentage();
            canvas.drawRect(20, 110, 20 + 200 * hpPercent, 130, paint);
        }
    }
    
    private void drawTile(Canvas canvas, Tile tile, int x, int y) {
        switch (tile.getType()) {
            case GRASS:
                paint.setColor(Color.rgb(50, 150, 50));
                break;
            case TALL_GRASS:
                paint.setColor(Color.rgb(30, 120, 30));
                break;
            case PATH:
                paint.setColor(Color.rgb(139, 90, 43));
                break;
            case TREE:
                paint.setColor(Color.rgb(34, 139, 34));
                break;
            case ROCK:
                paint.setColor(Color.GRAY);
                break;
            default:
                paint.setColor(Color.BLACK);
        }
        canvas.drawRect(x, y, x + TILE_SIZE, y + TILE_SIZE, paint);
    }
    
    private void drawBattle(Canvas canvas) {
        if (currentBattle == null) return;
        
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        
        // Background
        paint.setColor(Color.rgb(100, 100, 150));
        canvas.drawRect(0, 0, screenWidth, screenHeight - 350, paint);
        
        // Opponent Ethereal area
        Ethereal opponent = currentBattle.getOpponentEthereal();
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        canvas.drawText(opponent.getNickname() + " Lv." + opponent.getLevel(), 20, 50, paint);
        
        // Opponent HP bar
        paint.setColor(Color.RED);
        canvas.drawRect(20, 60, 320, 80, paint);
        paint.setColor(Color.GREEN);
        float oppHp = opponent.getStats().getHpPercentage();
        canvas.drawRect(20, 60, 20 + 300 * oppHp, 80, paint);
        
        // Opponent sprite (placeholder circle)
        paint.setColor(opponent.getType().getColor());
        canvas.drawCircle(screenWidth - 100, 150, 60, paint);
        
        // Player Ethereal area
        Ethereal player = currentBattle.getPlayerEthereal();
        paint.setColor(Color.WHITE);
        canvas.drawText(player.getNickname() + " Lv." + player.getLevel(), screenWidth - 320, screenHeight - 470, paint);
        
        // Player HP bar
        paint.setColor(Color.RED);
        canvas.drawRect(screenWidth - 320, screenHeight - 460, screenWidth - 20, screenHeight - 440, paint);
        paint.setColor(Color.GREEN);
        float playerHp = player.getStats().getHpPercentage();
        canvas.drawRect(screenWidth - 320, screenHeight - 460, screenWidth - 320 + 300 * playerHp, screenHeight - 440, paint);
        
        // HP text
        paint.setTextSize(18);
        canvas.drawText(player.getStats().getCurrentHp() + "/" + player.getStats().getMaxHp(), 
                       screenWidth - 320, screenHeight - 420, paint);
        
        // Player sprite
        paint.setColor(player.getType().getColor());
        canvas.drawCircle(100, screenHeight - 550, 60, paint);
    }
    
    private void drawBattleUI(Canvas canvas) {
        if (currentBattle == null) return;
        
        paint.setStyle(Paint.Style.FILL);
        
        // Draw ability buttons
        Ethereal player = currentBattle.getPlayerEthereal();
        java.util.List<Ability> abilities = player.getAbilities();
        
        for (int i = 0; i < 4; i++) {
            RectF button = i == 0 ? ability1Button : i == 1 ? ability2Button : i == 2 ? ability3Button : ability4Button;
            
            if (i < abilities.size()) {
                paint.setColor(Color.DKGRAY);
                canvas.drawRect(button, paint);
                paint.setColor(Color.WHITE);
                paint.setTextSize(18);
                Ability ability = abilities.get(i);
                canvas.drawText(ability.getName(), button.left + 10, button.top + 30, paint);
                canvas.drawText("PWR:" + ability.getPower(), button.left + 10, button.top + 50, paint);
            }
        }
        
        // Draw action buttons
        paint.setColor(Color.rgb(100, 100, 200));
        canvas.drawRect(glyphButton, paint);
        canvas.drawRect(etherealButton, paint);
        canvas.drawRect(fleeButton, paint);
        
        paint.setColor(Color.WHITE);
        paint.setTextSize(20);
        canvas.drawText("GLYPH", glyphButton.left + 20, glyphButton.centerY() + 7, paint);
        canvas.drawText("SWITCH", etherealButton.left + 15, etherealButton.centerY() + 7, paint);
        canvas.drawText("FLEE", fleeButton.left + 25, fleeButton.centerY() + 7, paint);
    }
    
    private void drawControls(Canvas canvas) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(3);
        paint.setColor(Color.WHITE);
        
        // Draw D-Pad
        canvas.drawRect(upButton, paint);
        canvas.drawRect(downButton, paint);
        canvas.drawRect(leftButton, paint);
        canvas.drawRect(rightButton, paint);
        
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(24);
        canvas.drawText("↑", upButton.centerX() - 10, upButton.centerY() + 10, paint);
        canvas.drawText("↓", downButton.centerX() - 10, downButton.centerY() + 10, paint);
        canvas.drawText("←", leftButton.centerX() - 10, leftButton.centerY() + 10, paint);
        canvas.drawText("→", rightButton.centerX() - 10, rightButton.centerY() + 10, paint);
        
        // Draw action buttons
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(200, 50, 50));
        canvas.drawOval(aButton, paint);
        paint.setColor(Color.rgb(50, 50, 200));
        canvas.drawOval(bButton, paint);
        
        paint.setColor(Color.WHITE);
        paint.setTextSize(28);
        canvas.drawText("A", aButton.centerX() - 10, aButton.centerY() + 10, paint);
        canvas.drawText("B", bButton.centerX() - 10, bButton.centerY() + 10, paint);
        
        paint.setColor(Color.GRAY);
        canvas.drawRect(startButton, paint);
        paint.setColor(Color.WHITE);
        paint.setTextSize(16);
        canvas.drawText("START", startButton.left + 5, startButton.centerY() + 6, paint);
    }
    
    private void drawMenu(Canvas canvas) {
        int screenWidth = getWidth();
        int screenHeight = getHeight();
        
        paint.setColor(Color.rgb(40, 40, 40));
        canvas.drawRect(0, 0, screenWidth, screenHeight, paint);
        
        paint.setColor(Color.WHITE);
        paint.setTextSize(32);
        canvas.drawText("MENU", screenWidth / 2 - 50, 100, paint);
        
        paint.setTextSize(24);
        canvas.drawText("Team", 100, 200, paint);
        canvas.drawText("Inventory", 100, 250, paint);
        canvas.drawText("Quests", 100, 300, paint);
        canvas.drawText("Aetherdex", 100, 350, paint);
        canvas.drawText("Save", 100, 400, paint);
        canvas.drawText("Resume", 100, 450, paint);
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            
            if (viewState == ViewState.OVERWORLD) {
                handleOverworldTouch(x, y);
            } else if (viewState == ViewState.BATTLE) {
                handleBattleTouch(x, y);
            }
        }
        return true;
    }
    
    private void handleOverworldTouch(float x, float y) {
        if (upButton.contains(x, y)) {
            gameEngine.movePlayer(0, -1);
        } else if (downButton.contains(x, y)) {
            gameEngine.movePlayer(0, 1);
        } else if (leftButton.contains(x, y)) {
            gameEngine.movePlayer(-1, 0);
        } else if (rightButton.contains(x, y)) {
            gameEngine.movePlayer(1, 0);
        } else if (startButton.contains(x, y)) {
            gameEngine.openMenu();
        }
        invalidate();
    }
    
    private void handleBattleTouch(float x, float y) {
        if (currentBattle == null) return;
        
        Ethereal player = currentBattle.getPlayerEthereal();
        java.util.List<Ability> abilities = player.getAbilities();
        
        if (ability1Button.contains(x, y) && abilities.size() > 0) {
            currentBattle.playerUseAbility(abilities.get(0));
            gameEngine.getSoundGenerator().playHitSound(1.0f);
        } else if (ability2Button.contains(x, y) && abilities.size() > 1) {
            currentBattle.playerUseAbility(abilities.get(1));
            gameEngine.getSoundGenerator().playHitSound(1.0f);
        } else if (ability3Button.contains(x, y) && abilities.size() > 2) {
            currentBattle.playerUseAbility(abilities.get(2));
            gameEngine.getSoundGenerator().playHitSound(1.0f);
        } else if (ability4Button.contains(x, y) && abilities.size() > 3) {
            currentBattle.playerUseAbility(abilities.get(3));
            gameEngine.getSoundGenerator().playHitSound(1.0f);
        } else if (glyphButton.contains(x, y)) {
            boolean captured = currentBattle.attemptCapture(1.0f); // Standard glyph
            if (captured) {
                gameEngine.getSoundGenerator().playCaptureSound(true);
                postDelayed(() -> gameEngine.endBattle(true), 1000);
            } else {
                gameEngine.getSoundGenerator().playCaptureSound(false);
            }
        } else if (fleeButton.contains(x, y)) {
            currentBattle.attemptFlee();
        }
        
        // Check if battle ended
        if (currentBattle.getState() == BattleSystem.BattleState.PLAYER_WON) {
            postDelayed(() -> gameEngine.endBattle(true), 1500);
        } else if (currentBattle.getState() == BattleSystem.BattleState.OPPONENT_WON) {
            postDelayed(() -> gameEngine.endBattle(false), 1500);
        }
        
        invalidate();
    }
    
    private void startGameLoop() {
        Runnable gameLoop = new Runnable() {
            @Override
            public void run() {
                gameEngine.update(1.0f);
                invalidate();
                postDelayed(this, 33); // ~30 FPS
            }
        };
        post(gameLoop);
    }
}
