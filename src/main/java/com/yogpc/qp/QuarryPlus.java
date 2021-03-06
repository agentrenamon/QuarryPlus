/*
 * Copyright (C) 2012,2013 yogpstop This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.yogpc.qp;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.ForgeChunkManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.oredict.OreDictionary;

import com.yogpc.mc_lib.PacketHandler;
import com.yogpc.mc_lib.WorkbenchRecipe;
import com.yogpc.qp.client.RenderEntityLaser;

import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameData;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

@Mod(modid = "QuarryPlus", name = "QuarryPlus", version = "{version}",
    dependencies = "required-after:YogpstopLib")
public class QuarryPlus {
  public static final CreativeTabs ct = new CreativeTabQuarryPlus();
  @SidedProxy(clientSide = "com.yogpc.qp.client.ClientProxy",
      serverSide = "com.yogpc.qp.CommonProxy")
  public static CommonProxy proxy;
  @Mod.Instance("QuarryPlus")
  public static QuarryPlus instance;
  public static final int refineryRenderID = RenderingRegistry.getNextAvailableRenderId();
  public static final int laserRenderID = RenderingRegistry.getNextAvailableRenderId();
  public static final int markerRenderID = RenderingRegistry.getNextAvailableRenderId();
  public static final int frameRenderID = RenderingRegistry.getNextAvailableRenderId();
  public static Block blockQuarry, blockMarker, blockMover, blockMiningWell, blockPump,
      blockRefinery, blockPlacer, blockBreaker, blockLaser, blockPlainPipe, blockFrame;
  public static Item itemTool;
  public static final int guiIdMover = 2;
  public static final int guiIdFList = 3;
  public static final int guiIdSList = 4;
  public static final int guiIdPlacer = 5;
  public static final int guiIdPump = 6;// reserved from 6 to 11

  @SubscribeEvent
  public void onWorldUnload(final WorldEvent.Unload event) {
    final TileMarker.Link[] la =
        TileMarker.linkList.toArray(new TileMarker.Link[TileMarker.linkList.size()]);
    for (final TileMarker.Link l : la)
      if (l.w == event.world)
        l.removeConnection(false);
    final TileMarker.Laser[] lb =
        TileMarker.laserList.toArray(new TileMarker.Laser[TileMarker.laserList.size()]);
    for (final TileMarker.Laser l : lb)
      if (l.w == event.world)
        l.destructor();
  }

  @SubscribeEvent
  @SideOnly(Side.CLIENT)
  public void loadTextures(final TextureStitchEvent.Pre evt) {
    if (evt.map.getTextureType() == 0) {
      final TextureMap map = evt.map;
      RenderEntityLaser.icons = new IIcon[4];
      RenderEntityLaser.icons[EntityLaser.DRILL] =
          map.registerIcon("yogpstop_qp:blockDrillTexture");
      RenderEntityLaser.icons[EntityLaser.DRILL_HEAD] =
          map.registerIcon("yogpstop_qp:blockDrillHeadTexture");
      RenderEntityLaser.icons[EntityLaser.RED_LASER] =
          map.registerIcon("yogpstop_qp:blockRedLaser");
      RenderEntityLaser.icons[EntityLaser.BLUE_LASER] =
          map.registerIcon("yogpstop_qp:blockBlueLaser");
    }
  }

  @Mod.EventHandler
  public void preInit(final FMLPreInitializationEvent event) {
    final Configuration cfg = new Configuration(event.getSuggestedConfigurationFile());
    try {
      cfg.load();
      final Property RD = cfg.get(Configuration.CATEGORY_GENERAL, "RecipeDifficulty", 2);
      RD.comment = "Default is 2.0";
      WorkbenchRecipe.difficulty = RD.getDouble(2.0);
      PowerManager.loadConfiguration(cfg);
    } finally {
      cfg.save();
    }
    blockQuarry = new BlockQuarry();
    blockMarker = new BlockMarker();
    blockMover = new BlockMover();
    blockMiningWell = new BlockMiningWell();
    blockPump = new BlockPump();
    blockRefinery = new BlockRefinery();
    blockPlacer = new BlockPlacer();
    blockBreaker = new BlockBreaker();
    blockLaser = new BlockLaser();
    blockPlainPipe = new BlockPlainPipe();
    blockFrame = new BlockFrame();
    itemTool = new ItemTool();
    ForgeChunkManager.setForcedChunkLoadingCallback(instance, new ChunkLoadingHandler());
    MinecraftForge.EVENT_BUS.register(this);
  }

  @Mod.EventHandler
  public void init(final FMLInitializationEvent event) {
    GameRegistry.registerItem(itemTool, "qpTool");
    GameRegistry.registerBlock(blockQuarry, ItemBlockQuarry.class, "QuarryPlus");
    GameRegistry.registerBlock(blockMarker, "MarkerPlus");
    GameRegistry.registerBlock(blockMover, "EnchantMover");
    GameRegistry.registerBlock(blockMiningWell, ItemBlockQuarry.class, "MiningWellPlus");
    GameRegistry.registerBlock(blockPump, ItemBlockPump.class, "PumpPlus");
    GameRegistry.registerBlock(blockRefinery, ItemBlockRefinery.class, "RefineryPlus");
    GameRegistry.registerBlock(blockPlacer, "PlacerPlus");
    GameRegistry.registerBlock(blockBreaker, ItemBlockBreaker.class, "BreakerPlus");
    GameRegistry.registerBlock(blockLaser, ItemBlockQuarry.class, "LaserPlus");
    GameRegistry.registerBlock(blockPlainPipe, "PlainPipePlus");
    GameRegistry.registerBlock(blockFrame, "FramePlus");
    GameRegistry.registerTileEntity(TileQuarry.class, "QuarryPlus");
    GameRegistry.registerTileEntity(TileMarker.class, "MarkerPlus");
    GameRegistry.registerTileEntity(TileMiningWell.class, "MiningWellPlus");
    GameRegistry.registerTileEntity(TilePump.class, "PumpPlus");
    GameRegistry.registerTileEntity(TileRefinery.class, "RefineryPlus");
    GameRegistry.registerTileEntity(TilePlacer.class, "PlacerPlus");
    GameRegistry.registerTileEntity(TileBreaker.class, "BreakerPlus");
    GameRegistry.registerTileEntity(TileLaser.class, "LaserPlus");
    WorkbenchRecipe.addRecipe(new ItemStack(blockMarker), 20000,
        new ItemStack(Items.redstone, 300), new ItemStack(Items.dye, 300, 4), new ItemStack(
            Items.gold_ingot, 175), new ItemStack(Items.iron_ingot, 150), new ItemStack(
            Items.glowstone_dust, 50), new ItemStack(Items.ender_pearl, 10));
    WorkbenchRecipe.addRecipe(new ItemStack(blockQuarry), 320000,
        new ItemStack(Items.diamond, 800), new ItemStack(Items.gold_ingot, 800), new ItemStack(
            Items.iron_ingot, 1600), new ItemStack(Items.redstone, 400), new ItemStack(
            Items.ender_pearl, 50), new ItemStack(Items.nether_star, 3));
    WorkbenchRecipe.addRecipe(new ItemStack(blockMover), 320000, new ItemStack(Blocks.obsidian,
        1600), new ItemStack(Items.diamond, 800), new ItemStack(Blocks.anvil, 50), new ItemStack(
        Items.redstone, 1200), new ItemStack(Items.gold_ingot, 200), new ItemStack(
        Items.iron_ingot, 200), new ItemStack(Items.nether_star, 1), new ItemStack(
        Items.ender_pearl, 25));
    WorkbenchRecipe.addRecipe(new ItemStack(blockMiningWell), 160000, new ItemStack(
        Items.iron_ingot, 800), new ItemStack(Items.redstone, 400), new ItemStack(Items.diamond,
        100), new ItemStack(Items.ender_pearl, 50), new ItemStack(Items.nether_star, 1),
        new ItemStack(Items.gold_ingot, 25));
    WorkbenchRecipe.addRecipe(new ItemStack(blockPump), 320000, new ItemStack(Items.iron_ingot,
        1200), new ItemStack(Items.redstone, 1600), new ItemStack(Blocks.glass, 12800),
        new ItemStack(Blocks.cactus, 2000), new ItemStack(Items.gold_ingot, 400), new ItemStack(
            Items.nether_star, 1), new ItemStack(Items.ender_pearl, 10));
    WorkbenchRecipe.addRecipe(new ItemStack(blockRefinery), 640000, new ItemStack(Items.diamond,
        900), new ItemStack(Items.gold_ingot, 600), new ItemStack(Items.iron_ingot, 600),
        new ItemStack(Blocks.glass, 3200), new ItemStack(Items.redstone, 800), new ItemStack(
            Blocks.anvil, 50), new ItemStack(Blocks.obsidian, 600), new ItemStack(
            Items.nether_star, 1), new ItemStack(Items.ender_pearl, 20));
    WorkbenchRecipe.addRecipe(new ItemStack(itemTool, 1, 0), 80000, new ItemStack(Items.gold_ingot,
        400), new ItemStack(Items.iron_ingot, 600), new ItemStack(Blocks.obsidian, 100),
        new ItemStack(Items.diamond, 100), new ItemStack(Items.redstone, 400), new ItemStack(
            Items.dye, 100, 4), new ItemStack(Items.ender_pearl, 3));
    WorkbenchRecipe.addRecipe(new ItemStack(itemTool, 1, 1), 160000, new ItemStack(
        Items.iron_ingot, 400), new ItemStack(Items.book, 1600), new ItemStack(Items.feather, 50),
        new ItemStack(Items.dye, 400), new ItemStack(Items.diamond, 100), new ItemStack(
            Items.redstone, 100), new ItemStack(Items.ender_pearl, 3));
    WorkbenchRecipe.addRecipe(new ItemStack(itemTool, 1, 2), 320000, new ItemStack(
        Items.iron_ingot, 1600), new ItemStack(Items.lava_bucket, 60), new ItemStack(
        Items.water_bucket, 60), new ItemStack(Items.ender_pearl, 3));
    WorkbenchRecipe.addRecipe(new ItemStack(blockBreaker), 320000, new ItemStack(Items.redstone,
        1600), new ItemStack(Items.diamond, 600), new ItemStack(Items.gold_ingot, 800),
        new ItemStack(Items.iron_ingot, 1600), new ItemStack(Items.ender_pearl, 50));
    WorkbenchRecipe.addRecipe(new ItemStack(blockPlacer), 320000, new ItemStack(Items.redstone,
        1600), new ItemStack(Items.diamond, 600), new ItemStack(Items.gold_ingot, 1600),
        new ItemStack(Items.iron_ingot, 800), new ItemStack(Items.ender_pearl, 50));
    WorkbenchRecipe.addRecipe(new ItemStack(blockLaser), 640000, new ItemStack(Items.diamond, 400),
        new ItemStack(Items.redstone, 4800), new ItemStack(Blocks.obsidian, 800), new ItemStack(
            Blocks.glass, 3600), new ItemStack(Items.glowstone_dust, 1600), new ItemStack(
            Items.gold_ingot, 800), new ItemStack(Items.ender_pearl, 5));
    NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());
    proxy.registerTextures();
    PacketHandler.registerStaticHandler(TileMarker.class);
  }

  public static class BlockData {
    public final String name;
    public final int meta;

    public BlockData(final String n, final int m) {
      this.name = n;
      this.meta = m;
    }

    @Override
    public boolean equals(final Object o) {
      if (o instanceof BlockData)
        return this.name.equals(((BlockData) o).name)
            && (this.meta == ((BlockData) o).meta || this.meta == OreDictionary.WILDCARD_VALUE || ((BlockData) o).meta == OreDictionary.WILDCARD_VALUE);
      return false;
    }

    @Override
    public int hashCode() {
      return this.name.hashCode();
    }
  }

  @SideOnly(Side.CLIENT)
  public static String getLocalizedName(final BlockData bd) {
    final StringBuffer sb = new StringBuffer();
    sb.append(bd.name);
    if (bd.meta != OreDictionary.WILDCARD_VALUE) {
      sb.append(":");
      sb.append(bd.meta);
    }
    sb.append("  ");
    sb.append(GameData.getBlockRegistry().getObject(bd.name).getLocalizedName());
    return sb.toString();
  }
}
