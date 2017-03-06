package com.sophie.fyp.crawler;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class Controller 
{
	private static int numberOfCrawlers = 1;
//	public static void main(String[] args) throws Exception 
//	{
//        String crawlStorageFolder = "data";
//        
//
//        CrawlConfig config = new CrawlConfig();
//        config.setCrawlStorageFolder(crawlStorageFolder);
//        int sitesToCrawl = 2000;
//        config.setMaxPagesToFetch(sitesToCrawl);
//        /*
//         * Instantiate the controller for this crawl.
//         */
//        PageFetcher pageFetcher = new PageFetcher(config);
//        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
//        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
//        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
//        //crawlFromFile(config, controller,sitesToCrawl,sitesToCrawl);
//        crawlFromSeeds(controller);
//    }
	
	public static void crawlFromSeeds(CrawlController controller)
	{
		controller.addSeed("http://www.xkcd.com");
        controller.addSeed("http://php.com");
        controller.addSeed("http://oracle.com");
        controller.addSeed("http://www.reddit.com");
        controller.addSeed("http://www.twitter.com");
        controller.addSeed("http://www.google.com");
        /*
         * Start the crawl. This is a blocking operation, meaning that your code
         * will reach the line after this only when crawling is finished.
         */
        controller.start(FYPCrawler.class, numberOfCrawlers);
	}
	
	public static void crawlFromFile(CrawlConfig config, CrawlController controller,int sitesToCrawl,int offset)
	{
		String urlFile = "/home/sophie/Documents/Computer Science Degree Y3/Final Year Project/Data/results.txt";
		config.setMaxDepthOfCrawling(0);
        try(BufferedReader reader = new BufferedReader(new FileReader(new File(urlFile)));)
		{			
			String url = null;
			int count = 0;
			int offsetCount = 0;
			//skip offset lines
			
			while(offsetCount <= offset)
			{
				System.out.println("Skipping " + reader.readLine());
				offsetCount++;
			}
			while((url = reader.readLine()) != null && count <sitesToCrawl)
			{
				if(url.trim() != "")
				{
					System.out.println("adding seed: " + url + " " + count);
					controller.addSeed(url);
					count++;
				}
			}
			controller.start(FYPCrawler.class, numberOfCrawlers);
		}
		catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void crawlSingleSite(String url) throws Exception
	{
	      String crawlStorageFolder = "data";
	      
	
	    CrawlConfig config = new CrawlConfig();
	    config.setCrawlStorageFolder(crawlStorageFolder);
        int sitesToCrawl = 1;
        config.setMaxPagesToFetch(sitesToCrawl);
     
	    PageFetcher pageFetcher = new PageFetcher(config);
	    RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
	    RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
	    CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
		config.setMaxDepthOfCrawling(0);
		controller.addSeed(url);
		controller.start(FYPCrawler.class, numberOfCrawlers);
		
	}
	
	

}
