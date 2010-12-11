package com.googlecode.htmlcompressor.compressor;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class HtmlCompressorTest {
	
	private static final String resPath = "d:/www/htmlcompressor/test/res/html/";

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}
	
	@Test
	public void testEnabled() throws Exception {
		String source = readResource("testEnabled.html");
		String result = readResource("testEnabledResult.html");
		
		HtmlCompressor compressor = new HtmlCompressor();
		compressor.setEnabled(false);
		
		assertEquals(result, compressor.compress(source));
		
	}
	
	@Test
	public void testRemoveComments() throws Exception {
		String source = readResource("testRemoveComments.html");
		String result = readResource("testRemoveCommentsResult.html");
		
		HtmlCompressor compressor = new HtmlCompressor();
		compressor.setRemoveComments(true);
		compressor.setRemoveIntertagSpaces(true);

		assertEquals(result, compressor.compress(source));
	}
	
	@Test
	public void testRemoveQuotes() throws Exception {
		String source = readResource("testRemoveQuotes.html");
		String result = readResource("testRemoveQuotesResult.html");
		
		HtmlCompressor compressor = new HtmlCompressor();
		compressor.setRemoveQuotes(true);
		
		assertEquals(result, compressor.compress(source));
	}
	
	@Test
	public void testRemoveMultiSpaces() throws Exception {
		String source = readResource("testRemoveMultiSpaces.html");
		String result = readResource("testRemoveMultiSpacesResult.html");
		
		HtmlCompressor compressor = new HtmlCompressor();
		compressor.setRemoveMultiSpaces(true);
		
		assertEquals(result, compressor.compress(source));
	}
	
	@Test
	public void testRemoveIntertagSpaces() throws Exception {
		String source = readResource("testRemoveIntertagSpaces.html");
		String result = readResource("testRemoveIntertagSpacesResult.html");
		
		HtmlCompressor compressor = new HtmlCompressor();
		compressor.setRemoveIntertagSpaces(true);
		
		assertEquals(result, compressor.compress(source));
	}
	
	@Test
	public void testPreservePatterns() throws Exception {
		String source = readResource("testPreservePatterns.html");
		String result = readResource("testPreservePatternsResult.html");
		
		List<Pattern> preservePatterns = new ArrayList<Pattern>();
		preservePatterns.add(HtmlCompressor.PHP_TAG_PATTERN); //<?php ... ?> blocks
		preservePatterns.add(HtmlCompressor.SERVER_SCRIPT_TAG_PATTERN); //<% ... %> blocks
		preservePatterns.add(Pattern.compile("<jsp:.*?>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE)); //<jsp: ... > tags

		HtmlCompressor compressor = new HtmlCompressor();
		compressor.setPreservePatterns(preservePatterns);
		compressor.setRemoveComments(true);
		compressor.setRemoveIntertagSpaces(true);
		
		assertEquals(result, compressor.compress(source));
	}
	
	@Test
	public void testCompressJavaScript() throws Exception {
		String source = readResource("testCompressJavaScript.html");
		String result = readResource("testCompressJavaScriptResult.html");
		
		HtmlCompressor compressor = new HtmlCompressor();
		compressor.setCompressJavaScript(true);
		compressor.setRemoveIntertagSpaces(true);
		
		assertEquals(result, compressor.compress(source));
	}
	
	@Test
	public void testCompressCss() throws Exception {
		String source = readResource("testCompressCss.html");
		String result = readResource("testCompressCssResult.html");
		
		HtmlCompressor compressor = new HtmlCompressor();
		compressor.setCompressCss(true);
		compressor.setRemoveIntertagSpaces(true);
		
		assertEquals(result, compressor.compress(source));
	}
	
	@Test
	public void testCompress() throws Exception {
		String source = readResource("testCompress.html");
		String result = readResource("testCompressResult.html");
		
		HtmlCompressor compressor = new HtmlCompressor();
		
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
