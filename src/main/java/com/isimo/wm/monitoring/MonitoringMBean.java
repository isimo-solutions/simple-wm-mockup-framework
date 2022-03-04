package com.isimo.wm.monitoring;

public interface MonitoringMBean {
	public int getFreeDatabaseConnections();
	public float getPercentFreeDatabaseConnections();
}
