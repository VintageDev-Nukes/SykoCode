package me.Ikillnukes.lotto;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import com.google.gson.Gson;

public class Database {
	
	  private static Connection connection;
	  private static String table = "LOTTO";
	  
	  public static HashMap<UUID, Member> lotto = new HashMap<UUID, Member>();
	  
	  protected static void connect()
	  {
	    try
	    {
	      Class.forName("org.sqlite.JDBC");
	      connection = DriverManager.getConnection("jdbc:sqlite:" + Util.plugin.getDataFolder() + File.separator + "database.db");
	      connection.createStatement().execute("CREATE TABLE IF NOT EXISTS " + table + " (UUID CHAR(36) NOT NULL PRIMARY KEY, MEMBER LONGTEXT NOT NULL)");
	      connection.setAutoCommit(false);
	    }
	    catch (Exception e)
	    {
	      e.printStackTrace();
	      Util.crash("An error ocurred while trying to access the SQLite database! Disabling...");
	    }
	    loadLotto();
	  }
	  
	  protected static void disconnect()
	  {
	    
	    if (connection != null) {
	      try
	      {
	        connection.close();
	      }
	      catch (SQLException e)
	      {
	        Util.handleException(e);
	      }
	    }
	  }
	  
	  private static void refreshConnection()
	  {
	    if (connection == null) {
	      connect();
	    }
	  }
	  
	  private static void loadLotto()
	  {
	    ResultSet rs = query("SELECT * FROM " + table);
	    Gson gson = new Gson();
	    try
	    {
	      while (rs.next()) {
	        lotto.put(UUID.fromString(rs.getString("UUID")), gson.fromJson(rs.getString("MEMBER"), Member.class));
	      }
	      rs.close();
	    }
	    catch (SQLException e)
	    {
	      Util.handleException(e);
	    }
	  }
	  
	  public static void saveLotto()
	  {
	    try
	    {
	      for (UUID uuid : lotto.keySet())
	      {
	        ResultSet rs = query("SELECT * FROM " + table + " WHERE UUID='" + uuid.toString() + "';");
	        Gson gson = new Gson();
	        if (rs.next()) {
	          execute("UPDATE " + table + " SET MEMBER =" + gson.toJson(lotto.get(uuid)) + " WHERE UUID='" + uuid.toString() + "';");
	        } else {
	          execute("INSERT INTO " + table + " (UUID,MEMBER) VALUES ('" + uuid + "', " + lotto.get(uuid) + " );");
	        }
	        rs.close();
	      }
	    }
	    catch (SQLException e)
	    {
	      Util.handleException(e);
	    }
	  }
	  
	  private static ResultSet query(String query)
	  {
	    refreshConnection();
	    ResultSet rs = null;
	    try
	    {
	      rs = connection.createStatement().executeQuery(query);
	    }
	    catch (SQLException e)
	    {
	      Util.handleException(e);
	    }
	    return rs;
	  }
	  
	  private static void execute(String update)
	  {
	    
	    try
	    {
	      Statement stmt = connection.createStatement();
	      stmt.executeUpdate(update);
	      connection.commit();
	      stmt.close();
	    }
	    catch (SQLException e)
	    {
	      Util.handleException(e);
	    }
	  }
	  
	  protected static void clearall() {
		  query("TRUNCATE "+table);
	  }
	  
	  protected static int getPot() {
		  ResultSet rs = query("SELECT * FROM " + table);
		  int totalPot = 0;
		  try
		  {
		      while (rs.next()) {
		    	  totalPot += Integer.valueOf(rs.getInt("MEMBER"));
		      }
		      rs.close();
		  }
		  catch (SQLException e)
		  {
		      Util.handleException(e);
		  }
		  return totalPot;
	  }
	  
}
