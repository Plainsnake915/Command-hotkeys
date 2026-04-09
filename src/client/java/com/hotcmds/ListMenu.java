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
        int itemHeight =  25;
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
            super(minecraftClient, width, height, y, itemHeight);
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
            public void render(DrawContext context, int mousex, int mousey, boolean hovered, float tickDelta) {
                // Get x/width/height from the parent list
                int index = entryList.children().indexOf(this);
                int x = entryList.getRowLeft();
                int y = entryList.getRowTop(index);
                int entryWidth = entryList.getRowWidth();
                int entryHeight = entryList.itemHeight; // or entryList.getRowHeight() if available

                // Background fill
                context.fill(x, y+1, x + entryWidth, y + entryHeight-2,0xFFAAAAAA);

                // Render the command-key text
                context.drawTextWithShadow(
                        getTextRenderer(),
                        Text.literal("Command: " + command + ", Key: " + GLFW.glfwGetKeyName(key, 0)),
                        x + 2, y + 2,
                        0xFFFFFFFF
                );

                // Position and render the remove button
                this.removeButton.setX(x + entryWidth - 70);
                this.removeButton.setY(y + 2);
                this.removeButton.render(context, mousex, mousey, tickDelta); // mouse coords not passed anymore

                // Visibility check
                if (removeButton.getY() < entryList.getBottom() - 20 && removeButton.getY() > entryList.getY()) {
                    removeButton.visible = true;
                } else {
                    removeButton.visible = false;
                }
            }
            @Override
            public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
                return this.removeButton.mouseClicked(click, doubled);

            }


        }

    }


}

