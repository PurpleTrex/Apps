package com.purpletrex.aether.battle;

import com.purpletrex.aether.entities.Ethereal;
import com.purpletrex.aether.entities.StatusCondition;
import java.util.ArrayList;
import java.util.List;

/**
 * Complete battle system managing turn-based combat
 */
public class BattleSystem {
    private Ethereal playerEthereal;
    private Ethereal opponentEthereal;
    private List<Ethereal> playerTeam;
    private List<Ethereal> opponentTeam;
    private int playerTeamIndex;
    private int opponentTeamIndex;
    private BattleState state;
    private boolean isWildBattle;
    private BattleListener listener;

    public enum BattleState {
        PLAYER_TURN,
        OPPONENT_TURN,
        PLAYER_WON,
        OPPONENT_WON,
        FLED,
        CAPTURE_ATTEMPT
    }

    public interface BattleListener {
        void onBattleMessage(String message);
        void onBattleEnd(boolean playerWon);
        void onEtherealFainted(boolean isPlayer);
    }

    public BattleSystem(List<Ethereal> playerTeam, List<Ethereal> opponentTeam, boolean isWildBattle) {
        this.playerTeam = new ArrayList<>(playerTeam);
        this.opponentTeam = new ArrayList<>(opponentTeam);
        this.isWildBattle = isWildBattle;
        this.playerTeamIndex = 0;
        this.opponentTeamIndex = 0;
        this.playerEthereal = playerTeam.get(0);
        this.opponentEthereal = opponentTeam.get(0);
        this.state = BattleState.PLAYER_TURN;
    }

    public void setListener(BattleListener listener) {
        this.listener = listener;
    }

    /**
     * Player uses an ability
     */
    public void playerUseAbility(Ability ability) {
        if (state != BattleState.PLAYER_TURN) return;

        sendMessage(playerEthereal.getNickname() + " used " + ability.getName() + "!");

        // Check if can act (status conditions)
        if (!playerEthereal.canAct()) {
            sendMessage(playerEthereal.getNickname() + " cannot move due to " + playerEthereal.getStatus());
            opponentTurn();
            return;
        }

        // Check if ability hits
        if (!ability.attemptHit()) {
            sendMessage("The attack missed!");
            opponentTurn();
            return;
        }

        // Calculate and apply damage
        int damage = playerEthereal.calculateDamage(ability, opponentEthereal);
        opponentEthereal.takeDamage(damage);
        
        float effectiveness = playerEthereal.getType().getEffectivenessAgainst(opponentEthereal.getType());
        if (effectiveness > 1.0f) {
            sendMessage("It's super effective!");
        } else if (effectiveness < 1.0f) {
            sendMessage("It's not very effective...");
        }

        sendMessage(opponentEthereal.getNickname() + " took " + damage + " damage!");

        // Check if opponent fainted
        if (opponentEthereal.isFainted()) {
            handleOpponentFainted();
            return;
        }

        opponentTurn();
    }

    /**
     * Opponent's turn to attack
     */
    private void opponentTurn() {
        state = BattleState.OPPONENT_TURN;
        
        // Apply end-of-turn effects
        playerEthereal.endTurnEffects();
        opponentEthereal.endTurnEffects();

        // Check if player's Ethereal fainted from status
        if (playerEthereal.isFainted()) {
            handlePlayerFainted();
            return;
        }

        // Simple AI: choose random ability
        List<Ability> abilities = opponentEthereal.getAbilities();
        if (abilities.isEmpty()) {
            sendMessage(opponentEthereal.getNickname() + " has no moves!");
            state = BattleState.PLAYER_TURN;
            return;
        }

        Ability ability = abilities.get((int)(Math.random() * abilities.size()));
        sendMessage(opponentEthereal.getNickname() + " used " + ability.getName() + "!");

        // Check if can act
        if (!opponentEthereal.canAct()) {
            sendMessage(opponentEthereal.getNickname() + " cannot move!");
            state = BattleState.PLAYER_TURN;
            return;
        }

        // Check if hits
        if (!ability.attemptHit()) {
            sendMessage("The attack missed!");
            state = BattleState.PLAYER_TURN;
            return;
        }

        // Calculate and apply damage
        int damage = opponentEthereal.calculateDamage(ability, playerEthereal);
        playerEthereal.takeDamage(damage);

        float effectiveness = opponentEthereal.getType().getEffectivenessAgainst(playerEthereal.getType());
        if (effectiveness > 1.0f) {
            sendMessage("It's super effective!");
        } else if (effectiveness < 1.0f) {
            sendMessage("It's not very effective...");
        }

        sendMessage(playerEthereal.getNickname() + " took " + damage + " damage!");

        // Check if player's Ethereal fainted
        if (playerEthereal.isFainted()) {
            handlePlayerFainted();
            return;
        }

        state = BattleState.PLAYER_TURN;
    }

