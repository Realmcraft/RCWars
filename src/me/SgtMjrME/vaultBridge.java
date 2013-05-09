package me.SgtMjrME;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class vaultBridge {
	public static RCWars plugin;
	public Economy economy = null;
	public boolean foundEconomy = false;
	public String economyName = "";

	public vaultBridge(RCWars instance) {
		plugin = instance;
		initEconomy();
	}

	public final void initEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = plugin.getServer()
				.getServicesManager().getRegistration(Economy.class);
		if (economyProvider != null) {
			economy = ((Economy) economyProvider.getProvider());
		}
		if (economy != null) {
			foundEconomy = true;
			economyName = ((Economy) plugin.getServer().getServicesManager()
					.getRegistration(Economy.class).getProvider()).getName();
			String message = "Hooked in to " + economyName + " via [Vault]!";
			plugin.getLogger().info(message);
		} else {
			plugin.getLogger().warning(
					"No economy plugin found! Economy Options Disabled!");
		}
	}
}