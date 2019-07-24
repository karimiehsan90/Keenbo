package in.nimbo.dao.elastic;

import in.nimbo.config.ElasticConfig;
import in.nimbo.entity.Page;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ElasticCLI {
    public static void main(String[] args) {
        ElasticConfig elasticConfig = ElasticConfig.load();
        RestHighLevelClient restHighLevelClient = new RestHighLevelClient(
                RestClient.builder(new HttpHost(elasticConfig.getHost(), elasticConfig.getPort())));
        ElasticDAO elasticDAO = new ElasticDAOImpl(restHighLevelClient, elasticConfig);
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("cmd> ");
            String type = input.next();
            switch (type) {
                case "getAllPages":
                    List<Page> allPages = elasticDAO.getAllPages();
                    for (Page page : allPages) {
                        System.out.println(page.getLink());
                        System.out.println(page.getTitle());
                        System.out.println("-----");
                    }
                    break;
                case "get": {
                    String link = input.next();
                    Optional<Page> page = elasticDAO.get(link);
                    if (page.isPresent())
                        System.out.println(page.get().getLink() + " " + page.get().getTitle());
                    else
                        System.out.println("Unable to find link: " + link);
                    break;
                }
                case "exit":
                    System.out.println("Good bye");
                    System.exit(0);
                default:
                    System.out.println("Invalid input");
                    break;
            }
        }
    }
}