package in.nimbo.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import in.nimbo.config.AppConfig;
import in.nimbo.dao.elastic.ElasticDAO;
import in.nimbo.dao.hbase.HBaseDAO;
import in.nimbo.entity.Page;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.*;

public class crawlerServiceTest {
    private static final String CONFIG_NAME = "app-config.properties";
    private static HBaseDAO hBaseDAO;
    private static ElasticDAO elasticDAO;
    private static ParserService parserService;
    private static AppConfig appConfig;
    private static Cache<String, LocalDateTime> cache;
    private static CrawlerService crawlerService;
    private static Optional<Page> page;
    private static String link;
    private static List<String> crawledLinks;
    private static String content;

    @BeforeClass
    public static void init() throws ConfigurationException {
        elasticDAO = mock(ElasticDAO.class);
        parserService = mock(ParserService.class);
        appConfig = new AppConfig();
        PropertiesConfiguration config = new PropertiesConfiguration(CONFIG_NAME);
        appConfig.setCaffeineMaxSize(config.getInt("caffeine.max.size"));
        appConfig.setCaffeineExpireTime(config.getInt("caffeine.expire.time"));
        Cache<String, LocalDateTime> cache = Caffeine.newBuilder().maximumSize(appConfig.getCaffeineMaxSize())
                .expireAfterWrite(appConfig.getCaffeineExpireTime(), TimeUnit.SECONDS).build();
        link = "http://nimbo.in/";
        content = "Be your best!";
        crawledLinks = new ArrayList<>();
        crawledLinks.add("https://www.google.com/");
        crawledLinks.add("https://stackoverflow.com/");
        crawledLinks.add("https://www.sahab.ir/");
        page = Optional.of(new Page(content, crawledLinks));
    }

    @Before
    public void createCash() {
        hBaseDAO = mock(HBaseDAO.class);
        when(parserService.parse(link)).thenReturn(page);
        doNothing().when(elasticDAO).save(link, content);
        doNothing().when(hBaseDAO).add(link);
        cache = Caffeine.newBuilder().maximumSize(appConfig.getCaffeineMaxSize())
                .expireAfterWrite(appConfig.getCaffeineExpireTime(), TimeUnit.SECONDS).build();
        crawlerService = new CrawlerService(appConfig, cache, hBaseDAO, elasticDAO, parserService);
    }

    @Test
    public void crawlTest() throws URISyntaxException {
        when(hBaseDAO.contains(link)).thenReturn(false);
        List<String> answer = crawlerService.crawl(link);
        Assert.assertEquals(answer, crawledLinks);
    }
}