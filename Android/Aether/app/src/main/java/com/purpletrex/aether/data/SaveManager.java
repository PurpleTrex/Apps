package com.purpletrex.aether.data;

import android.content.Context;
import android.content.SharedPreferences;
import com.purpletrex.aether.core.PlayerData;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Manages saving and loading game data
 */
public class SaveManager {
    private static final String PREFS_NAME = "AetherSaveData";
    private static final String KEY_PLAYER_DATA = "player_data";
    private static final String KEY_HAS_SAVE = "has_save";
    
    private Context context;
    private SharedPreferences prefs;

    public SaveManager(Context context) {
        this.context = context;
        this.prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Save game data
     */
    public boolean saveGame(PlayerData playerData) {
        try {
            JSONObject saveData = new JSONObject();
            
            // Basic player info
            saveData.put("name", playerData.getName());
            saveData.put("x", playerData.getPlayerX());
            saveData.put("y", playerData.getPlayerY());
            saveData.put("region", playerData.getCurrentRegion());
            saveData.put("playTime", playerData.getPlayTime());
            saveData.put("money", playerData.getInventory().getMoney());
            
            // Team
            JSONArray teamArray = new JSONArray();
            for (int i = 0; i < playerData.getTeam().size(); i++) {
                JSONObject etherealData = serializeEthereal(playerData.getTeam().get(i));
                teamArray.put(etherealData);
            }
            saveData.put("team", teamArray);
            
            // Captured species
            JSONArray capturedArray = new JSONArray();
            for (int speciesId : playerData.getCapturedSpecies()) {
                capturedArray.put(speciesId);
            }
            saveData.put("captured", capturedArray);
            
            // Save to preferences
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_PLAYER_DATA, saveData.toString());
            editor.putBoolean(KEY_HAS_SAVE, true);
            return editor.commit();
            
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Load game data
     */
    public PlayerData loadGame() {
        if (!hasSaveData()) {
            return null;
        }
        
        try {
            String saveDataStr = prefs.getString(KEY_PLAYER_DATA, null);
            if (saveDataStr == null) return null;
            
            JSONObject saveData = new JSONObject(saveDataStr);
            
            // Create player
            String name = saveData.getString("name");
            PlayerData playerData = new PlayerData(name);
            
            // Load basic info
            playerData.setPlayerX(saveData.getInt("x"));
            playerData.setPlayerY(saveData.getInt("y"));
            playerData.setCurrentRegion(saveData.getString("region"));
            playerData.incrementPlayTime(saveData.getInt("playTime"));
            
            // Load money
            int money = saveData.getInt("money");
            playerData.getInventory().addMoney(money - 3000); // Adjust for starting money
            
            // Load team
            JSONArray teamArray = saveData.getJSONArray("team");
            playerData.getTeam().clear();
            for (int i = 0; i < teamArray.length(); i++) {
                JSONObject etherealData = teamArray.getJSONObject(i);
                // Note: In full implementation, deserialize Ethereal from data
                // For now, create placeholder
                int speciesId = etherealData.getInt("speciesId");
                int level = etherealData.getInt("level");
                playerData.addToTeam(EtherealDatabase.createEthereal(speciesId, level));
            }
            
            return playerData;
            
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Serialize an Ethereal to JSON
     */
    private JSONObject serializeEthereal(com.purpletrex.aether.entities.Ethereal ethereal) throws JSONException {
        JSONObject data = new JSONObject();
        data.put("speciesId", ethereal.getSpeciesId());
        data.put("nickname", ethereal.getNickname());
        data.put("level", ethereal.getLevel());
        data.put("experience", ethereal.getExperience());
        data.put("currentHp", ethereal.getStats().getCurrentHp());
        data.put("isShiny", ethereal.isShiny());
        data.put("friendship", ethereal.getFriendship());
        return data;
    }

    /**
     * Check if save data exists
     */
    public boolean hasSaveData() {
        return prefs.getBoolean(KEY_HAS_SAVE, false);
    }

    /**
     * Delete save data
     */
    public boolean deleteSave() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        return editor.commit();
    }
}
