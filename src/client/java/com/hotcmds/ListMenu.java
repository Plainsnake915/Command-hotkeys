package com.hotcmds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.EntryListWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ListMenu extends Screen {
    private Screen parent;
    private CommandEntryListWidget entryList;
    private ButtonWidget addButton;
    private ButtonWidget quitButton;

    protected ListMenu(Screen parent) {
        super(Text.of("Command Keybinds"));
        this.parent = parent;

    }

    @Override
    protected void init(){
        int listWidth  = 300;
        int listHeight = 160;
        int x = (this.width  - listWidth)  / 2;
        int y = (this.height - listHeight) / 2;
        int itemHeight =  30;
        entryList = new CommandEntryListWidget(this.client, x, y-40, listWidth, listHeight, itemHeight);
        this.addSelectableChild(entryList);

        addButton = ButtonWidget.builder(Text.of("Add New"), btn -> {
            client.setScreen(new AddNew(this));
        }).dimensions(this.width/2, this.height/2+100, 100, 20).build();
        addDrawableChild(addButton);
        quitButton = ButtonWidget.builder(Text.of("Back"), btn -> {
            client.setScreen(parent);
        }).dimensions(this.width/2-100,this.height/2+100,100, 20).build();
        addDrawableChild(quitButton);



    }



    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta){
        super.render(context, mouseX, mouseY, delta);


        entryList.render(context,mouseX,mouseY,delta);




    }

    private class CommandEntryListWidget extends EntryListWidget<CommandEntryListWidget.CommandEntry> {
        public CommandEntryListWidget(MinecraftClient minecraftClient, int x, int y, int width, int height, int itemHeight) {
            super(minecraftClient, width, height, y, itemHeight, itemHeight);
            this.setX(x);

            for(KeyCommandPair dat: HotcmdsClient.INSTANCE.getKeybinds()){

                this.addEntry(new CommandEntry(dat.command, dat.key));



            }


        }



        @Override
        public int getRowWidth() {
            return this.width - 10;
        }

        @Override
        protected void appendClickableNarrations(NarrationMessageBuilder builder) {

        }


        private class CommandEntry extends EntryListWidget.Entry<CommandEntry>{
            private final String command;
            private final int key;
            private ButtonWidget removeButton;


            public CommandEntry(String command, int key) {
                this.command = command;
                this.key = key;
                this.removeButton = ButtonWidget.builder(
                                Text.literal("Remove"),
                                button -> {
                                    HotcmdsClient.INSTANCE.removeKey(key);
                                    entryList.removeEntry(this);
                                })
                        .dimensions(0, 0, 60, 20) // Position will be set in render
                        .build();
            }

            @Override
            public void render(DrawContext context, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickProgress) {


                context.fill(x, y, x + entryWidth, y + entryHeight, 0xFF808080);

                // Render the command-key text


                context.drawText(getTextRenderer(), Text.literal("Command: " + command + ", Key: " + GLFW.glfwGetKeyName(key, 0)), x+2, y + 2, 0xFFFFFFFF, true);

                // Position and render the remove button
                this.removeButton.setX(x + entryWidth - 70); // Align to the right
                this.removeButton.setY(y+2);
                this.removeButton.render(context, mouseX, mouseY, tickProgress);
                if(removeButton.getY() < entryList.getBottom()-20  && removeButton.getY() > entryList.getY()) {
                    removeButton.visible = true;
                }else {
                    removeButton.visible=false;
                }


            }
            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {

                return this.removeButton.mouseClicked(mouseX, mouseY, button);
            }


        }

    }


}

