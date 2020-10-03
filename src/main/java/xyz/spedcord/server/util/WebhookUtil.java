package xyz.spedcord.server.util;

import com.google.gson.JsonObject;
import xyz.spedcord.server.SpedcordServer;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @author Maximilian Dorn
 * @version 2.0.0
 * @since 1.0.0
 */
public class WebhookUtil {

    private static final ExecutorService executorService = Executors.newCachedThreadPool();
    private static final List<String> webhooks = new ArrayList<>();

    private WebhookUtil() {
        throw new UnsupportedOperationException();
    }

    public static void loadWebhooks() {
        File webhooksFile = new File("webhooks.txt");
        if (!webhooksFile.exists()) {
            try {
                webhooksFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            webhooks.addAll(Files.readAllLines(webhooksFile.toPath()).stream()
                    .filter(s -> !s.equals("")).collect(Collectors.toList()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void callWebhooks(long user, JsonObject data, String event) {
        executorService.submit(() -> {
            JsonObject object = new JsonObject();
            object.addProperty("event", event);
            object.addProperty("user", user);
            object.add("data", data);

            webhooks.forEach(s -> {
                try {
                    URL url = new URL(s);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                    connection.setRequestMethod("POST");
                    connection.setRequestProperty("User-Agent", "SpedcordServer");
                    connection.setRequestProperty("Authorization", "Bearer " + SpedcordServer.KEY);

                    connection.setDoOutput(true);

                    OutputStream outputStream = connection.getOutputStream();
                    outputStream.write(object.toString().getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();

                    connection.getResponseCode();
                } catch (IOException e) {
                    System.err.println("Failed to connect to webhook " + s + ": " + e.getMessage());
                }
            });
        });
    }

    public static List<String> getWebhooks() {
        return webhooks;
    }

}
