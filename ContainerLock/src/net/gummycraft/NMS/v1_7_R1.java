package net.gummycraft.NMS;

import java.lang.reflect.Field;

import net.minecraft.server.v1_7_R1.TileEntityBeacon;
import net.minecraft.server.v1_7_R1.TileEntityBrewingStand;
import net.minecraft.server.v1_7_R1.TileEntityChest;
import net.minecraft.server.v1_7_R1.TileEntityDispenser;
import net.minecraft.server.v1_7_R1.TileEntityDropper;
import net.minecraft.server.v1_7_R1.TileEntityFurnace;
import net.minecraft.server.v1_7_R1.TileEntityHopper;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.craftbukkit.v1_7_R1.block.CraftBeacon;
import org.bukkit.craftbukkit.v1_7_R1.block.CraftBrewingStand;
import org.bukkit.craftbukkit.v1_7_R1.block.CraftChest;
import org.bukkit.craftbukkit.v1_7_R1.block.CraftDispenser;
import org.bukkit.craftbukkit.v1_7_R1.block.CraftDropper;
import org.bukkit.craftbukkit.v1_7_R1.block.CraftFurnace;
import org.bukkit.craftbukkit.v1_7_R1.block.CraftHopper;

public class v1_7_R1 implements Versionized {

	@Override
	public void test() {
		Bukkit.getLogger().info("Successfully integrated with v1_7_R1");
	}

	@Override
	public void renamePlacedContainer(Block block, String name) {
		// only thing that can be done with way that I'm not is enchantment tables
		// just wish I would do anvils, but for some fucked up reason they don't have an inventory?
		BlockState bs = block.getState();
		if ( bs instanceof CraftChest ) {
			placedChestRename( bs, name );
		} else if ( bs instanceof CraftFurnace ) {
			// TileEntityFurnace
			placedFurnaceRename( bs, name );
		} else if ( bs instanceof CraftHopper ) {
			// TileEntityHopper
			placedHopperRename( bs, name );
		} else if ( bs instanceof CraftDropper ) {
			// TileEntityDropper
			placedDropperRename( bs, name );
		} else if ( bs instanceof CraftDispenser ) {
			// TileEntityDispenser
			placedDispenserRename( bs, name );
		} else if ( bs instanceof CraftBrewingStand ) {
			// TileEntityBrewingStand
			placedBrewingRename( bs, name );
		//} else if ( bs instanceof CraftBeacon ) {
			// TileEntityBeacon
			//placedBeaconRename( bs, name );
		}
		
	}
	
	
	// all should be able to follow the same as what I always did for chests
	//   cast BlockState to the CraftChest
	//   get the field that holds the tileEntity
	//   make field accessible
	//   get the tileEntity
	//   call the rename for that
	
	public static void placedChestRename(BlockState bs, String name) {
		CraftChest chest = (CraftChest) bs;
		try {
			Field chestField = chest.getClass().getDeclaredField("chest"); 
			chestField.setAccessible(true); 
			TileEntityChest tileChest = ((TileEntityChest) chestField.get(chest));
			tileChest.a(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void placedFurnaceRename(BlockState bs, String name) {
		CraftFurnace furn = (CraftFurnace) bs;
		try {
			Field furnField = furn.getClass().getDeclaredField("furnace");
			furnField.setAccessible(true);
			TileEntityFurnace tileFurnace = ((TileEntityFurnace) furnField.get(furn));
			tileFurnace.a(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
		
	public static void placedHopperRename(BlockState bs, String name) {
		CraftHopper craftContainer = (CraftHopper) bs;
		try {
			Field containerTileField = craftContainer.getClass().getDeclaredField("hopper"); 
			containerTileField.setAccessible(true); 
			TileEntityHopper containerTile = ((TileEntityHopper) containerTileField.get(craftContainer));
			containerTile.a(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void placedDropperRename(BlockState bs, String name) {
		CraftDropper craftContainer = (CraftDropper) bs;
		try {
			Field containerTileField = craftContainer.getClass().getDeclaredField("dropper"); 
			containerTileField.setAccessible(true); 
			TileEntityDropper containerTile = ((TileEntityDropper) containerTileField.get(craftContainer));
			containerTile.a(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	
	public static void placedDispenserRename(BlockState bs, String name) {
		CraftDispenser craftContainer = (CraftDispenser) bs;
		try {
			Field containerTileField = craftContainer.getClass().getDeclaredField("dispenser"); 
			containerTileField.setAccessible(true); 
			TileEntityDispenser containerTile = ((TileEntityDispenser) containerTileField.get(craftContainer));
			containerTile.a(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void placedBrewingRename(BlockState bs, String name) {
		CraftBrewingStand craftContainer = (CraftBrewingStand) bs;
		try {
			Field containerTileField = craftContainer.getClass().getDeclaredField("brewingStand"); 
			containerTileField.setAccessible(true); 
			TileEntityBrewingStand containerTile = ((TileEntityBrewingStand) containerTileField.get(craftContainer));
			containerTile.a(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void placedBeaconRename(BlockState bs, String name) {
		CraftBeacon craftContainer = (CraftBeacon) bs;
		try {
			Field containerTileField = craftContainer.getClass().getDeclaredField("beacon"); 
			containerTileField.setAccessible(true); 
			TileEntityBeacon containerTile = ((TileEntityBeacon) containerTileField.get(craftContainer));
			containerTile.a(name);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}



}
