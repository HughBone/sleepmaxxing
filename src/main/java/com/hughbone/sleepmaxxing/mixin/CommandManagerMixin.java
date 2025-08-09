package com.hughbone.sleepmaxxing.mixin;

import com.hughbone.sleepmaxxing.Main;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class CommandManagerMixin {

  @Shadow public abstract ServerPlayerEntity getPlayer();

  @Shadow protected abstract void checkForSpam();

  @Inject(method = "onCommandExecution", at = @At(value = "HEAD"), cancellable = true)
  private void execute(CommandExecutionC2SPacket packet, CallbackInfo ci) {
    // Return early if not wake cmd
    String command = packet.command();
    if (!command.contains(Main.wakeMsg)) {
      return;
    }
    String[] splitCmd = command.split(" ");
    if (!splitCmd[0].equals("trigger")) {
      return;
    }

    Text feedbackMsg = Text
      .literal(splitCmd[1] + " is offline. who you trying to wake up?")
      .formatted(Formatting.GRAY)
      .formatted(Formatting.ITALIC);

    // Prevent self waking
    if (this.getPlayer().getNameForScoreboard().equals(splitCmd[1])) {
      feedbackMsg = Text
        .literal("why you trying to wake yourself lil bro? are you okay???")
        .formatted(Formatting.GRAY)
        .formatted(Formatting.ITALIC);
    } else {
      for (ServerPlayerEntity sleeper : this.getPlayer().getWorld().getPlayers()) {
        if (sleeper.getNameForScoreboard().equals(splitCmd[1])) {
          if (sleeper.isSleeping()) {
            feedbackMsg = Text
              .literal("you woke up " + sleeper.getNameForScoreboard() + "!")
              .formatted(Formatting.GRAY)
              .formatted(Formatting.ITALIC);

            Text wakeMsg = Text
              .literal("message from " + this
                .getPlayer()
                .getNameForScoreboard() + ": " + Main.wakeMsg)
              .formatted(Formatting.GRAY)
              .formatted(Formatting.ITALIC);

            sleeper.sendMessage(wakeMsg, false);
            sleeper.wakeUp();
          } else {
            feedbackMsg = Text
              .literal(splitCmd[1] + " is already awake. bruh")
              .formatted(Formatting.GRAY)
              .formatted(Formatting.ITALIC);
          }
          break;
        }
      }
    }

    this.getPlayer().sendMessage(feedbackMsg, false);
    this.checkForSpam();
    ci.cancel();
  }

}
