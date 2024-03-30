package com.pandapulsestudios.pulseauth.Configs;

import com.pandapulsestudios.pulseauth.Enum.AuthMethod;
import com.pandapulsestudios.pulseconfig.Enums.SaveableType;
import com.pandapulsestudios.pulseconfig.Interfaces.IgnoreSave;
import com.pandapulsestudios.pulseconfig.Interfaces.PulseClass;
import com.pandapulsestudios.pulseconfig.Objects.Savable.SaveableArrayList;
import com.pandapulsestudios.pulseconfig.Objects.Savable.SaveableHashmap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class UserMethodSettings implements PulseClass {
    public String playerName;
    public SaveableHashmap<AuthMethod, Boolean> userRequiredPerMethod = new SaveableHashmap<>(SaveableType.CONFIG, AuthMethod.class, Boolean.class);
    @IgnoreSave
    public SaveableArrayList<AuthMethod> stillToAuth = new SaveableArrayList<>(SaveableType.CONFIG, AuthMethod.class);
    @IgnoreSave
    public SaveableArrayList<AuthMethod> hasValidated = new SaveableArrayList<>(SaveableType.CONFIG, AuthMethod.class);

    public UserMethodSettings(){}
    public UserMethodSettings(String playerName){
        this.playerName = playerName;
    }

    @Override
    public void BeforeSave() {
        for(var value : AuthMethod.values()){
            if(!userRequiredPerMethod.hashMap.containsKey(value)){
                userRequiredPerMethod.hashMap.put(value, true);
            }
        }
    }

    public void ResetStillToAuthMethods(SaveableArrayList<AuthMethod> authMethods, SaveableHashmap<AuthMethod, Boolean> opBypassMethod, SaveableHashmap<AuthMethod, String> permBypassMethod,
                                        Player player, boolean cacheUserAuthUntilServerRestart){
        stillToAuth.arrayList.clear();
        if(!cacheUserAuthUntilServerRestart){
            hasValidated.arrayList.clear();
        }

        for(var authMethod : authMethods.arrayList){
            var opBypass = opBypassMethod.hashMap.getOrDefault(authMethod, false);
            var permBypass = permBypassMethod.hashMap.getOrDefault(authMethod, "");
            if(opBypass && player.isOp()) continue;
            if(!permBypass.isEmpty() && player.hasPermission(permBypass)) continue;
            if(!userRequiredPerMethod.hashMap.getOrDefault(authMethod, true)) continue;
            if(hasValidated.arrayList.contains(authMethod)) continue;
            player.sendMessage("Need to validate... " + authMethod.name());
            stillToAuth.arrayList.add(authMethod);
        }
    }
}
