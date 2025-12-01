package org.govnorgatization.githubuseractivity;


import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main {
    static void main(String[] args) {


        Path target = Path.of(System.getProperty("user.home"), "Buffers", "GitHubUserActivity", "activity.json");

        try {
            String user = String.format("https://api.github.com/users/%s/events", args[0]);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder().uri(URI.create(user)).build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            ObjectMapper mapper = new ObjectMapper();

            Files.createDirectories(target.getParent());

            JsonNode json = mapper.readTree(response.body());
            mapper.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), json);
            JsonNode jsonNode = mapper.readTree(target.toFile());

            HashMap<String, Integer> amount_of_pushes = new HashMap<>();
            ArrayList<String> repos_created = new ArrayList<>();


            for (JsonNode kal : jsonNode) {
                if (kal.get("type").asString().equals("PushEvent")) {
                    String repo_name = kal.get("repo").get("name").asString();
                    amount_of_pushes.put(repo_name, amount_of_pushes.getOrDefault(repo_name, 0) + 1);
                } else if (kal.get("type").asString().equals("CreateEvent")) {
                    repos_created.add(kal.get("repo").get("name").asString());
                }

            }

            for (Map.Entry<String, Integer> entry : amount_of_pushes.entrySet()) {
                System.out.printf("User pushed %d commits to %s \n", entry.getValue(), entry.getKey());
            }
            for (String repo_name : repos_created) {
                System.out.printf("Started %s \n", repo_name);
            }


        } catch (Exception e) {
            System.out.println("something went wrong");
        }

    }
}
