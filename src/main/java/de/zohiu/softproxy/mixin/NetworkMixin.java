package de.zohiu.softproxy.mixin;

import de.zohiu.softproxy.proxy.Proxy;
import de.zohiu.softproxy.Softproxy;
import net.minecraft.server.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.net.InetAddress;

@Mixin(ServerNetworkIo.class)
public class NetworkMixin {
    @Shadow @Final private MinecraftServer server;

    /**
     * @author Zohiu
     * @reason Replace Minecraft's entire networking with a simple reverse proxy
     */
    @Overwrite
    public void bind(@Nullable InetAddress address, int port) {
        Softproxy.server = this.server;
        Proxy proxy = new Proxy(Softproxy.config.targetHost, Softproxy.config.targetPort, port);
        Thread t = new Thread(proxy);
        Softproxy.proxyThread = t;
        t.start();
    }
}