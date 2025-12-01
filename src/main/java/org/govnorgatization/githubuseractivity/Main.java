package org.govnorgatization.githubuseractivity;


import tools.jackson.core.type.TypeReference;
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
import java.util.List;
import java.util.Map;

public class Main {
    static void main(String[] args) throws IOException, InterruptedException {

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://api.github.com/users/Goosefromhell/events")).build();
        HttpResponse<String> responese = client.send(request, HttpResponse.BodyHandlers.ofString());

        Path target = Path.of(System.getProperty("user.home"), "Buffers", "GitHubUserActivity", "test.json");

        try {
            ObjectMapper mapper = new ObjectMapper();
            Files.createDirectories(target.getParent());
            if (args[0].equals("fetch")) {
                JsonNode jsno = mapper.readTree(responese.body());
                mapper.writerWithDefaultPrettyPrinter().writeValue(target.toFile(), jsno);

            } else if (args[0].equals("info")) {
                HashMap<String, Integer> amout_of_pushes = new HashMap<>();
                ArrayList<String> repos_created = new ArrayList<>();
                JsonNode jsonNode = mapper.readTree(target.toFile());
               int commits = 0;
               int creates = 0;
               String repo = "holder";
                for (JsonNode kal : jsonNode){
                    if (kal.get("type").asString().equals("PushEvent")) {
                        String repo_name = kal.get("repo").get("name").asString();
                        amout_of_pushes.put(repo_name, amout_of_pushes.getOrDefault(repo_name,0)+1);
                    } else if (kal.get("type").asString().equals("CreateEvent")) {

                        repos_created.add(kal.get("repo").get("name").asString());

                    }

                }

                for(Map.Entry<String,Integer> entry : amout_of_pushes.entrySet()){
                    System.out.printf("User pushed %d commits to %s \n",entry.getValue(),entry.getKey());
                }
                for(String repo_name:repos_created){
                    System.out.printf("Started %s \n", repo_name);
                }
            }
            else {
                System.out.println("kekekek");
            }


        } catch (Exception e) {
            System.out.println("something went wronk");
        }

    }
}
