package me.avankziar.ftl.velocity.object;

import com.velocitypowered.api.proxy.player.TabListEntry;

public class TabPlayer
{
	private TabListEntry tabListEntry;
	private int permissionGroupWeight;
	
	public TabPlayer(TabListEntry tabListEntry, int permissionGroupWeight)
	{
		setTabListEntry(tabListEntry);
		setPermissionGroupWeight(permissionGroupWeight);
	}

	public TabListEntry getTabListEntry()
	{
		return tabListEntry;
	}

	public void setTabListEntry(TabListEntry tabListEntry)
	{
		this.tabListEntry = tabListEntry;
	}

	public int getPermissionGroupWeight()
	{
		return permissionGroupWeight;
	}

	public void setPermissionGroupWeight(int permissionGroupWeight)
	{
		this.permissionGroupWeight = permissionGroupWeight;
	}
}