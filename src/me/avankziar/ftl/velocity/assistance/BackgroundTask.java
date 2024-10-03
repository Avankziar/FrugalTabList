package me.avankziar.ftl.velocity.assistance;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.velocitypowered.api.proxy.Player;

import me.avankziar.ftl.general.assistance.ChatApi;
import me.avankziar.ftl.general.assistance.StaticValues;
import me.avankziar.ftl.general.assistance.TimeHandler;
import me.avankziar.ftl.velocity.FTL;
import me.avankziar.ftl.velocity.handler.TabListHandler;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;

public class BackgroundTask 
{
	private FTL plugin;
	
	public BackgroundTask(FTL plugin)
	{
		this.plugin = plugin;
		callReplacer();
	}
	
	private void callReplacer()
	{
		plugin.getServer().getScheduler().buildTask(plugin, (task) ->
		{
			for(Player player : plugin.getServer().getAllPlayers())
			{
				callReplacer(player);
			}
		}).repeat(plugin.getYamlHandler().getConfig().getLong("TabList.ReplacerRefreshTime", 30L), TimeUnit.SECONDS).schedule();
	}
	
	public static void callReplacer(Player player)
	{
		LinkedHashMap<String, String> map = TabListHandler.replacer.get(player.getUniqueId());
		ArrayList<String> toSend = new ArrayList<>();
		for(Entry<String, String> entry : map.entrySet())
		{
			FTL.getPlugin().getLogger().info("0 callReplacer > "+entry.getKey()+" : "+entry.getValue()); //REMOVEME
			String s = resolveInternReplacer(player, entry.getKey());
			if(s != null)
			{
				entry.setValue(s);
				FTL.getPlugin().getLogger().info("1 callReplacer > s : "+s); //REMOVEME
			} else
			{
				toSend.add(entry.getKey());
			}
		}
		sendToResolveReplacer(player, toSend);
	}
	
	public static void sendToResolveReplacer(Player player, ArrayList<String> l)
	{
		if(player == null)
		{
			return;
		}
		ByteArrayOutputStream streamout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(streamout);
        try {
        	out.writeUTF(StaticValues.RESOLVE_REPLACER);
			out.writeUTF(player.getUniqueId().toString());
			out.writeInt(l.size());
			for(String s : l)
			{
				out.writeUTF(s);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        if(player.getCurrentServer().isPresent())
        {
        	player.getCurrentServer().get().sendPluginMessage(FTL.TOBACKEND, streamout.toByteArray());
        } else
        {
        	FTL.getPlugin().getLogger().info("sendToResolveReplacer is Empty"); //REMOVEME
        }
	}
	
	private static String resolveInternReplacer(Player player, String s)
	{
		switch(s)
		{
		default:
			break;
		case "%time%": return TimeHandler.getDateTime(System.currentTimeMillis(), "HH:mm");
		case "%times%": return TimeHandler.getDateTime(System.currentTimeMillis(), "HH:mm:ss");
		case "%date%": return TimeHandler.getDateTime(System.currentTimeMillis(), "dd.MM");
		case "%dates%": return TimeHandler.getDateTime(System.currentTimeMillis(), "dd.MM.yyyy");
		case "%player_username%": return player.getUsername();
		case "%player_username_lower%": return player.getUsername().toLowerCase();
		case "%player_useruniqueid%": return player.getUniqueId().toString();
		case "%player_currentserver%": return player.getCurrentServer().get().getServerInfo().getName();
		case "%player_clientbrand%": return player.getClientBrand();
		case "%player_ping%": return String.valueOf(player.getPing());
		case "%players_online%": return String.valueOf(FTL.getPlugin().getServer().getAllPlayers().size());
		case "%players_online_currentserver%": return String.valueOf(FTL.getPlugin().getServer().getAllPlayers().stream()
					.filter(x -> x.getCurrentServer().get().getServerInfo().getName().equals(player.getCurrentServer().get().getServerInfo().getName()))
					.collect(Collectors.toList()).size());
		case "%server_amount%": return String.valueOf(FTL.getPlugin().getServer().getAllServers().size());
		case "%player_permission_prefix%":
			try
			{
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getPlayerAdapter(Player.class).getUser(player);
				return user.getCachedData().getMetaData().getPrefix();
			} catch(Exception e)
			{
				break;
			}
		case "%player_permission_suffix%":
			try
			{
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getPlayerAdapter(Player.class).getUser(player);
				return user.getCachedData().getMetaData().getSuffix();
			} catch(Exception e)
			{
				break;
			}
		case "%player_permission_primaryrole%":	
			try
			{
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getPlayerAdapter(Player.class).getUser(player);
				return user.getPrimaryGroup();
			} catch(Exception e)
			{
				break;
			}
		case "%player_permission_primaryrole_display_name%":
			try
			{
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getPlayerAdapter(Player.class).getUser(player);
				Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());
				return group.getDisplayName();
			} catch(Exception e)
			{
				e.printStackTrace();
				break;
			}
		case "%player_permission_primaryrole_display_color%":
			try
			{
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getPlayerAdapter(Player.class).getUser(player);
				Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());
				String g = String.valueOf(user.getPrimaryGroup().charAt(0)).toUpperCase()+user.getPrimaryGroup().substring(1);
				String display = group.getDisplayName()
						.replace(user.getPrimaryGroup(), "")
						.replace(user.getPrimaryGroup().toLowerCase(), "")
						.replace(user.getPrimaryGroup().toUpperCase(), "")
						.replace(g, "");
				return ChatApi.oldBukkitFormat(display);
			} catch(Exception e)
			{
				break;
			}
		case "%player_permission_primaryrole_weight%":
			try
			{
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getPlayerAdapter(Player.class).getUser(player);
				Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());
				return String.valueOf(group.getWeight());
			} catch(Exception e)
			{
				break;
			}
		}
		return null;
	}
}
