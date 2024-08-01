package me.avankziar.ftl.velocity.handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.player.TabListEntry;

import me.avankziar.ftl.general.assistance.ChatApi;
import me.avankziar.ftl.velocity.FTL;
import me.avankziar.ftl.velocity.assistance.BackgroundTask;
import me.avankziar.ftl.velocity.object.TabPlayer;
import me.avankziar.ftl.velocity.object.TabPlayerSortAlphabetical;
import me.avankziar.ftl.velocity.object.TabPlayerSortPermissionWeight;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

public class TabListHandler 
{
	public enum SortingType
	{
		ALPHABETICAL_ASC,
		ALPHABETICAL_DESC,
		PERMISSION_GROUP_WEIGHT_ASC,
		PERMISSION_GROUP_WEIGHT_DESC;
	}
	
	private static FTL plugin;
	public static void init(FTL plugin)
	{
		TabListHandler.plugin = plugin;
	}
	
	public static LinkedHashMap<UUID, TabListHandler> handler = new LinkedHashMap<>();
	private static ArrayList<TabPlayer> tabPlayers = new ArrayList<>();
	public static ArrayList<UUID> hidingPlayers = new ArrayList<>();
	
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
		handler.values().stream().forEach(x -> x.setReload(true));
	}
	
	public static void removePlayer(final UUID uuid)
	{
		handler.remove(uuid);
		replacer.remove(uuid);
		plugin.getServer().getAllPlayers().stream().forEach(x -> x.getTabList().removeEntry(uuid));
	}
	
	public static void addPlayer(UUID uuid, TabListHandler tlh)
	{
		handler.put(uuid, tlh);
	}
	
	public static void playerJoins(Player player)
	{
		FTL.getPlugin().getServer().getScheduler().buildTask(FTL.getPlugin(), (task) ->
		{
			int weight = 0;
			if(LuckPermsProvider.get() != null)
			{
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getPlayerAdapter(Player.class).getUser(player);
				Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());
				weight = group.getWeight().orElse(0);
			}
			TabPlayer tp = new TabPlayer(player.getTabList().getEntry(player.getUniqueId()).get(), weight);
			tabPlayers.add(tp);
			for(Player all : FTL.getPlugin().getServer().getAllPlayers())
			{
				if(!all.getUsername().equals(player.getUsername()))
				{
					continue;
				}
				TabListHandler tll = handler.get(all.getUniqueId());
				ArrayList<TabPlayer> list = (ArrayList<TabPlayer>) tabPlayers.stream().filter(
						x -> x.getTabListEntry().isListed()).collect(Collectors.toList());
				switch(tll.sortingType)
				{
				case ALPHABETICAL_ASC:
					Arrays.sort(list.toArray(new TabPlayer[list.size()]), new TabPlayerSortAlphabetical());
					break;
				case ALPHABETICAL_DESC:
					Arrays.sort(list.toArray(new TabPlayer[list.size()]), new TabPlayerSortAlphabetical().reversed());
					break;
				case PERMISSION_GROUP_WEIGHT_ASC:
					Arrays.sort(list.toArray(new TabPlayer[list.size()]), new TabPlayerSortPermissionWeight());
					break;
				case PERMISSION_GROUP_WEIGHT_DESC:
					Arrays.sort(list.toArray(new TabPlayer[list.size()]), new TabPlayerSortPermissionWeight().reversed());
					break;
				}
				all.getTabList().clearAll();
				for(TabPlayer t : tabPlayers)
				{
					Optional<Player> p = plugin.getServer().getPlayer(t.getTabListEntry().getProfile().getId());
					TabListEntry tle = t.getTabListEntry();
					tle.setDisplayName(ChatApi.tl(getPlayerReplacer(p.get(), tll.playerentry)));
					all.getTabList().addEntry(tle);
				}
			}
		}).delay(500L, TimeUnit.MILLISECONDS).schedule();
	}
	
	private static String getPlayerReplacer(Player player, String playerentry)
	{
		if(player == null)
		{
			return playerentry;
		}
		String s = playerentry;
		LinkedHashMap<String, String> map = replacer.get(player.getUniqueId());
		for(Entry<String, String> entry : map.entrySet())
		{
			s = s.replace(entry.getKey(), entry.getValue());
		}
		return s;
	}
	
	public static void playerHides(Player player)
	{
		for(TabPlayer t : tabPlayers)
		{
			if(t.getTabListEntry().getProfile().getId().equals(player.getUniqueId()))
			{
				t.getTabListEntry().setListed(false);
			}
		}
	}
	
	public static void playerShowsUp(Player player)
	{
		for(TabPlayer t : tabPlayers)
		{
			if(t.getTabListEntry().getProfile().getId().equals(player.getUniqueId()))
			{
				t.getTabListEntry().setListed(true);
			}
		}
	}
	
	//Normal Class
	
	private boolean reload = false;
	public void setReload(boolean reload)
	{
		this.reload = reload;
	}
	
	private boolean join = false;
	public void setJoin(boolean join)
	{
		this.join = join;
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
	private AtomicInteger itab;
	private Long animationTime;
	private LinkedHashMap<Integer, String> header;
	private LinkedHashMap<Integer, String> footer;
	private String playerentry;
	private SortingType sortingType = SortingType.ALPHABETICAL_ASC;
	
	public TabListHandler(final Player player)
	{
		uuid = player.getUniqueId();
		tablist = determineTabList(player);
		ihe = new AtomicInteger(0); //Which Level the header/footer is, if it exist a animation
		ifo = new AtomicInteger(0); //Which Level the header/footer is, if it exist a animation
		itab = new AtomicInteger(0); //When the TabListEntry are updated
		animationTime = plugin.getYamlHandler().getConfig().getLong(tablist+".AnimationTime", 200L);
		if(animationTime < 200L)
		{
			animationTime = 200L;
		}
		int itabraz = (int) (1000L/animationTime);
		int itabReverseAtZero = itabraz < 1 ? 30 : itabraz * 30;
		header = new LinkedHashMap<>();
		List<String> hl = plugin.getYamlHandler().getConfig().getStringList(tablist+".Header");
		footer = new LinkedHashMap<>();
		List<String> fl = plugin.getYamlHandler().getConfig().getStringList(tablist+".Footer");
		try
		{
			sortingType = SortingType.valueOf(plugin.getYamlHandler().getConfig().getString(tablist+".SortingType"));
		} catch(Exception e) {}
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
		playerentry = plugin.getYamlHandler().getConfig().getString(tablist+".PlayerEntry");
		setJoin(true);
		FTL.getPlugin().getServer().getScheduler().buildTask(FTL.getPlugin(), (task) ->
		{
			if(header.get(ihe.intValue()) == null)
			{
				ihe.set(0);
			}
			if(footer.get(ifo.intValue()) == null)
			{
				ifo.set(0);
			}
			if(itab.intValue() == itabReverseAtZero)
			{
				TabListHandler.callReplacer(player);
				itab.set(0);
			}
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
			player.sendPlayerListHeaderAndFooter(
					ChatApi.tl(header.get(ihe.intValue())), ChatApi.tl(footer.get(ifo.intValue())));
			if(itab.intValue() != 0 || !join)
			{
				return;
			}
			if(join)
			{
				setJoin(false);
			}
			TabListHandler tll = this;
			ArrayList<TabPlayer> list = (ArrayList<TabPlayer>) tabPlayers.stream().filter(
					x -> x.getTabListEntry().isListed()).collect(Collectors.toList());
			switch(tll.sortingType)
			{
			case ALPHABETICAL_ASC:
				Arrays.sort(list.toArray(new TabPlayer[list.size()]), new TabPlayerSortAlphabetical());
				break;
			case ALPHABETICAL_DESC:
				Arrays.sort(list.toArray(new TabPlayer[list.size()]), new TabPlayerSortAlphabetical().reversed());
				break;
			case PERMISSION_GROUP_WEIGHT_ASC:
				Arrays.sort(list.toArray(new TabPlayer[list.size()]), new TabPlayerSortPermissionWeight());
				break;
			case PERMISSION_GROUP_WEIGHT_DESC:
				Arrays.sort(list.toArray(new TabPlayer[list.size()]), new TabPlayerSortPermissionWeight().reversed());
				break;
			}
			player.getTabList().clearAll();
			for(TabPlayer t : tabPlayers)
			{
				Optional<Player> p = plugin.getServer().getPlayer(t.getTabListEntry().getProfile().getId());
				TabListEntry tle = t.getTabListEntry();
				tle.setDisplayName(ChatApi.tl(getPlayerReplacer(p.get(), tll.playerentry)));
				player.getTabList().addEntry(tle);
			}
			ihe.addAndGet(1);
			ifo.addAndGet(1);
			itab.addAndGet(1);
			
		}).delay(200L, TimeUnit.MILLISECONDS).repeat(animationTime, TimeUnit.MILLISECONDS).schedule();
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
}
