package com.hotcmds;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AlwaysSelectedEntryListWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;


import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.widget.TextFieldWidget;

import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

import java.util.List;
@Environment(EnvType.CLIENT)
public class AddNew extends Screen {
    private final Screen parent;
    private TextFieldWidget commandField;
    private ButtonWidget setKeyButton;
    private ButtonWidget saveButton;
    private ButtonWidget quitButton;
    private boolean waitforkey = false;
    private int keybinding = GLFW.GLFW_KEY_A;


    public AddNew(Screen parent) {
        super(Text.literal("Add new"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Command input field
        commandField = new TextFieldWidget(this.textRenderer, centerX - 100, centerY - 50, 200, 20, Text.of("Command"));
        commandField.setMaxLength(100);
        commandField.setPlaceholder(Text.of(""));
        this.addDrawableChild(commandField);
        // Key button
        setKeyButton = ButtonWidget.builder(Text.of("SET"), btn -> {
            waitforkey = true;
            btn.setMessage(Text.of("Press key..."));
        }).dimensions(centerX - 100, centerY - 20, 100, 20).build();
        this.addDrawableChild(setKeyButton);
        saveButton = ButtonWidget.builder(Text.of("Save and Close"), btn -> {
            String command = commandField.getText();
            HotcmdsClient.INSTANCE.addKeyMapping(keybinding, command);
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(centerX, centerY + 100, 100, 20).build();
        this.addDrawableChild(saveButton);
        quitButton = ButtonWidget.builder(Text.of("Quit"), btn -> {
            MinecraftClient.getInstance().setScreen(parent);
        }).dimensions(centerX - 100, centerY + 100, 100, 20).build();
        this.addDrawableChild(quitButton);

    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawText(this.textRenderer, "/", commandField.getX() - 10, commandField.getY() + 6, 0xFFFFFFFF, true);
        context.drawText(textRenderer, "key: " + GLFW.glfwGetKeyName(keybinding, 0), setKeyButton.getX() + 120, setKeyButton.getY() + 7, 0xFFFFFFFF, true);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (waitforkey) {
            keybinding = keyCode;
            setKeyButton.setMessage(Text.of(GLFW.glfwGetKeyName(keyCode, 0)));
            waitforkey = false;
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

}