package com.googlecode.htmlcompressor.compressor;

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class that compresses given XML source by removing comments, extra spaces and 
 * line breaks while preserving content within CDATA blocks.
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 */
public class XmlCompressor implements Compressor {
	
	private boolean enabled = true;
	
	//default settings
	private boolean removeComments = true;
	private boolean removeIntertagSpaces = true;
	
	//optional settings
	private int threads = 1;
	
	//temp replacements for preserved blocks 
	private static final String tempCdataBlock = "%%%COMPRESS~CDATA%%%";
	
	//preserved block containers
	private List<String> cdataBlocks = new ArrayList<String>();
	
	//compiled regex patterns
	private static final Pattern cdataPattern = Pattern.compile("<!\\[CDATA\\[.*?\\]\\]>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern commentPattern = Pattern.compile("<!--.*?-->", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern intertagPattern = Pattern.compile(">\\s+<", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	
	private String xml = null;
	private List<String> xmlParts = new ArrayList<String>();
	
	/**
	 * The main method that compresses given XML source and returns compressed result.
	 * 
	 * @param source XML content to compress
	 * @return compressed content.
	 * @throws Exception
	 */
	@Override
	public String compress(String source) throws Exception {
		if(!enabled || source == null || source.length() == 0) {
			return source;
		}
		
		xml = source;
		
		//preserve blocks
		preserveBlocks();
		
		if(threads <=1) {
			//process pure xml
			xml = processXml(xml);
		} else {
			//split xml into parts to divide between threads
			splitXml();
			
			ExecutorService taskExecutor = Executors.newFixedThreadPool(threads);
			
			//submit tasks
			for(int i=0; i<xmlParts.size(); i++) {
				taskExecutor.execute(new CompressorTask(i));
			}
			
			//wait for completion
			taskExecutor.shutdown();
			taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			
			//merge compressed parts 
			mergeXml();
		}
		
		//return preserved blocks
		returnBlocks();
		
		return this.xml.trim();
	}

	private void preserveBlocks() {
		//preserve CDATA blocks
		Matcher cdataMatcher = cdataPattern.matcher(xml);
		while(cdataMatcher.find()) {
			cdataBlocks.add(cdataMatcher.group(0));
		}
		xml = cdataMatcher.replaceAll(tempCdataBlock);
	}
	
	private void returnBlocks() {
		int index = 0;
		
		StringBuilder source = new StringBuilder(xml);
		
		//put CDATA blocks back
		int prevIndex = 0;
		while(cdataBlocks.size() > 0) {
			index = source.indexOf(tempCdataBlock, prevIndex);
			String replacement = cdataBlocks.remove(0);
			source.replace(index, index+tempCdataBlock.length(), replacement);
			prevIndex = index + replacement.length();
		}
		
		xml = source.toString();
	}
	
	private void splitXml() {

		xmlParts.clear();
		
		int partSize = (int)((double)xml.length() / threads);
		//if number of threads more than symbols in xml
		if(partSize == 0) {
			xmlParts.add(xml);
			return;
		}
		
		int startPos = 0;
		int endPos = 0;
		for(int i=0; i<threads; i++) {
			if(startPos == xml.length()) {
				break;
			}
			
			endPos = startPos + partSize;
			
			//try to find closest tag
			if(endPos < xml.length()) {
				int tagPos = xml.indexOf("<", endPos);
				endPos = tagPos != 1 ? tagPos : xml.length();
			} else {
				endPos = xml.length();
			}
			
			xmlParts.add(xml.substring(startPos, endPos));
			
			startPos = endPos;
		}
	}
	
	private void mergeXml() {
		StringBuilder source = new StringBuilder();
		
		for(String part : xmlParts) {
			source.append(part);
		}
		
		xml = source.toString();
	}
	
	private String processXml(String xml) throws Exception {
		//remove comments
		if(removeComments) {
			xml = commentPattern.matcher(xml).replaceAll("");
		}
		
		//remove inter-tag spaces
		if(removeIntertagSpaces) {
			xml = intertagPattern.matcher(xml).replaceAll("><");
		}
		return xml;
	}
	
	private class CompressorTask implements Runnable  {
		private int index;
		
		public CompressorTask(int index) {
			this.index = index;
		}
		
		public void run() {
			String source = null;
			
			synchronized(XmlCompressor.this.xmlParts) {
				source = XmlCompressor.this.xmlParts.get(index);
			}
			try {
				source = XmlCompressor.this.processXml(source);
			} catch (Exception e) {}
			synchronized(XmlCompressor.this.xmlParts) {
				XmlCompressor.this.xmlParts.set(index, source);
			}
		}
	}
	
	/**
	 * Returns <code>true</code> if compression is enabled.  
	 * 
	 * @return <code>true</code> if compression is enabled.
	 */
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * If set to <code>false</code> all compression will be bypassed. Might be useful for testing purposes. 
	 * Default is <code>true</code>.
	 * 
	 * @param enabled set <code>false</code> to bypass all compression
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns <code>true</code> if all XML comments will be removed.
	 * 
	 * @return <code>true</code> if all XML comments will be removed
	 */
	public boolean isRemoveComments() {
		return removeComments;
	}

	/**
	 * If set to <code>true</code> all XML comments will be removed.   
	 * Default is <code>true</code>.
	 * 
	 * @param removeComments set <code>true</code> to remove all XML comments
	 */
	public void setRemoveComments(boolean removeComments) {
		this.removeComments = removeComments;
	}

	/**
	 * Returns <code>true</code> if all inter-tag whitespace characters will be removed.
	 * 
	 * @return <code>true</code> if all inter-tag whitespace characters will be removed.
	 */
	public boolean isRemoveIntertagSpaces() {
		return removeIntertagSpaces;
	}

	/**
	 * If set to <code>true</code> all inter-tag whitespace characters will be removed.
	 * Default is <code>true</code>.
	 * 
	 * @param removeIntertagSpaces set <code>true</code> to remove all inter-tag whitespace characters
	 */
	public void setRemoveIntertagSpaces(boolean removeIntertagSpaces) {
		this.removeIntertagSpaces = removeIntertagSpaces;
	}
	
	/**
	 * Returns number of threads that are used during a compression.
	 * 
	 * @return number of threads that are used during a compression.
	 */
	public int getThreads() {
		return threads;
	}

	/**
	 * If set to more than 1, the Compressor will try to split internal compression tasks 
	 * into provided number of threads and process them in parallel. It is recommended to use 
	 * this option on multicore systems to improve performance while processing 
	 * large files. 
	 * Usually optimal number of threads equals to a number of processor cores in the system.
	 * Default value is 1 (no threading, everything is done in the main thread).
	 * 
	 * @param threads number of threads that are used during a compression 
	 */
	public void setThreads(int threads) {
		this.threads = threads;
	}
}