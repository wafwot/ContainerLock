package net.gummycraft.containerLock;

import java.util.ArrayList;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;


// *lock.create			-- lets user create unlimited locked chests
// *lock.create.others		-- create chests other's own. Add/remove users from other people's chests
// *lock.add         		-- lets user add in additional users to their own chest
// *lock.add.others        -- yeah, you get it by now
// *lock.open				-- lets user open a locked chest they have access to
// *lock.open.others		-- lets user open anyone's chest
// *lock.info				-- lets user list full information on a chest they have access to
// *lock.info.others		-- get full information on anyone's chest
// *lock.destroy 			-- lets user destroy chests they own (not user of)
// *lock.destroy.others	-- destroy anyone's chest
// *lock.anywhere			-- lets user place a [Locked] chest sign even where it doesn't work

// Any user that owns a chest and has lock.add can get basic information about the locked container

public class LocksListener implements Listener {
	static boolean failSilent = false;
	
	LocksListener(JavaPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}


	/*
	 * Player is molesting something...see if it's something we care about!
	 *
	 * Everything here must be aware of versions, try not to cause issues with servers being updated
	 * and the plugin not. Just stop certain activities but keep chest protections....
	 * 
	 */
	@EventHandler(priority = EventPriority.NORMAL)
	public void somethingMolested(PlayerInteractEvent event) {
		if ( event.getAction() == Action.RIGHT_CLICK_BLOCK ) {
			Block b = event.getClickedBlock();
			if ( SignHelper.isMySign(b)) {
				LockedContainer lc;
				try {
					lc = new LockedContainer(b);
				} catch ( IllegalArgumentException iae ) {
					return;
				}					
				Player p = event.getPlayer();				
				if ( p.isOp() || p.hasPermission("lock.info.others") || ( p.hasPermission("lock.info") && lc.isValidUser(p,  false)) ) {
					p.sendMessage(ChatColor.GOLD + "Locked Container user list");
					lc.showInfo(p);
					return;				
				}
			} else if ( LockedContainer.isLocked(b)) {
				LockedContainer lc = new LockedContainer(b);
				Player p = event.getPlayer();				
				if ( !( p.hasPermission("lock.open") && lc.isValidUser(p,  false)) && !p.isOp() && !p.hasPermission("lock.open.others") ) {
					if ( !failSilent )
						p.sendMessage(ChatColor.RED + "That is locked!");
					event.setCancelled(true);
					return;				
				}							
			}
		}
	}
	
	@EventHandler
	public void BrokeSomething(BlockBreakEvent event) {
		Block b = event.getBlock();
		if ( SignHelper.isMySign(b) || LockedContainer.isLocked(b) ) {
			LockedContainer lc;
			try {
				lc = new LockedContainer(b);
			} catch ( IllegalArgumentException iae ) {
				return;
			}
			Player p = event.getPlayer();			
			if ( !( p.hasPermission("lock.destroy") && lc.isValidUser(p,  true)) && !p.isOp() && !p.hasPermission("lock.destroy.others") ) {			
				event.setCancelled(true);
			} else {
				lc.unlockContainer();
			}
		}
	}
	
	// in case a chest gets doubled
	// in case a hopper is placed under a locked container
	@EventHandler
	public void PlaceChestNextToChest(BlockPlaceEvent event) {
		Block b = event.getBlockPlaced();
		Material m = b.getType();
		if ( m == Material.CHEST || m == Material.TRAPPED_CHEST ) {
			Block nextToBlock;
			if ( (nextToBlock=LockedContainer.getMyCompanion(b)) != null) {
				if ( LockedContainer.isLocked(nextToBlock)) {
					LockedContainer lc = new LockedContainer( nextToBlock );
					Player p = event.getPlayer();					
					if ( !( p.hasPermission("lock.create") && lc.isValidUser(p,  true)) && !p.isOp() && !p.hasPermission("lock.create.others") ) {			
						p.sendMessage(ChatColor.RED + "You can not place that there");
						event.setCancelled(true);
					}
				}
			}
		} else if ( m == Material.HOPPER ) {
			Block upBlock = b.getRelative(BlockFace.UP);
			if ( LockedContainer.isLocked(upBlock)) {
				LockedContainer lc = new LockedContainer( upBlock );
				Player p = event.getPlayer();
				if ( !( p.hasPermission("lock.create") && lc.isValidUser(p,  true)) && !p.isOp() && !p.hasPermission("lock.create.others") ) {
					p.sendMessage(ChatColor.RED + "You can not place that there");
					event.setCancelled(true);
				}
			}			
		}
	}
	
