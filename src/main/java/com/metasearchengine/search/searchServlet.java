package com.metasearchengine.search;

import java.io.IOException;
import java.util.List;
import java.lang.String;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.bson.Document;
import com.mongodb.MongoClient;
import com.mongodb.MongoException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.FindIterable;

import static com.mongodb.client.model.Filters.*;

import com.metasearchengine.aggregator.SearchResultsAggregator;

public class searchServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
		/* Create a Mongo DB Client */
		MongoClient mongo = new MongoClient( "localhost" , 27017 );
		
		/* Open a database */
		MongoDatabase db = mongo.getDatabase("testMeta");
	 
	    /* Run query */
		FindIterable<Document> iterable = db.getCollection("filteredList").find(eq("name", (String)request.getParameter("query")));
		
		Iterator<Document> it = iterable.iterator();
		
		String resp = "";
		
		if(it.hasNext())
		{
			resp = resp + it.next().getString("data");
		}
		else
		{
			SearchResultsAggregator.collectData((String)request.getParameter("query"));
			
			/* Run query */
			FindIterable<Document> iterable1 = db.getCollection("filteredList").find(eq("name", (String)request.getParameter("query")));
			
			Iterator<Document> it1 = iterable1.iterator();
			
			if(it1.hasNext())
			{
				resp = resp + it1.next().getString("data");
			}
		}
		
		response.getWriter().write(resp);
	}
}
