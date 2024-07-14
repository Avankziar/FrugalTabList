package me.avankziar.ftl.velocity.listener;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.util.LinkedHashMap;
import java.util.UUID;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.proxy.ServerConnection;

import me.avankziar.ftl.general.assistance.StaticValues;
import me.avankziar.ftl.velocity.FTL;
import me.avankziar.ftl.velocity.handler.TabListHandler;

public class ServerListener 
{	
	@Subscribe
    public void onPluginMessageFromBackend(PluginMessageEvent event) 
    {
        if (!(event.getSource() instanceof ServerConnection)) 
        {
            return;
        }
        if (!event.getIdentifier().getId().equalsIgnoreCase(FTL.TOVELO.getId())) 
        {
            return;
        }
        DataInputStream in = new DataInputStream(new ByteArrayInputStream(event.getData()));
        try
        {
        	String task = in.readUTF();
            if(task.equals(StaticValues.RESOLVE_REPLACER)) 
            {
            	String uuid = in.readUTF();
            	int amount = in.readInt();
            	LinkedHashMap<String, String> replacer = TabListHandler.replacer.get(UUID.fromString(uuid));
            	if(replacer == null)
            	{
            		return;
            	}
            	for(int i = 0; i < amount; i++)
            	{
            		String[] s = in.readUTF().split("~!~");
            		if(s.length != 2)
            		{
            			continue;
            		}
            		String key = s[0];
            		String value = s[1];
            		replacer.replace(key, value);
            	}
            	TabListHandler.replacer.replace(UUID.fromString(uuid), replacer);
            }
        } catch(Exception e) {}
    }
}
