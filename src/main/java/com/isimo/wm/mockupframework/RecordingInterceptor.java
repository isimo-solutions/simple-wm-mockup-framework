package com.isimo.wm.mockupframework;

import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.xml.bind.JAXB;

import com.isimo.wm.mockupframework.xml.Recording;
import com.wm.app.b2b.server.BaseService;
import com.wm.app.b2b.server.invoke.InvokeChainProcessor;
import com.wm.app.b2b.server.invoke.ServiceStatus;
import com.wm.data.IData;
import com.wm.util.Config;
import com.wm.util.ServerException;
import com.wm.util.coder.IDataXMLCoder;

public class RecordingInterceptor extends InterceptorBase implements InvokeChainProcessor {
	static final String RECORDING_CONFIG = Config.getProperty("config/recordingconfig.xml","watt.server.recordinginterceptor.config");
	String RECORDING_DIR;
	String TIMESTAMPFORMAT;
	String FILENAMEFORMAT;
	long lastconfigread = Long.MIN_VALUE;
	File recordingConfigFile;
	Recording recordingConfig = null;
	List<Pattern> patterns;
	IDataXMLCoder xmlcoder = new IDataXMLCoder();
	SimpleDateFormat tpattern;
	NumberFormat nformat;
	long sequence = 0;
	public RecordingInterceptor() {
		this(RECORDING_CONFIG);
	}
	
	enum Direction { IN, OUT };
	
	public RecordingInterceptor(String recordingConfig) {
		log("Recording Interceptor initialized");
		this.recordingConfigFile = new File(recordingConfig);
		File recordingDir = new File(RECORDING_DIR);
		if(!recordingDir.mkdirs()) {
			log("Directory "+recordingDir+" has not been created successfully");
		}
	}
	
	void readRecordingConfig() {
		try  {
			if(!recordingConfigFile.exists()) {
				emptyRecording();
				return;
			}
			if(recordingConfig==null || lastconfigread < recordingConfigFile.lastModified()) {
				try {
					log("Reading recording config from file: "+recordingConfigFile);
					recordingConfig = JAXB.unmarshal(recordingConfigFile, Recording.class);
					RECORDING_DIR = Config.getProperty("pipeline/recording","watt.server.recordinginterceptor.pipelinesdir");
					TIMESTAMPFORMAT = Config.getProperty("yyyyMMdd_HHmmssSSS","watt.server.recordinginterceptor.timestampformat");
					FILENAMEFORMAT = Config.getProperty("%SERVICE%_%TIMESTAMP%_%SEQUENCE%_%INOUT%.xml","watt.server.recordinginterceptor.filenameformat");
					tpattern = new SimpleDateFormat(TIMESTAMPFORMAT);
					nformat = new DecimalFormat("0000000");
				} catch(Exception e) {
					log("Problems initializing recording config: "+e.getMessage());
					emptyRecording();
				}
			}
			patterns = recordingConfig.pattern.stream().map(p -> Pattern.compile(p)).collect(Collectors.toList());
		} catch(Exception e) {
			log("Problems reading recording config file: "+e.getMessage());
			emptyRecording();
		}
	}
	
	void emptyRecording() {
		recordingConfig = new Recording();
		patterns = new ArrayList<Pattern>();
	}
	
	public void process(Iterator chain, BaseService service, IData pipeline,
			ServiceStatus status) throws ServerException {
		synchronized (this) {
			sequence++;
		}
		readRecordingConfig();
		boolean record = serviceNameMatches(service);
		Date serviceStarted = new Date();
		
		if(record) {
			writePipelineToFile(getInputPipelineFile(service,serviceStarted), pipeline);
		}
		continueToNext(chain, service, pipeline, status);
		if(record) {
			writePipelineToFile(getOutputPipelineFile(service,serviceStarted), pipeline);
		}
	}
	
	void writePipelineToFile(File file, IData pipeline) throws ServerException {
		try {
			xmlcoder.writeToFile(file, pipeline);
		} catch(Exception e) {
			throw new ServerException(e);
		}
	}
	
	File getPipelineFile(Direction inout, BaseService service, Date serviceStarted) {
		String filename = FILENAMEFORMAT;
		filename = filename.replaceAll("%SERVICE%", service.getNSName().getFullName());
		filename = filename.replaceAll("%TIMESTAMP%", tpattern.format(serviceStarted));
		filename = filename.replaceAll("%SEQUENCE%", nformat.format(sequence));
		filename = filename.replaceAll("%INOUT%", inout.name());
		return new File(RECORDING_DIR+File.separator+filename);
	}
	
	File getInputPipelineFile(BaseService service, Date serviceStarted) {
		return getPipelineFile(Direction.IN, service, serviceStarted);
	}
	
	File getOutputPipelineFile(BaseService service, Date serviceStarted) {
		return getPipelineFile(Direction.OUT, service, serviceStarted);
	}
	
	boolean serviceNameMatches(BaseService service) {
		String name = service.getNSName().getFullName();
		for(Pattern pattern: patterns) {
			if(pattern.matcher(name).matches())
				return true;
		}
		for(String n: recordingConfig.service) {
			if(name.equals(n))
				return true;
		}
		return false;
	}
}
