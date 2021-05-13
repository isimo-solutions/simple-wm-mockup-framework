package com.isimo.wm.mockupframework.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Recording {
	public List<String> service = new ArrayList<String>();
	public List<String> pattern = new ArrayList<String>();
}
