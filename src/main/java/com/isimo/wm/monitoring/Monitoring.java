package com.isimo.wm.monitoring;

import com.wm.app.b2b.server.JDBCConnectionManager;
import com.wm.app.b2b.server.Server;
import com.wm.app.b2b.server.ServerAPI;

public class Monitoring implements MonitoringMBean {
	@Override
	public int getFreeDatabaseConnections() {
		return 0;
	}

	@Override
	public float getPercentFreeDatabaseConnections() {
		return 0;
	}

}
