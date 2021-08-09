package com.branders.spawnermod.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

import com.branders.spawnermod.SpawnerMod;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 	Simple config manager using a <modid>.json file. Init the config by calling initConfig() with
 * 	mod id as parameter.
 * 
 * 	@author Anders <Branders> Blomqvist
 */
public class ModConfigManager {
	
	static class Pair<L, R> {
		private final L left;
		private final R right;
		
		public Pair(L left, R right) {
			assert left != null;
			assert right != null;
			
			this.left = left;
			this.right = right;
		}
		
		public L getLeft() { return left; }
		public R getRight() { return right; }
	}
	
	private static File file;
	public static final Gson GSON = new GsonBuilder()
			.setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
			.setPrettyPrinting().create();
	
	/**
	 * 	Initialize the mod config. Try find an existing config file. If it exists we set values from
	 * 	file. Otherwise we create a new config file with default values.
	 */
	public static void initConfig(String modid, File absoluteFile) {
		
		// Config values will be overwritten if a config file exists.
		ConfigValues.setDefaultConfigValues();
		
		file = new File(absoluteFile, "/config/" + modid + ".json");
		
		if(!file.exists()) {
			// No config file found. Create a new default config
			SpawnerMod.LOGGER.info("Could not find config, generating new default config.");
			saveConfig();
		}
		else {
			SpawnerMod.LOGGER.info("Reading config values from file.");
			readConfig();
		}
	}
	
	/**
	 * 	Reads the .json file where we iterate over the CONFIG_SPEC key values and read the value for
	 * 	each key.
	 */
	private static void readConfig() {
		try {
			BufferedReader reader =  new BufferedReader(new FileReader(file));
			JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
			
			Pair<JsonObject, Boolean> validConfig = validateConfig(json);
			JsonObject entities = json.getAsJsonObject("disable_specific_egg_drops");
			
			for(String key : ConfigValues.getKeys()) {
				if(json.get(key) != null)
					ConfigValues.put(key, json.get(key).getAsInt());
				else if(entities.get(key) != null)
					ConfigValues.put(key, entities.get(key).getAsInt());
				else
					SpawnerMod.LOGGER.warn("Key error: Could not find key: " + key);
			}
			
			// If the loaded config was broken, save the fixed version now.
			if(validConfig.getRight()) {
				SpawnerMod.LOGGER.info("Config was broken. Saving new config which is fixed!");
				saveConfig();
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 	Saves the current values of the config to file.
	 */
	private static void saveConfig() {
		JsonObject config = new JsonObject();
		
		// Sort the keys so it's easier for a human to edit config file
		Object[] keys = ConfigValues.getKeys().toArray();
		Arrays.sort(keys);
		
		JsonObject entities = new JsonObject();
		
		for(Object key : keys) {
			if(((String) key).matches("\\w+:\\w+"))	// Matches: "minecraft:creeper" or "modid:entityname"
				entities.addProperty((String) key, ConfigValues.get((String) key));
			else
				config.addProperty((String) key, ConfigValues.get((String) key));
		}
			
		config.add("disable_specific_egg_drops", entities);
		
		String jsonConfig = GSON.toJson(config);
		
		try {
			FileWriter writer = new FileWriter(file);
			writer.write(jsonConfig);
			writer.close();
		} catch (IOException e) {
			SpawnerMod.LOGGER.warn("Could not save config file.");
			e.printStackTrace();
		}
	}
	
	/**
	 * 	Validates the config file. It searches through all keys and checks if any key is missing.
	 * 	If a key is missing we add it to the json with the default value.
	 * 	
	 * 	@param json Parsed json object.
	 * 	@return A Pair with the potentially fixed config on left and a state indicating whether the config was broken or not on the right. 
	 */
	private static Pair<JsonObject, Boolean> validateConfig(JsonObject json) {
		boolean brokenConfig = false;
		
		if(json.getAsJsonObject("disable_specific_egg_drops") == null) {
			// If we are missing the whole group "disable_specific_egg_drops" then all of its children 
			// are missing too. Which means: add them.
			SpawnerMod.LOGGER.info("Broken config. Group key=disable_specific_egg_drops was not found. Adding it and all its children ...");
			JsonObject entities = new JsonObject();
			for(Object k : ConfigValues.getKeys().toArray())
				if(((String) k).matches("\\w+:\\w+"))
					entities.addProperty((String) k, 0);
			json.add("disable_specific_egg_drops", entities);
		}
		
		for(String key : ConfigValues.getKeys()) {
			JsonElement elem = json.get(key);
			
			if(key.matches("\\w+:\\w+"))
				elem = json.getAsJsonObject("disable_specific_egg_drops").get(key);
			
			if(elem == null) {
				SpawnerMod.LOGGER.info("Broken config. Key=" + key + " was not found. Adding it ...");
				brokenConfig = true;
				json.addProperty(key, ConfigValues.get(key));
			}
		}
		
		return new Pair<JsonObject, Boolean>(json, brokenConfig);
	}
}
