package com.hotcmds;






import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;


import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;
@Environment(EnvType.CLIENT)
public class AddNew extends Screen {
    private final Screen parent;
    private EditBox commandField;
    private Button setKeyButton;
    private Button saveButton;
    private Button quitButton;
    private boolean waitforkey = false;
    private int keybinding = GLFW.GLFW_KEY_A;


    public AddNew(Screen parent) {
        super(Component.literal("Add new"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Command input field
        commandField = new EditBox(this.font, centerX - 100, centerY - 50, 200, 20, Component.literal("Command"));
        commandField.setMaxLength(100);
        commandField.setValue("null");
        
        this.addRenderableWidget(commandField);
        // Key button
        setKeyButton = Button.builder(Component.literal("SET"), btn -> {
            waitforkey = true;
            btn.setMessage(Component.literal("Press key..."));
        }).bounds(centerX - 100, centerY - 20, 100, 20).build();
        this.addRenderableWidget(setKeyButton);
        saveButton = Button.builder(Component.literal("Save and Close"), btn -> {
            String command = commandField.getValue();
            HotcmdsClient.INSTANCE.addKeyMapping(keybinding, command);
            Minecraft.getInstance().setScreen(parent);
        }).bounds(centerX, centerY + 100, 100, 20).build();
        this.addRenderableWidget(saveButton);
        quitButton = Button.builder(Component.literal("Back"), btn -> {
            Minecraft.getInstance().setScreen(parent);
        }).bounds(centerX - 100, centerY + 100, 100, 20).build();
        this.addRenderableWidget(quitButton);

    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);


        context.text(this.font, "/", commandField.getX() - 10, commandField.getY() + 6, 0xFFFFFFFF, true);
        context.text(font, "key: " + GLFW.glfwGetKeyName(keybinding, 0), setKeyButton.getX() + 120, setKeyButton.getY() + 7, 0xFFFFFFFF, true);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {

        if (waitforkey) {
            keybinding = event.key();
            setKeyButton.setMessage(Component.literal(GLFW.glfwGetKeyName(event.key(), 0)));
            waitforkey = false;
            return true;
        }
        return super.keyPressed(event);
    }

}