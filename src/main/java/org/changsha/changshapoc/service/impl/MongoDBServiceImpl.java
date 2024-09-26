package org.changsha.changshapoc.service.impl;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.changsha.changshapoc.service.MongoDBService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class MongoDBServiceImpl implements MongoDBService {

    @Value("${mongo.datasource.url}")
    private String mongoDBUrl;

    @Value("${mongo.datasource.database}")
    private String mongoDBDatabase;

//    @Value("${mongo.datasource.limit}")
//    private Integer limit;

    @Override
    public JSONArray getMongoDBData(String collectionName, Integer limit) {
        MongoClient mongoClient = MongoClients.create(mongoDBUrl);
        MongoDatabase database = mongoClient.getDatabase(mongoDBDatabase);
        MongoCollection<Document> collection = database.getCollection(collectionName);
        JSONArray jsonArray = new JSONArray();
        for (Document doc : collection.find().limit(limit)) {
            JSONObject jsonObject = new JSONObject(doc.toJson());
            if (doc.getDate("logTime") != null) {
                Date logTime = doc.getDate("logTime");
                jsonObject.remove("logTime");
                jsonObject.put("logTime", logTime);
            }
            jsonArray.put(jsonObject);
        }
        return jsonArray;
    }

    public static void main(String[] args) {
        MongoClient mongoClient = MongoClients.create("mongodb://127.0.0.1:12920");
        MongoDatabase database = mongoClient.getDatabase("changshaBankPOC");
        MongoCollection<Document> collection = database.getCollection("host");
//        // 创建文档
//        Document doc1 = new Document("name", "John Biden")
//                .append("age", 35)
//                .append("city", "London");
//
//        Document doc2 = new Document("name", "Jane Taylor")
//                .append("age", 20)
//                .append("city", "Paris");
//
//        // 插入文档到集合
//        collection.insertMany(Arrays.asList(doc1, doc2));
        JSONArray jsonArray = new JSONArray();
        for (Document doc : collection.find()) {
            JSONObject jsonObject = new JSONObject(doc.toJson());
            jsonArray.put(jsonObject);
        }
        System.out.println(jsonArray);

        mongoClient.close();
    }
}
