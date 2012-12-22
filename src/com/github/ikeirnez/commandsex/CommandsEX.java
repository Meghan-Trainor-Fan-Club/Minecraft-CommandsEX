package com.github.ikeirnez.commandsex;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.craftbukkit.v1_4_6.CraftServer;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.mcstats.Metrics;

import com.github.ikeirnez.commandsex.commands.CommandManager;
import com.github.ikeirnez.commandsex.handlers.EventManager;
import com.github.ikeirnez.commandsex.helpers.LogHelper;

public class CommandsEX extends JavaPlugin {

    public static CommandsEX plugin;
    public static FileConfiguration config;
    public static Logger logger;
    public static CommandManager cmdManger;
    public static EventManager evntManager;
    public static Database database;
    
    public void onEnable(){
        try {
            plugin = this;
            logger = Bukkit.getLogger();
            config = getConfig();
            config.options().copyDefaults(true);
            saveConfig();
            
            PluginManager pm = getServer().getPluginManager();

            if (!(Bukkit.getServer() instanceof CraftServer)){
                logger.log(Level.WARNING, "Unfortunately CommandsEX is not compatible with custom CraftBukkit builds");
                logger.log(Level.WARNING, "Please alert the developers of your CraftBukkit build and we may add support for it!");
                pm.disablePlugin(this);
                return;
            }
            
            try {
                LogHelper.logInfo("Connecting to CommandsEX database...");
                
                switch (config.getString("database.type").toLowerCase()){
                case "sqlite" :
                    database = new Database(config.getString("database.name"), config.getString("database.prefix"));
                    break;
                case "mysql" :
                    database = new Database(config.getString("database.name"), config.getString("database.username"),
                            config.getString("database.password"), config.getString("database.host"),
                            config.getString("database.port"), config.getString("database.prefix"));
                    break;
                default : 
                    LogHelper.logSevere("Incorrect database type, disabling plugin...");
                    pm.disablePlugin(this);
                    break;
                }
                
                LogHelper.logInfo("Successfully connected to the CommandsEX database");
            } catch (Exception e){
                LogHelper.addExceptionToEventLog(e);
                LogHelper.logSevere("Error while connecting to CommandsEX database, disabling plugin...");
                pm.disablePlugin(this);
            }
            
            cmdManger = new CommandManager();
            evntManager = new EventManager();
            cmdManger.registerCommands();
            evntManager.registerEvents();
            
            // create the CommandsEX directory if it has not been created already
            getDataFolder().mkdirs();
            File log = new File(getDataFolder(), "log.txt");
            
            // creates the log.txt file if it hasn't been created already
            try {
                log.createNewFile();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            
            if (config.getBoolean("metricsEnabled")){
                try {
                    Metrics metrics = new Metrics(this);
                    metrics.start();
                } catch (IOException e){
                    e.printStackTrace();
                }
            }
            
            LogHelper.addToEventLog("CommandsEX was successfully enabled without any errors");
        } catch (Exception e){
            LogHelper.addExceptionToEventLog(e);
        }
    }

    public void onDisable(){
        database.close();
    }

}