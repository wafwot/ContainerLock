package net.gummycraft.containerLock;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.BlockIterator;

public class LocksMain extends JavaPlugin {
	static boolean haveNMSVersion = true;
	
	@Override
    public void onEnable() {
		new LocksListener(this);
	}
	
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		// /lock +player -player
		// /lock magicsign player
		// /lock magicsign player locked
		// /lock versions		
		if ( args.length < 1 )
			return(false);
		Player p;
		if ( args[0].equalsIgnoreCase("version")) {
			sender.sendMessage("ContainerLock by wafwot, Version " + this.getDescription().getVersion() );
			sender.sendMessage("Running server " + Bukkit.getVersion() );
		} else if ( args[0].equalsIgnoreCase("magicsign") && sender.isOp() == true ) {
			if ( args.length < 2 ) {
				sender.sendMessage("syntax:  /lock magicsign <player> - give a sign anyone can use");
				sender.sendMessage("syntax:  /lock magicsign <player> locked - gives a sign locked to the user");
			} else if ( (p=Bukkit.getPlayer( args[1] )) == null ) {
				sender.sendMessage(ChatColor.RED + "Can only give signs to players online");
			} else {
				boolean ret;
				if ( args.length > 2 && args[2].equalsIgnoreCase("locked") )
					ret = SignHelper.giveMagicSign(p, true);
				else
					ret = SignHelper.giveMagicSign(p, false);
				if ( ret ) {
					sender.sendMessage(ChatColor.GREEN + "MagicSign has been given to " + p.getName() );
				} else {
					sender.sendMessage(ChatColor.RED + "FAILED - could not give MagicSign to " + p.getName() );
				}
			}
		} else {
			if ( !(sender instanceof Player) ) {
				sender.sendMessage("Console can not add or remove players from locked containers");
				return(true);
			} else if ( LockedContainer.isUpdateable() == false ) {
				sender.sendMessage(ChatColor.RED + "Incompatible versions. Locks can not be created.");
				sender.sendMessage(ChatColor.RED + "Current locks will continue to operate.");
				return(true);
			}
			p = (Player) sender;
			BlockIterator it = new BlockIterator(p, 12);
			Block b = it.next();
			while( it.hasNext() ) {
				b = it.next();
				if ( b.getType() != Material.AIR )
					break;
			}
			if ( SignHelper.isMySign(b) ) {
				b = SignHelper.getBlockAttachedTo(b);
			}
			if ( !LockedContainer.isLocked(b)) {
				sender.sendMessage(ChatColor.RED + "You can not modify users for that block");
				return(true);
			}				
			LockedContainer lc = new LockedContainer(b);
			if ( !lc.isValidUser(p,  true) && !p.hasPermission("some permission") && !p.isOp() ) {
				p.sendMessage(ChatColor.RED + "You can't change users for someone else's container");
				return(true);
			}
			for (int i=0; i < args.length; i++) {
				if ( args[i].length() > 2 && args[i].startsWith("+")) {
					if ( lc.addUser( args[i].substring(1) ) ) {
						p.sendMessage(ChatColor.GREEN + "User " + args[i].substring(1) + " added");
					} else {
						p.sendMessage(ChatColor.RED + "Skipped add for " + args[i].substring(1));
					}
				} else if ( args[i].length() > 2 && args[i].startsWith("-")) {
					if ( lc.removeUser( args[i].substring(1) ) ) {
						p.sendMessage(ChatColor.GREEN + "User " + args[i].substring(1) + " removed");
					} else {
						p.sendMessage(ChatColor.RED + "Skipped removal for " + args[i].substring(1));
					}
				} else {
					p.sendMessage(ChatColor.RED + "Unknown syntax " + args[i] );
				}
			}
		}
		return(true);
	}
	
	
}
