package me.avankziar.ftl.spigot.listener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import me.avankziar.ftl.general.assistance.StaticValues;
import me.avankziar.ftl.spigot.FTL;

public class ServerListener implements PluginMessageListener
{
	private FTL plugin;
	private boolean papi;
	
	public ServerListener(FTL plugin)
	{
		this.plugin = plugin;
		papi = plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI");
	}
	
	@Override
	public void onPluginMessageReceived(String channel, Player player, byte[] bytes) 
	{
		if(channel.equals(FTL.TOBACKEND)) 
		{
        	ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
            DataInputStream in = new DataInputStream(stream);
            String task = null;
            try 
            {
            	task = in.readUTF();
            	if(task.equals(StaticValues.RESOLVE_REPLACER))
            	{
            		String uuid = in.readUTF();
            		int size = in.readInt();
            		LinkedHashMap<String, String> replacerMap = new LinkedHashMap<>();
            		for(int i = 0; i < size; i++)
            		{
            			String key = in.readUTF();
            			String value = resolveReplacer(key, uuid);
            			replacerMap.put(key, value != null ? value : "null");
            		}
            		sendAnswer(uuid, replacerMap);
            	}
            } catch (IOException e) 
            {
    			e.printStackTrace();
    		}
		}
	}
	
	private String resolveReplacer(String key, String uuid)
	{
		Player player = Bukkit.getPlayer(UUID.fromString(uuid));
		if(player == null)
		{
			return null;
		}
		switch(key)
		{
		default:
			if(papi)
			{
				return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, key);
			}
			return null;
		case "%server_tps_1m%": return String.valueOf(plugin.getServer().getTPS()[0]);
		case "%server_tps_5m%": return String.valueOf(plugin.getServer().getTPS()[1]);
		case "%server_tps_15m%": return String.valueOf(plugin.getServer().getTPS()[2]);
		case "%player_currentworld%": return player.getWorld().getName();
		case "%vault_balance%":	return String.valueOf(plugin.getVaultEco().getBalance(player));
		case "%vault_currency%": return plugin.getVaultEco().currencyNamePlural();
		}
	}
	
	private void sendAnswer(String uuid, LinkedHashMap<String, String> replacerMap)
	{
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(stream);
        try {
			out.writeUTF(StaticValues.RESOLVE_REPLACER);
			out.writeUTF(uuid);
			out.writeInt(replacerMap.size());
			for(Entry<String, String> e : replacerMap.entrySet())
			{
				out.writeUTF(e.getKey()+"|||"+e.getValue());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
        plugin.getServer().sendPluginMessage(plugin, FTL.TOVELO, stream.toByteArray());
	}
}