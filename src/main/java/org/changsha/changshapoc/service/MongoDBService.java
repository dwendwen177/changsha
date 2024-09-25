package org.changsha.changshapoc.service;

import org.json.JSONArray;

public interface MongoDBService {
    JSONArray getMongoDBData(String collectionName);
}
