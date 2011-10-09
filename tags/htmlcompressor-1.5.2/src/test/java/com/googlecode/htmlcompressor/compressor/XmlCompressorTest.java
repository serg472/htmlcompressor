package com.googlecode.htmlcompressor.compressor;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class XmlCompressorTest {
	
	private static final String resPath = "./src/test/resources/xml/";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testCompress() throws Exception {
		String source = readResource("testCompress.xml");
		String result = readResource("testCompressResult.xml");
		
		XmlCompressor compressor = new XmlCompressor();
		
		assertEquals(result, compressor.compress(source));
	}

	@Test
	public void testEnabled() throws Exception {
		String source = readResource("testEnabled.xml");
		String result = readResource("testEnabledResult.xml");
		
		XmlCompressor compressor = new XmlCompressor();
		compressor.setEnabled(false);
		
		assertEquals(result, compressor.compress(source));
		
	}

	@Test
	public void testRemoveComments() throws Exception {
		String source = readResource("testRemoveComments.xml");
		String result = readResource("testRemoveCommentsResult.xml");
		
		XmlCompressor compressor = new XmlCompressor();
		compressor.setRemoveComments(true);
		
		assertEquals(result, compressor.compress(source));
	}

	@Test
	public void testRemoveIntertagSpaces() throws Exception {
		String source = readResource("testRemoveIntertagSpaces.xml");
		String result = readResource("testRemoveIntertagSpacesResult.xml");
		
		XmlCompressor compressor = new XmlCompressor();
		compressor.setRemoveIntertagSpaces(true);
		
		assertEquals(result, compressor.compress(source));
	}
	
	private String readResource(String filename) {
		
		StringBuilder builder = new StringBuilder();
		try {
			FileInputStream stream = new FileInputStream(new File(resPath + filename));
			try {
				Reader reader = new BufferedReader(new InputStreamReader(stream));
				
				char[] buffer = new char[8192];
				int read;
				while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
					builder.append(buffer, 0, read);
				}
				
			} finally {
				stream.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return builder.toString();
		
	}
	
	private void writeResource(String filename, String content) {
		try {
			Writer output = new BufferedWriter(new FileWriter(new File(resPath + filename)));
			try {
				output.write(content);
			} finally {
				output.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
