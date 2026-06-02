package com.hughbone.sleepmaxxing.mixin;

import com.hughbone.sleepmaxxing.Main;
import com.hughbone.sleepmaxxing.ModStats;
import com.mojang.datafixers.util.Either;
import net.minecraft.ChatFormatting;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin {

  @Shadow @Final private MinecraftServer server;

  @Inject(method = "startSleepInBed",
    at = @At(value = "INVOKE",
      target = "Lnet/minecraft/server/level/ServerLevel;updateSleepingPlayerList()V",
      shift = At.Shift.AFTER))
  private void sendSleepingStatus(
    BlockPos pos,
    CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir)
  {
    ServerPlayer player = (ServerPlayer) (Object) this;
    String randomMsg = Main.sleepMsgs.get(new Random().nextInt(Main.sleepMsgs.size()));
    int playerTextColor = 0xC1F1FF;
    int msgTextColor = 0x00FFA6;

    Component playerText = Component
      .literal(player.getScoreboardName())
      .withStyle(style -> style.withColor(playerTextColor));

    Component sleepMsgText = Component
      .literal(" " + randomMsg)
      .withStyle(style -> style
        .withHoverEvent(new HoverEvent.ShowText(Component.nullToEmpty("wake")))
        .withClickEvent(new ClickEvent.RunCommand("/wakeup " + player.getScoreboardName()))
        .withColor(msgTextColor));

    sleepMsgText = playerText.copy().append(sleepMsgText);

    for (ServerPlayer serverPlayerEntity : server.getPlayerList().getPlayers()) {
      serverPlayerEntity.sendSystemMessage(sleepMsgText, false);
    }
  }

  // Fires when a player gets out of bed. Only a full night's sleep (slept long
  // enough to skip the night) counts as a successful sleep; kicks and early
  // exits leave isSleepingLongEnough() false. Checked at HEAD before the sleep
  // timer is reset.
  @Inject(method = "stopSleepInBed", at = @At("HEAD"))
  private void onStopSleepInBed(boolean wakeImmediately, boolean updateLevelForSleeping, CallbackInfo ci) {
    ServerPlayer player = (ServerPlayer) (Object) this;
    if (!player.isSleepingLongEnough()) {
      return;
    }

    // Sleep war win: successfully slept after being kicked out 3+ times in the last minute.
    if (Main.checkSleepWarWin(player.getUUID())) {
      player.awardStat(ModStats.SLEEP_WAR_WINS);
      Component winMsg = Component
        .literal(player.getScoreboardName() + " won a sleep war! 🛌")
        .withStyle(ChatFormatting.GOLD)
        .withStyle(ChatFormatting.BOLD);
      for (ServerPlayer serverPlayerEntity : server.getPlayerList().getPlayers()) {
        serverPlayerEntity.sendSystemMessage(winMsg, false);
      }
    }
  }

}
