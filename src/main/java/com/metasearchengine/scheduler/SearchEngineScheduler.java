package com.metasearchengine.scheduler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.billylieurance.azuresearch.AzureSearchResultSet;
import net.billylieurance.azuresearch.AzureSearchWebQuery;
import net.billylieurance.azuresearch.AzureSearchWebResult;

import org.carrot2.clustering.lingo.LingoClusteringAlgorithm;
import org.carrot2.core.Controller;
import org.carrot2.core.ControllerFactory;
import org.carrot2.core.Document;
import org.carrot2.core.ProcessingResult;
import org.carrot2.core.attribute.CommonAttributesDescriptor;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.google.common.collect.Maps;

public class SearchEngineScheduler implements Job
{

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		AzureSearchWebQuery aq = new AzureSearchWebQuery();
		aq.setAppid("TEGsX/02tQYrLLKq8UOXK7RQF7NXs5WzOxqgxAPFfoY");
		aq.setQuery("IBM products");
		// The results are paged. You can get 50 results per page max.
		// This example gets 150 results
		ArrayList<Document> documents = new ArrayList<Document>();
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

			}
		}

		try {
			saveResultsToJSON(documents);
		} catch (IOException e) {
			e.printStackTrace();
		}		
	}
	
	private static void saveResultsToJSON(List<Document> document)
			throws IOException {
		// Let's fetch some results from MSN first
		final Controller controller = ControllerFactory.createSimple();
		final Map<String, Object> attributes = Maps.newHashMap();
		CommonAttributesDescriptor.attributeBuilder(attributes)
				.documents(new ArrayList<Document>(document)).query("IBM products");

		final ProcessingResult result = controller.process(attributes,
				LingoClusteringAlgorithm.class);

		// Now, we can serialize the entire result to XML like this
		result.serializeJson(new PrintWriter(System.out));
		System.out.println();

		// Optionally, we can provide a callback for JSON-P-style calls
		result.serializeJson(new PrintWriter(System.out), "loadResults",
				true /* indent */, false /* save documents */, true /*
																	 * save
																	 * clusters
																	 */);
	}

}
