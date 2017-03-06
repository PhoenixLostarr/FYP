package com.sophie.fyp.crawler;

import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.languagetool.rules.RuleMatch;

public class FYPRuleMatch extends RuleMatch
{
	public FYPRuleMatch(RuleMatch match)
	{
		super(match.getRule(), match.getFromPos(), match.getToPos(), match.getMessage());
		this.setSuggestedReplacements(match.getSuggestedReplacements());
		
	}
	
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
	    if (o == null || getClass() != o.getClass()) return false;
	    FYPRuleMatch other = (FYPRuleMatch) o;
	    List<String> otherReplacements = other.getSuggestedReplacements();
	    List<String> thisReplacements = this.getSuggestedReplacements();
	    if(CollectionUtils.isEqualCollection(otherReplacements, thisReplacements))
	    {
	    	return true;
	    }
		return false;
		
	}
	
	@Override
	public int hashCode() {
		  return 0;
		}

}
