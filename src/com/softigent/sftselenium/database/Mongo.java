package com.softigent.sftselenium.database;

import org.bson.Document;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;

public class Mongo {

	private String host;
	private int port;
	private String dbName;
	private String username;
	private String password;
	
	private MongoClient mongoClient;
	private MongoDatabase database;
	private MongoCollection<Document> collection;

	public Mongo(String host, int port, String dbName) {
		this(host, port, dbName, null, null);
	}

	public Mongo(String host, int port, String dbName, String username, String password) {
		this.host = host;
		this.port = port;
		this.dbName = dbName;
		this.username = username;
		this.password = password;
	}

	public MongoDatabase getConnection() throws Exception {
		if (mongoClient == null) {
			if (username != null && password != null) {
				MongoCredential credential = MongoCredential.createCredential(username, dbName, password.toCharArray());
				MongoClientOptions options = new MongoClientOptions.Builder().build();
				mongoClient = new MongoClient(new ServerAddress(host, port), credential, options);
			} else {
				mongoClient = new MongoClient(host, port);
			}
			database = mongoClient.getDatabase(dbName);
			database.listCollectionNames().first(); //Force Exception hanlder
		}
		return database;
	}

	public MongoCollection<Document> getCollection(String collectionName) throws Exception {
		return getConnection().getCollection(collectionName);
	}
	
	public MongoCollection<Document> changeCollection(String collectionName) throws Exception {
		collection = getCollection(collectionName);
		return collection;
	}

	public Document getDocument(final String key, final Object value) {
		return new Document(key, value);
	}

	public FindIterable<Document> find(String filterKey, Object filterValue) throws Exception {
		return this.find(collection, filterKey, filterValue);
	}
	
	public FindIterable<Document> find(MongoCollection<Document> collection, String filterKey, Object filterValue) throws Exception {
		return collection.find(Filters.eq(filterKey, filterValue));
	}
	
	public UpdateResult update(String filterKey, Object filterValue, String updateKey, Object updateValue) throws Exception {
		return update(collection, filterKey, filterValue, updateKey, updateValue);
	}
	
	public UpdateResult update(MongoCollection<Document> collection, String filterKey, Object filterValue,
			String updateKey, Object updateValue) throws Exception {
		return collection.updateOne(getDocument(filterKey, filterValue),
				new Document("$set", getDocument(updateKey, updateValue)));
	}
	
	public void insert(String filterKey, Object filterValue, String updateKey, Object updateValue) throws Exception {
		insert(collection, filterKey, filterValue, updateKey, updateValue);
	}
	
	public void insert(MongoCollection<Document> collection, String filterKey, Object filterValue,
			String updateKey, Object updateValue) throws Exception {
		collection.insertOne(getDocument(filterKey, filterValue));
	}

	public DeleteResult delete(String filterKey, Object filterValue) throws Exception {
		return this.delete(collection, filterKey, filterValue);
	}
	
	public DeleteResult delete(MongoCollection<Document> collection, String filterKey, Object filterValue) throws Exception {
		return collection.deleteOne(Filters.eq(filterKey, filterValue));
	}
	
	public void close() {
		mongoClient.close();
		mongoClient = null;
	}

	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	public String getDbName() {
		return dbName;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public MongoClient getMongoClient() {
		return mongoClient;
	}

	public MongoDatabase getMongoDatabase() {
		return database;
	}
}
