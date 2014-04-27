package net.gummycraft.containerLock;

import java.util.List;

import net.gummycraft.NMS.Versionized;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;


// TODO - when done with changing things, remove all the null checks, they shouldn't need to be there
//			it's just my bad (or is it good) habits coming out
// TODO - version 05 = save whocreated, howcreated
// TODO - with above, when chest destroyed drop the proper mahicsign if used to create lock
// TODO - clean up stuff left over or "made to work" from old version


public class LockedContainer {
	private static Versionized	NMSVersion = null;
	private static Boolean		haveNMSVersion = null;
	private Block 	  			container = null;
	private Block				companionContainer = null;
	private String				namePrefixOnContainer = null;
	private String[]  			usersNameString = new String[6];
	private String[]			usersUUIDString = new String[6];
	private String[]			usersLastAccess = new String[6];
	private Sign				lockedSign = null;
	private String				whoCreated = null;
	private String				howCreated = null;
	
	LockedContainer(Block b) throws IllegalArgumentException {
		if ( SignHelper.isWallSign(b) ) {
			b = SignHelper.getBlockAttachedTo(b);
		}
		if ( !isLockable(b)) {
			throw new IllegalArgumentException("Not a lockable Container");
		}
		container = b;
		companionContainer = getMyCompanion(b);
		scourForSigns(container);
		if ( companionContainer != null) {
			scourForSigns(companionContainer);
		}
		decodeContainerName();
	}

	/*
	 * Specific to the constructor
	 */

	// checks if this Block has a companion (as in a double chest)
	static public final Block getMyCompanion(Block b) {
		Material me = b.getType();
		if ( me == Material.CHEST || me == Material.TRAPPED_CHEST ) {
			if ( b.getRelative(BlockFace.NORTH).getType() == me ) {
				return( b.getRelative(BlockFace.NORTH) );
			} else if ( b.getRelative(BlockFace.EAST).getType() == me ) {
				return( b.getRelative(BlockFace.EAST) );
			} else if ( b.getRelative(BlockFace.SOUTH).getType() == me ) {
				return( b.getRelative(BlockFace.SOUTH) );
			} else if ( b.getRelative(BlockFace.WEST).getType() == me ) {
				return( b.getRelative(BlockFace.WEST) );
			}
		}
		return(null);
	}
	// gets the [Locked] sign that should be attached to this block
	private final void scourForSigns(Block b) {
		List<Sign> signs = SignHelper.getAttachedSigns(b);
		for ( Sign s : signs ) { 
			if ( SignHelper.isMySign(s) ) {
				lockedSign = s;
			}
		}
	}
	// header:04:origname:uuid:name:last:
	// header:05:origname:how:who:uuid:name:last:---5 more users
	private final void decodeContainerName() {
		String thisName = ( (InventoryHolder) container.getState()).getInventory().getName();
		if ( thisName.length() < invPrefixLen+5 || !thisName.substring(invPrefixLen, invPrefixLen+4).equals(":04:"))
			thisName = convertToMostRecent(thisName);
		
		String[] tk = thisName.split("[:]");
		for (int i=0; i < 6; i++) {
			usersUUIDString[i] = tk[(i*3)+3];
			usersNameString[i] = tk[(i*3)+4];
			usersLastAccess[i] = tk[(i*3)+5];
			
			//Bukkit.getLogger().info(usersUUIDString[i] + " " + usersNameString[i] + " " + usersLastAccess[i]);
		}
		namePrefixOnContainer = tk[0] + ":" + tk[1] + ":" + tk[2];
	}
	public final void writeOwnerString() {
		StringBuilder a = new StringBuilder(namePrefixOnContainer);
		for (int i=0; i < 6; i++) {
			a.append(":");
			a.append(usersUUIDString[i]);
			a.append(":");
			a.append(usersNameString[i]);
			a.append(":");
			a.append(usersLastAccess[i]);
		}
		writeContainerName( a.toString() );
	}
	private void writeContainerName(String name) {
		if ( isUpdateable() ) {
			NMSVersion.renamePlacedContainer(container, name);
			if ( companionContainer != null )
				NMSVersion.renamePlacedContainer(container, name);
		}
	}
	
	public boolean makeOwner(String name) {
		usersNameString[0] = name;
		usersUUIDString[0] = makeTempUUIDString(name);		
		writeOwnerString();
		return(true);
	}
	
	public boolean addUser(String name) {
		if ( name.equalsIgnoreCase( usersNameString[0] ))
			return(false);
		int slot = getNameSlot(name);
		if ( slot != -1 )
			return(false);
		slot = getEmptySlot();
		if ( slot != -1 ) {
			usersNameString[slot] = name;
			usersUUIDString[slot] = makeTempUUIDString(name);
			usersLastAccess[slot] = "0";
			writeOwnerString();
			return(true);
		}
		return(false);
	}
	public boolean removeUser(String name) {
		if ( name.equalsIgnoreCase( usersNameString[0] ))
			return(false);
		int slot = getNameSlot(name);
		if ( slot == -1)
			return(false);
		usersNameString[slot] = "";
		usersUUIDString[slot] = unusedUUID;
		usersLastAccess[slot] = "0";
		writeOwnerString();
		return(true);
	}
	
