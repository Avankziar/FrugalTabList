package me.avankziar.ftl.velocity.listener;

import java.util.UUID;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;

import me.avankziar.ftl.velocity.FTL;
import me.avankziar.ftl.velocity.handler.TabListHandler;

public class JoinLeaveSwitchListener
{	
	@Subscribe
	public void onPlayerJoin(PostLoginEvent event)
	{
		/*
		 * Calls all Replacer for the player
		 */
		TabListHandler.callReplacer(event.getPlayer());
		/**
		 * Change for the other player the new player
		 */
		TabListHandler.playerJoins(event.getPlayer());
		/*
		 * Starts the player own tablist change
		 */
		TabListHandler.addPlayer(event.getPlayer().getUniqueId(), new TabListHandler(event.getPlayer()));
	}
	
	@Subscribe
	public void onPlayerQuit(DisconnectEvent event)
	{
		final UUID uuid = event.getPlayer().getUniqueId();
		TabListHandler.removePlayer(uuid);
	}
	
	@Subscribe
	public void onServerSwitch(ServerConnectedEvent event)
	{
		if(event.getPreviousServer().isEmpty())
		{
			return;
		}
		TabListHandler.callReplacer(event.getPlayer());
	}
	
	@Subscribe
    public void onKick(KickedFromServerEvent event) 
	{
		final UUID uuid = event.getPlayer().getUniqueId();
		TabListHandler.removePlayer(uuid);
	}
	
	@Subscribe
    public void proxyReload(ProxyReloadEvent event) {
        
        FTL.getPlugin().getLogger().info("Velocitab has been reloaded!");
    }
}