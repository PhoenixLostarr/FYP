package com.sophie.fyp.crawler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;
import org.languagetool.JLanguageTool;
import org.languagetool.language.BritishEnglish;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;

public class DataGatherer
{
	private static final String DOMAINS_FILE = "domains.txt";
	public static int readLevenshtein( String domain) throws IOException
	{
		BufferedReader reader = new BufferedReader(new FileReader(DOMAINS_FILE));
		int lowestDistance = Integer.MAX_VALUE;
		String line = reader.readLine();
		while(line != null)
		{
			
			int levenshteinDistance = StringUtils.getLevenshteinDistance(domain, line);
			if(levenshteinDistance < lowestDistance)
			{
				lowestDistance = levenshteinDistance;					
			}
			if(lowestDistance ==0)
			{
				break;
			}
			line = reader.readLine();
			
			
		}
		reader.close();
		return lowestDistance;
	}
	
	public static boolean getRedirectionStatus(String url) throws IOException
	{
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setInstanceFollowRedirects(false);
		conn.connect();
		if(Integer.toString(conn.getResponseCode()).startsWith("3"))
		{
			return true;
		}
		return false;
	}
	
	public static Set<FYPRuleMatch> getSpellingErrors(Response response) throws IOException
	{
		Document doc = response.parse();
		
		String text = doc.text();
		JLanguageTool jlang = new JLanguageTool(new BritishEnglish());
		for (Rule rule : jlang.getAllRules()) {
			  if (!rule.isDictionaryBasedSpellingRule()) {
			    jlang.disableRule(rule.getId());
			  }
			}
		List<RuleMatch> matches = jlang.check(text);
		Set<FYPRuleMatch> fypMatches = new HashSet<>();
		for(RuleMatch match: matches)
		{

			  fypMatches.add(new FYPRuleMatch(match));
		}
		
		return fypMatches;
	}
}
