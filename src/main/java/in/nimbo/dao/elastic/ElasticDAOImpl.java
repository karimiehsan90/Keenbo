package in.nimbo.dao.elastic;

import in.nimbo.config.ElasticConfig;
import in.nimbo.entity.Page;
import in.nimbo.exception.ElasticException;
import org.apache.http.HttpHost;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ElasticDAOImpl implements ElasticDAO {
    private final ElasticConfig config;
    private RestHighLevelClient client;

    public ElasticDAOImpl(ElasticConfig config, RestHighLevelClient client) {
        this.config = config;
        this.client = client;
    }

    public ElasticDAOImpl(ElasticConfig config) {
        this.config = config;
        client = new RestHighLevelClient(RestClient.builder(new HttpHost(config.getHost(), config.getPort())));
    }

    @Override
    public void save(Page page) {
        try {
            IndexRequest request = new IndexRequest(config.getIndexName())
                    .id(page.getLink()).type(config.getType());

            XContentBuilder builder = XContentFactory.jsonBuilder();
            builder.startObject();
            builder.field("title", page.getTitle());
            builder.field("link", page.getLink());
            builder.field("content", page.getContentWithoutTags());
            builder.field("meta", page.getMetas());
            builder.field("point", page.getPageRate());
            builder.endObject();
            request.source(builder);
            IndexResponse index = client.index(request, RequestOptions.DEFAULT);
            if (index.getResult() != DocWriteResponse.Result.CREATED)
                throw new ElasticException("Indexing failed!");
        } catch (IOException e) {
            throw new ElasticException("Indexing failed!", e);
        }
    }

    @Override
    public Optional<String> get(String link) {
        try {
            GetRequest getRequest = new GetRequest(config.getIndexName(), config.getType(), link);
            GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
            String text = null;
            if (response.isExists()) {
                text = (String) response.getSource().get("text");
            }
            return Optional.ofNullable(text);
        } catch (IOException e) {
            throw new ElasticException("Get failed", e);
        } catch (ClassCastException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<String> getAllLinks() {
        try {
            ArrayList<String> links = new ArrayList<>();
            SearchRequest searchRequest = new SearchRequest(config.getIndexName());
            searchRequest.types(config.getType());
            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
            searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            searchRequest.source(searchSourceBuilder);
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
            SearchHit[] hits = response.getHits().getHits();
            for (SearchHit hit : hits)
                links.add(hit.getId());
            return links;
        } catch (IOException e) {
            throw new ElasticException("Search failed", e);
        }

    }
}