	private int getEmptySlot() {
		for (int i=1; i < 6; i++) {
			if ( usersUUIDString[i].equals( unusedUUID ))
				return(i);
		}
		return(-1);
	}
	
	public void showInfo(Player p) {
		//Bukkit.getLogger().info("[" + ( (InventoryHolder) container.getState()).getInventory().getName() + "]");
		for (int i=0; i < 6; i++) {
			if ( !usersUUIDString[i].equals(unusedUUID)) {
				String color = ""+ChatColor.GREEN;
				String access = " never";
				if ( usersUUIDString[i].startsWith("zz")) {
					color = "" + ChatColor.RED;
				} else {
					Long apple = (System.currentTimeMillis()/1000) - Long.parseLong( usersLastAccess[i] );
					access = SignHelper.secondsReadableLong(apple) + " ago";
				}
				p.sendMessage(color+usersNameString[i] + " Last Access " + access );				
			}
		}
	}
	
	
	// checks that the Player can access this container...or if is the owner
	//   if there are no owners (as in a "new" LockedContainer it will just return true)
	public boolean isValidUser(Player p, boolean ownerOnly) {
		String real = p.getUniqueId().toString();
		String name = p.getName();		

		boolean unowned = true;
		for (int i=0; i < 6; i++) {
			if ( !usersUUIDString[i].startsWith("xx")) {
				unowned = false;
			}
		}
		if ( unowned )
			return(true);
		
		int slotNumber = -1;
		if ( (slotNumber = getUUIDSlot(real, ownerOnly)) == -1 ) {
			String fake = makeTempUUIDString( name );
			if ( (slotNumber = getUUIDSlot(fake, ownerOnly)) == -1 ) {
				return(false);
			}
			usersUUIDString[slotNumber] = real;
		}
		usersNameString[slotNumber] = p.getName();
		usersLastAccess[slotNumber] = String.valueOf(System.currentTimeMillis() / 1000);
		writeOwnerString();
		if ( lockedSign != null )
			lockedSign.setLine(2, String.valueOf( System.currentTimeMillis() / 1000));
		return(true);
	}
	
	// Gets the slot number the given UUIDString is in
	private int getUUIDSlot(String uuidString, boolean ownerOnly) {
		for (int i=0; i < (ownerOnly ? 1 : 6); i++) {
			if ( usersUUIDString[i].equals( uuidString ) )
				return(i);
		}
		return(-1);
	}
	private int getNameSlot(String name) {
		for (int i=0; i < 6; i++) {
			if ( usersNameString[i].equalsIgnoreCase( name ) )
				return(i);
		}
		return(-1);		
	}



	
	// just checks if a block is locked
	static public boolean isLocked(Block b) {
		if ( !isLockable(b))
			return(false);
		String ownerUUIDString = ( (InventoryHolder) b.getState()).getInventory().getName();
		return( ownerUUIDString.startsWith(invPrefix) );
	}
		
	// Checks if the given block is lockable
	static public boolean isLockable(Block b) {
		if ( b == null )
			return(false);
		if ( !(b.getState() instanceof InventoryHolder) )
			return(false);
		switch ( b.getType() ) {
			case CHEST:
			case TRAPPED_CHEST:
			case FURNACE:
			case ENCHANTMENT_TABLE:
			case HOPPER:
			case DISPENSER:
			case DROPPER:
			case BREWING_STAND:
				return( true );
			default:
				return(false);	
		}
	}

	// Unlocks a chest
	public void unlockContainer() {
		writeContainerName("");
		if ( lockedSign != null ) {
			lockedSign.getBlock().setType(Material.AIR);
		}
	}
	
	
	static private final String invPrefix = ChatColor.RED + "Protected by ContainerLock    " + ChatColor.RESET;
	static private final String invVersion = ":04:";
	static private final int invPrefixLen = invPrefix.length();
	static private final String unusedUUID = "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx";
	static private final String unknownUUID = "zzzzzzzz----------------------------";

	
	public static String convertToMostRecent(String oldName) {
		// just make a new one right now since I erased everything with ver 01 02 03...I think
		StringBuilder a = new StringBuilder(invPrefix + invVersion);
		if ( oldName.length() < 25 )
			a.append(oldName);
		else
			a.append("chest");
		for (int i=0; i < 6; i++) {
			a.append(":");
			a.append(unusedUUID);
			a.append(":none:0");
		}
		return(a.toString() );
	}
	
	public static String makeTempUUIDString(String name) {
		if ( name.length() > 15 ) {
			name = name.substring(0,15);
		}
		String apple = unknownUUID.substring(0, unknownUUID.length()-name.length()) + name.toLowerCase();
		return(apple);
	}
	
	
	// Check to see if we update the names of the placed items...this is NMS dependent
	public static boolean isUpdateable() {
		if ( haveNMSVersion == null ) {
			NMSVersion = net.gummycraft.NMS.Versionizer.getNMSVersionized();
			haveNMSVersion = false;
			if ( NMSVersion == null ) {
				Bukkit.getLogger().severe("ContainerLock can not find a suppoerted NMS Version");
				Bukkit.getLogger().severe("Prior set Locks should work, new locks are disabled");				
			} else {
				// just in case something real bad happens...we will only cause Exception once
				// the text will fail and haveNMSVersion will remain false until reloaded
				NMSVersion.test();
				haveNMSVersion = true;
			}
		}
		return(haveNMSVersion);
	}
}
