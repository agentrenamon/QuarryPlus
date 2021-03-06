package com.yogpc.ip;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.MobSpawnerBaseLogic;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityMobSpawner;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.ForgeDirection;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;
import com.yogpc.mc_lib.PacketHandler;
import com.yogpc.mc_lib.YogpstopLib;
import com.yogpc.mc_lib.YogpstopPacket;

import cpw.mods.fml.common.FMLCommonHandler;

public class BlockController extends Block {
  public BlockController() {
    super(Material.circuits);
    setBlockName("spawnercontroller");
    setBlockTextureName("yogpstop_qp:spawnctl");
    setHardness(1.0f);
    setCreativeTab(CreativeTabs.tabRedstone);
  }

  private static final MobSpawnerBaseLogic getSpawner(final World w, final int x, final int y,
      final int z) {
    for (final ForgeDirection d : ForgeDirection.VALID_DIRECTIONS) {
      final TileEntity t = w.getTileEntity(x + d.offsetX, y + d.offsetY, z + d.offsetZ);
      if (!(t instanceof TileEntityMobSpawner))
        continue;
      final MobSpawnerBaseLogic l = ((TileEntityMobSpawner) t).func_145881_a();
      if (l == null)
        continue;
      return l;
    }
    return null;
  }

  @Override
  public boolean onBlockActivated(final World w, final int x, final int y, final int z,
      final EntityPlayer p, final int s, final float hx, final float hy, final float hz) {
    if (!w.isRemote)
      try {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        final DataOutputStream dos = new DataOutputStream(bos);
        dos.writeByte(1);
        for (final Object e : EntityList.stringToClassMapping.entrySet()) {
          final Map.Entry<?, ?> ve = (Entry<?, ?>) e;
          final Class<?> c = (Class<?>) ve.getValue();
          if (c == null || Modifier.isAbstract(c.getModifiers())
              || YogpstopLib.spawnerBlacklist.contains(ve.getKey()))
            continue;
          dos.writeByte(0);
          dos.writeUTF((String) ve.getKey());
        }
        dos.writeByte(-1);
        dos.writeInt(w.provider.dimensionId);
        dos.writeInt(x);
        dos.writeInt(y);
        dos.writeInt(z);
        PacketHandler.sendPacketToPlayer(new YogpstopPacket(bos.toByteArray(),
            BlockController.class), p);
      } catch (final IOException c) {
        throw new RuntimeException(c);
      }
    return true;
  }

  @PacketHandler.Handler
  public static void openGui(final byte[] ba) {
    final ByteArrayDataInput data = ByteStreams.newDataInput(ba);
    switch (data.readByte()) {
      case 0:
        final MobSpawnerBaseLogic l =
            getSpawner(DimensionManager.getWorld(data.readInt()), data.readInt(), data.readInt(),
                data.readInt());
        if (l == null)
          return;
        final String tmp = data.readUTF();
        if (!YogpstopLib.spawnerBlacklist.contains(tmp))
          l.setEntityName(tmp);
        l.getSpawnerWorld().getTileEntity(l.getSpawnerX(), l.getSpawnerY(), l.getSpawnerZ())
            .markDirty();
        l.getSpawnerWorld().markBlockForUpdate(l.getSpawnerX(), l.getSpawnerY(), l.getSpawnerZ());
        return;
      case 1:
        final List<String> s = new ArrayList<String>();
        while (data.readByte() >= 0)
          s.add(data.readUTF());
        FMLCommonHandler.instance().showGuiScreen(
            YogpstopLib.proxy.getGuiController(data.readInt(), data.readInt(), data.readInt(),
                data.readInt(), s));
    }
  }

  @Override
  public void onNeighborBlockChange(final World w, final int x, final int y, final int z,
      final Block b) {
    if (w.isRemote)
      return;
    final boolean r =
        w.isBlockIndirectlyGettingPowered(x, y, z)
            || w.isBlockIndirectlyGettingPowered(x, y + 1, z);
    final int m = w.getBlockMetadata(x, y, z);
    if (r && m == 0) {
      final MobSpawnerBaseLogic l = getSpawner(w, x, y, z);
      if (l != null) {
        l.spawnDelay = 0;
        final EntityPlayer p = FakePlayerFactory.getMinecraft((WorldServer) w);
        p.setWorld(l.getSpawnerWorld());
        p.setPosition(l.getSpawnerX(), l.getSpawnerY(), l.getSpawnerZ());
        l.getSpawnerWorld().playerEntities.add(p);
        l.updateSpawner();
        l.getSpawnerWorld().playerEntities.remove(p);
      }
      w.setBlockMetadataWithNotify(x, y, z, 1, 4);
    } else if (!r && m == 1)
      w.setBlockMetadataWithNotify(x, y, z, 0, 4);
  }

}
