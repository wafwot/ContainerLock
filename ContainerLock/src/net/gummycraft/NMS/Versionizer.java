package net.gummycraft.NMS;

import org.bukkit.Bukkit;

public class Versionizer {
	
	public static Versionized getNMSVersionized() {
		Versionized NMSVersion = null;
		try {
			NMSVersion = net.gummycraft.NMS.Versionizer.getNMS();
		} catch (Exception e) {
			NMSVersion = null;
		}
		return(NMSVersion);
	}
	
	
	public static Versionized getNMS() throws Exception {
		String Ver = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
		Class<?> cls = Class.forName("net.gummycraft.NMS." + Ver );
		return (Versionized) cls.getConstructor().newInstance();
	}

}
