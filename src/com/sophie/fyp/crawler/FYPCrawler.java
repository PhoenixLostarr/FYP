package com.sophie.fyp.crawler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.crawler.WebCrawler;
import edu.uci.ics.crawler4j.url.WebURL;

public class FYPCrawler extends WebCrawler 
{
	public static ArffData getData()
	{
		return data;
	}

	
	public static ArffData data;
	private Map<String,Boolean> redirections = new HashMap<>();

	@Override
	public boolean shouldVisit(Page referringPage, WebURL url)
	{
		System.out.println("FYPCrawler.shouldVisit()");
		if(referringPage.getRedirectedToUrl() != null)
		{
			redirections.put(url.getURL(), true);
		}
		return true;
			
		
	}

	@Override
	public void visit(Page page)
	{
		System.out.println("FYPCrawler.visit()");
		WebURL webUrl = page.getWebURL();
		String url = webUrl.getURL();
		System.out.println("URL: " + url);
		data = new ArffData();
		try
		{
			String domain = webUrl.getDomain();			
		
			data.setUrlSimilarity(DataGatherer.readLevenshtein(domain));
					
			System.out.println("Domain: " + domain);
			data.setRedirection(DataGatherer.getRedirectionStatus(url));	
			
			Response response = Jsoup.connect(url).execute();
			data.setSpellingErrors(DataGatherer.getSpellingErrors(response).size());
			System.out.println(data);
			
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	

	
}
