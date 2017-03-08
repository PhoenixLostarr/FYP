package com.sophie.fyp.ui;

public class Result
{
	private boolean isPhishing;
	private double accuracy;
	
	public Result(boolean isPhishing, double accuracy)
	{
		super();
		this.isPhishing = isPhishing;
		this.accuracy = accuracy;
	}
	public double getAccuracy()
	{
		return accuracy;
	}
	public void setAccuracy(double accuracy)
	{
		this.accuracy = accuracy;
	}
	
	public boolean isPhishing()
	{
		return isPhishing;
	}
	public void setPhishing(boolean isPhishing)
	{
		this.isPhishing = isPhishing;
	}
}