	// don't let explosions hurt our signs or containers
	@EventHandler
	public void ExplosionIsBreakingMyLocks(EntityExplodeEvent event) {		 
        for (Block block : new ArrayList<Block>(event.blockList())) {
            if ( LockedContainer.isLocked(block) || SignHelper.isMySign(block)) {
                event.blockList().remove(block);
            }
        }
	}
	
	
	// watch for signs being placed on containers, if it's a lock, be sure to update container name as well
	@EventHandler
	public void SignIsChanged(SignChangeEvent event) {
		boolean magicSign = false;
		Player p = event.getPlayer();
		Block b = event.getBlock();

		// I don't think this can ever happen...but weirder things have happened with Minecraft/Bukkit/spigot
		if ( p == null || b == null )
			return;
		
		
		if ( SignHelper.isHoldingMagicSign(p)) {
			// if magic sign is "tied" to a user ... be sure this is the correct user
			magicSign = true;
			event.setLine(0,  "[Locked]");
		}
		
		
		// If it's not a sign with our text in it, just leave
		if ( !SignHelper.isSign(b) || !SignHelper.isMySign( event.getLine(0) ) )
			return;

		// if player doesn't have create permission...just leave
		if ( !magicSign && !p.isOp() && !p.hasPermission("lock.create") && !p.hasPermission("lock.create.others")) {
			p.sendMessage(ChatColor.RED + "You can't make locked containers");
			killSignPlacement(b, event);
			return;
		}
		
		// If it's a sign post...isOp can keep it...all others..remove the sign
		if ( b.getType() == Material.SIGN_POST || !LockedContainer.isLockable( SignHelper.getBlockAttachedTo(b) ) ) {			
			if ( !p.isOp() && !p.hasPermission("lock.anywhere")) {
				p.sendMessage(ChatColor.RED + "You can't do that here");
				killSignPlacement(b, event);
			}
        	return;			
		}
		
		// If I can't alter the name of the container do not let this action continue!
		// this means this plugin does not have correct NMS interface for version of server running
		if ( !LockedContainer.isUpdateable() ) {
			p.sendMessage(ChatColor.RED + "Incompatible versions. Locks can not be created.");
			p.sendMessage(ChatColor.RED + "Current locks will continue to operate.");
			killSignPlacement(b, event);
			return;
		}
		
		// if this is already owned, only the owner can continue
		LockedContainer lc = new LockedContainer(b);
		if ( !lc.isValidUser(p,  true)) {
			p.sendMessage(ChatColor.RED + "You can't place signs on someone else's chest");
			killSignPlacement(b, event);
			return;
		}
		

		String line1 = event.getLine(1);
		if ( line1.length() < 2 || !p.hasPermission("lock.create.others") ) {
			lc.makeOwner( p.getName() );
			event.setLine( 1, p.getName() );
		} else {
			lc.makeOwner( line1 );
			event.setLine( 1, line1 );			
		}
		event.setLine(2, "");
		event.setLine(3, "");
	}
	
	private void killSignPlacement(Block b, SignChangeEvent event ) {
		event.setCancelled(true);
		//ItemStack is = new ItemStack(Material.SIGN, 1);
		ItemStack is = event.getPlayer().getItemInHand().clone();
		is.setAmount(1);
		b.getWorld().dropItemNaturally(b.getLocation(), is);
		b.setType( Material.AIR );		
	}
}
