package com.mcsunnyside.AuthmeEZAntiBot;

import fr.xephi.authme.api.v3.AuthMeApi;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;

public class Main extends JavaPlugin implements Listener {
    private int joiningPlayerIn60s = 0;
    private ArrayList<String> blockingIp = new ArrayList<String>();

    @Override
    public void onEnable() {
        getLogger().info("AuthmeEZAntiBot now is loaded!");
        Bukkit.getPluginManager().registerEvents(this, this);
        saveDefaultConfig();
        new BukkitRunnable() {
            public void run() {
                joiningPlayerIn60s = 0;
                blockingIp.clear();
            }
        }.runTaskTimer(this, 0, 60*20);
    }

    private boolean underAttacking() {
        joiningPlayerIn60s++;
        if (joiningPlayerIn60s > getConfig().getInt("limit"))
            return true;
        return false;
    }

    private void add2IpBlackList(String ip){
        if(blockingIp.contains(ip))
            return;
        blockingIp.add(ip);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onAsyncJoining(AsyncPlayerPreLoginEvent e) {
        if (!underAttacking())
            return;
        String ip = e.getAddress().getHostAddress();

        if(blockingIp.contains(ip)){
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, getConfig().getString("msg"));
            return;
        }

        if (!Bukkit.getOfflinePlayer(e.getUniqueId()).hasPlayedBefore()) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, getConfig().getString("msg"));
            add2IpBlackList(ip);
            return;
        }
        if (!AuthMeApi.getInstance().isRegistered(e.getName())) {
            e.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, getConfig().getString("msg"));
            add2IpBlackList(ip);
            return;
        }
    }
    @EventHandler(priority = EventPriority.LOWEST)
    public void onJoining(PlayerLoginEvent e) {
        if (!underAttacking())
            return;
        if((System.currentTimeMillis() - e.getPlayer().getFirstPlayed()) < getConfig().getLong("minplayedtime")){
            e.disallow(PlayerLoginEvent.Result.KICK_OTHER, getConfig().getString("msg"));
        }
    }
}
