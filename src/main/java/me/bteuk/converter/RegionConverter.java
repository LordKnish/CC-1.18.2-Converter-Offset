package me.bteuk.converter;

/*

This class will be run to convert a single region file.
This will allow the converter to run multiple threads so regions can be converter simultaneously.

 */

import cubicchunks.regionlib.api.region.key.RegionKey;
import cubicchunks.regionlib.impl.EntryLocation2D;
import cubicchunks.regionlib.impl.EntryLocation3D;
import cubicchunks.regionlib.impl.MinecraftChunkLocation;
import cubicchunks.regionlib.impl.header.TimestampHeaderEntryProvider;
import cubicchunks.regionlib.impl.save.MinecraftSaveSection;
import cubicchunks.regionlib.lib.provider.SimpleRegionProvider;
import me.bteuk.converter.cc.MemoryWriteRegion;
import me.bteuk.converter.cc.RWLockingCachedRegionProvider;
import me.bteuk.converter.cc.Utils;
import me.bteuk.converter.utils.LegacyID;
import me.bteuk.converter.utils.MinecraftIDConverter;
import net.querz.nbt.tag.CompoundTag;
import net.querz.nbt.tag.DoubleTag;
import net.querz.nbt.tag.ListTag;
import net.querz.nbt.tag.StringTag;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import static cubicchunks.regionlib.impl.save.MinecraftSaveSection.MinecraftRegionType.MCA;

public class RegionConverter extends Thread {

    ThreadManager mng;

    private Thread t;

    boolean unique;

    final BlockingQueue<String> queue;

    //Biome related stuff.
    ListTag<StringTag> biomePalette = new ListTag<>(StringTag.class);
    HashMap<Byte, Integer> biomePaletteID = new HashMap<>();
    String biomeName;

    ArrayList<LegacyID> uniqueBlocks = new ArrayList<>();
    ListTag<CompoundTag> tile_entities = new ListTag<>(CompoundTag.class);
    CompoundTag tEntity;

    RegionKey regionKey;
    ListTag<CompoundTag> entities;

    HashMap<LegacyID, Integer> paletteID = new HashMap<>();
    ListTag<CompoundTag> palette = new ListTag<>(CompoundTag.class);

    int entryX;
    int entryZ;

    UUID uuid;

    JSONArray jaEntities;

    public RegionConverter(UUID uuid, BlockingQueue<String> queue, ThreadManager mng) {

        this.uuid = uuid;
        this.queue = queue;
        this.mng = mng;

        //Create json array to store entities.
        jaEntities = new JSONArray();

    }

