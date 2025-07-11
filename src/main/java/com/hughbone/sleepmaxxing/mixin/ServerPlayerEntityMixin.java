package com.hughbone.sleepmaxxing.mixin;

import com.hughbone.sleepmaxxing.Main;
import com.mojang.datafixers.util.Either;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Unit;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Random;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin {

    @Shadow public abstract ServerWorld getWorld();

    @Inject(method = "trySleep", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;updateSleepingPlayers()V", shift = At.Shift.AFTER))
    private void sendSleepingStatus(BlockPos pos, CallbackInfoReturnable<Either<PlayerEntity.SleepFailureReason, Unit>> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        String randomMsg = Main.sleepMsgs.get(new Random().nextInt(Main.sleepMsgs.size()));
        int playerTextColor = 0xC1F1FF;
        int msgTextColor = 0x00FFA6;

        Text playerText = Text.literal(player.getNameForScoreboard())
            .styled(style -> style.withColor(playerTextColor));

        Text sleepMsgText = Text.literal(" " + randomMsg)
            .styled(style -> style
                .withHoverEvent(new HoverEvent.ShowText(Text.of("wake")))
                .withClickEvent(new ClickEvent.RunCommand(
                    "/trigger " + player.getNameForScoreboard() + " " + Main.wakeMsg))
                .withColor(msgTextColor)
            );

        sleepMsgText = playerText.copy().append(sleepMsgText);

        for(ServerPlayerEntity serverPlayerEntity : this.getWorld().getPlayers()) {
            serverPlayerEntity.sendMessage(sleepMsgText, false);
        }
    }

}
