package org.changsha.changshapoc.service.impl;

//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoDatabase;
//import com.mongodb.client.model.Filters;
//import org.bson.Document;
import org.changsha.changshapoc.service.MongoDBService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

@Service
public class MongoDBServiceImpl implements MongoDBService {

//    @Override
//    public JSONArray getMongoDBData(Long startTime, Long endTime) {
//        MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
//        MongoDatabase database = mongoClient.getDatabase("mydb");
//        MongoCollection<Document> collection = database.getCollection("users");
//        JSONArray jsonArray = new JSONArray();
//        for (Document doc : collection.find(Filters.and(Filters.gt("logTime", startTime), Filters.lte("logTime", endTime)))) {
//            JSONObject jsonObject = new JSONObject(doc.toJson());
//            jsonArray.put(jsonObject);
//        }
//        return jsonArray;
//    }
}
