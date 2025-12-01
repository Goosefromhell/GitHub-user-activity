package org.govnorgatization.githubuseractivity;


import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
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

        if (args.length != 1) {
            System.out.println("Write user name");
        } else {

            try {


                String user = String.format("https://api.github.com/users/%s/events", args[0]);

                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder().uri(URI.create(user)).build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 404) {
                    System.out.println("User is not found");
                } else if (response.statusCode() != 200 && response.statusCode() != 404) {
                    System.out.println("Error occurred: " + response.statusCode());
                } else {


                    ObjectMapper mapper = new ObjectMapper();

                    Files.createDirectories(target.getParent());

                    JsonNode json = mapper.readTree(response.body());
                    mapper.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), json);
                    JsonNode jsonNode = mapper.readTree(target.toFile());

                    HashMap<String, Integer> amount_of_pushes = new HashMap<>();
                    ArrayList<String> repos_created = new ArrayList<>();
                    HashMap<String, Integer> amount_of_pull_requests_reviews = new HashMap<>();


                    for (JsonNode kal : jsonNode) {
                        if (kal.get("type").asString().equals("PushEvent")) {
                            String repo_name = kal.get("repo").get("name").asString();
                            amount_of_pushes.put(repo_name, amount_of_pushes.getOrDefault(repo_name, 0) + 1);
                        } else if (kal.get("type").asString().equals("CreateEvent")) {
                            repos_created.add(kal.get("repo").get("name").asString());
                        } else if (kal.get("type").asString().equals("PullRequestEvent")) {
                            switch (kal.get("payload").get("action").asString()) {
                                case "opened" ->
                                        System.out.printf("Opened new pullrequest in %s \n", kal.get("repo").get("name"));
                                case "closed" ->
                                        System.out.printf("Closed pullrequest in %s \n", kal.get("repo").get("name"));
                                case "reopened" ->
                                        System.out.printf("Reopened  pullrequest in %s \n", kal.get("repo").get("name"));
                                default -> System.out.println("Unknown action");

                            }

                        } else if (kal.get("type").asString().equals("PullRequestReviewEvent")) {
                            String repo_name = kal.get("repo").get("name").asString();
                            amount_of_pull_requests_reviews.put(repo_name, amount_of_pull_requests_reviews.getOrDefault(repo_name, 0) + 1);

                        } else if (kal.get("type").asString().equals("IssuesEvent")) {
                            switch (kal.get("payload").get("action").asString()) {
                                case "opened" ->
                                        System.out.printf("Opened new issue in %s \n", kal.get("repo").get("name"));
                                case "closed" ->
                                        System.out.printf("Closed  issue in %s \n", kal.get("repo").get("name"));
                                case "reopened" ->
                                        System.out.printf("Reopened  issue in %s \n", kal.get("repo").get("name"));
                                default -> System.out.println("Unknown action");
                            }

                        }

                    }

                    for (Map.Entry<String, Integer> entry : amount_of_pushes.entrySet()) {
                        System.out.printf("User pushed %d commits to %s \n", entry.getValue(), entry.getKey());
                    }
                    for (String repo_name : repos_created) {
                        System.out.printf("Started %s \n", repo_name);
                    }

                    for (Map.Entry<String, Integer> entry : amount_of_pull_requests_reviews.entrySet()) {
                        System.out.printf("Reviewed %d requests in %s repositories \n", entry.getValue(), entry.getKey());
                    }

                }
            } catch (InterruptedException | IOException e) {
                System.out.println("Error occurred:" + e);

            }
        }


    }
}
