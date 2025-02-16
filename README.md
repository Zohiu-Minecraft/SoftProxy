# SoftProxy
This [Fabric](https://fabricmc.net/) mod replaces Minecraft's networking with a simple reverse
proxy, allowing you redirect all players to another server.

SoftProxy can be used for adding external servers to free hosting platforms like 
[Minehut](https://app.minehut.com/) without paying for their official external server plan.
**This violates most hosting network's TOS. Use it at your own risk.**

# Usage
## Definitions
- **softproxy server**: The server your players will connect to (A Minehut server for example).
- **target server**: The server you want your players to end up on (An external server for example).

## Setup
Setup a normal 1.21.4 Fabric server as your softproxy server and install the newest mod jar from releases.
Then start your server once to create the config file. Restart it after you edited the config to 
contain your desired values.

*Since all traffic is proxied, only the server version of the target server matters but
because the mod was made targetting 1.21.4, that's the version that should be used on the softproxy server.*

**Your target server can have any version and server software independent from the softproxy server**

If your softproxy server is not behind a Minecraft proxy like [Velocity](https://papermc.io/software/velocity),
you are done with the setup and it should instantly work as expected. There is no additional setup
required on the target server.

## Velocity compatibility
If the server you want to use as a proxy to your own server is part of an existing server
network using [Velocity](https://papermc.io/software/velocity) and uses modern forwarding (like Minehut does),
your target server will need to know the forwarding secret so a connection can be established.

On most setups you can just copy the secret from the softproxy server's config file to the 
target server's config file. [FabricProxyLite](https://modrinth.com/mod/fabricproxy-lite)
is the most popular way to use Velocity with Fabric, which means you'll likely find the secret
in the `config/FabricProxy-Lite.toml` file on your softproxy server, which should have been
setup by the hosting provider.

### Minehut
Minehut uses their own custom version of Velocity, which changes the forwarding secret every
time you restart your server. That's not only an issue because you'll have to keep updating
the secret on your target server when your softproxy server starts, it also means that the
secret is not stored in any file - you can't even see it.

The way this mod gets around this is by hooking into FabricProxyLite during runtime and
grabbing the secret from there. It will then be printed to the server console where you
can see it and also sent to a webhook configured in the `config/SoftProxy.json` config file.

### Webhook
The webhook works by sending a POST request containing the forwarding secret 
to the URL defined in the config. From there you can GET the saved value automatically on your
target server and restart it to use the new forwarding secret automatically. This requires 
extra setup though which you'll need to figure out yourself.



## Config
Once you started the server once, stop it again and edit the `config/SoftProxy.json` file.
```json
{
    "targetHost":"example.com",
    "targetPort":25565,
    "startupWaitTime": 20,
    "webhookURL":"http://localhost/",
    "webhookFormat":"{\"content\":\"%secret%\"}",
    "webhookContentType":"application/json",
    "webhookAuthorization":"Bearer YOUR_ACCESS_TOKEN"
}
```
### Values
- **targetHost**: The domain or IP of the server you want players to connect to
- **targetPort**: The port of the server you want players to connect to
- **targetPort**: The time in seconds to wait on startup before accepting connections. This
wait happens after the webhook is sent to allow the target server to restart.
- **webhookURL**: The URL to send a POST request to containing the 
extracted secret from FabricProxyLite
- **webhookFormat**: The format for the POST body. The string `%secret%` will be replaced by
the actual forwarding secret. The default value works with Discord webhooks.
- **webhookContentType**: The content type for the POST request. *Only change this if you 
know what you're doing.*
- **webhookAuthorization**: The authorization header of the POST request gets set to this value.
Useful for when you want to restrict access to the secret. *Only change this if you know what you're doing.*