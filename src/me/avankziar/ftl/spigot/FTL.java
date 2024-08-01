package me.avankziar.ftl.spigot;

import java.io.IOException;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import me.avankziar.ftl.spigot.listener.ServerListener;
import me.avankziar.ftl.spigot.metric.Metrics;
import me.avankziar.ifh.spigot.economy.Economy;

public class FTL extends JavaPlugin
{
	public static Logger logger;
	private static FTL plugin;
	public static String pluginname = "FrugalTabList";
	
	private Economy ecoConsumer;
	private net.milkbowl.vault.economy.Economy vEco;
	
	public static String TOVELO = "ftl:tovelo";
	public static String TOBACKEND = "ftl:tobackend";
	
	@SuppressWarnings("deprecation")
	public void onEnable()
	{
		plugin = this;
		logger = getLogger();
		
		//https://patorjk.com/software/taag/#p=display&f=ANSI%20Shadow&t=FTL
		logger.info(" ███████╗████████╗██╗      | API-Version: "+plugin.getDescription().getAPIVersion());
		logger.info(" ██╔════╝╚══██╔══╝██║      | Author: "+plugin.getDescription().getAuthors().toString());
		logger.info(" █████╗     ██║   ██║      | Plugin Website: "+plugin.getDescription().getWebsite());
		logger.info(" ██╔══╝     ██║   ██║      | Depend Plugins: "+plugin.getDescription().getDepend().toString());
		logger.info(" ██║        ██║   ███████╗ | SoftDepend Plugins: "+plugin.getDescription().getSoftDepend().toString());
		logger.info(" ╚═╝        ╚═╝   ╚══════╝ | LoadBefore: "+plugin.getDescription().getLoadBefore().toString());		
		
		setupListeners();
		setupBstats();
	}
	
	public void onDisable()
	{
		Bukkit.getScheduler().cancelTasks(this);
		HandlerList.unregisterAll(this);
		logger = null;
		getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
		getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
		if(getServer().getPluginManager().isPluginEnabled("InterfaceHub")) 
	    {
	    	getServer().getServicesManager().unregisterAll(plugin);
	    }
		logger.info(pluginname + " is disabled!");
	}

	public static FTL getPlugin()
	{
		return plugin;
	}
	
	public static void shutdown()
	{
		FTL.getPlugin().onDisable();
	}
	
	public void setupListeners()
	{
		getServer().getMessenger().registerIncomingPluginChannel(plugin, TOBACKEND, new ServerListener(plugin));
		getServer().getMessenger().registerOutgoingPluginChannel(plugin, TOVELO);
	}
	
	public boolean reload() throws IOException
	{
		return true;
	}
	
	public boolean existHook(String externPluginName)
	{
		if(plugin.getServer().getPluginManager().getPlugin(externPluginName) == null)
		{
			return false;
		}
		logger.info(pluginname+" hook with "+externPluginName);
		return true;
	}
	
	public void setupIFHConsumer()
	{
		setupIFHEconomy();
	}
	
	private void setupIFHEconomy()
    {
		if(!plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub")
				&& !plugin.getServer().getPluginManager().isPluginEnabled("Vault")) 
	    {
			logger.severe("Plugin InterfaceHub or Vault are missing!");
			logger.severe("Disable "+pluginname+"!");
			FTL.shutdown();
	    	return;
	    }
		if(plugin.getServer().getPluginManager().isPluginEnabled("InterfaceHub"))
		{
			RegisteredServiceProvider<me.avankziar.ifh.spigot.economy.Economy> rsp = 
	                getServer().getServicesManager().getRegistration(Economy.class);
			if (rsp == null) 
			{
				logger.severe("A economy plugin which supported InterfaceHub or Vault is missing!");
	            return;
			}
			ecoConsumer = rsp.getProvider();
			logger.info(pluginname + " detected InterfaceHub >>> Economy.class is consumed!");
		}
		if(plugin.getServer().getPluginManager().isPluginEnabled("Vault"))
		{
			RegisteredServiceProvider<net.milkbowl.vault.economy.Economy> rsp = getServer()
	        		.getServicesManager()
	        		.getRegistration(net.milkbowl.vault.economy.Economy.class);
	        if (rsp == null) 
	        {
	        	logger.severe("A economy plugin which supported Vault is missing!");
	            return;
	        }
	        vEco = rsp.getProvider();
	        logger.info(pluginname + " detected Vault >>> Economy.class is consumed!");
		}
        return;
    }
	
	public Economy getIFHEco()
	{
		return this.ecoConsumer;
	}
	
	public net.milkbowl.vault.economy.Economy getVaultEco()
	{
		return this.vEco;
	}
	
	public void setupBstats()
	{
		int pluginId = 22842;
        new Metrics(this, pluginId);
	}
}