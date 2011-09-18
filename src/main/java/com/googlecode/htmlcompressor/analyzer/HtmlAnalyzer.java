package com.googlecode.htmlcompressor.analyzer;

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

import java.text.NumberFormat;
import java.util.Formatter;

import com.googlecode.htmlcompressor.compressor.ClosureJavaScriptCompressor;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

/**
 * Class that compresses provided source with different compression 
 * settings and displays page size gains in a report
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 */
public class HtmlAnalyzer {
	
	private String jsCompressor = HtmlCompressor.JS_COMPRESSOR_YUI;
	
	public HtmlAnalyzer() { 
		
	}

	public HtmlAnalyzer(String jsCompressor) { 
		this.jsCompressor = jsCompressor;
	}
	
	public void analyze(String source) {
		int originalSize = source.length();
		
		HtmlCompressor compressor = getCleanCompressor();
		String compResult = compressor.compress(source);
		
		printHeader();
		
		System.out.println(formatLine("Compression disabled", originalSize, originalSize, originalSize));
		int prevSize = originalSize;
		
		
		//spaces inside tags
		System.out.println(formatLine("All settings disabled", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();
		
		//remove comments
		compressor.setRemoveComments(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("Comments removed", originalSize, compResult.length(), prevSize));	
		prevSize = compResult.length();
		
		//remove mulispaces
		compressor.setRemoveMultiSpaces(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("Multiple spaces removed", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();
		
		//remove intertag spaces
		compressor.setRemoveIntertagSpaces(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("No spaces between tags", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();

		//remove min surrounding spaces
		compressor.setRemoveSurroundingSpaces(HtmlCompressor.BLOCK_TAGS_MIN);
		compResult = compressor.compress(source);
		System.out.println(formatLine("No surround spaces (min)", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();

		//remove max surrounding spaces
		compressor.setRemoveSurroundingSpaces(HtmlCompressor.BLOCK_TAGS_MAX);
		compResult = compressor.compress(source);
		System.out.println(formatLine("No surround spaces (max)", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();

		//remove all surrounding spaces
		compressor.setRemoveSurroundingSpaces(HtmlCompressor.ALL_TAGS);
		compResult = compressor.compress(source);
		System.out.println(formatLine("No surround spaces (all)", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();
		
		//remove quotes
		compressor.setRemoveQuotes(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("Quotes removed from tags", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();
		
		//link attrib
		compressor.setRemoveLinkAttributes(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("<link> attr. removed", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();
		
		//style attrib
		compressor.setRemoveStyleAttributes(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("<style> attr. removed", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();
		
		//script attrib
		compressor.setRemoveScriptAttributes(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("<script> attr. removed", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();
		
		//form attrib
		compressor.setRemoveFormAttributes(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("<form> attr. removed", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();
		
		//input attrib
		compressor.setRemoveInputAttributes(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("<input> attr. removed", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();
		
		//simple bool
		compressor.setSimpleBooleanAttributes(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("Simple boolean attributes", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();
		
		//simple doctype
		compressor.setSimpleDoctype(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("Simple doctype", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();
		
		//js protocol
		compressor.setRemoveJavaScriptProtocol(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("Remove js pseudo-protocol", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();

		//http protocol
		compressor.setRemoveHttpProtocol(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("Remove http protocol", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();

		//https protocol
		compressor.setRemoveHttpsProtocol(true);
		compResult = compressor.compress(source);
		System.out.println(formatLine("Remove https protocol", originalSize, compResult.length(), prevSize));
		prevSize = compResult.length();

		
		//inline css
		try {
			compressor.setCompressCss(true);
			compResult = compressor.compress(source);
			System.out.println(formatLine("Compress inline CSS (YUI)", originalSize, compResult.length(), prevSize));
			prevSize = compResult.length();
		} catch (NoClassDefFoundError e){
			System.out.println(formatEmptyLine("Compress inline CSS (YUI)"));
		}
		
		if(jsCompressor.equals(HtmlCompressor.JS_COMPRESSOR_YUI)) {
			//inline js yui
			try {
				compressor.setCompressJavaScript(true);
				compResult = compressor.compress(source);
				System.out.println(formatLine("Compress inline JS (YUI)", originalSize, compResult.length(), prevSize));
				prevSize = compResult.length();
			} catch (NoClassDefFoundError e){
				System.out.println(formatEmptyLine("Compress inline JS (YUI)"));
			}
		} else {
			//inline js yui
			try {
				compressor.setCompressJavaScript(true);
				compressor.setJavaScriptCompressor(new ClosureJavaScriptCompressor());
				compResult = compressor.compress(source);
				System.out.println(formatLine("Compress JS (Closure)", originalSize, compResult.length(), prevSize));
				prevSize = compResult.length();
			} catch (NoClassDefFoundError e){
				System.out.println(formatEmptyLine("Compress JS (Closure)"));
			}
		}
		
		printFooter();
		
	}
	
	private HtmlCompressor getCleanCompressor() {
		HtmlCompressor compressor = new HtmlCompressor();
		compressor.setRemoveComments(false);
		compressor.setRemoveMultiSpaces(false);
		
		return compressor;
	}
	
	private String formatLine(String descr, int originalSize, int compressedSize, int prevSize) {
		Formatter fmt = new Formatter();
		fmt.format("%-25s | %16s | %16s | %12s |", descr, formatDecrease(prevSize, compressedSize), formatDecrease(originalSize, compressedSize), formatSize(compressedSize));
		return fmt.toString();
	}
	
	private String formatEmptyLine(String descr) {
		Formatter fmt = new Formatter();
		fmt.format("%-25s | %16s | %16s | %12s |", descr, "-", "-", "-");
		return fmt.toString();
	}
	
	private void printHeader() {
		System.out.println();
		System.out.println("================================================================================");
		System.out.format("%-25s | %-16s | %-16s | %-12s |", "         Setting", "Incremental Gain", "   Total Gain", " Page Size");
		System.out.println();
		System.out.println("================================================================================");
		
	}

	private void printFooter() {
		System.out.println("================================================================================");
		System.out.println();
		System.out.println("Each consecutive compressor setting is applied on top of previous ones.");
		System.out.println("In order to see JS and CSS compression results, YUI jar file must be present.");
		System.out.println("All sizes are in bytes.");
	}
	
	private String formatDecrease(int originalSize, int compressedSize) {
		NumberFormat nf = NumberFormat.getPercentInstance();
		nf.setGroupingUsed(true);
		nf.setMinimumFractionDigits(1);
		nf.setMaximumFractionDigits(1);
		
		return formatSize(originalSize - compressedSize)  + " (" + nf.format(1 - (double) compressedSize / originalSize) + ")";
	}
	
	private String formatSize(int size) {
		NumberFormat nf = NumberFormat.getInstance();
		nf.setGroupingUsed(true);
		nf.setParseIntegerOnly(true);
		return nf.format(size);
	}
}
