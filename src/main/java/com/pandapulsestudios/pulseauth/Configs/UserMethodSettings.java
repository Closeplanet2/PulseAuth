package com.pandapulsestudios.pulseauth.Configs;

import com.pandapulsestudios.pulseauth.Enum.AuthMethod;
import com.pandapulsestudios.pulseconfig.Interface.DontSave;
import com.pandapulsestudios.pulseconfig.Interface.PulseClass;
import com.pandapulsestudios.pulseconfig.Objects.SaveableArrayList;
import com.pandapulsestudios.pulseconfig.Objects.SaveableHashmap;
import com.pandapulsestudios.pulsecore.Java.PulseAutoRegister;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class UserMethodSettings implements PulseClass {
    public String playerName;
    public SaveableHashmap<AuthMethod, Boolean> userRequiredPerMethod = new SaveableHashmap<>(AuthMethod.class, Boolean.class);
    @DontSave
    public SaveableArrayList<AuthMethod> stillToAuth = new SaveableArrayList<>(AuthMethod.class);
    @DontSave
    public SaveableArrayList<AuthMethod> hasValidated = new SaveableArrayList<>(AuthMethod.class);

    public UserMethodSettings(){}
    public UserMethodSettings(String playerName){
        this.playerName = playerName;
    }

    @Override
    public void BeforeSaveConfig() {
        for(var value : AuthMethod.values()){
            if(!userRequiredPerMethod.containsKey(value)){
                userRequiredPerMethod.put(value, true);
            }
        }
    }

    public void ResetStillToAuthMethods(SaveableArrayList<AuthMethod> authMethods, SaveableHashmap<AuthMethod, Boolean> opBypassMethod, SaveableHashmap<AuthMethod, String> permBypassMethod,
                                        Player player, boolean cacheUserAuthUntilServerRestart){
        stillToAuth.clear();
        if(!cacheUserAuthUntilServerRestart){
            hasValidated.clear();
        }

        for(var authMethod : authMethods.ReturnArrayList()){
            var opBypass = opBypassMethod.getOrDefault(authMethod, false);
            var permBypass = permBypassMethod.getOrDefault(authMethod, "");
            if(opBypass && player.isOp()) continue;
            if(!permBypass.isEmpty() && player.hasPermission(permBypass)) continue;
            if(!userRequiredPerMethod.getOrDefault(authMethod, true)) continue;
            if(hasValidated.contains(authMethod)) continue;
            player.sendMessage("Need to validate... " + authMethod.name());
            stillToAuth.add(authMethod);
        }
    }
}
