package com.isimo.wm.mockupframework;

import java.util.Iterator;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.Server;
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSNode;
import com.wm.lang.ns.NSType;
import com.wm.lang.ns.Namespace;
import com.wm.util.Config;
import com.wm.util.JournalLogger;
import com.wm.util.ServerException;

public class MockingInterceptor implements InvokeChainProcessor {
	static NSType SERVICE_TYPE = NSType.create("Service");
	public void process(Iterator paramIterator, BaseService paramBaseService, IData paramIData,
			ServiceStatus paramServiceStatus) throws ServerException {
		// read the invoke manager extended setting
		String profilesStr = Config.getProperty("watt.server.mockinginterceptor.profiles");
		String[] profiles = profilesStr.split(",");
		NSName ns = paramBaseService.getNSName();
		Namespace namespace = paramBaseService.getNamespace();
		for(String profile: profiles) {
			NSName mockCandidateNS = fromBaseName(ns, profile);
			NSNode mockCandidateNode = namespace.getNode(mockCandidateNS);
			if(mockCandidateNode != null && mockCandidateNode.getNodeTypeObj().equals(SERVICE_TYPE)) {
				paramBaseService.setNSName(mockCandidateNS);
				JournalLogger.logInfo(0, JournalLogger.FAC_SERVICE, "Service "+ns.getFullName()+" mocked by "+mockCandidateNS.getFullName());
			}
		}
	}
	
	NSName fromBaseName(NSName baseName, String profile) {
		NSName retval = NSName.create(baseName.getInterfaceName()+".mockup_"+profile, baseName.getNodeName().toString());
		return retval;
	}
}
