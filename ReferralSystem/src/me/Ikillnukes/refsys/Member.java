package me.Ikillnukes.refsys;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;

public class Member 
	implements ConfigurationSerializable
{

	public enum Field { username, rewarded };
	
	public String username;
	public boolean rewarded;
	public int playedTime = 0;
	
	static
	{
	    ConfigurationSerialization.registerClass(Member.class);
	}
	
	public Member(final String u) 
	{
		username = u;
	}

	public Member(final Map<String, Object> args) 
	{
		username = (String)args.get("username");
		rewarded = (boolean)args.get("rewarded");
		playedTime = (int)args.get("timings");
	}
	
	public Member modify(final Object value, final Field type) 
	{
		switch(type) 
		{
		case username:
			username = (String)value;
			break;
		case rewarded:
			rewarded = (boolean)value;
			break;
		}
		return this;
	}
	
	protected static Member getMember(final List<Member> members, final String username) 
	{
		Member mem = null;
		for(Member m : members) 
			if(m.username == username) 
			{
				mem = m;
				break;
			}
		return mem;
	}
	
	@Override
	public Map<String, Object> serialize() 
	{
		Map<String, Object> r = new HashMap<String, Object>();
		r.put("username", username);
		r.put("rewarded", rewarded);
		r.put("timings", playedTime);
		return r;
	}
	
}