    /**
     * Player attempts to flee
     */
    public boolean attemptFlee() {
        if (!isWildBattle) {
            sendMessage("Cannot flee from trainer battles!");
            return false;
        }

        // 50% chance to flee in wild battles
        if (Math.random() < 0.5) {
            sendMessage("Got away safely!");
            state = BattleState.FLED;
            if (listener != null) {
                listener.onBattleEnd(false);
            }
            return true;
        } else {
            sendMessage("Can't escape!");
            opponentTurn();
            return false;
        }
    }

    /**
     * Player attempts to capture (wild battles only)
     */
    public boolean attemptCapture(float glyphMultiplier) {
        if (!isWildBattle) {
            sendMessage("Cannot capture trainer Ethereals!");
            return false;
        }

        state = BattleState.CAPTURE_ATTEMPT;
        sendMessage("You threw a capture glyph!");

        float captureRate = opponentEthereal.calculateCaptureRate(glyphMultiplier);
        boolean captured = Math.random() < captureRate;

        if (captured) {
            sendMessage("Gotcha! " + opponentEthereal.getNickname() + " was captured!");
            state = BattleState.PLAYER_WON;
            if (listener != null) {
                listener.onBattleEnd(true);
            }
            return true;
        } else {
            sendMessage("Oh no! The Ethereal broke free!");
            opponentTurn();
            return false;
        }
    }

    /**
     * Switch player's active Ethereal
     */
    public void switchEthereal(int teamIndex) {
        if (teamIndex < 0 || teamIndex >= playerTeam.size()) return;
        if (playerTeam.get(teamIndex).isFainted()) {
            sendMessage("That Ethereal has fainted!");
            return;
        }

        playerTeamIndex = teamIndex;
        playerEthereal = playerTeam.get(teamIndex);
        sendMessage("Go, " + playerEthereal.getNickname() + "!");
        opponentTurn();
    }

    /**
     * Handle player's Ethereal fainting
     */
    private void handlePlayerFainted() {
        sendMessage(playerEthereal.getNickname() + " fainted!");
        if (listener != null) {
            listener.onEtherealFainted(true);
        }

        // Check if player has more Ethereals
        boolean hasAlive = false;
        for (int i = 0; i < playerTeam.size(); i++) {
            if (!playerTeam.get(i).isFainted()) {
                hasAlive = true;
                break;
            }
        }

        if (!hasAlive) {
            sendMessage("You have no more Ethereals!");
            state = BattleState.OPPONENT_WON;
            if (listener != null) {
                listener.onBattleEnd(false);
            }
        } else {
            sendMessage("Choose another Ethereal!");
            // Wait for player to switch
        }
    }

    /**
     * Handle opponent's Ethereal fainting
     */
    private void handleOpponentFainted() {
        sendMessage(opponentEthereal.getNickname() + " fainted!");
        if (listener != null) {
            listener.onEtherealFainted(false);
        }

        // Award experience
        int expGain = (opponentEthereal.getLevel() * 50) / playerTeam.size();
        playerEthereal.gainExperience(expGain);
        sendMessage(playerEthereal.getNickname() + " gained " + expGain + " EXP!");

        // Check if opponent has more Ethereals
        boolean hasAlive = false;
        for (int i = 0; i < opponentTeam.size(); i++) {
            if (!opponentTeam.get(i).isFainted()) {
                opponentTeamIndex = i;
                opponentEthereal = opponentTeam.get(i);
                hasAlive = true;
                sendMessage("Opponent sent out " + opponentEthereal.getNickname() + "!");
                break;
            }
        }

        if (!hasAlive) {
            sendMessage("You won the battle!");
            state = BattleState.PLAYER_WON;
            if (listener != null) {
                listener.onBattleEnd(true);
            }
        } else {
            state = BattleState.PLAYER_TURN;
        }
    }

    private void sendMessage(String message) {
        if (listener != null) {
            listener.onBattleMessage(message);
        }
    }

    // Getters
    public Ethereal getPlayerEthereal() { return playerEthereal; }
    public Ethereal getOpponentEthereal() { return opponentEthereal; }
    public BattleState getState() { return state; }
    public boolean isWildBattle() { return isWildBattle; }
    public List<Ethereal> getPlayerTeam() { return playerTeam; }
    public List<Ethereal> getOpponentTeam() { return opponentTeam; }
}
