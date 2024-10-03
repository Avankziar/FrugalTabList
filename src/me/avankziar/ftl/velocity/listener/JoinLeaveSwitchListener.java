package me.avankziar.ftl.velocity.listener;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.proxy.Player;

import me.avankziar.ftl.velocity.FTL;
import me.avankziar.ftl.velocity.handler.TabListHandler;

public class JoinLeaveSwitchListener
{	
	@Subscribe
	public void onPlayerJoin(PostLoginEvent event)
	{
		final Player player = event.getPlayer();
		FTL.getPlugin().getServer().getScheduler().buildTask(FTL.getPlugin(), (task) ->
		{
			/*
			 * Calls all Replacer for the player
			 */
			TabListHandler.callReplacer(player);
			/**
			 * Change for the other player the new player
			 */
			TabListHandler.playerJoins(player);
			/*
			 * Starts the player own tablist change
			 */
			TabListHandler.addPlayer(player.getUniqueId(), new TabListHandler(player));
		}).delay(1L, TimeUnit.SECONDS).schedule();
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
		final Player player = event.getPlayer();
		FTL.getPlugin().getServer().getScheduler().buildTask(FTL.getPlugin(), (task) ->
		{
			TabListHandler.callReplacer(player);
		}).delay(1L, TimeUnit.SECONDS).schedule();
	}
	
	@Subscribe
    public void onKick(KickedFromServerEvent event) 
	{
		final UUID uuid = event.getPlayer().getUniqueId();
		TabListHandler.removePlayer(uuid);
	}
	
	@Subscribe
    public void proxyReload(ProxyReloadEvent event) 
	{
        FTL.getPlugin().getLogger().info("Velocitab has been reloaded!");
    }
}