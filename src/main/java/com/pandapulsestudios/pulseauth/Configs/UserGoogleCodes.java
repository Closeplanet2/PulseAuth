package com.pandapulsestudios.pulseauth.Configs;

import com.pandapulsestudios.pulseauth.PulseAuth;
import com.pandapulsestudios.pulseconfig.API.StorageAPI;
import com.pandapulsestudios.pulseconfig.Interface.PulseConfig;
import com.pandapulsestudios.pulseconfig.Objects.SaveableHashmap;
import com.pandapulsestudios.pulsecore.Data.API.VariableAPI;
import com.pandapulsestudios.pulsecore.Java.PulseAutoRegister;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

@PulseAutoRegister
public class UserGoogleCodes implements PulseConfig {

    @Override
    public JavaPlugin mainClass() { return PulseAuth.Instance;}

    public SaveableHashmap<UUID, String> playerCodes = new SaveableHashmap<>(UUID.class, String.class);

    public boolean GeneratePasscode(Player player){
        if(!playerCodes.containsKey(player.getUniqueId())){
            var googleAuth = new GoogleAuthenticator();
            var googleAuthKey = googleAuth.createCredentials();
            playerCodes.put(player.getUniqueId(), googleAuthKey.getKey());
            player.sendMessage(ChatColor.AQUA + "First Time Google Auth Code: " + ChatColor.GREEN + googleAuthKey.getKey());
            player.sendMessage(ChatColor.BOLD + "YOU MUST ENTER INTO GOOGLE AUTHENTICATOR BEFORE YOU LEAVE THE SERVER!");
            StorageAPI.Save(this, false);
            return true;
        }
        return false;
    }

    public boolean IsPasswordCorrect(Player player, String password){
        if(GeneratePasscode(player)) return true;
        var storedKey = playerCodes.get(player.getUniqueId());
        var googleAuth = new GoogleAuthenticator();
        if(VariableAPI.RETURN_TEST_FROM_TYPE(Integer.class).IsType(password)){
            return googleAuth.authorize(storedKey, Integer.parseInt(password));
        }
        return false;
    }
}
