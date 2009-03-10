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
	
	private String tempPrefix = "%%%COMPRESS~";
	private String tempSuffix = "%%%";
	
	/**
	 * The main method that compresses given XML source and returns compressed result.
	 * 
	 * @param xml XML content to compress
	 * @return compressed content.
	 * @throws Exception
	 */
	@Override
	public String compress(String xml) throws Exception {
		if(xml == null || xml.length() == 0) {
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
		
		result = cdataMatcher.replaceAll(tempPrefix + "CDATA" + tempSuffix);
		
		//process pure xml
		result = processXml(result);
		
		//process preserved blocks
		result = processCdataBlocks(result, cdataBlocks);
		
		return result.trim();
	}
	
	private String processXml(String xml) throws Exception {
		String result = xml;
		
		//remove comments
		Pattern commentPattern = Pattern.compile("<!--.*?-->", Pattern.DOTALL | Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
		result = commentPattern.matcher(result).replaceAll("");
		
		//remove whitespace characters between tags
		result = result.replaceAll(">\\s+<","><");
		
		return result;
	}
	
	private String processCdataBlocks(String xml, List<String> blocks) throws Exception {
		String result = xml;
		
		//put preserved blocks back
		while(result.contains(tempPrefix + "CDATA" + tempSuffix)) {
			result = result.replaceFirst(tempPrefix + "CDATA" + tempSuffix, Matcher.quoteReplacement(blocks.remove(0)));
		}
		
		return result;
	}
}