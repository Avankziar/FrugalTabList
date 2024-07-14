package me.avankziar.ftl.velocity.assistance;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import com.velocitypowered.api.proxy.Player;

import me.avankziar.ftl.general.assistance.StaticValues;
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
		}).repeat(30L, TimeUnit.SECONDS).schedule();
	}
	
	public static void callReplacer(Player player)
	{
		LinkedHashMap<String, String> map = TabListHandler.replacer.get(player.getUniqueId());
		ArrayList<String> toSend = new ArrayList<>();
		for(Entry<String, String> entry : map.entrySet())
		{
			if(entry.getKey().contains(":"))
			{
				toSend.add(entry.getKey());
			} else
			{
				entry.setValue(resolveInternReplacer(player, entry.getKey()));
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
        player.getCurrentServer().get().sendPluginMessage(FTL.TOBACKEND, streamout.toByteArray());
	}
	
	private static String resolveInternReplacer(Player player, String s)
	{
		switch(s)
		{
		default:
			return "";
		case "%player_username%": return player.getUsername();
		case "%player_username_lower%": return player.getUsername().toLowerCase();
		case "%player_uuid%": return player.getUniqueId().toString();
		case "%player_currentserver%": return player.getCurrentServer().get().getServerInfo().getName();
		case "%player_clientbrand%": return player.getClientBrand();
		case "%player_ping%": return String.valueOf(player.getPing());
		case "%players_online%": return String.valueOf(FTL.getPlugin().getServer().getAllPlayers().size());
		case "%servers%": return String.valueOf(FTL.getPlugin().getServer().getAllServers().size());
		case "%player_luckperm_prefix%":
			try
			{
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getPlayerAdapter(Player.class).getUser(player);
				return user.getCachedData().getMetaData().getPrefix();
			} catch(Exception e)
			{
				return "";
			}
		case "%player_luckperm_suffix%":
			try
			{
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getPlayerAdapter(Player.class).getUser(player);
				return user.getCachedData().getMetaData().getSuffix();
			} catch(Exception e)
			{
				return "";
			}
		case "%player_luckperm_primaryrole%":	
			try
			{
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getPlayerAdapter(Player.class).getUser(player);
				return user.getPrimaryGroup();
			} catch(Exception e)
			{
				return "";
			}
		case "%player_luckperm_primaryrole_display_name%":
			try
			{
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getPlayerAdapter(Player.class).getUser(player);
				Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());
				return group.getDisplayName();
			} catch(Exception e)
			{
				return "";
			}
		case "%player_luckperm_primaryrole_display_color%":
			try
			{
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getPlayerAdapter(Player.class).getUser(player);
				Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());
				return group.getDisplayName().replace(user.getPrimaryGroup(), "");
			} catch(Exception e)
			{
				return "";
			}
		case "%player_luckperm_primaryrole_weight%":
			try
			{
				LuckPerms api = LuckPermsProvider.get();
				User user = api.getPlayerAdapter(Player.class).getUser(player);
				Group group = api.getGroupManager().getGroup(user.getPrimaryGroup());
				return String.valueOf(group.getWeight());
			} catch(Exception e)
			{
				return "";
			}
		}
	}
}