    public void run() {

        //Iterate until the queue is empty.
        try {
            while (true) {
                String take = queue.take();

                if (take.equals("end")) {
                    mng.activeThreads.decrementAndGet();

                    //Write entities file.
                    FileWriter ppFile = new FileWriter(mng.itr.output.resolve("entities") + "/" + uuid + ".json");
                    ppFile.write(jaEntities.toJSONString());
                    ppFile.flush();
                    ppFile.close();
                    break;
                }

                try {
                    convert(take);
                } catch (IOException ex) {
                    System.out.println("An IOException occurred converting the file: " + take);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println("An Exception occurred converting the file: " + take);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void convert(String file) throws IOException {

        byte meta;
        byte[] data;

        byte[] blocks;

        int cX;
        int cY;
        int cZ;

        EntryLocation3D e3d;
        CompoundTag block_entity;

        //Create new json array to store blocks for post-processing in.
        JSONArray ja = new JSONArray();

        //Iterate through all possible chunk columns in the file and continue with any that contain data.
        //A region2d is 512x512 which is 32*32 in terms of chunks, so 1024 individual chunks to iterate.
        for (int i = 0; i < 1024; i++) {

            CompoundTag chunk = new CompoundTag();

            long[] newData = null;
            long[] biomeData;
            RegionKey key = new RegionKey(file);
            EntryLocation2D e2d = mng.provider2d.fromRegionAndId(key, i);
            entryX = e2d.getEntryX();
            entryZ = e2d.getEntryZ();

            //Create list of sections to store the new sections in.
            ListTag<CompoundTag> sections = new ListTag<>(CompoundTag.class);

            //Gets the data from the 2dr file.
            Optional<ByteBuffer> column = mng.itr.saveCubeColumns.load(e2d, true);

            //Create a list of Block Entities for this chunk.
            ListTag<CompoundTag> block_entities = new ListTag<>(CompoundTag.class);

            //If the columndata is not empty
            if (column.isPresent()) {

                //Create a check for if a cube is empty, if the whole column is empty it can be skipped.
                boolean isEmpty = true;

                //Clear the biome palette.
                biomePalette.clear();
                biomePaletteID.clear();

                CompoundTag columnTag = (CompoundTag) mng.readCompressedCC(new ByteArrayInputStream(column.get().array())).getTag();
                CompoundTag columnLevel = columnTag.getCompoundTag("Level");

                CompoundTag biomes = new CompoundTag();

                //Get the biome data.
                byte[] oldBiomes = columnLevel.getByteArray("Biomes");
                if (oldBiomes != null) {

                    //Get the biomes, since Minecraft 1.18.2 stored biomes in 4x4x4 cubes
                    // we must create a 4x4x4 array of cubes to fill a section.
                    //However we it is stored using a palette with a reference to the palette,
                    // so we start by constructing the palette using the number of unique biomes.
                    for (int ind = 0; ind < oldBiomes.length; ind++) {

                        //We can skip 3 out of 4 indexes since we only need a 4x4 layer rather than 16x16.
                        //We'll skip all indixes divisible by 2 and 3, this will leave the second index out of 4 (0,1,2,3).
                        if (ind % 4 == 1 && ind / 16 % 4 == 1) {

                            biomeName = MinecraftIDConverter.getBiome(oldBiomes[ind]);
                            if (!containsBiome(biomeName)) {
                                //Index equals biome size before adding the new biome.
                                biomePaletteID.put(oldBiomes[ind], biomePalette.size());
                                biomePalette.add(new StringTag(biomeName));
                            } else if (!biomePaletteID.containsKey(oldBiomes[ind])) {
                                int index = 0;
                                for (StringTag biome : biomePalette) {
                                    if (biome.getValue().equals(biomeName)) {
                                        //Index equals biome size before adding the new biome.
                                        biomePaletteID.put(oldBiomes[ind], index);
                                    }
                                    index++;
                                }
                            }
                        }
                    }

                    //Add the biome palette
                    biomes.put("palette", biomePalette);

                    //If palette has more than 1 biome we need to create a data array.
                    if (biomePalette.size() > 1) {

                        //Find the number of blocks that will be stored in each long array. If the count is 16 or less then each block will use 4 bits.
                        //A long allows for 64 bits in total.
                        int blockSize = mng.log2(biomePalette.size() - 1);
                        int blocksPerLong = 64 / blockSize;
                        biomeData = new long[(64 + blocksPerLong - 1) / blocksPerLong];

                        int counter = 0;
                        //To remember the index of the long.
                        int index = 0;
                        long blockIndex;

                        //To get the index from the old biome array.
                        int oldIndex = 0;

                        //Long value
                        long blockStorage = 0;

                        //Convert the data to the new section structure of Minecraft 1.18.2
                        for (int j = 0; j < 64; j++) {

                            //If counter is equal or greater than the blocksPerLong we need to create a new long and reset the counter.
                            if (counter >= blocksPerLong) {
                                counter = 0;
                                blockStorage = 0;
                                index++;
                            }

                            //Find the next correct index for the old biome array.
                            while (oldIndex % 4 != 1 || oldIndex / 16 % 4 != 1) {
                                //If we've reached the end of the array indexes return to 1, which is the first valid index.
                                if (oldIndex >= 255) {
                                    oldIndex = 1;
                                }
                                oldIndex++;
                            }

                            //Get the id for the block.
                            blockIndex = biomePaletteID.get(oldBiomes[oldIndex]);
                            oldIndex++;

                            //Shift the blockIndex by blockSize * counter so the value gets put in the right place in the long.
                            //Using the bitwise or function we can set the values in the long.
                            blockIndex = blockIndex << (counter * blockSize);
                            blockStorage = blockStorage | blockIndex;

                            //Update the counter.
                            counter++;

                            //Set the long in the array when the counter is at the blocksPerLong value.
                            if (counter == blocksPerLong || j == 63) {
                                biomeData[index] = blockStorage;
                            }
                        }

                        //Add the biome data to the biomes tag.
                        biomes.putLongArray("data", biomeData);
                    }
                }

                //Get all cubes that could be in the range of heights.
                for (int y = Main.MIN_Y_CUBE; y < Main.MAX_Y_CUBE; y++) {

                    //Get the cube of data.
                    e3d = new EntryLocation3D(e2d.getEntryX(), y, e2d.getEntryZ());
                    Optional<ByteBuffer> cube = mng.itr.saveCubeColumns.load(e3d, true);

                    //Check if it exists.
                    if (cube.isPresent()) {

                        //Retrieve the data from the cube.
                        CompoundTag cubeTag = (CompoundTag) mng.readCompressedCC(new ByteArrayInputStream(cube.get().array())).getTag();
                        CompoundTag cubeLevel = cubeTag.getCompoundTag("Level");

                        if (cubeLevel == null) {
                            sections.add(mng.createEmptySection(y));
                            continue;
                        }

                        if (cubeLevel.get("Sections") == null) {
                            sections.add(mng.createEmptySection(y));
                            continue;
                        }

                        //Get the sections from the cube.
                        ListTag<CompoundTag> oldSections = (ListTag<CompoundTag>) cubeLevel.getListTag("Sections");
                        CompoundTag oldSection = oldSections.get(0);

                        //Get unique blocks in section using Blocks and Data.
                        blocks = oldSection.getByteArray("Blocks");
                        data = oldSection.getByteArray("Data");

                        //Clear uniqueBlocks.
                        uniqueBlocks.clear();

                        //Load entities if unique.
                        if (!e3d.getRegionKey().equals(regionKey)) {
                            //Load entities.
                            entities = (ListTag<CompoundTag>) cubeLevel.getListTag("Entities");
                            regionKey = e3d.getRegionKey();

                            //Add them to the json file.
                            for (CompoundTag entity : entities) {

                                JSONObject jo = new JSONObject();
                                jo.put("id", entity.getString("id"));

                                ListTag<DoubleTag> pos = (ListTag<DoubleTag>) entity.getListTag("Pos");

                                jo.put("x", pos.get(0).asDouble());
                                jo.put("y", pos.get(1).asDouble());
                                jo.put("z", pos.get(2).asDouble());

                                jaEntities.add(jo);

                            }
                        }

                        //Load tile entities.
                        tile_entities = (ListTag<CompoundTag>) cubeLevel.getListTag("TileEntities");


                        for (int j = 0; j < 4096; j++) {

                            //Get data for block.
                            if (j % 2 == 0) {
                                meta = (byte) (data[j >> 1] & 0x0f);
                            } else {
                                meta = (byte) ((data[j >> 1] >> 4) & 0x0f);
                            }

                            //Add block if unique.
                            addUnique(blocks[j], meta);

                            //Convert block entities
                            //If the block is a block entity, load it.
                            if (MinecraftIDConverter.isBlockEntity(blocks[j]) || MinecraftIDConverter.requiredPostProcessing(blocks[j], meta)) {

                                //Convert current block to x,y,z coordinate.
                                //blockPos = y*256 + z*16 + x = i
                                cY = j / 256;
                                cZ = (j - (cY * 256)) / 16;
                                cX = j - (cY * 256) - (cZ * 16);

                                //Find the tile entity from the list.
                                if (MinecraftIDConverter.isBlockEntity(blocks[j])) {

                                    for (CompoundTag tile_entity : tile_entities) {

                                        //If the coordinates are equal.
                                        if ((tile_entity.getInt("x") == (entryX * 16) + cX) &&
                                                (tile_entity.getInt("z") == (entryZ * 16) + cZ) &&
                                                (tile_entity.getInt("y") == (y * 16 + cY))) {

                                            //Get the block entity
                                            block_entity = MinecraftIDConverter.getBlockEntity(blocks[j], meta, tile_entity);
                                            tEntity = tile_entity.clone();

                                            //Add the block entity to the list.
                                            //Only if the block entity needs to be added.
                                            if (!MinecraftIDConverter.blockEntityNotAdded(blocks[j])) {
                                                block_entities.add(block_entity);
                                            }
                                            break;

                                        }
                                    }
                                }

                                //If block requires post-processing add it to the txt file.
                                if (MinecraftIDConverter.requiredPostProcessing(blocks[j], meta)) {

                                    //Create new json object for this block and add it to the array.
                                    JSONObject obj = new JSONObject();
                                    obj.put("block", MinecraftIDConverter.getNameSpace(blocks[j], meta));

                                    //Add the coordinates of the block to the object.
                                    obj.put("x", (entryX * 16) + cX);
                                    obj.put("y", ((y * 16) + Main.OFFSET + cY));
                                    obj.put("z", (entryZ * 16) + cZ);

                                    //Add properties for certain blocks.
                                    if (MinecraftIDConverter.hasProperties(blocks[j])) {
                                        //If it's a block entity.
                                        if (MinecraftIDConverter.isBlockEntity(blocks[j])) {

                                            //Add the properties.
                                            obj.put("properties", MinecraftIDConverter.getProperties(blocks[j], meta, tEntity));

                                        } else {

                                            //Add the properties
                                            obj.put("properties", MinecraftIDConverter.getProperties(blocks[j], meta, null));

                                        }
                                    }

                                    //Add object to array.
                                    ja.add(obj);
                                }
                            }
                        }

                        //Convert the list of unique blocks to a 1.18.2 block palette.
                        palette = new ListTag<>(CompoundTag.class);
                        paletteID = new HashMap<>();

                        int counter = 0;
                        for (LegacyID id : uniqueBlocks) {
                            //Store the index of this block, so we can easily reference it
                            // from the palette without having the convert it again.
                            paletteID.put(id, counter);
                            counter++;
                            palette.add(MinecraftIDConverter.getBlock(id));
                        }

                        //If the palette only has one block skip the next part entirely.
                        if (palette.size() != 1) {

                            //Set column to not empty.
                            isEmpty = false;

                            //Find the number of blocks that will be stored in each long array. If the count is 16 or less then each block will use 4 bits.
                            //A long allows for 64 bits in total.
                            int blockSize = mng.log2(uniqueBlocks.size() - 1);
                            if (blockSize < 4) {
                                blockSize = 4;
                            }
                            int blocksPerLong = 64 / blockSize;
                            newData = new long[(4096 + blocksPerLong - 1) / blocksPerLong];

                            counter = 0;
                            //To remember the index of the long.
                            int index = 0;
                            long blockIndex;

                            //Long value
                            long blockStorage = 0;

                            //Convert the data to the new section structure of Minecraft 1.18.2
                            for (int j = 0; j < 4096; j++) {

                                //If counter is equal or greater than the blocksPerLong we need to create a new long and reset the counter.
                                if (counter >= blocksPerLong) {
                                    counter = 0;
                                    blockStorage = 0;
                                    index++;
                                }

                                //Get data for block.
                                if (j % 2 == 0) {
                                    meta = (byte) (data[j >> 1] & 0x0f);
                                } else {
                                    meta = (byte) ((data[j >> 1] >> 4) & 0x0f);
                                }

                                //Get the id for the block.
                                blockIndex = getPaletteID(blocks[j], meta);

                                //Shift the blockIndex by blockSize * counter so the value gets put in the right place in the long.
                                //Using the bitwise or function we can set the values in the long.
                                blockIndex = blockIndex << (counter * blockSize);
                                blockStorage = blockStorage | blockIndex;

                                //Update the counter.
                                counter++;

                                //Set the long in the array when the counter is at the blocksPerLong value.
                                if (counter == blocksPerLong || j == 4095) {
                                    newData[index] = blockStorage;
                                }
                            }

                        } else {

                            //If the single block is not air, set the chunk to not empty.
                            if (uniqueBlocks.get(0).getID() != 0) {
                                isEmpty = false;
                            } else {
                                sections.add(mng.createEmptySection(y));
                                continue;
                            }
                        }

                        //Construct section.
                        CompoundTag section = new CompoundTag();

                        section.putByte("Y", (byte) (y+(Main.OFFSET/16)));
                        //Create block_states compound tag.
                        CompoundTag block_states = new CompoundTag();
                        block_states.put("palette", palette);
                        if (palette.size() > 1) {
                            block_states.putLongArray("data", newData);
                        }

                        section.put("block_states", block_states);

                        section.put("biomes", biomes);

                        section.putByteArray("BlockLight", oldSection.getByteArray("BlockLight"));
                        section.putByteArray("SkyLight", oldSection.getByteArray("SkyLight"));

                        //Add section to sections.
                        sections.add(section);

                    } else {

                        //Add empty section
                        sections.add(mng.createEmptySection(y));

                    }
                }

                //If the whole chunk is empty, don't save it.
                if (!isEmpty) {

                    //Add block entities to the chunk.
                    chunk.put("block_entities", block_entities);

                    //Add sections to chunk.
                    chunk.put("sections", sections);

                    //Set chunk status.
                    chunk.putString("Status", "heightmaps");

                    //Set data version
                    chunk.putInt("DataVersion", 2975);

                    //Set position of chunk.
                    chunk.putInt("xPos", columnLevel.getInt("x"));
                    chunk.putInt("zPos", columnLevel.getInt("z"));
                    chunk.putInt("yPos", (Main.MIN_Y_CUBE + (Main.OFFSET / 16)));

                    //Set last update.
                    chunk.putLong("LastUpdate", 0);

                    //Add block and fluid ticks.
                    chunk.put("fluid_ticks", new ListTag<>(CompoundTag.class));
                    chunk.put("block_ticks", new ListTag<>(CompoundTag.class));

                    //Inhabited Time
                    chunk.putLong("InhabitedTime", 0);

                    //Add Post Processing
                    chunk.put("PostProcessing", mng.post_processing);

                    //Add Structures
                    chunk.put("structures", mng.structures);

                    //Is Light On
                    chunk.putBoolean("isLightOn", true);

                    //Save the chunk in the region file.
                    save(mng.itr.output.resolve("region"), mng.writeCompressed(chunk, true));
                }
            }  //Else Skip this column.
        }

        try {

            //Write the json array to a file.
            FileWriter ppFile = new FileWriter(mng.itr.output.resolve("post-processing") + "/" + file.replace(".2dr", ".json"));
            ppFile.write(ja.toJSONString());
            ppFile.flush();
            ppFile.close();

        } catch (
                IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (t == null) {
            t = new Thread(this);
            t.start();
        }
    }

    private boolean containsBiome(String biome) {
        for (StringTag biomeNamespace : biomePalette) {
            if (biomeNamespace.getValue().equals(biome)) {
                return true;
            }
        }
        return false;
    }

    private void addUnique(byte block, byte data) {
        unique = true;
        for (LegacyID id : uniqueBlocks) {
            if (id.equals(block, data)) {
                unique = false;
            }
        }
        if (unique) {
            uniqueBlocks.add(new LegacyID(block, data));
        }
    }

    private int getPaletteID(byte block, byte data) {
        for (LegacyID id : uniqueBlocks) {
            if (id.equals(block, data)) {
                return paletteID.get(id);
            }
        }
        return 0;
    }

    private void save(Path regionDir, ByteBuffer data) throws IOException {
        Utils.createDirectories(regionDir);
        MinecraftSaveSection save = new MinecraftSaveSection(new RWLockingCachedRegionProvider<>(
                new SimpleRegionProvider<>(new MinecraftChunkLocation.Provider(MCA.name().toLowerCase()), regionDir, (keyProvider, regionKey) ->
                        MemoryWriteRegion.<MinecraftChunkLocation>builder()
                                .setDirectory(regionDir)
                                .setSectorSize(4096)
                                .setKeyProvider(keyProvider)
                                .setRegionKey(regionKey)
                                .addHeaderEntry(new TimestampHeaderEntryProvider<>(TimeUnit.SECONDS))
                                .build(),
                        (file, key) -> Files.exists(file)
                )
        ));
        save.save(new MinecraftChunkLocation(entryX, entryZ, "mca"), data);
    }
}
