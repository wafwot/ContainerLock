package net.gummycraft.containerLock;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/*
 * a bunch of static routines that check, find and manipulate signs
 * 		A bunch may not be used, I don't care, they are small and I may use them later
 */
public class SignHelper {
	
	// Gets the block this sign is applied to
	static public Block getBlockAttachedTo(Block signAsBlock) {
		if ( signAsBlock == null || (signAsBlock.getType() != Material.WALL_SIGN && signAsBlock.getType() != Material.SIGN_POST) )
			return(null);
		org.bukkit.material.Sign s = (org.bukkit.material.Sign) signAsBlock.getState().getData();
		return( signAsBlock.getRelative( s.getAttachedFace() ));
	}
	
	// Checks if the Block/Sign is a wall sign
	static public boolean isWallSign(Block b) {
		if ( b == null ||  b.getType() != Material.WALL_SIGN )
			return(false);
		return(true);
	}
	static public boolean isWallSign(Sign s) {
		if ( s == null ||  s.getType() != Material.WALL_SIGN )
			return(false);
		return(true);		
	}
	// Checks if the Block is a sign
	static public boolean isSign(Block b) {
		if ( b == null ||  (b.getType() != Material.WALL_SIGN && b.getType() != Material.SIGN_POST) )
			return(false);
		return(true);
	}	
	
	// just a stupid convience method...kinda like the others, i guess
	static private Sign getIfSign(Block b, BlockFace bf) {
		Block bSign = b.getRelative( bf );
		if ( isSign(bSign) )
			return( (Sign) bSign.getState() );
		return(null);
	}	
	// gets all the signs attached to this block
	static public List<Sign> getAttachedSigns(Block b) {
		Sign s;
		List<Sign> signList = new ArrayList<Sign>();
		if ( (s = getIfSign(b, BlockFace.NORTH )) != null )
			signList.add(s);
		if ( (s = getIfSign(b, BlockFace.SOUTH )) != null )
			signList.add(s);
		if ( (s = getIfSign(b, BlockFace.EAST )) != null )
			signList.add(s);
		if ( (s = getIfSign(b, BlockFace.WEST )) != null )
			signList.add(s);
		if ( (s = getIfSign(b, BlockFace.UP )) != null )
			signList.add(s);		
		return(signList);
	}
	
	
	// checks if something is a sign specific to locking containers
	static public boolean isMySign(Block b) {
		if ( !SignHelper.isSign(b) )
			return(false);
		return( isMySign( ((Sign) b.getState()).getLine(0)  ) );
	}
	static public boolean isMySign(Sign s) {
		return( isMySign( s.getLine(0) ) );
	}
	static public boolean isMySign(String line) {
		return( line.endsWith("[Locked]") );
	}

	// magic sign stuff
	static public boolean isHoldingMagicSign(Player p) {
		ItemStack compareWith = makeMagicSign( "" );
		if ( compareWith.isSimilar( p.getItemInHand() ) ) {
			return(true);
		} else if ( p.getItemInHand().isSimilar( makeMagicSign(p.getName()) ) ) {
			return(true);
		}
		return(false);
	}
	static public boolean giveMagicSign(Player p, boolean personal) {
		String dest = "";
		if ( personal )
			dest = p.getName();
		ItemStack is = makeMagicSign(dest);
		Inventory inv = p.getInventory();
		return ( inv.addItem(is).isEmpty() );
	}
	static private ItemStack makeMagicSign(String privateName) {
		ItemStack is = new ItemStack(Material.SIGN, 1);
		is.addUnsafeEnchantment(Enchantment.ARROW_DAMAGE, 1);
		is.addUnsafeEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, 1);
		List<String> loreList = new ArrayList<String>();
		if ( privateName.length() > 1 ) {
			loreList.add(ChatColor.RED + "");
			loreList.add(ChatColor.DARK_AQUA + "only " + privateName + " can use this!");
		}
		loreList.add(" ");
		loreList.add(ChatColor.YELLOW + "Creates a [Private] container");
		loreList.add(" ");
		loreList.add(ChatColor.RED + "Just place on a lockable container");
		loreList.add(ChatColor.RED + "and a lock in your name will be");
		loreList.add(ChatColor.RED + "created. If you have permission");
		loreList.add(ChatColor.RED + "to make locks for other people");
		loreList.add(ChatColor.RED + "place their name on the second");
		loreList.add(ChatColor.RED + "of the sign when placing.");
		if ( privateName.length() > 1 ) {
			loreList.add(ChatColor.RED + "");
			loreList.add(ChatColor.DARK_AQUA + "only " + privateName + " can use this!");
		}
		
