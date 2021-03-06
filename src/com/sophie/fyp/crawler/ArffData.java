package com.sophie.fyp.crawler;

public class ArffData 
{
	/**
	 * This class will contain the data to be written to the ARFF file.
	 * 
	 */
	private int urlSimilarity;
	private boolean redirection;
	private int spellingErrors;
	
	private String lookAndFeel;
	private String offers;
	
	public String getLookAndFeel()
	{
		return lookAndFeel;
	}
	public void setLookAndFeel(String lookAndFeel)
	{
		this.lookAndFeel = lookAndFeel;
	}
	public String getOffers()
	{
		return offers;
	}
	public void setOffers(String offers)
	{
		this.offers = offers;
	}
	public int getUrlSimilarity()
	{
		return urlSimilarity;
	}
	public void setUrlSimilarity(int urlSimilarity)
	{
		this.urlSimilarity = urlSimilarity;
	}
	public boolean isRedirection()
	{
		return redirection;
	}
	public void setRedirection(boolean redirection)
	{
		this.redirection = redirection;
	}
	public int getSpellingErrors()
	{
		return spellingErrors;
	}
	public void setSpellingErrors(int errors)
	{
		this.spellingErrors = errors;
	}
	
	@Override
	public String toString()
	{
		return String.format("urlsimilarity: %d, redirection: "
				+ "%b,spellingErrors: %d,lookandfeel: %s, offers: %s", 
				urlSimilarity,redirection,spellingErrors,lookAndFeel,offers);
	}
	
}


