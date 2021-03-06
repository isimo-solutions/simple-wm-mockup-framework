package com.isimo.wm.mockupframework;

import java.util.Iterator;

import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.lang.ns.NSName;
import com.wm.lang.ns.NSNode;
import com.wm.lang.ns.NSService;
import com.wm.lang.ns.Namespace;
import com.wm.util.Config;
import com.wm.util.ServerException;

public class MockingInterceptor extends InterceptorBase implements InvokeChainProcessor {	
	public MockingInterceptor() {
		log("Mocking Interceptor initialized");
	}
	
	public void process(Iterator chain, BaseService service, IData pipeline,
			ServiceStatus status) throws ServerException {
		// read the invoke manager extended setting
		String profilesStr = Config.getProperty("watt.server.mockinginterceptor.profiles");
		if(profilesStr==null || profilesStr.isEmpty()) {
			continueToNext(chain, service, pipeline, status);
			return;
		}
		String[] profiles = profilesStr.split(",");
		NSName ns = service.getNSName();
		Namespace namespace = service.getNamespace();
		for(String profile: profiles) {
			NSName mockCandidateNS = fromBaseName(ns, profile);
			NSNode mockCandidateNode = namespace.getNode(mockCandidateNS);
			if(mockCandidateNode != null && mockCandidateNode.getNodeTypeObj().equals(NSService.TYPE)) {
				service = (BaseService) mockCandidateNode;
				log("Service "+ns.getFullName()+" mocked by "+mockCandidateNS.getFullName());
				break;
			}
		}
		continueToNext(chain, service, pipeline, status);
	}
	
	NSName fromBaseName(NSName baseName, String profile) {
		NSName retval = NSName.create(baseName.getInterfaceName()+".mockup_"+profile, baseName.getNodeName().toString());
		return retval;
	}
}
