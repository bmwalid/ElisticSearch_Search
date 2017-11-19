package techn.novagen.utils;

import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.put.PutMappingRequest;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.profile.ProfileShardResult;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by h_ste on 08/12/2016.
 */
public class ElasticUtils {


    public static final String IP_ADDRESS = "137.74.41.51";
    public static Client client = getClient();

    public static Client getClient() {

        //la  version 2.4.0 --> � changer pour la version 5.0.0
        TransportClient client = null;
        try {
            client = TransportClient.builder().build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(IP_ADDRESS), 9300));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        return client;
    }


    public static void deleteAndCreateIndex() {
        final IndicesExistsResponse tuto01 = client.admin().indices().exists(new IndicesExistsRequest("tuto01")).actionGet();
        final boolean exists = tuto01.isExists();
        System.out.println("Existe ?  " + exists);
        if (exists) {
            client.admin().indices().delete(new DeleteIndexRequest("tuto01"));
        }
        client.admin().indices().create(new CreateIndexRequest("tuto01"));
        final PutMappingRequest tuto011 = new PutMappingRequest("tuto01");
        tuto011.source(createStringFromResource("/tps/01/mapping_01.json"));
        client.admin().indices().putMapping(tuto011);


        final List<String> tt = createListStringFromResource("/tps/01/data01.json");

        tt.forEach(t -> {
            try {
                client.prepareIndex().setIndex("tuto01").setType("sample").setSource(t.getBytes()).execute().actionGet();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });


    }

    public static String createStringFromResource(String nameResource) {
        InputStream ras = ElasticUtils.class.getResourceAsStream(nameResource);
        InputStreamReader in = new InputStreamReader(ras);
        BufferedReader bri = new BufferedReader(in);

        final String collect = bri.lines().collect(Collectors.joining());
        return collect;

    }

    public static List<String> createListStringFromResource(String nameResource) {
        InputStream ras = ElasticUtils.class.getResourceAsStream(nameResource);
        InputStreamReader in = new InputStreamReader(ras);
        BufferedReader bri = new BufferedReader(in);
        return bri.lines().collect(Collectors.toList());

    }


    public static void excuteSimpleAggregation() {

        final SearchRequestBuilder srb = client.prepareSearch("tuto01").setTypes("sample")
                .setQuery(QueryBuilders.matchAllQuery())
                .addAggregation(AggregationBuilders.terms("auteurs").field("auteur"))
                .setExplain(true)//pour calculer le score
                .setProfile(true);//pour expliquer les performances

        final SearchResponse searchResponse = srb.execute().actionGet();
        final Map<String, List<ProfileShardResult>> profileResults = searchResponse.getProfileResults();
        System.out.println("profileResults = " + profileResults);
        profileResults.forEach( (t,p) -> {
            System.out.println("t = " + t);
            p.stream().forEach( pp -> {
                System.out.println("p = " + pp.getQueryResults().get(0));
            });
        });

    }
}
