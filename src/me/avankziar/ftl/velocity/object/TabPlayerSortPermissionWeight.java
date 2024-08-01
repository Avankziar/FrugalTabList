package me.avankziar.ftl.velocity.object;

import java.util.Comparator;

public class TabPlayerSortPermissionWeight implements Comparator<TabPlayer>
{
	public int compare(TabPlayer o1, TabPlayer o2)
	{
		int i = o1.getPermissionGroupWeight() - o2.getPermissionGroupWeight();
		if(i == 0)
		{
			i = o1.getTabListEntry().getProfile().getName().compareTo(o2.getTabListEntry().getProfile().getName());
		}
		return i;
	}
}