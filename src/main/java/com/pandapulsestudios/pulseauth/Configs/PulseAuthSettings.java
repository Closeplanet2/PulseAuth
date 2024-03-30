package com.pandapulsestudios.pulseauth.Configs;

import com.pandapulsestudios.pulseauth.Enum.AuthMethod;
import com.pandapulsestudios.pulseauth.PulseAuth;
import com.pandapulsestudios.pulseconfig.APIS.ConfigAPI;
import com.pandapulsestudios.pulseconfig.Enums.SaveableType;
import com.pandapulsestudios.pulseconfig.Interfaces.Config.PulseConfig;
import com.pandapulsestudios.pulseconfig.Interfaces.ConfigComment;
import com.pandapulsestudios.pulseconfig.Objects.Savable.SaveableArrayList;
import com.pandapulsestudios.pulseconfig.Objects.Savable.SaveableHashmap;
import com.pandapulsestudios.pulsecore.Chat.PulsePrompt;
import com.pandapulsestudios.pulsecore.Movement.MovementAPI;
import com.pandapulsestudios.pulsecore.Player.PlayerAPI;
import com.pandapulsestudios.pulsecore.Player.PlayerAction;
import com.pandapulsestudios.pulsecore.PulseCore;
import com.warrenstrange.googleauth.GoogleAuthenticator;
import org.bukkit.Bukkit;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

//TODO limit every event
//TODO New auth method - kick all no ops
//TODO Kick all non perms
public class PulseAuthSettings implements PulseConfig {
    @Override
    public String documentID() { return "PulseAuthSettings"; }

    @Override
    public JavaPlugin mainClass() { return PulseAuth.Instance; }

    @Override
    public boolean useSubFolder() { return false; }

    public SaveableArrayList<AuthMethod> authMethods = new SaveableArrayList<>(SaveableType.CONFIG, AuthMethod.class);
    public SaveableHashmap<AuthMethod, Boolean> opBypassMethod = new SaveableHashmap<>(SaveableType.CONFIG, AuthMethod.class, Boolean.class);
    public SaveableHashmap<AuthMethod, String> permBypassMethod = new SaveableHashmap<>(SaveableType.CONFIG, AuthMethod.class, String.class);
    public SaveableHashmap<UUID, UserMethodSettings> userMethodSettings = new SaveableHashmap<>(SaveableType.CONFIG, UUID.class, UserMethodSettings.class);
    public boolean cacheUserAuthUntilServerRestart = false;
    public boolean requireAuthOnServerReload = false;
    public boolean kickPlayersOnServerReload = false;

    @Override
    public void FirstLoad() {
        for(var value : AuthMethod.values()){
            authMethods.arrayList.add(value);
            opBypassMethod.hashMap.put(value, false);
            permBypassMethod.hashMap.put(value, "");
        }
        for(var player : Bukkit.getOfflinePlayers()){
            userMethodSettings.hashMap.put(player.getUniqueId(), new UserMethodSettings(player.getName()));
        }
    }

    public void PlayerJoinServer(Player player, UserPasswords userPasswords, UserGoogleCodes userGoogleCodes){
        var userMethod = userMethodSettings.hashMap.getOrDefault(player.getUniqueId(), null);
        if(userMethod == null) userMethod = new UserMethodSettings(player.getDisplayName());
        userMethod.ResetStillToAuthMethods(authMethods, opBypassMethod, permBypassMethod, player, cacheUserAuthUntilServerRestart);

        if(!userMethod.stillToAuth.arrayList.isEmpty()){
            if(PlayerAPI.CanPlayerAction(PlayerAction.PlayerMove, player)) MovementAPI.LockPlayerLocation(player, false, player.getLocation());
        }else{
            if(!PlayerAPI.CanPlayerAction(PlayerAction.PlayerMove, player)) MovementAPI.LockPlayerLocation(player, true, null);
        }

        userMethodSettings.hashMap.put(player.getUniqueId(), userMethod);
        ConfigAPI.Save(this, false);
        TryAndSendNextAuthMethodToPlayer(player, userPasswords, userGoogleCodes);
    }

