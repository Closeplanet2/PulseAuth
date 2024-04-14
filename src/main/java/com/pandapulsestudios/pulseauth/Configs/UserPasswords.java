package com.pandapulsestudios.pulseauth.Configs;

import com.pandapulsestudios.pulseauth.PulseAuth;
import com.pandapulsestudios.pulseconfig.API.StorageAPI;
import com.pandapulsestudios.pulseconfig.APIS.ConfigAPI;
import com.pandapulsestudios.pulseconfig.Enums.SaveableType;
import com.pandapulsestudios.pulseconfig.Interface.PulseConfig;
import com.pandapulsestudios.pulseconfig.Interfaces.BinaryFile.PulseBinaryFile;
import com.pandapulsestudios.pulseconfig.Interfaces.Config.PulseConfig;
import com.pandapulsestudios.pulseconfig.Objects.Savable.SaveableHashmap;
import com.pandapulsestudios.pulseconfig.Objects.SaveableHashmap;
import com.pandapulsestudios.pulsecore.Java.PulseAutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

@PulseAutoRegister
public class UserPasswords implements PulseConfig {

    @Override
    public String documentID() { return "UserPasswords"; }

    @Override
    public JavaPlugin mainClass() { return PulseAuth.Instance;}

    @Override
    public boolean useSubFolder() { return false; }

    public SaveableHashmap<UUID, String> playerPassword = new SaveableHashmap<>(UUID.class, String.class);

    @Override
    public void FirstLoadConfig() {
        for(var player : Bukkit.getOfflinePlayers()){
            playerPassword.put(player.getUniqueId(), UUID.randomUUID().toString());
        }
    }

    public boolean IsPasswordCorrect(Player player, String password){
        if(!playerPassword.containsKey(player.getUniqueId())){
            playerPassword.put(player.getUniqueId(), UUID.randomUUID().toString());
            StorageAPI.Save(this, false);
            return false;
        }
        else{
            var storedPassword = playerPassword.get(player.getUniqueId());
            return storedPassword.equals(password);
        }
    }
}
