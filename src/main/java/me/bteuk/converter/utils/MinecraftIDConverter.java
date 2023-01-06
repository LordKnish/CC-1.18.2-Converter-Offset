package me.bteuk.converter.utils;

import com.flowpowered.nbt.CompoundMap;
import com.flowpowered.nbt.CompoundTag;
import com.flowpowered.nbt.StringTag;

public class MinecraftIDConverter {

    /*

    This is the to-do list of unique cases which could otherwise be forgotten:

    TODO: For double plant blocks we need to store the details of the bottom-half somewhere so when we get to a top-half we can query that list to figure out which top-half is needed,
     since in 1.12.2 they all have the same id 175:11.

    TODO: Do the same for stairs, fences and walls, iron bars, glass panes, chests, redstone dust, chorus plant

    TODO: Change colour of the bed in post-processing, this can be retrieved from the block entity data.

    TODO: Change colour of banners in post-processing and add the patterns.

    TODO: Check if melon/pumpkin stem is attached to a melon/pumpkin in post-processing.

    TODO: Convert flower pots to correct type in post-processing by using block entity data.

    TODO: Convert mob heads in post-processing since the block entity data needs to be used.

    TODO: Convert Note blocks in post-processing since the block entity data needs to be used.

     */




    //Convert a legacy Minecraft block id and get the 1.18.2 palette for the block.
    public static CompoundTag getBlock(LegacyID id) {

        //Create map for block.
        CompoundMap block = new CompoundMap();

        //Add block name as String tag.
        block.put(new StringTag("Name", getNameSpace(id.getID(), id.getData())));

        //If block has properties, add them.
        if (hasBlockStates(id.getID())) {
            block.put(getBlockStates(id.getID(), id.getData()));
        }


        return new CompoundTag("", block);
    }


    //Convert a legacy Minecraft block id and get the 1.18.2 namespace version.
    public static String getNameSpace(byte id, byte data) {
        return ("minecraft:" + getBlockName(id, data));
    }

    //Check if the block has block states in 1.18.2, in this case it needs a properties tag in the palette.
    public static boolean hasBlockStates(byte id) {

        switch (id) {

            case 2, 3, 6, 23, 46, 51, 54, 61, 62, 64, 65, 71, 77, 81, 84, 85, 91, 92, 101, 102, 110, 113, 117, 118, 120, 127,
                    (byte) 130, (byte) 137, (byte) 141, (byte) 143, (byte) 145, (byte) 146, (byte) 151, (byte) 154,
                    (byte) 158, (byte) 160, (byte) 170, (byte) 176, (byte) 177, (byte) 178, (byte) 188, (byte) 189,
                    (byte) 190, (byte) 191, (byte) 192, (byte) 193, (byte) 194, (byte) 195, (byte) 196, (byte) 197,
                    (byte) 198, (byte) 199, (byte) 200, (byte) 207,(byte) 210, (byte) 211, (byte) 212, (byte) 216,
                    (byte) 235, (byte) 236, (byte) 237, (byte) 238, (byte) 239, (byte) 240, (byte) 241, (byte) 242,
                    (byte) 243, (byte) 244, (byte) 245, (byte) 246, (byte) 247, (byte) 248, (byte) 249, (byte) 250,
                    70, 72, (byte) 147, (byte) 148 -> {return true;}

        }

        return false;

    }

    //Check if the block is a block entity (formally known as tile entity)
    public static boolean isBlockEntity(byte id) {

        switch (id) {

            case 23, 26, 29, 33, 34, 52, 54, 61, 62, 63, 64, 84, 116, 117, 119, (byte) 130, (byte) 137, (byte) 138,
                    (byte) 144, (byte) 146, (byte) 149, (byte) 150, (byte) 151, (byte) 154, (byte) 158, (byte) 176,
                    (byte) 177, (byte) 178, (byte) 209, (byte) 210, (byte) 211, (byte) 219, (byte) 220, (byte) 221,
                    (byte) 222, (byte) 223, (byte) 224, (byte) 225, (byte) 226, (byte) 227, (byte) 228, (byte) 229,
                    (byte) 230, (byte) 231, (byte) 232, (byte) 233, (byte) 234, (byte) 255 -> {return true;}

        }

        return false;

    }