    public void PlayerLeaveServer(Player player){
        var userMethod = userMethodSettings.hashMap.get(player.getUniqueId());
        userMethod.ResetStillToAuthMethods(authMethods, opBypassMethod, permBypassMethod, player, cacheUserAuthUntilServerRestart);
        userMethodSettings.hashMap.put(player.getUniqueId(), userMethod);
        ConfigAPI.Save(this, false);
    }

    public void TryAndSendNextAuthMethodToPlayer(Player player, UserPasswords userPasswords, UserGoogleCodes userGoogleCodes){
        var userMethod = userMethodSettings.hashMap.get(player.getUniqueId());
        if(userMethod.stillToAuth.arrayList.isEmpty()){
            if(!PlayerAPI.CanPlayerAction(PlayerAction.PlayerMove, player)) MovementAPI.LockPlayerLocation(player, true, null);
            return;
        }

        var authMethod = userMethod.stillToAuth.arrayList.get(0);
        if(authMethod == AuthMethod.PasswordUsername){
            SendPasswordPrompt(userMethod, userPasswords, userGoogleCodes, player);
        }else if(authMethod == AuthMethod.GoogleAuth){
            SendGooglePasswordPrompt(userMethod, userPasswords, userGoogleCodes, player);
        }
    }

    private void SendGooglePasswordPrompt(UserMethodSettings userMethod, UserPasswords userPasswords, UserGoogleCodes userGoogleCodes, Player player){
        if(userGoogleCodes.GeneratePasscode(player)) return;
        PulsePrompt.PulsePromptBuilder()
                .promptText("Enter your google auth code:")
                .clearPlayerChatOnStart(true)
                .clearPlayerChatOnRestart(true)
                .clearPlayerChatOnEnd(true)
                .translateColorCodes(true)
                .translateHexCodes(true)
                .onResponseCallback((triplet)->{
                    triplet.getC().setSessionData("END", userGoogleCodes.IsPasswordCorrect(triplet.getA(), triplet.getB()));
                })
                .onConversationRestartCallback((triplet)->{
                    triplet.getC().getForWhom().sendRawMessage("Incorrect Password! Try Again.....");
                })
                .onEndConversationCallback((triplet)->{
                    triplet.getC().getForWhom().sendRawMessage("Correct Password!");
                    AuthUserMethod(userMethod, AuthMethod.GoogleAuth, userPasswords, userGoogleCodes, triplet.getA());
                }).StartConversation(player, false);
    }

    private void SendPasswordPrompt(UserMethodSettings userMethod, UserPasswords userPasswords, UserGoogleCodes userGoogleCodes, Player player){
        PulsePrompt.PulsePromptBuilder()
                .promptText("Enter Your Password:")
                .clearPlayerChatOnStart(true)
                .clearPlayerChatOnRestart(true)
                .clearPlayerChatOnEnd(true)
                .translateColorCodes(true)
                .translateHexCodes(true)
                .onResponseCallback((triplet)->{
                    triplet.getC().setSessionData("END", userPasswords.IsPasswordCorrect(triplet.getA(), triplet.getB()));
                })
                .onConversationRestartCallback((triplet)->{
                    triplet.getC().getForWhom().sendRawMessage("Incorrect Password! Try Again.....");
                })
                .onEndConversationCallback((triplet)->{
                    triplet.getC().getForWhom().sendRawMessage("Correct Password!");
                    AuthUserMethod(userMethod, AuthMethod.PasswordUsername, userPasswords, userGoogleCodes, triplet.getA());
                })
                .StartConversation(player, false);
    }

    private void AuthUserMethod(UserMethodSettings userMethod, AuthMethod authMethod, UserPasswords userPasswords, UserGoogleCodes userGoogleCodes, Player player){
        userMethod.stillToAuth.arrayList.remove(0);
        userMethod.hasValidated.arrayList.add(authMethod);
        userMethodSettings.hashMap.put(player.getUniqueId(), userMethod);
        TryAndSendNextAuthMethodToPlayer(player, userPasswords, userGoogleCodes);
    }
}
