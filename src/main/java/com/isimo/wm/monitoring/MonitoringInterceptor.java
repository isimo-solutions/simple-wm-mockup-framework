package com.isimo.wm.monitoring;

import java.lang.management.ManagementFactory;
import java.util.Iterator;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import com.isimo.wm.mockupframework.InterceptorBase;
import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.util.ServerException;

public class MonitoringInterceptor extends InterceptorBase implements InvokeChainProcessor {
	static {
		MonitoringInterceptor.registerMBean();
	}
	
	public void process(Iterator chain, BaseService service, IData pipeline,
			ServiceStatus status) throws ServerException {
		continueToNext(chain, service, pipeline, status);
	}
	
	
	public static void registerMBean() {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
			ObjectName name = new ObjectName("com.wm.monitoring:type=basic,name=monitoring");
			mbs.registerMBean(new Monitoring(), name);
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}
}
