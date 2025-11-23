package com.purpletrex.aether.core;

import com.purpletrex.aether.entities.Ethereal;
import com.purpletrex.aether.world.GameMap;
import com.purpletrex.aether.battle.BattleSystem;
import com.purpletrex.aether.data.EtherealDatabase;
import com.purpletrex.aether.data.AbilityDatabase;
import com.purpletrex.aether.entities.ElementType;
import com.purpletrex.aether.generation.SpriteGenerator;
import com.purpletrex.aether.generation.SoundGenerator;
import java.util.ArrayList;
import java.util.List;

/**
 * Main game engine managing game state and logic
 */
public class GameEngine {
    private GameState currentState;
    private PlayerData playerData;
    private GameMap currentMap;
    private BattleSystem activeBattle;
    private SpriteGenerator spriteGenerator;
    private SoundGenerator soundGenerator;
    private GameListener listener;
    private int stepCounter;

    public enum GameState {
        TITLE_SCREEN,
        NEW_GAME_SETUP,
        OVERWORLD,
        BATTLE,
        MENU,
        DIALOGUE,
        PAUSED
    }

    public interface GameListener {
        void onStateChanged(GameState newState);
        void onMessage(String message);
        void onBattleStart(BattleSystem battle);
        void onMapChanged(GameMap map);
    }

    public GameEngine() {
        this.currentState = GameState.TITLE_SCREEN;
        this.spriteGenerator = new SpriteGenerator();
        this.soundGenerator = new SoundGenerator();
        this.stepCounter = 0;
    }

    public void setListener(GameListener listener) {
        this.listener = listener;
    }

    /**
     * Start a new game
     */
    public void startNewGame(String playerName) {
        playerData = new PlayerData(playerName);
        
        // Give starter Ethereal (player choice would be implemented in UI)
        int starterChoice = 1; // Sproutling
        Ethereal starter = EtherealDatabase.createEthereal(starterChoice, 5);
        
        // Give starter 4 abilities
        starter.learnAbility(AbilityDatabase.getAbility(1)); // Tackle
        starter.learnAbility(AbilityDatabase.getAbility(11)); // Vine Whip
        starter.learnAbility(AbilityDatabase.getAbility(22)); // Photosynthesis
        starter.learnAbility(AbilityDatabase.getAbility(12)); // Razor Leaf
        
        playerData.addToTeam(starter);
        
        // Load starting map
        currentMap = new GameMap("Verdant Hollow - Route 1", 50, 50, ElementType.NATURE, 2, 7);
        
        changeState(GameState.OVERWORLD);
        
        if (listener != null) {
            listener.onMessage("Welcome to Aether, " + playerName + "!");
            listener.onMapChanged(currentMap);
        }
    }

    /**
     * Player movement
     */
    public void movePlayer(int dx, int dy) {
        if (currentState != GameState.OVERWORLD) return;
        
        int newX = playerData.getPlayerX() + dx;
        int newY = playerData.getPlayerY() + dy;
        
        // Check if new position is walkable
        if (currentMap.isWalkable(newX, newY)) {
            playerData.setPlayerX(newX);
            playerData.setPlayerY(newY);
            stepCounter++;
            
            // Check for random encounter every few steps
            if (stepCounter % 3 == 0 && currentMap.checkEncounter(newX, newY)) {
                triggerWildEncounter();
            }
        }
    }

    /**
     * Trigger a wild Ethereal encounter
     */
    private void triggerWildEncounter() {
        int wildLevel = currentMap.getWildEtherealLevel();
        int wildSpecies = currentMap.getWildEtherealSpecies();
        
        Ethereal wildEthereal = EtherealDatabase.createEthereal(wildSpecies, wildLevel);
        
        // Give wild Ethereal some abilities
        wildEthereal.learnAbility(AbilityDatabase.getAbility(1 + (wildSpecies % 10)));
        wildEthereal.learnAbility(AbilityDatabase.getAbility(11 + (wildSpecies % 20)));
        
        List<Ethereal> wildTeam = new ArrayList<>();
        wildTeam.add(wildEthereal);
        
        activeBattle = new BattleSystem(playerData.getTeam(), wildTeam, true);
        
        changeState(GameState.BATTLE);
        
        if (listener != null) {
            listener.onMessage("A wild " + wildEthereal.getNickname() + " appeared!");
            listener.onBattleStart(activeBattle);
        }
        
        soundGenerator.playEtherealCry(wildSpecies, wildEthereal.getType());
    }

    /**
     * Start trainer battle
     */
    public void startTrainerBattle(List<Ethereal> trainerTeam) {
        activeBattle = new BattleSystem(playerData.getTeam(), trainerTeam, false);
        
        changeState(GameState.BATTLE);
        
        if (listener != null) {
            listener.onBattleStart(activeBattle);
        }
    }

    /**
     * End current battle
     */
    public void endBattle(boolean playerWon) {
        if (playerWon && activeBattle != null && activeBattle.isWildBattle()) {
            // Check if captured
            Ethereal wildEthereal = activeBattle.getOpponentEthereal();
            if (!wildEthereal.isFainted()) {
                // Was captured
                if (playerData.addToTeam(wildEthereal)) {
                    if (listener != null) {
                        listener.onMessage(wildEthereal.getNickname() + " joined your team!");
                    }
                } else {
                    playerData.addToStorage(wildEthereal);
                    if (listener != null) {
                        listener.onMessage(wildEthereal.getNickname() + " was sent to storage!");
                    }
                }
            }
        }
        
        // Heal team if won trainer battle
        if (playerWon && activeBattle != null && !activeBattle.isWildBattle()) {
            for (Ethereal e : playerData.getTeam()) {
                e.fullHeal();
            }
        }
        
        activeBattle = null;
        changeState(GameState.OVERWORLD);
    }

    /**
     * Open menu
     */
    public void openMenu() {
        if (currentState == GameState.OVERWORLD) {
            changeState(GameState.MENU);
        }
    }

    /**
     * Close menu
     */
    public void closeMenu() {
        if (currentState == GameState.MENU) {
            changeState(GameState.OVERWORLD);
        }
    }

    /**
     * Change game state
     */
    private void changeState(GameState newState) {
        currentState = newState;
        if (listener != null) {
            listener.onStateChanged(newState);
        }
    }

    /**
     * Update game (called every frame)
     */
    public void update(float deltaTime) {
        // Update play time
        playerData.incrementPlayTime((int)deltaTime);
        
        // State-specific updates
        switch (currentState) {
            case OVERWORLD:
                // Check triggers, NPCs, etc.
                break;
            case BATTLE:
                // Battle is handled separately
                break;
        }
    }

    // Getters
    public GameState getCurrentState() { return currentState; }
    public PlayerData getPlayerData() { return playerData; }
    public GameMap getCurrentMap() { return currentMap; }
    public BattleSystem getActiveBattle() { return activeBattle; }
    public SpriteGenerator getSpriteGenerator() { return spriteGenerator; }
    public SoundGenerator getSoundGenerator() { return soundGenerator; }
}
