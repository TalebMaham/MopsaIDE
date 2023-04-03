package com.example;

import static org.junit.Assert.*;

import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.MessageType;
import org.junit.Test;

import com.mopsa.MopsaServerAnalysis;

public class Test1 {

	@Test
	public void test() {
		
		
		MopsaServerAnalysis serverAnalysis = new MopsaServerAnalysis(); 
		String version = serverAnalysis.getMopsaVersion(); 
		assertEquals("1.0~pre2 (release)", version);
	}
	
	@Test
	public void testMessageParamsCreation() {
	    String result = "1.0~pre2 (release)";
	    MessageParams message = new MessageParams(MessageType.Info, "Found infer: " + result);
	    assertEquals(message.getType(), MessageType.Info);
	    assertEquals(message.getMessage(), "Found infer: 1.0~pre2 (release)");
	}
	
	@Test
	public void testSource() {
	    MopsaServerAnalysis server = new MopsaServerAnalysis();
	    String expectedSource = "mopsa";
	    String actualSource = server.source();
	    assertEquals(expectedSource, actualSource);
	}

	@Test
	public void testJSON() {
	    MopsaServerAnalysis server = new MopsaServerAnalysis();
	    String expectedSource = "mopsa";
	    String actualSource = server.source();
	    assertEquals(expectedSource, actualSource);
	}


}