    //Get the block states of a block.
    public static CompoundTag getBlockStates(byte id, byte data) {

        CompoundMap block_states = new CompoundMap();

        switch (id) {

            //Anvil (all types)
            case (byte) 145 -> {

                switch (data) {
                    case 0,4,8 -> block_states.put(new StringTag("facing", "south"));
                    case 1,5,9 -> block_states.put(new StringTag("facing", "west"));
                    case 2,6,10 -> block_states.put(new StringTag("facing", "north"));
                    case 3,7,11 -> block_states.put(new StringTag("facing", "east"));
                }
            }

            //Standing Banner
            case (byte) 176 -> block_states.put(new StringTag("rotation", String.valueOf(data)));

            //Wall Banner and Mob Heads
            case (byte) 177, (byte) 144 -> {

                switch (data) {

                    case 2 -> block_states.put(new StringTag("facing", "north"));
                    case 3 -> block_states.put(new StringTag("facing", "south"));
                    case 4 -> block_states.put(new StringTag("facing", "west"));
                    case 5 -> block_states.put(new StringTag("facing", "east"));

                }
            }

            //Bed (all colours)
            case 26 -> {

                //part
                switch (data) {

                    case 0,1,2,3,4,5,6,7 -> block_states.put(new StringTag("part", "foot"));
                    case 8,9,10,11,12,13,14,15 -> block_states.put(new StringTag("part", "head"));

                }

                //facing
                switch (data) {

                    case 0,4,8,12 -> block_states.put(new StringTag("facing", "south"));
                    case 1,5,9,13 -> block_states.put(new StringTag("facing", "west"));
                    case 2,6,10,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,7,11,15 -> block_states.put(new StringTag("facing", "east"));

                }

                //occupied
                block_states.put(new StringTag("occupied", "false"));

            }

            //Beetroots
            case (byte) 207 -> block_states.put(new StringTag("age", String.valueOf(data)));

            //Bone Block
            case (byte) 216 -> {

                switch (data) {

                    case 0,1,2,3,12,13,14,15 -> block_states.put(new StringTag("axis", "y"));
                    case 4,5,6,7 -> block_states.put(new StringTag("axis", "x"));
                    case 8,9,10,11 -> block_states.put(new StringTag("axis", "z"));

                }
            }

            //Brewing Stand
            case 117 -> {

                //Has Bottle 0
                switch (data) {

                    case 0,2,4,6,8,10,12,14 -> block_states.put(new StringTag("has_bottle_0", "false"));
                    case 1,3,5,7,9,11,13,15 -> block_states.put(new StringTag("has_bottle_0", "true"));

                }

                //Has Bottle 1
                switch (data) {

                    case 0,1,4,5,8,9,12,13 -> block_states.put(new StringTag("has_bottle_1", "false"));
                    case 2,3,6,7,10,11,14,15 -> block_states.put(new StringTag("has_bottle_1", "true"));

                }

                //Has Bottle 2
                switch (data) {

                    case 0,1,2,3,8,9,10,11 -> block_states.put(new StringTag("has_bottle_2", "false"));
                    case 4,5,6,7,12,13,14,15 -> block_states.put(new StringTag("has_bottle_2", "true"));

                }
            }

            //Stone/Wooden Button
            case 77, (byte) 143 -> {

                //face
                switch (data) {

                    case 0,8 -> block_states.put(new StringTag("face", "ceiling"));
                    case 1,2,3,4,9,10,11,12 -> block_states.put(new StringTag("face", "wall"));
                    case 5,6,7,13,14,15 -> block_states.put(new StringTag("face", "floor"));

                }

                //facing
                switch (data) {

                    case 0,4,5,6,7,8,12,13,14,15 -> block_states.put(new StringTag("facing", "north"));
                    case 1,9 -> block_states.put(new StringTag("facing", "east"));
                    case 2,10 -> block_states.put(new StringTag("facing", "west"));
                    case 3,11 -> block_states.put(new StringTag("facing", "south"));

                }

                //powered
                block_states.put(new StringTag("powered", "false"));

            }

            //Cactus
            case 81 -> block_states.put(new StringTag("age", "0"));

            //Cake
            case 92 -> {
                block_states.put(new StringTag("bites", String.valueOf(data)));
                block_states.put(new StringTag("lit", "false"));
            }

            //Carrots
            case (byte) 141 -> block_states.put(new StringTag("age", String.valueOf(data)));

            //Cauldron
            case 118 -> block_states.put(new StringTag("level", String.valueOf(data)));

            //(Trapped) Chest
            case 54, (byte) 146 -> {

                //facing
                switch (data) {

                    case 0,1,2,6,7,8,12,13,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,9,15 -> block_states.put(new StringTag("facing", "south"));
                    case 4,10 -> block_states.put(new StringTag("facing", "west"));
                    case 5,11 -> block_states.put(new StringTag("facing", "east"));

                }

                //type
                block_states.put(new StringTag("type", "single"));

                //waterlogged
                block_states.put(new StringTag("waterlogged", "false"));

            }

            //Ender Chest
            case (byte) 130 -> {

                //facing
                switch (data) {

                    case 0,1,2,6,7,8,12,13,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,9,15 -> block_states.put(new StringTag("facing", "south"));
                    case 4,10 -> block_states.put(new StringTag("facing", "west"));
                    case 5,11 -> block_states.put(new StringTag("facing", "east"));

                }

                //waterlogged
                block_states.put(new StringTag("waterlogged", "false"));

            }

            //Chorus Flower
            case (byte) 200 -> block_states.put(new StringTag("age", String.valueOf(data)));

            //Chorus Plant
            case (byte) 199 -> {

                //down
                block_states.put(new StringTag("down", "false"));

                //east
                block_states.put(new StringTag("east", "false"));

                //north
                block_states.put(new StringTag("north", "false"));

                //south
                block_states.put(new StringTag("south", "false"));

                //up
                block_states.put(new StringTag("up", "false"));

                //west
                block_states.put(new StringTag("west", "false"));

            }

            //Cocoa
            case 127 -> {

                //age
                switch (data) {

                    case 0,1,2,3 -> block_states.put(new StringTag("age", "0"));
                    case 4,5,6,7 -> block_states.put(new StringTag("age", "1"));
                    case 8,9,10,11 -> block_states.put(new StringTag("age", "2"));

                }

                //facing
                switch (data) {

                    case 0,4,8 -> block_states.put(new StringTag("facing", "south"));
                    case 1,5,9 -> block_states.put(new StringTag("facing", "west"));
                    case 2,6,10 -> block_states.put(new StringTag("facing", "north"));
                    case 3,7,11 -> block_states.put(new StringTag("facing", "east"));

                }
            }

            //Command Block
            case (byte) 137, (byte) 210, (byte) 211 -> {

                //conditional
                switch (data) {

                    case 0,1,2,3,4,5,6,7 -> block_states.put(new StringTag("conditional", "false"));
                    case 8,9,10,11,12,13,14,15 -> block_states.put(new StringTag("conditional", "true"));

                }

                //facing
                switch (data) {

                    case 0,6,8,14 -> block_states.put(new StringTag("facing", "down"));
                    case 1,7,9,15 -> block_states.put(new StringTag("facing", "up"));
                    case 2,10 -> block_states.put(new StringTag("facing", "north"));
                    case 3,11 -> block_states.put(new StringTag("facing", "south"));
                    case 4,12 -> block_states.put(new StringTag("facing", "west"));
                    case 5,13 -> block_states.put(new StringTag("facing", "earth"));

                }
            }

            //Daylight Sensor
            case (byte) 151 -> {

                //inverted
                block_states.put(new StringTag("inverted", "false"));

                //power
                block_states.put(new StringTag("power", "0"));


            }

            //Inverted Daylight Sensor
            case (byte) 178 -> {

                //inverted
                block_states.put(new StringTag("inverted", "true"));

                //power
                block_states.put(new StringTag("power", "0"));

            }

            //Dispenser and Dropper
            case 23, (byte) 158 -> {

                //facing
                switch (data) {

                    case 0,6,8,14 -> block_states.put(new StringTag("facing", "down"));
                    case 1,7,9,15 -> block_states.put(new StringTag("facing", "up"));
                    case 2,10 -> block_states.put(new StringTag("facing", "north"));
                    case 3,11 -> block_states.put(new StringTag("facing", "south"));
                    case 4,12 -> block_states.put(new StringTag("facing", "west"));
                    case 5,13 -> block_states.put(new StringTag("facing", "east"));

                }

                //triggered
                block_states.put(new StringTag("triggered", "false"));

            }

            //Doors
            case 64, 71, (byte) 193, (byte) 194, (byte) 195, (byte) 196, (byte) 197 -> {

                //facing
                switch (data) {

                    case 0,4,8,12 -> block_states.put(new StringTag("facing", "east"));
                    case 1,5,9,13 -> block_states.put(new StringTag("facing", "south"));
                    case 2,6,10,14 -> block_states.put(new StringTag("facing", "west"));
                    case 3,7,11,15 -> block_states.put(new StringTag("facing", "north"));

                }

                //half
                switch (data) {

                    case 0,1,2,3,4,5,6,7 -> block_states.put(new StringTag("half", "lower"));
                    case 8,9,10,11,12,13,14,15 -> block_states.put(new StringTag("half", "upper"));

                }

                //hinge
                switch (data) {

                    case 9,11,13,15 -> block_states.put(new StringTag("hinge", "right"));
                    default -> block_states.put(new StringTag("hinge", "left"));

                }

                //open
                switch (data) {

                    case 4,5,6,7 -> block_states.put(new StringTag("open", "true"));
                    default -> block_states.put(new StringTag("open", "false"));

                }

                //powered
                block_states.put(new StringTag("powered", "false"));

            }

            //End Portal Frame
            case 120 -> {

                //eye
                switch (data) {

                    case 4,5,6,7,12,13,14,15 -> block_states.put(new StringTag("eye", "true"));
                    default -> block_states.put(new StringTag("eye", "false"));

                }

                //facing
                switch (data) {

                    case 0,4,8,12 -> block_states.put(new StringTag("facing", "south"));
                    case 1,5,9,13 -> block_states.put(new StringTag("facing", "west"));
                    case 2,6,10,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,7,11,15 -> block_states.put(new StringTag("facing", "east"));

                }
            }

            //End Rod
            case (byte) 198 -> {

                //facing
                switch (data) {

                    case 0,6,12 -> block_states.put(new StringTag("facing", "down"));
                    case 1,7,13 -> block_states.put(new StringTag("facing", "up"));
                    case 2,8,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,9,15 -> block_states.put(new StringTag("facing", "south"));
                    case 4,10 -> block_states.put(new StringTag("facing", "west"));
                    case 5,11 -> block_states.put(new StringTag("facing", "east"));

                }
            }

            //Farmland
            case 60 -> {

                switch (data) {

                    //moisture
                    case 0, 8 -> block_states.put(new StringTag("moisture", "0"));
                    case 1, 9 -> block_states.put(new StringTag("moisture", "1"));
                    case 2, 10 -> block_states.put(new StringTag("moisture", "2"));
                    case 3, 11 -> block_states.put(new StringTag("moisture", "3"));
                    case 4, 12 -> block_states.put(new StringTag("moisture", "4"));
                    case 5, 13 -> block_states.put(new StringTag("moisture", "5"));
                    case 6, 14 -> block_states.put(new StringTag("moisture", "6"));
                    case 7, 15 -> block_states.put(new StringTag("moisture", "7"));

                }
            }

            //Fences
            case 85, 113, (byte) 188, (byte) 189, (byte) 190, (byte) 191, (byte) 192 -> {

                block_states.put(new StringTag("east", "false"));
                block_states.put(new StringTag("north", "false"));
                block_states.put(new StringTag("south", "false"));
                block_states.put(new StringTag("waterlogged", "false"));
                block_states.put(new StringTag("west", "false"));

            }

            //Fence Gates
            case 107, (byte) 183, (byte) 184, (byte) 185, (byte) 186, (byte) 187 -> {

                //facing
                switch (data) {

                    case 0,4,8,12 -> block_states.put(new StringTag("facing", "south"));
                    case 1,5,9,13 -> block_states.put(new StringTag("facing", "west"));
                    case 2,6,10,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,7,11,15 -> block_states.put(new StringTag("facing", "east"));

                }

                //in_wall
                block_states.put(new StringTag("in_wall", "false"));

                //open
                switch (data) {

                    case 4,5,6,7,12,13,14,15 -> block_states.put(new StringTag("open", "true"));
                    default -> block_states.put(new StringTag("open", "false"));

                }

                //powered
                block_states.put(new StringTag("powered", "false"));

            }

            //Fire
            case 51 -> {

                block_states.put(new StringTag("age", "0"));
                block_states.put(new StringTag("east", "false"));
                block_states.put(new StringTag("north", "false"));
                block_states.put(new StringTag("south", "false"));
                block_states.put(new StringTag("up", "false"));
                block_states.put(new StringTag("west", "false"));

            }

            //Double Plant
            case (byte) 175 -> {

                if (data == 11) {
                    block_states.put(new StringTag("half", "upper"));
                } else {
                    block_states.put(new StringTag("half", "lower"));
                }

            }

            //Frosted Ice
            case (byte) 212 -> {

                if (data < 3) {
                    block_states.put(new StringTag("age", String.valueOf(data)));
                } else {
                    block_states.put(new StringTag("age", "3"));
                }

            }

            //Furnace
            case 61,62 -> {

                //lit
                if (id == 62) {
                    block_states.put(new StringTag("lit", "true"));
                } else {
                    block_states.put(new StringTag("lit", "false"));
                }

                //facing
                switch (data) {

                    case 3,9,15 -> block_states.put(new StringTag("facing", "south"));
                    case 4,10 -> block_states.put(new StringTag("facing", "west"));
                    case 5,11 -> block_states.put(new StringTag("facing", "east"));
                    default -> block_states.put(new StringTag("facing", "north"));

                }
            }

            //Glass Panes and Iron Bars
            case 102, (byte) 160, 101 -> {

                block_states.put(new StringTag("east", "false"));
                block_states.put(new StringTag("north", "false"));
                block_states.put(new StringTag("south", "false"));
                block_states.put(new StringTag("watterlogged", "false"));
                block_states.put(new StringTag("west", "false"));

            }

            //Glazed Terracotta and Jack o'Lantern
            case (byte) 235, (byte) 236, (byte) 237, (byte) 238, (byte) 239, (byte) 240, (byte) 241, (byte) 242,
                    (byte) 243, (byte) 244, (byte) 245, (byte) 246, (byte) 247, (byte) 248, (byte) 249,
                    (byte) 250, 91 -> {

                switch (data) {

                    case 0,4,8,12 -> block_states.put(new StringTag("facing", "south"));
                    case 1,5,9,13 -> block_states.put(new StringTag("facing", "west"));
                    case 2,6,10,14 -> block_states.put(new StringTag("facing", "north"));
                    case 3,7,11,15 -> block_states.put(new StringTag("facing", "east"));

                }
            }

            //Grass, Myceleum and Podzol
            case 2, 110, 3 -> {

                if (!(id == 3 && data != 2)) {

                    block_states.put(new StringTag("snowy", "false"));

                }
            }

            //Hay Bale
            case (byte) 170 -> {

                switch (data) {

                    case 4,5,6,7 -> block_states.put(new StringTag("axis", "x"));
                    case 8,9,10,11 -> block_states.put(new StringTag("axis", "z"));
                    default -> block_states.put(new StringTag("axis", "y"));

                }
            }

            //Hopper
            case (byte) 154 -> {

                //enabled
                block_states.put(new StringTag("enabled", "false"));

                //facing
                switch (data) {

                    case 0,6,8,14,15 -> block_states.put(new StringTag("facing", "down"));
                    case 2,10 -> block_states.put(new StringTag("facing", "north"));
                    case 3,11 -> block_states.put(new StringTag("facing", "south"));
                    case 4,12 -> block_states.put(new StringTag("facing", "west"));
                    case 5,13 -> block_states.put(new StringTag("facing", "east"));

                }
            }

            //Jukebox
            case 84 -> block_states.put(new StringTag("has_record", "false"));

            //Ladder
            case 65 -> {

                switch (data) {

                    case 3, 9, 15 -> block_states.put(new StringTag("facing", "south"));
                    case 4, 10 -> block_states.put(new StringTag("facing", "west"));
                    case 5, 11 -> block_states.put(new StringTag("facing", "east"));
                    default -> block_states.put(new StringTag("facing", "north"));

                }
            }

            //Lava
            case 10, 11 -> block_states.put(new StringTag("level", String.valueOf(data)));

            //Leaves
            case 18, (byte) 161 -> {

                block_states.put(new StringTag("distance", "7"));
                block_states.put(new StringTag("persistent", "true"));
                block_states.put(new StringTag("waterlogged", "false"));

            }

            //Lever
            case 69 -> {

                //face
                switch (data) {

                    case 0,7,8,15 -> block_states.put(new StringTag("face", "floor"));
                    case 5,6,13,14 -> block_states.put(new StringTag("face", "ceiling"));
                    case 1,2,3,4,9,10,11,12 -> block_states.put(new StringTag("face", "wall"));

                }

                //facing
                switch (data) {

                    case 0,1,6,8,9,14 -> block_states.put(new StringTag("facing", "east"));
                    case 3,5,7,11,13,15 -> block_states.put(new StringTag("facing", "south"));
                    case 2,10 -> block_states.put(new StringTag("facing", "west"));
                    case 4,12 -> block_states.put(new StringTag("facing", "north"));

                }

                //powered
                if (data >= 8) {
                    block_states.put(new StringTag("powered", "true"));
                } else {
                    block_states.put(new StringTag("powered", "false"));
                }

            }

            //Logs
            case 17, (byte) 162 -> {
                switch (data) {

                    case 5,6,7,8 -> block_states.put(new StringTag("axis", "x"));
                    case 9,10,11,12 -> block_states.put(new StringTag("axis", "z"));
                    default -> block_states.put(new StringTag("axis", "y"));

                }
            }

            //Melon Stem
            case 104, 105 -> block_states.put(new StringTag("age", String.valueOf(data)));

            //Mushroom Blocks
            case 99, 100 -> {

                //east
                if (data == 3 || data == 6 || data == 9 || data == 10 || data == 14 || data == 15) {
                    block_states.put(new StringTag("east", "true"));
                } else {
                    block_states.put(new StringTag("east", "false"));
                }

                //down
                if (data == 14 || data == 15) {
                    block_states.put(new StringTag("down", "true"));
                } else {
                    block_states.put(new StringTag("down", "false"));
                }

                //north
                if (data == 1 || data == 2 || data == 3 || data == 10 || data == 14 || data == 15) {
                    block_states.put(new StringTag("north", "true"));
                } else {
                    block_states.put(new StringTag("north", "false"));
                }

                //south
                if (data == 7 || data == 8 || data == 9 || data == 10 || data == 14 || data == 15) {
                    block_states.put(new StringTag("south", "true"));
                } else {
                    block_states.put(new StringTag("south", "false"));
                }

                //up
                if (data == 0 || data == 10) {
                    block_states.put(new StringTag("east", "false"));
                } else {
                    block_states.put(new StringTag("east", "true"));
                }

                //west
                if (data == 1 || data == 4 || data == 7 || data == 10 || data == 14 || data == 15) {
                    block_states.put(new StringTag("east", "true"));
                } else {
                    block_states.put(new StringTag("east", "false"));
                }
            }

            //Nether Wart
            case 115 -> block_states.put(new StringTag("age", String.valueOf(data)));

            //Nether Portal
            case 90 -> {
                if (data == 1) {
                    block_states.put(new StringTag("axis", "z"));
                } else {
                    block_states.put(new StringTag("axis", "x"));
                }
            }

            //Note Block
            case 25 -> {
                block_states.put(new StringTag("instrument", "harp"));
                block_states.put(new StringTag("note", "0"));
                block_states.put(new StringTag("powered", "false"));
            }

            //Observer

            //Pistons

            //Potatoes

            //Pressure Plates
            case 70, 72, (byte) 147, (byte) 148 -> block_states.put(new StringTag("powered", "false"));

            //Carved Pumpkin

            //Pumpkin Stem

            //Purpur and Quartz Pillar

            //Rail

            //Activator Rail, Detector Rail and Powered Rail

            //Redstone Comparator

            //Redstone Dust

            //Redstone Lamp

            //Redstone Ore

            //Redstone Repeater

            //Redstone Torch

            //Saplings
            case 6 -> block_states.put(new StringTag("stage", "0"));

            //Shulker Boxes

            //Sign

            //Slabs

            //Snow

            //Sponge

            //Stairs

            //Structure Block

            //Sugar Cane

            //TNT
            case 46 -> block_states.put(new StringTag("unstable", "false"));

            //Torch

            //Trapdoors

            //Tripwire

            //Tripwire Hook

            //Vines

            //Walls

            //Water
            case 8, 9 -> block_states.put(new StringTag("level", String.valueOf(data)));

            //Wheat Crop

            //Wood





        }


        return new CompoundTag("Properties", block_states);
    }

