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
	
	private final String tempCdataBlock = "%%%COMPRESS~CDATA%%%";
	
	/**
	 * The main method that compresses given XML source and returns compressed result.
	 * 
	 * @param xml XML content to compress
	 * @return compressed content.
	 * @throws Exception
	 */
	@Override
	public String compress(String xml) throws Exception {
		if(!enabled || xml == null || xml.length() == 0) {
			return xml;
		}
		
		List<String> cdataBlocks = new ArrayList<String>();
		
		String result = xml;
		
		//preserve CDATA blocks
		String cdataRule = "<!\\[CDATA\\[.*?\\]\\]>";
		Pattern cdataPattern = Pattern.compile(cdataRule, Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		
		Matcher cdataMatcher = cdataPattern.matcher(result);
		while(cdataMatcher.find()) {
			cdataBlocks.add(cdataMatcher.group(0));
		}
		
		result = cdataMatcher.replaceAll(tempCdataBlock);
		
		//process pure xml
		result = processXml(result);
		
		//process preserved blocks
		result = processCdataBlocks(result, cdataBlocks);
		
		return result.trim();
	}
	
	private String processXml(String xml) throws Exception {
		String result = xml;
		
		//remove comments
		if(removeComments) {
			Pattern commentPattern = Pattern.compile("<!--.*?-->", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
			result = commentPattern.matcher(result).replaceAll("");
		}
		
		//remove inter-tag spaces
		if(removeIntertagSpaces) {
			result = result.replaceAll(">\\s+<","><");
		}
		return result;
	}
	
	private String processCdataBlocks(String xml, List<String> blocks) throws Exception {
		String result = xml;
		
		//put preserved blocks back
		while(result.contains(tempCdataBlock)) {
			result = result.replaceFirst(tempCdataBlock, Matcher.quoteReplacement(blocks.remove(0)));
		}
		
		return result;
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
	
}