package com.isimo.wm.mockupframework;

import java.util.Iterator;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.util.JournalLogger;
import com.wm.util.ServerException;

public abstract class InterceptorBase implements InvokeChainProcessor {
	protected static void log(String msg) {
		JournalLogger.log(3, 90, JournalLogger.INFO, msg);
	}

	@Override
	public abstract void process(Iterator arg0, BaseService arg1, IData arg2, ServiceStatus arg3) throws ServerException;

	protected void continueToNext(Iterator chain, BaseService service, IData pipeline, ServiceStatus status) throws ServerException {
		if (chain.hasNext())
			((InvokeChainProcessor)chain.next()).process(chain, service, pipeline, status);
	}

	
}
