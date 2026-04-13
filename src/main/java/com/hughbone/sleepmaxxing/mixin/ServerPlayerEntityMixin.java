package com.hughbone.sleepmaxxing.mixin;

import com.hughbone.sleepmaxxing.Main;
import com.mojang.datafixers.util.Either;
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

}