		ItemMeta im = is.getItemMeta();
		im.setDisplayName("[Private] Chest");		
		im.setLore( loreList );
		is.setItemMeta(im);
		return(is);
	}
	

	// this isn't sign stuff...but i'm not bringing in my whole BukkitUtil class just for this one routine
	// so I placed it here since it really belongs nowhere
	public static String secondsReadableLong(long total) {
		String st;
		long secs = total;
		long mins = 0;
		long hours = 0;
		long days = 0;
		long weeks = 0;
		long years = 0;
		String secSuff = "s";
		String minSuff = "s";
		String hourSuff = "s";
		String daySuff = "s";
		String weekSuff = "s";
		String yearSuff = "s";
		
		mins = (secs / 60);
		secs -= mins * 60;
		
		hours = (mins / 60);
		mins -= hours * 60;
		
		days = (hours / 24);
		hours -= days * 24;
		
		weeks = (days / 7);
		days -= weeks * 7;
		
		years = (weeks / 52);
		weeks -= years * 52;
		
		if ( secs == 1 )	secSuff = "";
		if ( mins == 1 )	minSuff = "";
		if ( hours == 1 )	hourSuff = "";
		if ( days == 1 )	daySuff = "";
		if ( weeks == 1 )	weekSuff = "";
		if ( years == 1 )   yearSuff = "";
		
		if ( years > 0 ) {
			if ( days > 0 ) {
				st = years + " year" + yearSuff + ", " + weeks + " week" + weekSuff + ", " + days + " day" + daySuff;
			} else if ( weeks > 0 ) {
				st = years + " year" + yearSuff + ", " + weeks + " week" + weekSuff;
			} else {
				st = years + " year" + yearSuff;
			}
			
		} else if ( weeks > 0 ) {
			if ( hours > 0 ) {
				st = weeks + " week" + weekSuff + ", " + days + " day" + daySuff + ", " + hours + " hour" + hourSuff;
			} else if ( days > 0 ) {
				st = weeks + " week" + weekSuff + ", " + days + " day" + daySuff;
			} else {
				st = weeks + " week" + weekSuff;
			}
		} else if ( days > 0 ) {
			if ( mins > 0 ) {
				st = days + " day" + daySuff + ", " + hours + " hour" + hourSuff + ", " + mins + " minute" + minSuff;	
			} else if ( hours > 0 ) {
				st = days + " day" + daySuff + ", " + hours + " hour" + hourSuff;
			} else {
				st = days + " day" + daySuff;
			}
		} else if ( hours > 0 ) {
			if ( secs > 0 ) {
				st = hours + " hour" + hourSuff + ", " + mins + " minute" + minSuff + ", " + secs + " second" + secSuff;
			} else if ( mins > 0 ) {
				st = hours + " hour" + hourSuff + ", " + mins + " minute" + minSuff;
			} else {
				st = hours + " hour" + hourSuff;
			}
		} else if ( mins > 0 ) {
			if ( secs > 0 ) {
				st = mins + " minute" + minSuff + ", " + secs + " second" + secSuff;
			} else {
				st = mins + " minute" + minSuff;
			}
		} else {
			st = secs + " second" + secSuff;
		}
		return(st);
	}
}
