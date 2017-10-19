package gnext.service.elastic.impl;

import gnext.service.elastic.SearchService;
import gnext.bean.elastic.Document;
import gnext.interceptors.annotation.enums.Module;
import gnext.service.config.ConfigService;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import org.apache.commons.lang3.StringUtils;
import static org.elasticsearch.index.query.QueryBuilders.*;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.IndexNotFoundException;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hungpham
 */
@Stateless
public class SearchServiceImpl implements SearchService{

    private static final long serialVersionUID = -6661449388494505812L;
    private Logger LOGGER = LoggerFactory.getLogger(SearchServiceImpl.class);
    private TransportClient client;
    
    @EJB private ConfigService config;
    
    private enum CONFIG{ ELASTIC_HOST, ELASTIC_PORT, ELASTIC_CLUSTER, ELASTIC_ENABLE };
    private String cluster;

    @PostConstruct
    public void startup() {
        try{
                
            String host = config.get(CONFIG.ELASTIC_HOST.toString());
            int port = config.getInt(CONFIG.ELASTIC_PORT.toString());
            cluster = config.get(CONFIG.ELASTIC_CLUSTER.toString());
            String enable = config.get(CONFIG.ELASTIC_ENABLE.toString());
            
            // comment below line for testing
            if(enable.equalsIgnoreCase("false")) return; //return if disable elastic in config
            
            if( StringUtils.isEmpty(host) || StringUtils.isBlank(host) ) host = "127.0.0.1";
            if( port == 0 ) port = 9300;
            if( StringUtils.isEmpty(cluster) || StringUtils.isBlank(cluster)  ) cluster = "elasticsearch";
            
            Settings settings = Settings.settingsBuilder()
                .put("cluster.name", cluster)
                .put("node.client", true)
                .put("client.transport.ignore_cluster_name", false)
                .put("client.transport.ping_timeout", "15s")
	        .put("client.transport.sniff", false)
	        .put("client.transport.nodes_sampler_interval", "15s")
                .build();
            client = TransportClient.builder().settings(settings).build()
                .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(host), port));
        }catch(UnknownHostException e){
            LOGGER.error(e.getMessage(), e);
        }
    }

    @PreDestroy
    public void shutdown() {
        if (client != null) {
            client.close();
        }
    }
    
    @Override
    public String index(Document doc, Module module){
        if(client != null){
            IndexResponse response = createIndexDocument(doc, module).get();
            return response.getId();
        }else{
            return null;
        }
    }
    
    private IndexRequestBuilder createIndexDocument(Document doc, Module module){
        return client.prepareIndex(cluster, module.getName().toLowerCase(), doc.getId()).setSource(doc.getData());
    }
    
    @Override
    public int bulkIndex(List<Document> documents, Module module){
        if(client != null){
            BulkRequestBuilder bulkRequest = client.prepareBulk();
            documents.forEach((document) -> {
                bulkRequest.add(createIndexDocument(document, module));
            });
            BulkResponse bulkResponse = bulkRequest.get();
            if (bulkResponse.hasFailures()) {
                // process failures by iterating through each bulk response item
                for(BulkItemResponse item : bulkResponse.getItems()){
                    System.out.println(item.getId() + "->" + item.getFailureMessage());
                }
                return documents.size() - bulkResponse.getItems().length;
            }
            return documents.size();
        }else{
            return 0;
        }
    }
    
    @Override
    public SearchHits search(Module module, String query, String keywords, List<String> columns){
        SearchRequestBuilder searchBuilder = client.prepareSearch(cluster)
            .setTypes(module.getName())
            .setSearchType(SearchType.DFS_QUERY_THEN_FETCH)
            .setFrom(0).setSize(60).setExplain(true);
        
        if(columns.size() > 0){
            searchBuilder.setFetchSource(columns.toArray(new String[columns.size()]), null);
        }
        
        if( !StringUtils.isEmpty(keywords) ){
            query = StringUtils.isEmpty(query) ? keywords :String.format("%s AND %s", keywords, query);
        }
        
        if( StringUtils.isEmpty(query) ){
            query = "*";
        }
        searchBuilder.setQuery(queryStringQuery(query));
        searchBuilder.addSort("updated_time", SortOrder.DESC);

        try{
            SearchResponse response = searchBuilder.execute().actionGet();
            return response.getHits();
        }catch(IndexNotFoundException infe){
            LOGGER.error(infe.getMessage(), infe);
            return null;
        }
    }
}
