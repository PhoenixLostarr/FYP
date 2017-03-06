package com.sophie.fyp.crawler.test;



import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Set;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.junit.Before;
import org.junit.Test;

import com.sophie.fyp.crawler.DataGatherer;
import com.sophie.fyp.crawler.FYPRuleMatch;

public class FYPCrawlerTest
{
	String url, redirectedURL;
	
	
	
	@Before
	public void setUp()
	{
		 url = "http://www.ebay.com/";
		 redirectedURL = "http://goo.gl/7AJgP";
	}
	
	@Test
	public void testSpellingErrors()
	{
		//getting spelling errors requires a JSoup connection, so lets get one
		Response response = null;
		Set<FYPRuleMatch> matches = null;
		try
		{
			response = Jsoup.connect(url).execute();
		}
		catch (IOException e)
		{
			fail("Could not connect to test server");
		}
		
		try
		{
			matches = DataGatherer.getSpellingErrors(response);
		}
		catch (IOException e)
		{
			fail("Failed to parse fetched response");
		}
		assertTrue(matches != null);
		assertTrue(matches.size() > 0);
	}
	
	@Test
	public void testNegativeRedirectionStatus()
	{
		
		try
		{
			//only want to test if the given URL redirects - dont care where to
			boolean isRedirect = DataGatherer.getRedirectionStatus(url);
			assertFalse(isRedirect);
			
		}
		catch (IOException e)
		{
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testPositiveRedirectionStatus()
	{	
		
		
		try
		{			
			//only want to test if the given URL redirects - dont care where to
			boolean shouldRedirect = DataGatherer.getRedirectionStatus(redirectedURL);
			assertTrue(shouldRedirect);	
			
		}
		catch (IOException e)
		{
			fail(e.getMessage());
		}		
	}
	
	@Test
	public void testLevenshteinInList()
	{		
		try
		{
			String domain = "ebay.com"; //domain of our ebay website
			int levenshtein = DataGatherer.readLevenshtein(domain);
			assertTrue(levenshtein == 0); //should be in the list, hence return 0
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testLevenshteinNotInList()
	{		
		try
		{
			String domain = "goo.gl"; //domain of our shortened website
			int levenshtein = DataGatherer.readLevenshtein(domain);
			assertFalse(levenshtein == 0); //should not be in the list, hence return +ve int
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	@Test
//	public void testVisit()
//	{
//		
//		WebURL webUrl = new WebURL();
//		webUrl.setURL(url);
//		File file = new File(FYPCrawler.DATA_FILE);
//		Page page = new Page(webUrl);
//		assertTrue(file.exists());
//		long prevFileLength = file.length();
//		assertTrue(prevFileLength > 0);
//		
//		crawler.visit(page);
//		
//		long currentFileLength = file.length();
//		assertTrue(currentFileLength > prevFileLength);
//			
//		
//		
//	}
}
