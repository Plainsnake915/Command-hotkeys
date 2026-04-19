package com.hotcmds;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import com.mojang.authlib.minecraft.client.MinecraftClient;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;

import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;


import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;


import net.minecraft.client.gui.screens.inventory.AbstractSignEditScreen;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import net.minecraft.client.gui.screens.inventory.BookEditScreen;


import net.minecraft.client.gui.screens.options.controls.KeyBindsList;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.resources.Identifier;
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


	public static KeyMapping MENU;
	public static KeyMapping.Category category;
	public static Identifier id;
	@Override
	public void onInitializeClient() {
		loadKeyMappings();
		registerCommands();


		id = Identifier.fromNamespaceAndPath("command-hotkeys", "open_menu");
		category = new KeyMapping.Category(id);


		MENU = new KeyMapping("Open menu", GLFW.GLFW_KEY_K, category);

		MENU = KeyMappingHelper.registerKeyMapping(MENU);




		// This entrypoint is suitable for setting up client-specific logic, such as rendering.
		LOGGER.info("passed");
		ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
			if (Minecraft.getInstance().screen != null && !(Minecraft.getInstance().screen instanceof AbstractContainerScreen<?>)) {
				return;
			}
			if(MENU.consumeClick()){
				minecraftClient.setScreen(new ListMenu(Minecraft.getInstance().screen));
			}


			for (int keyCode : INSTANCE.keyToCommand.keySet()) {

				boolean isPressed = false;

				try {
					isPressed = InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), keyCode);
				} catch (Exception e) {
					return;
				}
				boolean wasPressed = INSTANCE.keyStates.getOrDefault(keyCode, false);
				if (isPressed && !wasPressed) {
					if (minecraftClient.player != null && minecraftClient.getConnection() != null && !isTyping(minecraftClient.screen)) {
						minecraftClient.player.connection.sendCommand(INSTANCE.keyToCommand.get(keyCode));

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

	private boolean isTyping(net.minecraft.client.gui.screens.Screen screen){
		if (screen == null) return false;

		// 1. If the focused element is a text field → typing
		if (screen.getFocused() instanceof EditBox) {
			return true;
		}

		// 2. If the screen is a chat screen → typing
		if (screen instanceof ChatScreen) {
			return true;
		}

		// 3. If the screen is a sign/book/anvil editor → typing
		if (screen instanceof AbstractSignEditScreen
				|| screen instanceof AnvilScreen
				|| screen instanceof BookEditScreen) {
			return true;
		}


		return false;



	}


	private void registerCommands() {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
			dispatcher.register(
					ClientCommands.literal("hotcmds")
							.executes(context -> {
								Minecraft client = Minecraft.getInstance();
								if (client != null) {
									client.execute(() -> {

										AtomicBoolean open = new AtomicBoolean(true);
										ClientTickEvents.END_CLIENT_TICK.register(minecraftClient -> {
											if (open.get())
												client.setScreen(new ListMenu(client.screen));
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