package me.avankziar.ftl.velocity.handler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabListEntry;

import me.avankziar.ftl.general.assistance.ChatApi;
import me.avankziar.ftl.velocity.FTL;
import me.avankziar.ftl.velocity.assistance.BackgroundTask;

public class TabListHandler 
{
	private static FTL plugin;
	public static void init(FTL plugin)
	{
		TabListHandler.plugin = plugin;
	}
	
	public static LinkedHashMap<UUID, TabListHandler> handler = new LinkedHashMap<>();
	
	public static ArrayList<String> usedReplacer = new ArrayList<>();
	/**
	 * Called if Server starts or cmd reload used
	 */
	public static void determineUsedReplacer()
	{
		usedReplacer = new ArrayList<>();
		for(String s : plugin.getYamlHandler().getConfig().getStringList("TabListListValues"))
		{
			String[] split = s.split(":");
			if(split.length != 2)
			{
				continue;
			}
			String name = split[0];
			for(String t : plugin.getYamlHandler().getConfig().getStringList(name+".Header"))
			{
				for(String v : getReplacer(t))
				{
					if(!usedReplacer.contains(v))
					{
						usedReplacer.add(v);
					}
				}
			}
			for(String t : plugin.getYamlHandler().getConfig().getStringList(name+".Footer"))
			{
				for(String v : getReplacer(t))
				{
					if(!usedReplacer.contains(v))
					{
						usedReplacer.add(v);
					}
				}
			}
			for(String v : getReplacer(plugin.getYamlHandler().getConfig().getString(name+".PlayerEntry")))
			{
				if(!usedReplacer.contains(v))
				{
					usedReplacer.add(v);
				}
			}
		}
	}
	
	private static ArrayList<String> getReplacer(String s)
	{
		ArrayList<String> l = new ArrayList<>();
		int i = 0;
		while(i < s.length())
		{
			char c = s.charAt(i);
			if(c == '%' && i+1 < s.length())
			{
				int j = i+1;
				while(j < s.length())
				{
					char cc = s.charAt(j);
					if(cc == '%')
					{
						String r = s.substring(i, j);
						if(!l.contains(r))
						{
							l.add(r);
							break;
						}
					}
					i++;
					j++;
				}
			}
		}
		return l;
	}
	
	public static LinkedHashMap<UUID, LinkedHashMap<String, String>> replacer = new LinkedHashMap<>();
	/**
	 * Call backendserver to get all non intern plugin replacer.
	 * @param player
	 */
	public static void callReplacer(Player player)
	{
		if(!replacer.containsKey(player.getUniqueId()))
		{
			LinkedHashMap<String, String> map = new LinkedHashMap<>();
			for(String s : usedReplacer)
			{
				if(!map.containsKey(s))
				{
					map.put(s, s);
				}
			}
			replacer.put(player.getUniqueId(), map);
		}
		BackgroundTask.callReplacer(player);
	}
	
	public static void reload()
	{
		for(Player player : FTL.getPlugin().getServer().getAllPlayers())
		{
			TabListHandler tlh = handler.get(player.getUniqueId());
			tlh.setReload(true);
		}
	}
	
	public static void removePlayer(UUID uuid)
	{
		handler.remove(uuid);
		replacer.remove(uuid);
	}
	
	public static void addPlayer(UUID uuid, TabListHandler tlh)
	{
		handler.put(uuid, tlh);
	}
	
	//Normal Class
	
	private boolean reload = false;
	public void setReload(boolean reload)
	{
		this.reload = reload;
	}
	
	private boolean quit = false;
	public void hasQuit()
	{
		this.quit = true;
	}
	
	private final UUID uuid;
	private final String tablist;
	private AtomicInteger ihe;
	private AtomicInteger ifo;
	private Long animationTime;
	private LinkedHashMap<Integer, String> header;
	private LinkedHashMap<Integer, String> footer;
	private String playerentry;
	
