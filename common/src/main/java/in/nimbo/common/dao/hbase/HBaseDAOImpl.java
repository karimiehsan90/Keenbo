package in.nimbo.common.dao.hbase;

import in.nimbo.common.config.HBaseConfig;
import in.nimbo.common.entity.Anchor;
import in.nimbo.common.entity.Page;
import in.nimbo.common.exception.HBaseException;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HBaseDAOImpl implements HBaseDAO {
    private Logger logger = LoggerFactory.getLogger("collector");
    private HBaseConfig config;
    private Connection connection;

    public HBaseDAOImpl(Connection connection, HBaseConfig config) {
        this.connection = connection;
        this.config = config;
    }

    public void close() throws IOException {
        connection.close();
    }

    @Override
    public void add(List<Page> pages) {
        checkConnection();
        addToHBase(getListOfPut(pages));
    }

    @Override
    public void add(List<Page> pages,  List<Map<String, Integer>> keywords) {
        checkConnection();
        addToHBase(getListOfPut(pages, keywords));
    }

    @Override
    public boolean contains(String link) {
        try (Table table = connection.getTable(TableName.valueOf(config.getPageTable()))) {
            Get get = new Get(Bytes.toBytes(link));
            return table.exists(get);
        } catch (IOException e) {
            throw new HBaseException(e);
        }
    }

    private void addToHBase(List<Put> puts) {
        try (Table table = connection.getTable(TableName.valueOf(config.getPageTable()))) {
            logger.info("Start sending bulk put to HBase");
            table.put(puts);
            logger.info("Finish sending bulk put to HBase");
        } catch (IllegalArgumentException | IOException e) {
            throw new HBaseException(e);
        }
    }

    private List<Put> getListOfPut(List<Page> pages, List<Map<String, Integer>> keywords) {
        List<Put> puts = new ArrayList<>();
        for (int i = 0; i < pages.size(); i++) {
            puts.add(getPut(pages.get(i), keywords.get(i)));
        }
        return puts;
    }

    private List<Put> getListOfPut(List<Page> pages) {
        List<Put> puts = new ArrayList<>();
        for (Page page : pages) {
            puts.add(getPut(page));
        }
        return puts;
    }

    private Put getPut(Page page, Map<String, Integer> keywords) {
        checkConnection();
        Put put = getPut(page);
        for (Map.Entry<String, Integer> keyword : keywords.entrySet()) {
            put.addColumn(config.getDataColumnFamily(),
                    Bytes.toBytes(keyword.getKey()), Bytes.toBytes(Integer.toString(keyword.getValue())));
        }
        return put;
    }

    private Put getPut(Page page) {
        checkConnection();
        Put put = new Put(Bytes.toBytes(page.getReversedLink()));

        for (Anchor anchor : page.getAnchors()) {
            put.addColumn(config.getAnchorColumnFamily(),
                    Bytes.toBytes(anchor.getHref()), Bytes.toBytes(anchor.getContent()));
        }

        put.addColumn(config.getDataColumnFamily(), config.getPageRankColumn(), Bytes.toBytes("1"));
        return put;
    }

    @Override
    public Result get(String link) {
        checkConnection();
        try (Table table = connection.getTable(TableName.valueOf(config.getSiteTable()))) {
            Get get = new Get(Bytes.toBytes(link));
            return table.get(get);
        } catch (IOException e) {
            throw new HBaseException(e);
        }
    }

    @Override
    public Result[] getBulk(List<String> links) {
        checkConnection();
        try (Table table = connection.getTable(TableName.valueOf(config.getSiteTable()))) {
            List<Get> getList = new ArrayList<>();
            for (String link : links) {
                getList.add(new Get(Bytes.toBytes(link)));
            }
            return table.get(getList);
        } catch (IOException e) {
            throw new HBaseException(e);
        }
    }

    private void checkConnection() {
        if (connection.isClosed()) {
            try {
                connection = ConnectionFactory.createConnection();
            } catch (IOException e) {
                throw new HBaseException(e);
            }
        }
    }
}
