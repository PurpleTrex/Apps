package com.purpletrex.aether.core;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.purpletrex.aether.R;
import com.purpletrex.aether.data.SaveManager;
import com.purpletrex.aether.ui.GameView;

/**
 * Main activity - entry point for the game
 */
public class MainActivity extends Activity {
    private GameEngine gameEngine;
    private SaveManager saveManager;
    private GameView gameView;
    private LinearLayout mainMenuLayout;
    private TextView titleText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize managers
        gameEngine = new GameEngine();
        saveManager = new SaveManager(this);
        
        // Show title screen
        showTitleScreen();
    }

    /**
     * Show title screen with menu options
     */
    private void showTitleScreen() {
        // Create title screen layout programmatically
        mainMenuLayout = new LinearLayout(this);
        mainMenuLayout.setOrientation(LinearLayout.VERTICAL);
        mainMenuLayout.setPadding(50, 100, 50, 50);
        
        // Title
        titleText = new TextView(this);
        titleText.setText("AETHER");
        titleText.setTextSize(48);
        titleText.setGravity(android.view.Gravity.CENTER);
        titleText.setPadding(0, 0, 0, 50);
        mainMenuLayout.addView(titleText);
        
        // Subtitle
        TextView subtitleText = new TextView(this);
        subtitleText.setText("A Monster-Catching RPG");
        subtitleText.setTextSize(18);
        subtitleText.setGravity(android.view.Gravity.CENTER);
        subtitleText.setPadding(0, 0, 0, 100);
        mainMenuLayout.addView(subtitleText);
        
        // New Game button
        Button newGameButton = new Button(this);
        newGameButton.setText(R.string.new_game);
        newGameButton.setTextSize(20);
        newGameButton.setPadding(20, 20, 20, 20);
        newGameButton.setOnClickListener(v -> startNewGame());
        mainMenuLayout.addView(newGameButton);
        
        // Continue button (only if save exists)
        if (saveManager.hasSaveData()) {
            Button continueButton = new Button(this);
            continueButton.setText(R.string.continue_game);
            continueButton.setTextSize(20);
            continueButton.setPadding(20, 20, 20, 20);
            continueButton.setOnClickListener(v -> continueGame());
            mainMenuLayout.addView(continueButton);
        }
        
        // Settings button
        Button settingsButton = new Button(this);
        settingsButton.setText(R.string.settings);
        settingsButton.setTextSize(20);
        settingsButton.setPadding(20, 20, 20, 20);
        settingsButton.setOnClickListener(v -> showSettings());
        mainMenuLayout.addView(settingsButton);
        
        // Exit button
        Button exitButton = new Button(this);
        exitButton.setText(R.string.exit);
        exitButton.setTextSize(20);
        exitButton.setPadding(20, 20, 20, 20);
        exitButton.setOnClickListener(v -> finish());
        mainMenuLayout.addView(exitButton);
        
        setContentView(mainMenuLayout);
    }

    /**
     * Start a new game
     */
    private void startNewGame() {
        // In full implementation, would show name input dialog
        String playerName = "Trainer";
        
        gameEngine.startNewGame(playerName);
        
        // Setup game listener
        gameEngine.setListener(new GameEngine.GameListener() {
            @Override
            public void onStateChanged(GameEngine.GameState newState) {
                runOnUiThread(() -> {
                    switch (newState) {
                        case OVERWORLD:
                            if (gameView != null) {
                                gameView.setGameState(GameView.ViewState.OVERWORLD);
                            }
                            break;
                        case BATTLE:
                            if (gameView != null) {
                                gameView.setGameState(GameView.ViewState.BATTLE);
                            }
                            break;
                        case MENU:
                            if (gameView != null) {
                                gameView.setGameState(GameView.ViewState.MENU);
                            }
                            break;
                    }
                });
            }

            @Override
            public void onMessage(String message) {
                runOnUiThread(() -> Toast.makeText(MainActivity.this, message, Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onBattleStart(com.purpletrex.aether.battle.BattleSystem battle) {
                runOnUiThread(() -> {
                    if (gameView != null) {
                        gameView.setBattle(battle);
                    }
                });
            }

            @Override
            public void onMapChanged(com.purpletrex.aether.world.GameMap map) {
                runOnUiThread(() -> {
                    if (gameView != null) {
                        gameView.setMap(map);
                    }
                });
            }
        });
        
        // Show game view
        showGameView();
    }

    /**
     * Continue saved game
     */
    private void continueGame() {
        PlayerData loadedData = saveManager.loadGame();
        if (loadedData != null) {
            // Would need to restore game engine state
            Toast.makeText(this, "Game loaded!", Toast.LENGTH_SHORT).show();
            showGameView();
        } else {
            Toast.makeText(this, "Failed to load save data", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Show settings menu
     */
    private void showSettings() {
        // Create settings layout
        LinearLayout settingsLayout = new LinearLayout(this);
        settingsLayout.setOrientation(LinearLayout.VERTICAL);
        settingsLayout.setPadding(50, 50, 50, 50);
        
        TextView settingsTitle = new TextView(this);
        settingsTitle.setText("Settings");
        settingsTitle.setTextSize(32);
        settingsTitle.setGravity(android.view.Gravity.CENTER);
        settingsTitle.setPadding(0, 0, 0, 50);
        settingsLayout.addView(settingsTitle);
        
        // Sound toggle
        Button soundToggle = new Button(this);
        soundToggle.setText("Sound: ON");
        soundToggle.setOnClickListener(v -> {
            // Toggle sound
            Toast.makeText(this, "Sound toggled", Toast.LENGTH_SHORT).show();
        });
        settingsLayout.addView(soundToggle);
        
        // Music toggle
        Button musicToggle = new Button(this);
        musicToggle.setText("Music: ON");
        musicToggle.setOnClickListener(v -> {
            // Toggle music
            Toast.makeText(this, "Music toggled", Toast.LENGTH_SHORT).show();
        });
        settingsLayout.addView(musicToggle);
        
        // Back button
        Button backButton = new Button(this);
        backButton.setText("Back");
        backButton.setOnClickListener(v -> showTitleScreen());
        settingsLayout.addView(backButton);
        
        setContentView(settingsLayout);
    }

    /**
     * Show main game view
     */
    private void showGameView() {
        gameView = new GameView(this, gameEngine);
        setContentView(gameView);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Save game when pausing
        if (gameEngine != null && gameEngine.getPlayerData() != null) {
            saveManager.saveGame(gameEngine.getPlayerData());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume game
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Clean up
    }

    @Override
    public void onBackPressed() {
        if (gameEngine != null && gameEngine.getCurrentState() == GameEngine.GameState.OVERWORLD) {
            gameEngine.openMenu();
        } else if (gameEngine != null && gameEngine.getCurrentState() == GameEngine.GameState.MENU) {
            gameEngine.closeMenu();
        } else {
            super.onBackPressed();
        }
    }
}