    //Get the 1.18.2 block name.
    public static String getBlockName(byte id, byte data) {

        switch (id) {

            //Air
            case 0 -> {
                return "air";
            }

            //Stone
            case 1 -> {
                switch (data) {

                    case 0 -> {
                        return "stone";
                    }

                    case 1 -> {
                        return "granite";
                    }

                    case 2 -> {
                        return "polished_granite";
                    }

                    case 3 -> {
                        return "diorite";
                    }

                    case 4 -> {
                        return "polished_diorite";
                    }

                    case 5 -> {
                        return "andesite";
                    }

                    case 6 -> {
                        return "polished_andesite";
                    }
                }
            }

            //Grass
            case 2 -> {
                return "grass_block";
            }

            //Dirt
            case 3 -> {
                switch (data) {

                    case 0 -> {
                        return "dirt";
                    }

                    case 1 -> {
                        return "coarse_dirt";
                    }

                    case 2 -> {
                        return "podzol";
                    }
                }
            }

            //Cobblestone
            case 4 -> {
                return "cobblestone";
            }

            //Planks
            case 5 -> {
                switch (data) {

                    case 0 -> {
                        return "oak_planks";
                    }

                    case 1 -> {
                        return "spruce_planks";
                    }

                    case 2 -> {
                        return "birch_planks";
                    }

                    case 3 -> {
                        return "jungle_planks";
                    }

                    case 4 -> {
                        return "acacia_planks";
                    }

                    case 5 -> {
                        return "dark_oak_planks";
                    }
                }
            }

            //Sapling
            case 6 -> {
                switch (data) {

                    case 1 -> {
                        return "spruce_sapling";
                    }

                    case 2 -> {
                        return "birch_sapling";
                    }

                    case 3 -> {
                        return "jungle_sapling";
                    }

                    case 4 -> {
                        return "acacia_sapling";
                    }

                    case 5 -> {
                        return "dark_oak_sapling";
                    }

                    default -> {
                        return "oak_sapling";
                    }
                }
            }

            //Bedrock
            case 7 -> {
                return "bedrock";
            }

            //Water
            case 8, 9 -> {
                return "water";
            }

            //Lava
            case 10, 11 -> {
                return "lava";
            }

            //Sand
            case 12 -> {
                switch (data) {

                    case 0 -> {
                        return "sand";
                    }

                    case 1 -> {
                        return "red_sand";
                    }
                }
            }

            //Gravel
            case 13 -> {
                return "gravel";
            }

            //Gold Ore
            case 14 -> {
                return "gold_ore";
            }

            //Iron Ore
            case 15 -> {
                return "iron_ore";
            }

            //Coal Ore
            case 16 -> {
                return "coal_ore";
            }

            //Log
            case 17 -> {
                switch (data) {

                    case 0,4,8 -> {
                        return "oak_log";
                    }

                    case 1,5,9 -> {
                        return "spruce_log";
                    }

                    case 2,6,10 -> {
                        return "birch_log";
                    }

                    case 3,7,11 -> {
                        return "jungle_log";
                    }

                    //Wood 6 sided
                    case 12 -> {
                        return "oak_wood";
                    }

                    case 13 -> {
                        return "spruce_wood";

                    }

                    case 14 -> {
                        return "birch_wood";
                    }

                    case 15 -> {
                        return "jungle_wood";
                    }
                }
            }

            //Leaves
            case 18 -> {
                switch (data) {

                    case 0,4,8,12 -> {
                        return "oak_leaves";
                    }

                    case 1,5,9,13 -> {
                        return "spruce_leaves";
                    }

                    case 2,6,10,14 -> {
                        return "birch_leaves";
                    }

                    case 3,7,11,15 -> {
                        return "jungle_leaves";
                    }
                }
            }

            //Sponge
            case 19 -> {
                switch (data) {

                    case 0 -> {
                        return "sponge";
                    }

                    case 1 -> {
                        return "wet_sponge";
                    }
                }
            }

            //Glass
            case 20 -> {
                return "glass";
            }

            //Lapis Ore
            case 21 -> {
                return "lapis_ore";
            }

            //Lapis Block
            case 22 -> {
                return "lapis_block";
            }

            //Dispenser
            case 23 -> {
                return "dispenser";
            }

            //Sandstone
            case 24 -> {
                switch (data) {

                    case 0 -> {
                        return "sandstone";
                    }

                    case 1 -> {
                        return "chiseled_sandstone";
                    }

                    case 2 -> {
                        return "cut_sandstone";
                    }
                }
            }

            //Noteblock
            case 25 -> {
                return "note_block";
            }

            //Colour of the bed is stored as a block entity, this will be fixed in post-processing.
            //Bed
            case 26 -> {
                return "red_bed";
            }

            //Golden Rail
            case 27 -> {
                return "powered_rail";
            }

            //Detector Rail
            case 28 -> {
                return "detector_rail";
            }

            //Sticky Piston
            case 29 -> {
                return "sticky_piston";
            }

            //Cobweb
            case 30 -> {
                return "cobweb";
            }

            //Tallgrass
            case 31 -> {
                switch (data) {

                    case 0 -> {
                        return "dead_bush";
                    }

                    case 1 -> {
                        return "grass";
                    }

                    case 2 -> {
                        return "fern";
                    }
                }
            }

            //Dead Bush
            case 32 -> {
                return "dead_bush";
            }

            //Piston
            case 33 -> {
                return "piston";
            }

            //Piston Head
            case 34 -> {
                return "piston_head";
            }

            //Wool
            case 35 -> {
                switch (data) {

                    case 0 -> {
                        return "white_wool";
                    }

                    case 1 -> {
                        return "orange_wool";
                    }

                    case 2 -> {
                        return "magenta_wool";
                    }

                    case 3 -> {
                        return "light_blue_wool";
                    }

                    case 4 -> {
                        return "yellow_wool";
                    }

                    case 5 -> {
                        return "lime_wool";
                    }

                    case 6 -> {
                        return "pink_wool";
                    }

                    case 7 -> {
                        return "gray_wool";
                    }

                    case 8 -> {
                        return "light_gray_wool";
                    }

                    case 9 -> {
                        return "cyan_wool";
                    }

                    case 10 -> {
                        return "purple_wool";
                    }

                    case 11 -> {
                        return "blue_wool";
                    }

                    case 12 -> {
                        return "brown_wool";
                    }

                    case 13 -> {
                        return "green_wool";
                    }

                    case 14 -> {
                        return "red_wool";
                    }

                    case 15 -> {
                        return "black_wool";
                    }
                }
            }

            //Yellow Flower
            case 37 -> {
                return "dandelion";
            }

            //Red Flower
            case 38 -> {
                switch (data) {

                    case 0 -> {
                        return "poppy";
                    }

                    case 1 -> {
                        return "blue_orchid";
                    }

                    case 2 -> {
                        return "allium";
                    }

                    case 3 -> {
                        return "azure_bluet";
                    }

                    case 4 -> {
                        return "red_tulip";
                    }

                    case 5 -> {
                        return "orange_tulip";
                    }

                    case 6 -> {
                        return "white_tulip";
                    }

                    case 7 -> {
                        return "pink_tulip";
                    }

                    case 8 -> {
                        return "oxeye_daisy";
                    }
                }
            }

            //Brown Mushroom
            case 39 -> {
                return "brown_mushroom";
            }

            //Red Mushroom
            case 40 -> {
                return "red_mushroom";
            }

            //Gold Block
            case 41 -> {
                return "gold_block";
            }

            //Iron Block
            case 42 -> {
                return "iron_block";
            }

            //Double Stone Slab
            case 43 -> {
                switch (data) {

                    case 0 -> {
                        return "smooth_stone_slab";
                    }

                    case 1 -> {
                        return "sandstone_slab";
                    }

                    case 2 -> {
                        return "oak_slab";
                    }

                    case 3 -> {
                        return "cobblestone_slab";
                    }

                    case 4 -> {
                        return "brick_slab";
                    }

                    case 5 -> {
                        return "stone_brick_slab";
                    }

                    case 6 -> {
                        return "nether_brick_slab";
                    }

                    case 7 -> {
                        return "quartz_slab";
                    }

                    case 8 -> {
                        return "smooth_stone";
                    }

                    case 9 -> {
                        return "smooth_sandstone";
                    }
                }
            }

            //Stone Slab
            case 44 -> {
                switch (data) {

                    case 0,8 -> {
                        return "smooth_stone_slab";
                    }

                    case 1,9 -> {
                        return "sandstone_slab";
                    }

                    case 2,10 -> {
                        return "oak_slab";
                    }

                    case 3,11 -> {
                        return "cobblestone_slab";
                    }

                    case 4,12 -> {
                        return "brick_slab";
                    }

                    case 5,13 -> {
                        return "stone_brick_slab";
                    }

                    case 6,14 -> {
                        return "nether_brick_slab";
                    }

                    case 7,15 -> {
                        return "quartz_slab";
                    }
                }
            }

            //Brick Block
            case 45 -> {
                return "bricks";
            }

            //Tnt
            case 46 -> {
                return "tnt";
            }

            //Bookshelf
            case 47 -> {
                return "bookshelf";
            }

            //Moss Stone
            case 48 -> {
                return "mossy_cobblestone";
            }

            //Obsidian
            case 49 -> {
                return "obsidian";
            }

            //Torch
            case 50 -> {
                switch (data) {

                    case 1,2,3,4 -> {
                        return "wall_torch";
                    }

                    default -> {
                        return "torch";
                    }
                }
            }

            //Fire
            case 51 -> {
                return "fire";
            }

            //Mob Spawner
            case 52 -> {
                return "spawner";
            }

            //Oak Stairs
            case 53 -> {
                return "oak_stairs";
            }

            //Chest
            case 54 -> {
                return "chest";
            }

            //Redstone Wire
            case 55 -> {
                return "redstone_wire";
            }

            //Diamond Ore
            case 56 -> {
                return "diamond_ore";
            }

            //Diamond Block
            case 57 -> {
                return "diamond_block";
            }

            //Crafting Table
            case 58 -> {
                return "crafting_table";
            }

            //Wheat Crops
            case 59 -> {
                return "wheat";
            }

            //Farmland
            case 60 -> {
                return "farmland";
            }

            //(Burning) Furnace
            case 61, 62 -> {
                return "furnace";
            }

            //Standing Sign Block
            case 63 -> {
                return "oak_sign";
            }

            //Oak Door Block
            case 64 -> {
                return "oak_door";
            }

            //Ladder
            case 65 -> {
                return "ladder";
            }

            //Rail
            case 66 -> {
                return "rail";
            }

            //Cobblestone Stairs
            case 67 -> {
                return "cobblestone_stairs";
            }

            //Wall Mounted Sign Block
            case 68 -> {
                return "oak_wall_sign";
            }

            //Lever
            case 69 -> {
                return "lever";
            }

            //Stone Pressure Plate
            case 70 -> {
                return "stone_pressure_plate";
            }

            //Iron Door Block
            case 71 -> {
                return "iron_door";
            }

            //Wooden Pressure Plate
            case 72 -> {
                return "oak_pressure_plate";
            }

            //Redstone Ore
            case 73, 74 -> {
                return "redstone_ore";
            }

            //Redstone Torch
            case 75,76 -> {
                switch (data) {

                    case 1,2,3,4 -> {
                        return "redstone_wall_torch";
                    }

                    default -> {
                        return "redstone_torch";
                    }
                }
            }

            //Stone Button
            case 77 -> {
                return "stone_button";
            }

            //Snow
            case 78 -> {
                return "snow";
            }

            //Ice
            case 79 -> {
                return "ice";
            }

            //Snow Block
            case 80 -> {
                return "snow_block";
            }

            //Cactus
            case 81 -> {
                return "cactus";
            }

            //Clay
            case 82 -> {
                return "clay";
            }

            //Sugar Canes
            case 83 -> {
                return "sugar_cane";
            }

            //Jukebox
            case 84 -> {
                return "jukebox";
            }

            //Oak Fence
            case 85 -> {
                return "fence";
            }

            //Pumpkin
            case 86 -> {
                return "carved_pumpkin";
            }

            //Netherrack
            case 87 -> {
                return "netherrack";
            }

            //Soul Sand
            case 88 -> {
                return "soul_sand";
            }

            //Glowstone
            case 89 -> {
                return "glowstone";
            }

            //Nether Portal
            case 90 -> {
                return "nether_portal";
            }

            //Jack o'Lantern
            case 91 -> {
                return "jack_o_lantern";
            }

            //Cake Block
            case 92 -> {
                return "cake";
            }

            //Redstone Repeater
            case 93,94 -> {
                return "repeater";
            }

            //Stained Glass
            case 95 -> {

                switch (data) {

                    case 0 -> {
                        return "white_stained_glass";
                    }

                    case 1 -> {
                        return "orange_stained_glass";
                    }

                    case 2 -> {
                        return "magenta_stained_glass";
                    }

                    case 3 -> {
                        return "light_blue_stained_glass";
                    }

                    case 4 -> {
                        return "yellow_stained_glass";
                    }

                    case 5 -> {
                        return "lime_stained_glass";
                    }

                    case 6 -> {
                        return "pink_stained_glass";
                    }

                    case 7 -> {
                        return "gray_stained_glass";
                    }

                    case 8 -> {
                        return "light_gray_stained_glass";
                    }

                    case 9 -> {
                        return "cyan_stained_glass";
                    }

                    case 10 -> {
                        return "purple_stained_glass";
                    }

                    case 11 -> {
                        return "blue_stained_glass";
                    }

                    case 12 -> {
                        return "brown_stained_glass";
                    }

                    case 13 -> {
                        return "green_stain_glass";
                    }

                    case 14 -> {
                        return "red_stained_glass";
                    }

                    case 15 -> {
                        return "black_stained_glass";
                    }
                }
            }

            //Wooden Trapdoor
            case 96 -> {
                return "oak_trapdoor";
            }

            //Monster Egg
            case 97 -> {

                switch (data) {

                    case 0 -> {
                        return "infested_stone";
                    }

                    case 1 -> {
                        return "infested_cobblestone";
                    }

                    case 2 -> {
                        return "infested_stone_bricks";
                    }

                    case 3 -> {
                        return "infested_mossy_stone_bricks";
                    }

                    case 4 -> {
                        return "infested_cracked_stone_bricks";
                    }

                    case 5 -> {
                        return "infested_chiseled_stone_bricks";
                    }
                }
            }

            //Stone Brick
            case 98 -> {
                switch (data) {

                    case 0 -> {
                        return "stone_bricks";
                    }

                    case 1 -> {
                        return "mossy_stone_bricks";
                    }

                    case 2 -> {
                        return "cracked_stone_bricks";
                    }

                    case 3 -> {
                        return "chiseled_stone_bricks";
                    }
                }
            }

            //Brown Mushroom Block
            case 99 -> {
                switch (data) {

                    case 10, 15 -> {
                        return "mushroom_stem";
                    }

                    default -> {
                        return "brown_mushroom_block";
                    }
                }
            }

            //Red Mushroom Block
            case 100 -> {
                switch (data) {

                    case 10, 15 -> {
                        return "mushroom_stem";
                    }

                    default -> {
                        return "red_mushroom_block";
                    }
                }
            }

            //Iron Bars
            case 101 -> {
                return "iron_bars";
            }

            //Glass Pane
            case 102 -> {
                return "glass_pane";
            }

            //Melon Block
            case 103 -> {
                return "melon";
            }

            //Pumpkin Stem
            case 104 -> {
                return "pumpkin_stem";
            }

            //Melon Stem
            case 105 -> {
                return "melon_stem";
            }

            //Vines
            case 106 -> {
                return "vine";
            }

            //Oak Fence Gate
            case 107 -> {
                return "oak_fence_gate";
            }

            //Brick Stairs
            case 108 -> {
                return "brick_stairs";
            }

            //Stone Brick Stairs
            case 109 -> {
                return "stone_brick_stairs";
            }

            //Myceleum
            case 110 -> {
                return "myceleum";
            }

            //Lily Pad
            case 111 -> {
                return "lily_pad";
            }

            //Nether Brick
            case 112 -> {
                return "nether_bricks";
            }

            //Nether Brick Fence
            case 113 -> {
                return "nether_brick_fence";
            }

            //Nether Brick Stairs
            case 114 -> {
                return "nether_brick_stairs";
            }

            //Nether Wart
            case 115 -> {
                return "nether_wart";
            }

            //Enchantment Table
            case 116 -> {
                return "enchanting_table";
            }

            //Brewing Stand
            case 117 -> {
                return "brewing_stand";
            }

            //Cauldron
            case 118 -> {
                return "cauldron";
            }

            //End Portal
            case 119 -> {
                return "end_portal";
            }

            //End Portal Frame
            case 120 -> {
                return "end_portal_frame";
            }

            //End Stone
            case 121 -> {
                return "end_stone";
            }

            //Dragon Egg
            case 122 -> {
                return "dragon_egg";
            }

            //Redstone Lamp
            case 123,124 -> {
                return "redstone_lamp";
            }

            //Double Wood Slabs
            case 125 -> {
                switch (data) {

                    case 0 -> {
                        return "oak_slab";
                    }

                    case 1 -> {
                        return "spruce_slab";
                    }

                    case 2 -> {
                        return "birch_slab";
                    }

                    case 3 -> {
                        return "jungle_slab";
                    }

                    case 4 -> {
                        return "acacia_slab";
                    }

                    case 5 -> {
                        return "dark_oak_slab";
                    }
                }
            }

            //Wooden Slab
            case 126 -> {
                switch (data) {

                    case 0,8 -> {
                        return "oak_slab";
                    }

                    case 1,9 -> {
                        return "spruce_slab";
                    }

                    case 2,10 -> {
                        return "birch_slab";
                    }

                    case 3,11 -> {
                        return "jungle_slab";
                    }

                    case 4,12 -> {
                        return "acacia_slab";
                    }

                    case 5,13 -> {
                        return "dark_oak_slab";
                    }
                }
            }

            //Cocoa
            case 127 -> {
                return "cocoa";
            }

            //Sandstone Stairs
            case (byte) 128 -> {
                return "sandstone_stairs";
            }

            //Emerald Ore
            case (byte) 129 -> {
                return "emerald_ore";
            }

            //Ender Chest
            case (byte) 130 -> {
                return "ender_chest";
            }

            //Tripwire Hook
            case (byte) 131 -> {
                return "tripwire_hook";
            }

            //Tripwire
            case (byte) 132 -> {
                return "tripwire";
            }

            //Emerald Block
            case (byte) 133 -> {
                return "emerald_block";
            }

            //Spruce Wood Stairs
            case (byte) 134 -> {
                return "spruce_stairs";
            }

            //Birch Wood Stairs
            case (byte) 135 -> {
                return "birch_stairs";
            }

            //Jungle Wood Stairs
            case (byte) 136 -> {
                return "jungle_stairs";
            }

            //Command Block
            case (byte) 137 -> {
                return "command_block";
            }

            //Beacon
            case (byte) 138 -> {
                return "beacon";
            }

            //Cobblestone Wall
            case (byte) 139 -> {
                switch (data) {

                    case 0 -> {
                        return "cobblestone_wall";
                    }

                    case 1 -> {
                        return "mossy_cobblestone_wall";
                    }
                }
            }

            //Flower Pot
            case (byte) 140 -> {
                return "flower_pot";
            }

            //Carrots
            case (byte) 141 -> {
                return "carrots";
            }

            //Potatoes
            case (byte) 142 -> {
                return "potatoes";
            }

            //Wooden Button
            case (byte) 143 -> {
                return "oak_button";
            }

            //Mob Head
            case (byte) 144 -> {
                return "skeleton_skull";
            }

            //Anvil
            case (byte) 145 -> {
                switch (data) {

                    case 0,1,2,3 -> {
                        return "anvil";
                    }

                    case 4,5,6,7 -> {
                        return "chipped_anvil";
                    }

                    case 8,9,10,11 -> {
                        return "damaged_anvil";
                    }
                }
            }

            //Trapped Chest
            case (byte) 146 -> {
                return "trapped_chest";
            }

            //Weighted Pressure Plate (light)
            case (byte) 147 -> {
                return "light_weighted_pressure_plate";
            }

            //Weighted Pressure Plate (heavy)
            case (byte) 148 -> {
                return "heavy_weighted_pressure_plate";
            }

            //Redstone Comporator
            case (byte) 149, (byte) 150 -> {
                return "comparator";
            }

            //Daylight Sensor
            case (byte) 151, (byte) 178 -> {
                return "daylight_detector";
            }

            //Redstone Block
            case (byte) 152 -> {
                return "redstone_block";
            }

            //Nether Quartz Ore
            case (byte) 153 -> {
                return "nether_quartz_ore";
            }

            //Hopper
            case (byte) 154 -> {
                return "hopper";
            }

            //Quartz Block
            case (byte) 155 -> {
                switch (data) {

                    case 0 -> {
                        return "quartz_block";
                    }

                    case 1 -> {
                        return "chiseled_quartz_block";
                    }

                    case 2 -> {
                        return "quartz_pillar";
                    }
                }
            }

            //Quartz Stairs
            case (byte) 156 -> {
                return "quartz_stairs";
            }

            //Activitor Rail
            case (byte) 157 -> {
                return "activator_rail";
            }

            //Dropper
            case (byte) 158 -> {
                return "dropper";
            }

            //Hardened Clay
            case (byte) 159 -> {
                switch (data) {

                    case 0 -> {
                        return "white_terracotta";
                    }

                    case 1 -> {
                        return "orange_terracotta";
                    }

                    case 2 -> {
                        return "magenta_terracotta";
                    }

                    case 3 -> {
                        return "light_blue_terracotta";
                    }

                    case 4 -> {
                        return "yellow_terracotta";
                    }

                    case 5 -> {
                        return "lime_terracotta";
                    }

                    case 6 -> {
                        return "pink_terracotta";
                    }

                    case 7 -> {
                        return "gray_terracotta";
                    }

                    case 8 -> {
                        return "light_gray_terracotta";
                    }

                    case 9 -> {
                        return "cyan_terracotta";
                    }

                    case 10 -> {
                        return "purple_terracotta";
                    }

                    case 11 -> {
                        return "blue_terracotta";
                    }

                    case 12 -> {
                        return "brown_terracotta";
                    }

                    case 13 -> {
                        return "green_terracotta";
                    }

                    case 14 -> {
                        return "red_terracotta";
                    }

                    case 15 -> {
                        return "black_terracotta";
                    }
                }
            }

            //Stained Glass Panes
            case (byte) 160 -> {
                switch (data) {

                    case 0 -> {
                        return "white_stained_glass_pane";
                    }

                    case 1 -> {
                        return "orange_stained_glass_pane";
                    }

                    case 2 -> {
                        return "magenta_stained_glass_pane";
                    }

                    case 3 -> {
                        return "light_blue_stained_glass_pane";
                    }

                    case 4 -> {
                        return "yellow_stained_glass_pane";
                    }

                    case 5 -> {
                        return "lime_stained_glass_pane";
                    }

                    case 6 -> {
                        return "pink_stained_glass_pane";
                    }

                    case 7 -> {
                        return "gray_stained_glass_pane";
                    }

                    case 8 -> {
                        return "light_gray_stained_glass_pane";
                    }

                    case 9 -> {
                        return "cyan_stained_glass_pane";
                    }

                    case 10 -> {
                        return "purple_stained_glass_pane";
                    }

                    case 11 -> {
                        return "blue_stained_glass_pane";
                    }

                    case 12 -> {
                        return "brown_stained_glass_pane";
                    }

                    case 13 -> {
                        return "green_stained_glass_pane";
                    }

                    case 14 -> {
                        return "red_stained_glass_pane";
                    }

                    case 15 -> {
                        return "black_stained_glass_pane";
                    }
                }
            }

            //Leaves 2
            case (byte) 161 -> {
                switch (data) {

                    case 0,4,8,12 -> {
                        return "acacia_leaves";
                    }

                    case 1,5,9,13 -> {
                        return "dark_oak_leaves";
                    }
                }
            }

            //Log 2
            case (byte) 162 -> {
                switch (data) {

                    case 0,4,8 -> {
                        return "acacia_log";
                    }

                    case 1,5,9 -> {
                        return "dark_oak_log";
                    }

                    case 12 -> {
                        return "acacia_wood";
                    }

                    case 13 -> {
                        return "dark_oak_wood";
                    }
                }
            }

            //Acacia Wood Stairs
            case (byte) 163 -> {
                return "acacia_stairs";
            }

            //Dark Oak Wood Stairs
            case (byte) 164 -> {
                return "dark_oak_stairs";
            }

            //Slime Block
            case (byte) 165 -> {
                return "slime_block";
            }

            //Barrier
            case (byte) 166 -> {
                return "barrier";
            }

            //Iron Trapdoor
            case (byte) 167 -> {
                return "iron_trapdoor";
            }

            //Prismarine
            case (byte) 168 -> {
                switch (data) {

                    case 0 -> {
                        return "prismarine";
                    }

                    case 1 -> {
                        return "prismarine_bricks";
                    }

                    case 2 -> {
                        return "dark_prismarine";
                    }
                }
            }

            //Sea Lantern
            case (byte) 169 -> {
                return "sea_lantern";
            }

            //Hay Bale
            case (byte) 170 -> {
                return "hay_block";
            }

            //Carpet
            case (byte) 171 -> {
                switch (data) {

                    case 0 -> {
                        return "white_carpet";
                    }

                    case 1 -> {
                        return "orange_carpet";
                    }

                    case 2 -> {
                        return "magenta_carpet";
                    }

                    case 3 -> {
                        return "light_blue_carpet";
                    }

                    case 4 -> {
                        return "yellow_carpet";
                    }

                    case 5 -> {
                        return "lime_carpet";
                    }

                    case 6 -> {
                        return "pink_carpet";
                    }

                    case 7 -> {
                        return "gray_carpet";
                    }

                    case 8 -> {
                        return "light_gray_carpet";
                    }

                    case 9 -> {
                        return "cyan_carpet";
                    }

                    case 10 -> {
                        return "purple_carpet";
                    }

                    case 11 -> {
                        return "blue_carpet";
                    }

                    case 12 -> {
                        return "brown_carpet";
                    }

                    case 13 -> {
                        return "green_carpet";
                    }

                    case 14 -> {
                        return "red_carpet";
                    }

                    case 15 -> {
                        return "black_carpet";
                    }
                }
            }

            //Hardened Clay
            case (byte) 172 -> {
                return "terracotta";
            }

            //Block of Coal
            case (byte) 173 -> {
                return "coal_block";
            }

            //Packed Ice
            case (byte) 174 -> {
                return "packed_ice";
            }

            //Double Plant
            //Top half is converted to the default (sunflower top), this will  be edited in post-processing
            case (byte) 175 -> {
                switch (data) {

                    case 1 -> {
                        return "lilac";
                    }

                    case 2 -> {
                        return "tall_grass";
                    }

                    case 3 -> {
                        return "large_fern";
                    }

                    case 4 -> {
                        return "rose_bush";
                    }

                    case 5 -> {
                        return "peony";
                    }

                    default -> {
                        return "sunflower";
                    }
                }
            }

            //Banner colour is stored in the block entity data, this will be applied post-processing.
            //Free-standing Banner
            case (byte) 176 -> {
                return "white_banner";
            }

            //Banner colour is stored in the block entity data, this will be applied post-processing.
            //Wall-mounted Banner
            case (byte) 177 -> {
                return "white_wall_banner";
            }

            //Red Sandstone
            case (byte) 179 -> {
                switch (data) {

                    case 0 -> {
                        return "red_sandstone";
                    }

                    case 1 -> {
                        return "chiseled_red_sandstone";
                    }

                    case 2 -> {
                        return "cut_red_sandstone";
                    }
                }
            }

            //Red Sandstone Stais
            case (byte) 180 -> {
                return "red_sandstone_stairs";
            }

            //Red Sandstone Slab
            case (byte) 181, (byte) 182 -> {
                return "red_sandstone_slab";
            }

            //Spruce Fence Gate
            case (byte) 183 -> {
                return "spruce_fence_gate";
            }

            //Birch Fence Gate
            case (byte) 184 -> {
                return "birch_fence_gate";
            }

            //Jungle Fence Gate
            case (byte) 185 -> {
                return "jungle_fence_gate";
            }

            //Dark Oak Fence Gate
            case (byte) 186 -> {
                return "dark_oak_fence_gate";
            }

            //Acacia Fence Gate
            case (byte) 187 -> {
                return "acacia_fence_gate";
            }

            //Spruce Fence
            case (byte) 188 -> {
                return "spruce_fence";
            }

            //Birch Fence
            case (byte) 189 -> {
                return "birch_fence";
            }

            //Jungle Fence
            case (byte) 190 -> {
                return "jungle_fence";
            }

            //Dark Oak Fence
            case (byte) 191 -> {
                return "dark_oak_fence";
            }

            //Acacia Fence
            case (byte) 192 -> {
                return "acacia_fence";
            }

            //Spruce Door
            case (byte) 193 -> {
                return "spruce_door";
            }

            //Birch Door
            case (byte) 194 -> {
                return "birch_door";
            }

            //Jungle Door
            case (byte) 195 -> {
                return "jungle_door";
            }

            //Dark Oak Door
            case (byte) 196 -> {
                return "dark_oak_door";
            }

            //Acacia Door
            case (byte) 197 -> {
                return "acacia_door";
            }

            //End Rod
            case (byte) 198 -> {
                return "end_rod";
            }

            //Chorus Plant
            case (byte) 199 -> {
                return "chorus_plant";
            }

            //Chorus Flower
            case (byte) 200 -> {
                return "chorus_flower";
            }

            //Purpur Block
            case (byte) 201 -> {
                return "purpur_block";
            }

            //Purpur Pillar
            case (byte) 202 -> {
                return "purpur_pillar";
            }

            //Purpur Stairs
            case (byte) 203 -> {
                return "purpur_stairs";
            }

            //Purpur Slab
            case (byte) 204, (byte) 205 -> {
                return "purpur_slab";
            }

            //End Stone Bricks
            case (byte) 206 -> {
                return "end_stone_bricks";
            }

            //Beetroot Block
            case (byte) 207 -> {
                return "beetroots";
            }

            //Grass Path
            case (byte) 208 -> {
                return "dirt_path";
            }

            //End Gateway
            case (byte) 209 -> {
                return "end_gateway";
            }

            //Repeating Command Block
            case (byte) 210 -> {
                return "repeating_command_block";
            }

            //Chain Command Block
            case (byte) 211 -> {
                return "chain_command_block";
            }

            //Frosted Ice
            case (byte) 212 -> {
                return "frosted_ice";
            }

            //Magma Block
            case (byte) 213 -> {
                return "magma_block";
            }

            //Nether Wart Block
            case (byte) 214 -> {
                return "nether_wart_block";
            }

            //Red Nether Brick
            case (byte) 215 -> {
                return "red_nether_bricks";
            }

            //Bone Block
            case (byte) 216 -> {
                return "bone_block";
            }

            //Structure Void
            case (byte) 217 -> {
                return "structure_void";
            }

            //Observer
            case (byte) 218 -> {
                return "observer";
            }

            //White Shulker Box
            case (byte) 219 -> {
                return "white_shulker_box";
            }

            //Orange Shulker Box
            case (byte) 220 -> {
                return "orange_shulker_box";
            }

            //Magenta Shulker Box
            case (byte) 221 -> {
                return "magenta_shulker_box";
            }

            //Light Blue Shulker Box
            case (byte) 222 -> {
                return "light_blue_shulker_box";
            }

            //Yellow Shulker Box
            case (byte) 223 -> {
                return "yellow_shulker_box";
            }

            //Lime Shulker Box
            case (byte) 224 -> {
                return "lime_shulker_box";
            }

            //Pink Shulker Box
            case (byte) 225 -> {
                return "pink_shulker_box";
            }

            //Gray Shulker Box
            case (byte) 226 -> {
                return "gray_shulker_box";
            }

            //Light Gray Shulker Box
            case (byte) 227 -> {
                return "light_gray_shulker_box";
            }

            //Cyan Shulker Box
            case (byte) 228 -> {
                return "cyan_shulker_box";
            }

            //Default Shulker Box
            case (byte) 229 -> {
                return "shulker_box";
            }

            //Blue Shulker Box
            case (byte) 230 -> {
                return "blue_shulker_box";
            }

            //Brown Shulker Box
            case (byte) 231 -> {
                return "brown_shulker_box";
            }

            //Green Shulker Box
            case (byte) 232 -> {
                return "green_shulker_box";
            }

            //Red Shulker Box
            case (byte) 233 -> {
                return "red_shulker_box";
            }

            //Black Shulker Box
            case (byte) 234 -> {
                return "black_shulker_box";
            }

            //White Glazed Terracotta
            case (byte) 235 -> {
                return "white_glazed_terracotta";
            }

            //Orange Glazed Terracotta
            case (byte) 236 -> {
                return "orange_glazed_terracotta";
            }

            //Magenta Glazed Terracotta
            case (byte) 237 -> {
                return "magenta_glazed_terracotta";
            }

            //Light Blue Glazed Terracotta
            case (byte) 238 -> {
                return "light_blue_glazed_terracotta";
            }

            //Yellow Glazed Terracotta
            case (byte) 239 -> {
                return "yellow_glazed_terracotta";
            }

            //Lime Glazed Terracotta
            case (byte) 240 -> {
                return "lime_glazed_terracotta";
            }

            //Pink Glazed Terracotta
            case (byte) 241 -> {
                return "pink_glazed_terracotta";
            }

            //Gray Glazed Terracotta
            case (byte) 242 -> {
                return "gray_glazed_terracotta";
            }

            //Light Gray Glazed Terracotta
            case (byte) 243 -> {
                return "light_gray_glazed_terracotta";
            }

            //Cyan Glazed Terracotta
            case (byte) 244 -> {
                return "cyan_glazed_terracotta";
            }

            //Purple Glazed Terracotta
            case (byte) 245 -> {
                return "purple_glazed_terracotta";
            }

            //Blue Glazed Terracotta
            case (byte) 246 -> {
                return "blue_glazed_terracotta";
            }

            //Brown Glazed Terracotta
            case (byte) 247 -> {
                return "brown_glazed_terracotta";
            }

            //Green Glazed Terracotta
            case (byte) 248 -> {
                return "green_glazed_terracotta";
            }

            //Red Glazed Terracotta
            case (byte) 249 -> {
                return "red_glazed_terracotta";
            }

            //Black Glazed Terracotta
            case (byte) 250 -> {
                return "black_glazed_terracotta";
            }

            //Concrete
            case (byte) 251 -> {
                switch (data) {

                    case 0 -> {
                        return "white_concrete";
                    }

                    case 1 -> {
                        return "orange_concrete";
                    }

                    case 2 -> {
                        return "magenta_concrete";
                    }

                    case 3 -> {
                        return "light_blue_concrete";
                    }

                    case 4 -> {
                        return "yellow_concrete";
                    }

                    case 5 -> {
                        return "lime_concrete";
                    }

                    case 6 -> {
                        return "pink_concrete";
                    }

                    case 7 -> {
                        return "gray_concrete";
                    }

                    case 8 -> {
                        return "light_gray_concrete";
                    }

                    case 9 -> {
                        return "cyan_concrete";
                    }

                    case 10 -> {
                        return "purple_concrete";
                    }

                    case 11 -> {
                        return "blue_concrete";
                    }

                    case 12 -> {
                        return "brown_concrete";
                    }

                    case 13 -> {
                        return "green_concrete";
                    }

                    case 14 -> {
                        return "red_concrete";
                    }

                    case 15 -> {
                        return "black_concrete";
                    }
                }
            }

            //Concrete Powder
            case (byte) 252 -> {
                switch (data) {

                    case 0 -> {
                        return "white_concrete_powder";
                    }

                    case 1 -> {
                        return "orange_concrete_powder";
                    }

                    case 2 -> {
                        return "magenta_concrete_powder";
                    }

                    case 3 -> {
                        return "light_blue_concrete_powder";
                    }

                    case 4 -> {
                        return "yellow_concrete_powder";
                    }

                    case 5 -> {
                        return "lime_concrete_powder";
                    }

                    case 6 -> {
                        return "pink_concrete_powder";
                    }

                    case 7 -> {
                        return "gray_concrete_powder";
                    }

                    case 8 -> {
                        return "light_gray_concrete_powder";
                    }

                    case 9 -> {
                        return "cyan_concrete_powder";
                    }

                    case 10 -> {
                        return "purple_concrete_powder";
                    }

                    case 11 -> {
                        return "blue_concrete_powder";
                    }

                    case 12 -> {
                        return "brown_concrete_powder";
                    }

                    case 13 -> {
                        return "green_concrete_powder";
                    }

                    case 14 -> {
                        return "red_concrete_powder";
                    }

                    case 15 -> {
                        return "black_concrete_powder";
                    }
                }
            }

            case (byte) 255 -> {
                return "structure_block";
            }
        }

        return "air";

    }
}
