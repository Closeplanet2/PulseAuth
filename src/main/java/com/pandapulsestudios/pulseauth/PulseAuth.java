package com.pandapulsestudios.pulseauth;

import com.mojang.authlib.yggdrasil.response.User;
import com.pandapulsestudios.pulseauth.Configs.PulseAuthSettings;
import com.pandapulsestudios.pulseauth.Configs.UserGoogleCodes;
import com.pandapulsestudios.pulseauth.Configs.UserPasswords;
import com.pandapulsestudios.pulseauth.Variables.AuthMethodVarTest;
import com.pandapulsestudios.pulseconfig.APIS.ConfigAPI;
import com.pandapulsestudios.pulsecore.Events.PulseCoreEvents;
import com.pandapulsestudios.pulsecore.Java.ClassAPI;
import com.pandapulsestudios.pulsecore.Java.JavaAPI;
import com.pandapulsestudios.pulsecore.Java.PulseAutoRegister;
import com.pandapulsestudios.pulsecore.Movement.MovementAPI;
import com.pandapulsestudios.pulsecore.Player.PlayerAPI;
import com.pandapulsestudios.pulsecore.Player.PlayerAction;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public final class PulseAuth extends JavaPlugin{
    public static PulseAuth Instance;
    public static ArrayList<UUID> hasSentGoogleAuth = new ArrayList<>();
    private static PulseAuthSettings PulseAuthSettings;
    private static UserPasswords UserPasswords;
    private static UserGoogleCodes UserGoogleCodes;

    @Override
    public void onEnable() {
        Instance = this;
        ClassAPI.RegisterPulseVariableTest(new AuthMethodVarTest());
        ClassAPI.RegisterPulseCoreEvents(new EventLibrary());

        PulseAuthSettings = new PulseAuthSettings();
        ConfigAPI.Load(PulseAuthSettings, false);

        UserPasswords = new UserPasswords();
        ConfigAPI.Load(UserPasswords, false);

        UserGoogleCodes = new UserGoogleCodes();
        ConfigAPI.Load(UserGoogleCodes, false);

        if(PulseAuthSettings.requireAuthOnServerReload){
            for(var player : Bukkit.getOnlinePlayers()) PulseAuthSettings.PlayerJoinServer(player, UserPasswords, UserGoogleCodes);
        }
    }

    @Override
    public void onDisable() {
        if(PulseAuthSettings.kickPlayersOnServerReload){
            for(var player : Bukkit.getOnlinePlayers()) player.kickPlayer("Server Restart!");
        }
    }

    @PulseAutoRegister
    public static class EventLibrary implements PulseCoreEvents {
        @Override
        public boolean op() { return false; }

        @Override
        public String[] perms() { return new String[0]; }

        @Override
        public String[] worlds() { return new String[0]; }

        @Override
        public String[] regions() { return new String[0]; }

        @Override
        public void PlayerJoinEvent(PlayerJoinEvent event) {
            PulseAuthSettings.PlayerJoinServer(event.getPlayer(), UserPasswords, UserGoogleCodes);
        }

        @Override
        public void PlayerQuitEvent(PlayerQuitEvent event) {
            PulseAuthSettings.PlayerLeaveServer(event.getPlayer());
        }

        @Override
        public boolean PlayerMove(Player player, Location lastLocation, Location newLocation, double moveDistance) {
           PulseAuthSettings.TryAndSendNextAuthMethodToPlayer(player, UserPasswords, UserGoogleCodes);
           return false;
        }
    }
}
