package com.hotcmds;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;

import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

@Environment(EnvType.CLIENT)
public class ListMenu extends net.minecraft.client.gui.screens.Screen {
    private net.minecraft.client.gui.screens.Screen parent;
    private CommandList entryList;
    private Button addButton;
    private Button quitButton;

    protected ListMenu(Screen parent) {
        super(Component.literal("Command Keybinds"));
        this.parent = parent;

    }

    @Override
    protected void init(){
        int listWidth  = 300;
        int listHeight = 160;
        int x = (this.width  - listWidth)  / 2;
        int y = (this.height - listHeight) / 2;
        int itemHeight =  25;
        entryList = new CommandList(Minecraft.getInstance(), x, y-40, listWidth, listHeight, itemHeight);
        this.addRenderableWidget(entryList);

        addButton = Button.builder(Component.literal("Add New"), btn -> {
            Minecraft.getInstance().setScreen(new AddNew(this));
        }).bounds(this.width/2, this.height/2+100, 100, 20).build();
        addRenderableWidget(addButton);
        quitButton = Button.builder(Component.literal("Back"), btn -> {
            Minecraft.getInstance().setScreen(parent);
        }).bounds(this.width/2-100,this.height/2+100,100, 20).build();
        addRenderableWidget(quitButton);



    }



    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta){
        super.extractRenderState(context, mouseX, mouseY, delta);


        entryList.extractRenderState(context,mouseX,mouseY,delta);




    }

    public class CommandList extends ObjectSelectionList<CommandList.CommandEntry> {
        public CommandList(Minecraft minecraftClient, int x, int y, int width, int height, int itemHeight) {
            super(Minecraft.getInstance(), width, height, y, itemHeight);
            this.setX(x);

            for(KeyCommandPair dat: HotcmdsClient.INSTANCE.getKeybinds()){

                this.addEntry(new CommandEntry(dat.command, dat.key));




            }


        }



        @Override
        public int getRowWidth() {
            return this.width - 10;
        }






        public class CommandEntry extends ObjectSelectionList.Entry<CommandList.CommandEntry> {
            private final String command;
            private final int key;
            private Button removeButton;



            public CommandEntry(String command, int key) {
                this.command = command;
                this.key = key;
                
                this.removeButton = Button.builder(
                                Component.literal("Remove"),
                                button -> {

                                    HotcmdsClient.INSTANCE.removeKey(key);
                                    entryList.removeEntry(this);


                                })
                        .bounds(0, 0, 60, 20) // Position will be set in render
                        .build();

            }
            @Override
            public void extractContent(GuiGraphicsExtractor context, int i, int j, boolean bl, float f){
                // Get x/width/height from the parent list
                int index = entryList.children().indexOf(this);
                int x = entryList.getRowLeft();
                int y = entryList.getRowTop(index);
                int entryWidth = entryList.getRowWidth();
                int entryHeight = entryList.getRowBottom(index)-y;
                // or entryList.getRowHeight() if available

                // Background fill
                context.fill(x, y+1, x + entryWidth, y + entryHeight,0x80555555);

                // Render the command-key text
                context.text(
                        Minecraft.getInstance().font,
                        Component.literal("Command: " + command + ", Key: " + GLFW.glfwGetKeyName(key, 0)),
                        x + 2, this.getContentYMiddle()-2,
                        0xFFFFFFFF,true
                );

                // Position and render the remove button
                this.removeButton.setX(x + entryWidth - 70);
                this.removeButton.setY(this.getContentYMiddle()-9);
                this.removeButton.extractRenderState(context, i, j, f); // mouse coords not passed anymore

                // Visibility check
                if (removeButton.getY() < entryList.getBottom() - 20 && removeButton.getY() > entryList.getY()) {
                    removeButton.visible = true;
                } else {
                    removeButton.visible = false;
                }
            }


            @Override
            public Component getNarration() {
                return getNarrationMessage();
            }

            @Override
            public boolean mouseClicked(MouseButtonEvent click, boolean doubleclick) {
                return this.removeButton.mouseClicked(click, doubleclick);

            }




        }

    }


}

