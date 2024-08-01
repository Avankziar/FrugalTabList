package me.avankziar.ftl.velocity;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.google.inject.Inject;
import com.velocitypowered.api.event.EventManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Dependency;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.PluginContainer;
import com.velocitypowered.api.plugin.PluginDescription;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;

import me.avankziar.ftl.general.database.YamlHandler;
import me.avankziar.ftl.general.database.YamlManager;
import me.avankziar.ftl.velocity.assistance.BackgroundTask;
import me.avankziar.ftl.velocity.handler.TabListHandler;
import me.avankziar.ftl.velocity.listener.JoinLeaveSwitchListener;
import me.avankziar.ftl.velocity.listener.ServerListener;
import me.avankziar.ftl.velocity.metric.Metrics;
import me.avankziar.ifh.velocity.IFH;
import me.avankziar.ifh.velocity.administration.Administration;
import me.avankziar.ifh.velocity.plugin.RegisteredServiceProvider;

@Plugin(
	id = "frugaltablist",
	name = "FrugalTablist",
	version = "1-0-0",
	url = "https://www.spigotmc.org/resources/authors/avankziar.332028/",
	dependencies = {
			@Dependency(id = "interfacehub")
	},
	description = "A Velocity Tablist",
	authors = {"Avankziar"}
)
public class FTL
{
	private static FTL plugin;
    private final ProxyServer server;
    private Logger logger = null;
    private Path dataDirectory;
    public String pluginname = "FrugalTabList";
    private final Metrics.Factory metricsFactory;
    private YamlHandler yamlHandler;
    private YamlManager yamlManager;
    
	private static Administration administrationConsumer;
	
	public static MinecraftChannelIdentifier TOVELO = MinecraftChannelIdentifier.from("ftl:tovelo");
	public static MinecraftChannelIdentifier TOBACKEND = MinecraftChannelIdentifier.from("ftl:tobackend");
    
    @Inject
    public FTL(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory, Metrics.Factory metricsFactory) 
    {
    	FTL.plugin = this;
        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        this.metricsFactory = metricsFactory;
    }
    
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) 
    {
    	PluginDescription pd = server.getPluginManager().getPlugin(pluginname.toLowerCase()).get().getDescription();
        List<String> dependencies = new ArrayList<>();
        pd.getDependencies().stream().allMatch(x -> dependencies.add(x.getId()));
        //https://patorjk.com/software/taag/#p=display&f=ANSI%20Shadow&t=FTL
		logger.info(" ███████╗████████╗██╗      | Id: "+pd.getId());
		logger.info(" ██╔════╝╚══██╔══╝██║      | Version: "+pd.getVersion().get());
		logger.info(" █████╗     ██║   ██║      | Author: ["+String.join(", ", pd.getAuthors())+"]");
		logger.info(" ██╔══╝     ██║   ██║      | Description: "+(pd.getDescription().isPresent() ? pd.getDescription().get() : "/"));
		logger.info(" ██║        ██║   ███████╗ | Plugin Website:"+pd.getUrl().toString());
		logger.info(" ╚═╝        ╚═╝   ╚══════╝ | Dependencies Plugins: ["+String.join(", ", dependencies)+"]");
        
		setupIFHAdministration();
		
		yamlHandler = new YamlHandler(YamlManager.Type.VELO, pluginname, logger, dataDirectory,
        		(plugin.getAdministration() == null ? null : plugin.getAdministration().getLanguage()));
        setYamlManager(yamlHandler.getYamlManager());
        
        setListeners();
        setupBstats();
        TabListHandler.init(plugin);
        TabListHandler.determineUsedReplacer();
        new BackgroundTask(plugin);
    }
    
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) 
    {
        logger.info("Successfully disabled "+pluginname);
    }
    
    public static FTL getPlugin()
    {
    	return FTL.plugin;
    }
    
    public ProxyServer getServer()
    {
    	return server;
    }
    
    public Logger getLogger()
    {
    	return logger;
    }
    
    public Path getDataDirectory()
    {
    	return dataDirectory;
    }
    
    public YamlHandler getYamlHandler()
    {
    	return yamlHandler;
    }
    
    public YamlManager getYamlManager()
    {
    	return yamlManager;
    }
    
    public void setYamlManager(YamlManager yamlManager)
    {
    	this.yamlManager = yamlManager;
    }
    
    private void setListeners()
    {
    	EventManager em = server.getEventManager();
    	em.register(plugin, new JoinLeaveSwitchListener());
    	em.register(plugin, new ServerListener());
    	plugin.getServer().getChannelRegistrar().register(TOVELO);
        plugin.getServer().getChannelRegistrar().register(TOBACKEND);
    }
    
    private void setupIFHAdministration()
	{ 
		Optional<PluginContainer> ifhp = plugin.getServer().getPluginManager().getPlugin("interfacehub");
        if (ifhp.isEmpty()) 
        {
        	logger.info(pluginname + " dont find InterfaceHub!");
            return;
        }
        me.avankziar.ifh.velocity.IFH ifh = IFH.getPlugin();
        RegisteredServiceProvider<Administration> rsp = ifh
        		.getServicesManager()
        		.getRegistration(Administration.class);
        if (rsp == null) 
        {
            return;
        }
        administrationConsumer = rsp.getProvider();
        if(administrationConsumer != null)
        {
    		logger.info(pluginname + " detected InterfaceHub >>> Administration.class is consumed!");
        }
        return;
	}
	
	public Administration getAdministration()
	{
		return administrationConsumer;
	}
    
    public void setupBstats()
	{
    	int pluginId = 22843;
        metricsFactory.make(this, pluginId);
	}
}