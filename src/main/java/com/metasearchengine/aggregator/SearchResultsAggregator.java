package com.metasearchengine.aggregator;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import net.billylieurance.azuresearch.AzureSearchResultSet;
import net.billylieurance.azuresearch.AzureSearchWebQuery;
import net.billylieurance.azuresearch.AzureSearchWebResult;

import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.Document;
import org.carrot2.core.ProcessingResult;
import org.carrot2.core.attribute.CommonAttributesDescriptor;

import com.google.common.collect.Maps;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.DBCollection;

public class SearchResultsAggregator
{
	public static void collectData(String query) {
		AzureSearchWebQuery aq = new AzureSearchWebQuery();
		aq.setAppid("TEGsX/02tQYrLLKq8UOXK7RQF7NXs5WzOxqgxAPFfoY");
		aq.setQuery(query);
		// The results are paged. You can get 50 results per page max.
		// This example gets 150 results
		ArrayList<Document> documents = new ArrayList<Document>();
		List<AzureSearchWebResult> resList = new ArrayList<AzureSearchWebResult>();
		for (int i = 1; i <= 3; i++) {
			aq.setPage(i);
			aq.doQuery();
			AzureSearchResultSet<AzureSearchWebResult> ars = aq
					.getQueryResult();
			for (AzureSearchWebResult result : ars) {
				System.out.println(result.getTitle());
				System.out.println(result.getUrl());
				System.out.println(result.getDisplayUrl());
				System.out.println(result.getDescription());
				System.out.println("\n");

				documents.add(new Document(result.getUrl(), result.getTitle(),
						result.getDescription()));
						
				resList.add(result);
			}
		}

		try {
			filterAndAddToDB(query, resList, documents);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private static void filterAndAddToDB(String query, List<AzureSearchWebResult> resList, List<Document> document)
			throws IOException {
		// Let's fetch some results from MSN first
		final Controller controller = ControllerFactory.createSimple();
		final Map<String, Object> attributes = Maps.newHashMap();
		CommonAttributesDescriptor.attributeBuilder(attributes)
				.documents(new ArrayList<Document>(document)).query(query);

		final ProcessingResult result = controller.process(attributes,
				LingoClusteringAlgorithm.class);
				
		Iterator<AzureSearchWebResult> it = resList.iterator();

		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		ArrayNode resultArray = mapper.createArrayNode();
		while(it.hasNext()) {
			AzureSearchWebResult resultItr = (AzureSearchWebResult)it.next();
			ObjectNode resultObject = mapper.createObjectNode();
			resultObject.put("title",resultItr.getTitle());
			resultObject.put("url",resultItr.getUrl());
			resultObject.put("desc",resultItr.getDescription());
			resultArray.add(resultObject);
			
		}
		rootNode.put("results",resultArray);
		
		/* Create a Mongo DB Client */
		MongoClient mongo = new MongoClient( "localhost" , 27017 );
		
		/* Open a database */
		MongoDatabase db = mongo.getDatabase("testMeta");
		
		org.bson.Document dbDoc = new org.bson.Document("name", query);
		dbDoc.append("data", rootNode.toString());
		 
		db.getCollection("filteredList").insertOne(dbDoc);
	}

}
