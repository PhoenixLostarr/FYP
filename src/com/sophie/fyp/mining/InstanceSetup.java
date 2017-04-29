package com.sophie.fyp.mining;

import com.sophie.fyp.crawler.ArffData;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

public class InstanceSetup
{
	
	public static Instance setUpInstance(ArffData arffData, String offers, String lf, Instances instances)
	{
		Instance inst = new DenseInstance(6);
		inst.setDataset(instances);
    	inst.setValue(0, arffData.getUrlSimilarity());
		inst.setValue(1, arffData.isRedirection() + "");
	    inst.setValue(2, arffData.getSpellingErrors());
	    
	    
	    if(lf == null || lf.contains("?"))
	    {
	    	inst.setMissing(3);
	    	arffData.setLookAndFeel("?");
	    }
	    else
	    {
	    	inst.setValue(3, lf);
	    	arffData.setLookAndFeel(lf);
	    }
	    
	    
	    if(offers == null || offers.contains("?"))
	    {
	    	inst.setMissing(4);
	    	arffData.setOffers("?");
	    }
	    else
	    {
	    	inst.setValue(4, offers);
	    	arffData.setOffers(offers);
	    }		    
	    
	    inst.setMissing(5);
		
		return inst;
	}
}