	public TabListHandler(final Player player)
	{
		uuid = player.getUniqueId();
		tablist = determineTabList(player);
		ihe = new AtomicInteger(0); //Which Level the header/footer is, if it exist a animation
		ifo = new AtomicInteger(0); //Which Level the header/footer is, if it exist a animation
		animationTime = plugin.getYamlHandler().getConfig().getLong(tablist+".AnimationTime", 200L);
		if(animationTime < 200L)
		{
			animationTime = 200L;
		}
		header = new LinkedHashMap<>();
		List<String> hl = plugin.getYamlHandler().getConfig().getStringList(tablist+".Header");
		footer = new LinkedHashMap<>();
		List<String> fl = plugin.getYamlHandler().getConfig().getStringList(tablist+".Footer");
		for(int j = 0; j < hl.size(); j++)
		{
			String s = hl.get(j);
			header.put(j, s);
		}
		for(int j = 0; j < fl.size(); j++)
		{
			String s = fl.get(j);
			footer.put(j, s);
		}
		String playerentry = plugin.getYamlHandler().getConfig().getString(tablist+".PlayerEntry");
		FTL.getPlugin().getServer().getScheduler().buildTask(FTL.getPlugin(), (task) ->
		{
			if(quit || player == null || !player.isActive())
			{
				task.cancel();
				removePlayer(uuid);
				return;
			}
			if(reload)
			{
				task.cancel();
				removePlayer(uuid);
				TabListHandler tlh = new TabListHandler(player);
				addPlayer(uuid, tlh);
				return;
			}
			if(header.get(ihe.intValue()) == null)
			{
				ihe.set(0);
			}
			if(footer.get(ifo.intValue()) == null)
			{
				ifo.set(0);
			}
			
			player.sendPlayerListHeaderAndFooter(
					ChatApi.tl(header.get(ihe.intValue())), ChatApi.tl(footer.get(ifo.intValue())));
			
			ArrayList<TabListEntry> l = new ArrayList<>();
			for(TabListEntry tle : player.getTabList().getEntries())
			{
				String username = tle.getProfile().getName();
				
			}
			
			ihe.addAndGet(1);
			ifo.addAndGet(1);
		}).delay(200L, TimeUnit.MILLISECONDS).repeat(200L, TimeUnit.MILLISECONDS).schedule();
	}
	
	private String determineTabList(Player player)
	{
		String n = null;
		for(String s : plugin.getYamlHandler().getConfig().getStringList("TabListListValues"))
		{
			String[] split = s.split(":");
			if(split.length != 2)
			{
				continue;
			}
			String name = split[0];
			String perm = split[1];
			if(perm.equals("null"))
			{
				n = name;
				break;
			}
			if(player.hasPermission(perm))
			{
				n = name;
				break;
			}
		}
		return n;
	}
	
	public static void playerJoins(Player player)
	{
		FTL.getPlugin().getServer().getScheduler().buildTask(FTL.getPlugin(), (task) ->
		{
			for(Player all : FTL.getPlugin().getServer().getAllPlayers())
			{
				if(!all.getUsername().equals(player.getUsername()))
				{
					continue;
				}
				TabListHandler tll = handler.get(all.getUniqueId());
				for(TabListEntry tle : all.getTabList().getEntries())
				{
					if(!tle.getProfile().getName().equals(player.getUsername()))
					{
						continue;
					}
					tle.setDisplayName(ChatApi.tl(getPlayerReplacer(player, tll.playerentry)));
				}
			}
		}).delay(500L, TimeUnit.MILLISECONDS).schedule();
	}
	
	private static String getPlayerReplacer(Player player, String playerentry)
	{
		String s = playerentry;
		LinkedHashMap<String, String> map = replacer.get(player.getUniqueId());
		for(Entry<String, String> entry : map.entrySet())
		{
			s = s.replace(entry.getKey(), entry.getValue());
		}
		return s;
	}
}
