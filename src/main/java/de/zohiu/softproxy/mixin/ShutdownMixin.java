package de.zohiu.softproxy.mixin;

import de.zohiu.softproxy.Softproxy;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class ShutdownMixin {
    @Inject(method="shutdown", at=@At("HEAD"))
    private void shutdown(CallbackInfo info) throws InterruptedException {
        Softproxy.server = null;
        System.out.println("Waiting for proxy to close.");
        Softproxy.proxyThread.join();
    }
}
