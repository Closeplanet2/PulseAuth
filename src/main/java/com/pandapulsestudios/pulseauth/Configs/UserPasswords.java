package com.pandapulsestudios.pulseauth.Configs;

import com.pandapulsestudios.pulseauth.PulseAuth;
import com.pandapulsestudios.pulseconfig.APIS.ConfigAPI;
import com.pandapulsestudios.pulseconfig.Enums.SaveableType;
import com.pandapulsestudios.pulseconfig.Interfaces.BinaryFile.PulseBinaryFile;
import com.pandapulsestudios.pulseconfig.Interfaces.Config.PulseConfig;
import com.pandapulsestudios.pulseconfig.Objects.Savable.SaveableHashmap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class UserPasswords implements PulseConfig {

    @Override
    public String documentID() { return "UserPasswords"; }

    @Override
    public JavaPlugin mainClass() { return PulseAuth.Instance;}

    @Override
    public boolean useSubFolder() { return false; }

    public SaveableHashmap<UUID, String> playerPassword = new SaveableHashmap<>(SaveableType.CONFIG, UUID.class, String.class);

    @Override
    public void FirstLoad() {
        for(var player : Bukkit.getOfflinePlayers()){
            playerPassword.hashMap.put(player.getUniqueId(), UUID.randomUUID().toString());
        }
    }

    public boolean IsPasswordCorrect(Player player, String password){
        if(!playerPassword.hashMap.containsKey(player.getUniqueId())){
            playerPassword.hashMap.put(player.getUniqueId(), UUID.randomUUID().toString());
            ConfigAPI.Save(this, false);
            return false;
        }
        else{
            var storedPassword = playerPassword.hashMap.get(player.getUniqueId());
            return storedPassword.equals(password);
        }
    }
}
