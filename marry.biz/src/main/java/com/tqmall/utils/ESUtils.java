package com.tqmall.utils;

import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.exists.types.TypesExistsResponse;
import org.elasticsearch.action.admin.indices.mapping.delete.DeleteMappingRequest;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Minutch on 2015-05-31.
 */
public class ESUtils {

    private static Client client;

    /**
     * 获取客户端
     * @return
     */
    public static Client getClient() {

        if (client == null) {
            Settings settings = ImmutableSettings.settingsBuilder()
                    .put("client.transport.sniff",true)
                    .put("cluster.name","Minutch")
                    .build();
            TransportClient transportClient = new TransportClient(settings);
            transportClient.addTransportAddress(new InetSocketTransportAddress("127.0.0.1",9300));
            client = transportClient;
        }
        return client;
    }

    /**
     * 删除类型
     * @param indexName
     * @param typeName
     * @return
     */
    public static boolean deleteType(String indexName,String typeName) {

        try {
            IndicesExistsResponse indicesExistsResponse = getClient().admin()
                    .indices().prepareExists(indexName).execute().actionGet();

            if (indicesExistsResponse.isExists()) {
                TypesExistsResponse response = getClient().admin().indices()
                        .prepareTypesExists(indexName).setTypes(typeName)
                        .execute().actionGet();
                if (response.isExists()) {
                    // 删除已有type
                    DeleteMappingRequest deleteMapping = Requests.deleteMappingRequest(indexName).types(typeName);
                    getClient().admin().indices().deleteMapping(deleteMapping).actionGet();
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 处理批量创建索引
     * @param indexName
     * @param typeName
     * @param primaryKeyName
     * @param documentList
     * @return
     */
    public static boolean processCreateIndex(String indexName,String typeName,String primaryKeyName,List<Map<String,Object>> documentList) {

        if (documentList == null || documentList.size()==0) {
            System.out.println("bulkInsertData:待创建的文档为空!");
            return true;
        }
        Map<String, XContentBuilder> xContentBuilderMap = makeXContent(primaryKeyName, documentList);
        boolean result = bulkCreateIndex(indexName,typeName,xContentBuilderMap);
        return result;
    }


    /**
     * 将文档转成JSON格式
     * @param primaryKeyName
     * @param documentList
     * @return
     */
    public  static Map<String,XContentBuilder> makeXContent(String primaryKeyName,List<Map<String,Object>> documentList) {

        Map<String, XContentBuilder> xContentBuilderMap = new HashMap<String, XContentBuilder>();
        for (Map<String,Object> document:documentList) {
            XContentBuilder xContentBuilder = null;

            //开始构建JSON
            try {
                xContentBuilder = XContentFactory.jsonBuilder().startObject();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //文档内容构建
            for (String key : document.keySet()) {
                Object value = document.get(key);
                try {
                    xContentBuilder.field(key,value);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            //构建JSON结束
            try {
                xContentBuilder.endObject();
            } catch (IOException e) {
                e.printStackTrace();
            }

            xContentBuilderMap.put(String.valueOf(document.get(primaryKeyName)),xContentBuilder);
        }
        return xContentBuilderMap;
    }

    /**
     * 批量创建索引
     * @param indexName
     * @param typeName
     * @param xContentBuilderMap
     * @return
     */
    public static boolean bulkCreateIndex(String indexName,String typeName,Map<String,XContentBuilder> xContentBuilderMap) {

        if (xContentBuilderMap==null || xContentBuilderMap.size()==0) {
            return true;
        }

        try {
            //refresh为false表示不立即刷新
            BulkRequestBuilder bulkRequestBuilder = getClient().prepareBulk().setRefresh(false);

            //构造批量请求
            for (String primaryKeyValue : xContentBuilderMap.keySet()) {
                IndexRequestBuilder indexRequestBuilder = getClient().prepareIndex(indexName,typeName,primaryKeyValue).setSource(xContentBuilderMap.get(primaryKeyValue));
                bulkRequestBuilder.add(indexRequestBuilder);
            }

            //执行批量请求
            BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
            if (!bulkResponse.hasFailures()) {
                return true;
            } else {
                System.err.println(bulkResponse.buildFailureMessage());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 快速批量刷新索引，不刷新
     * @param indexName
     * @param typeName
     * @param idList
     * @return
     */
    public static boolean bulkDeleteIndex(String indexName,String typeName,List<Integer> idList) {

        if (idList == null || idList.size()==0) {
            return true;
        }

        try {
            BulkRequestBuilder bulkRequestBuilder = getClient().prepareBulk();
            for (Integer id : idList) {
                bulkRequestBuilder.add(getClient().prepareDelete(indexName,typeName,String.valueOf(id)));
            }
            BulkResponse bulkResponse = bulkRequestBuilder.execute().actionGet();
            if (bulkResponse.hasFailures()) {
                System.out.println(bulkResponse.buildFailureMessage());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }
}






















