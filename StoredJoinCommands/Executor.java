package me.Ikillnukes.sjc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Executor
{
	public HashMap<String, Boolean> commands = new HashMap<String, Boolean>();
	public Executor(HashMap<String, Boolean> cmds) 
	{
		commands = cmds;
	}
	public List<String> getCommands() 
	{
		if(commands != null)
			return new ArrayList<String>(commands.keySet());
		return null;
	}
}
