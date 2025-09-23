package com.hotcmds;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.MinecraftClient;

import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.text.JTextComponent;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;


class KeyCommandPair{
	public int key;
	public String command;
	public KeyCommandPair(int k, String str){
		key = k;
		command = str;
	}
}
public class HotcmdsClient implements ClientModInitializer {
	public static final HotcmdsClient INSTANCE = new HotcmdsClient();

	public static final String MOD_ID = "hotcmds";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private final Map<Integer, String> keyToCommand = new HashMap<>();
	private final Map<Integer, Boolean> keyStates = new HashMap<>();
	private final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("hotcmds/keybindings.json");
	private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

	public static final KeyBinding MENU = new KeyBinding("Open Menu", GLFW.GLFW_KEY_K,"Command Hotkeys");

	@Override
	public void onInitializeClient() {
		loadKeyMappings();
		registerCommands();
		KeyBindingHelper.registerKeyBinding(MENU);


		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		LOGGER.info("passed");
		ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
			if (MinecraftClient.getInstance().currentScreen != null && !(MinecraftClient.getInstance().currentScreen instanceof HandledScreen<?>)) {
				return;
			}
			if(MENU.wasPressed()){
				minecraftClient.setScreen(new ListMenu(minecraftClient.currentScreen));
			}


			for (int keyCode : INSTANCE.keyToCommand.keySet()) {

				boolean isPressed = false;

				try {
					isPressed = InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), keyCode);
				} catch (Exception e) {
					return;
				}
				boolean wasPressed = INSTANCE.keyStates.getOrDefault(keyCode, false);
				if (isPressed && !wasPressed) {
					if (minecraftClient.player != null && minecraftClient.getNetworkHandler() != null) {
						minecraftClient.getNetworkHandler().sendChatCommand(INSTANCE.keyToCommand.get(keyCode));

					}
				}
				INSTANCE.keyStates.put(keyCode, isPressed);
			}
		});
	}

	private void loadKeyMappings() {
		INSTANCE.keyToCommand.clear();
		INSTANCE.keyStates.clear();


		if (Files.exists(configPath)) {
			try (Reader reader = Files.newBufferedReader(configPath)) {
				List<KeyCommandPair> pairs = gson.fromJson(reader, new TypeToken<List<KeyCommandPair>>() {
				}.getType());
				LOGGER.info("loaded into put" + pairs.isEmpty());
				for (KeyCommandPair pair : pairs) {
					INSTANCE.keyToCommand.put(pair.key, pair.command);
					INSTANCE.keyStates.put(pair.key, false);
					LOGGER.info("loaded into put");
				}
				LOGGER.info("loaded");


			} catch (IOException | com.google.gson.JsonSyntaxException e) {
				System.err.println("Failed to load key mappings: " + e.getMessage());

			}
		}
	}

	private void saveKeyMappings() {
		try {
			Files.createDirectories(configPath.getParent());
			LOGGER.info("file created");
		} catch (IOException e) {
			System.err.println("Failed to create config directory: " + e.getMessage());
			return;
		}
		List<KeyCommandPair> pairs = new ArrayList<>();
		for (Map.Entry<Integer, String> entry : INSTANCE.keyToCommand.entrySet()) {
			pairs.add(new KeyCommandPair(entry.getKey(), entry.getValue()));
		}
		try (Writer writer = Files.newBufferedWriter(configPath)) {
			gson.toJson(pairs, writer);
			LOGGER.info("saved");
		} catch (IOException e) {
			System.err.println("Failed to save key mappings: " + e.getMessage());
		}

	}

	public void addKeyMapping(int keyCode, String command) {

		INSTANCE.keyToCommand.put(keyCode, command);
		INSTANCE.keyStates.putIfAbsent(keyCode, false);

		saveKeyMappings();
	}

	public void removeKey(int keyCode) {
		INSTANCE.keyToCommand.remove(keyCode);
		INSTANCE.keyStates.remove(keyCode);
		saveKeyMappings();
	}

	public List<KeyCommandPair> getKeybinds() {
		List<KeyCommandPair> list = new ArrayList<>();
		for (Map.Entry<Integer, String> entry : INSTANCE.keyToCommand.entrySet()) {
			list.add(new KeyCommandPair(entry.getKey(), entry.getValue()));
		}
		return list;
	}


	private void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommandManager.literal("hotcmds")
							.executes(context -> {
								MinecraftClient client = MinecraftClient.getInstance();
								if (client != null) {
									client.execute(() -> {

										AtomicBoolean open = new AtomicBoolean(true);
										ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
											if (open.get())
												client.setScreen(new ListMenu(client.currentScreen));
											open.set(false);
										});

									});
								} else {
									System.out.println("MinecraftClient is null");
								}
								return 1;
							})

			);

		});
	}


}