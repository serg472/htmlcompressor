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

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.yahoo.platform.yui.compressor.CssCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Class that compresses given HTML source by removing comments, extra spaces and 
 * line breaks while preserving content within &lt;pre>, &lt;textarea>, &lt;script> 
 * and &lt;style> tags. Can optionally compress content inside &lt;script> 
 * or &lt;style> tags using 
 * <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a> 
 * library.
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 */
public class HtmlCompressor implements Compressor {
	
	private boolean enabled = true;
	
	//default settings
	private boolean removeComments = true;
	private boolean removeMultiSpaces = true;
	
	//optional settings
	private boolean removeIntertagSpaces = false;
	private boolean removeQuotes = false;
	private boolean compressJavaScript = false;
	private boolean compressCss = false;
	private int threads = 1;
	
	//YUICompressor settings
	private boolean yuiJsNoMunge = false;
	private boolean yuiJsPreserveAllSemiColons = false;
	private boolean yuiJsDisableOptimizations = false;
	private int yuiJsLineBreak = -1;
	private int yuiCssLineBreak = -1;
	
	//temp replacements for preserved blocks 
	private static final String tempPreBlock = "%%%COMPRESS~PRE%%%";
	private static final String tempTextAreaBlock = "%%%COMPRESS~TEXTAREA%%%";
	private static final String tempScriptBlock = "%%%COMPRESS~SCRIPT%%%";
	private static final String tempStyleBlock = "%%%COMPRESS~STYLE%%%";
	
	//preserved block containers
	private List<String> preBlocks = new ArrayList<String>();
	private List<String> taBlocks = new ArrayList<String>();
	private List<String> scriptBlocks = new ArrayList<String>();
	private List<String> styleBlocks = new ArrayList<String>();
	
	//compiled regex patterns
	private static final Pattern commentPattern = Pattern.compile("<!--[^\\[].*?-->", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern intertagPattern = Pattern.compile(">\\s+?<", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern multispacePattern = Pattern.compile("\\s{2,}", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern prePattern = Pattern.compile("<pre[^>]*?>.*?</pre>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern taPattern = Pattern.compile("<textarea[^>]*?>.*?</textarea>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern tagquotePattern = Pattern.compile("\\s*=\\s*([\"'])([a-z0-9-_]+?)\\1(?=[^<]*?>)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern scriptPattern = Pattern.compile("<script[^>]*?>.*?</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern stylePattern = Pattern.compile("<style[^>]*?>.*?</style>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern scriptPatternNonEmpty = Pattern.compile("<script[^>]*?>(.+?)</script>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern stylePatternNonEmpty = Pattern.compile("<style[^>]*?>(.+?)</style>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	
	private String html = null;
	private List<String> htmlParts = new ArrayList<String>();
	private enum Task {
		HTML, SCRIPT, STYLE;
	}
	
	/**
	 * The main method that compresses given HTML source and returns compressed result.
	 * 
	 * @param source HTML content to compress
	 * @return compressed content.
	 * @throws Exception
	 */
	@Override
	public String compress(String source) throws Exception {
		if(!enabled || source == null || source.length() == 0) {
			return source;
		}
		html = source;
		
		//preserve blocks
		preserveBlocks();
		
		if(threads <=1) {
			//process pure html
			html = processHtml(html);
			
			//process preserved blocks
			processScriptBlocks();
			processStyleBlocks();
		} else {
			//split html into parts to divide between threads
			splitHtml();
			
			ExecutorService taskExecutor = Executors.newFixedThreadPool(threads);
			
			//submit js tasks
			if(compressJavaScript) {
				for(int i=0; i<scriptBlocks.size(); i++) {
					taskExecutor.execute(new CompressorTask(Task.SCRIPT, i));
				}
			}
			
			//submit css tasks
			if(compressCss) {
				for(int i=0; i<styleBlocks.size(); i++) {
					taskExecutor.execute(new CompressorTask(Task.STYLE, i));
				}
			}
			
			//submit html tasks
			for(int i=0; i<htmlParts.size(); i++) {
				taskExecutor.execute(new CompressorTask(Task.HTML, i));
			}
			
			//wait for completion
			taskExecutor.shutdown();
			taskExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
			
			//merge compressed parts 
			mergeHtml();
		}
		
		//put blocks back
		returnBlocks();
		
		return this.html.trim();
	}

	private void preserveBlocks() {
		//preserve PRE tags
		Matcher preMatcher = prePattern.matcher(html);
		while(preMatcher.find()) {
			preBlocks.add(preMatcher.group(0));
		}
		html = preMatcher.replaceAll(tempPreBlock);
		
		//preserve TEXTAREA tags
		Matcher taMatcher = taPattern.matcher(html);
		while(taMatcher.find()) {
			taBlocks.add(taMatcher.group(0));
		}
		html = taMatcher.replaceAll(tempTextAreaBlock);
		
		//preserve SCRIPT tags
		Matcher scriptMatcher = scriptPattern.matcher(html);
		while(scriptMatcher.find()) {
			scriptBlocks.add(scriptMatcher.group(0));
		}
		html = scriptMatcher.replaceAll(tempScriptBlock);
		
		//preserve STYLE tags
		Matcher styleMatcher = stylePattern.matcher(html);
		while(styleMatcher.find()) {
			styleBlocks.add(styleMatcher.group(0));
		}
		html = styleMatcher.replaceAll(tempStyleBlock);
	}
	
	private void returnBlocks() {
		int index = 0;
		
		StringBuilder source = new StringBuilder(html);
		
		//put pre blocks back
		int prevIndex = 0;
		while(preBlocks.size() > 0) {
			index = source.indexOf(tempPreBlock, prevIndex);
			String replacement = preBlocks.remove(0);
			source.replace(index, index+tempPreBlock.length(), replacement);
			prevIndex = index + replacement.length();
		}
		
		//put textarea blocks back
		prevIndex = 0;
		while(taBlocks.size() > 0) {
			index = source.indexOf(tempTextAreaBlock, prevIndex);
			String replacement = taBlocks.remove(0);
			source.replace(index, index+tempTextAreaBlock.length(), replacement);
			prevIndex = index + replacement.length();
		}
		
		//put script blocks back
		prevIndex = 0;
		while(scriptBlocks.size() > 0) {
			index = source.indexOf(tempScriptBlock, prevIndex);
			String replacement = scriptBlocks.remove(0);
			source.replace(index, index+tempScriptBlock.length(), replacement);
			prevIndex = index + replacement.length();
		}
		
		//put style blocks back
		prevIndex = 0;
		while(styleBlocks.size() > 0) {
			index = source.indexOf(tempStyleBlock, prevIndex);
			String replacement = styleBlocks.remove(0);
			source.replace(index, index+tempStyleBlock.length(), replacement);
			prevIndex = index + replacement.length();
		}
		
		html = source.toString();
	}
	
	private String processHtml(String html) throws Exception {
		//remove comments
		if(removeComments) {
			html = commentPattern.matcher(html).replaceAll("");
		}
		
		//remove inter-tag spaces
		if(removeIntertagSpaces) {
			html = intertagPattern.matcher(html).replaceAll("><");
		}
		
		//remove multi whitespace characters
		if(removeMultiSpaces) {
			html = multispacePattern.matcher(html).replaceAll(" ");
		}
		
		//remove quotes from tag attributes
		if(removeQuotes) {
			html = tagquotePattern.matcher(html).replaceAll("=$2");
		}
		
		return html;
	}
	
	private void splitHtml() {

		htmlParts.clear();
		
		int partSize = (int)((double)html.length() / threads);
		//if number of threads more than symbols in html
		if(partSize == 0) {
			htmlParts.add(html);
			return;
		}
		
		int startPos = 0;
		int endPos = 0;
		for(int i=0; i<threads; i++) {
			if(startPos == html.length()) {
				break;
			}
			
			endPos = startPos + partSize;
			
			//try to find closest tag
			if(endPos < html.length()) {
				int tagPos = html.indexOf("<", endPos);
				endPos = tagPos != 1 ? tagPos : html.length();
			} else {
				endPos = html.length();
			}
			
			htmlParts.add(html.substring(startPos, endPos));
			
			startPos = endPos;
		}
	}
	
	private void mergeHtml() {
		StringBuilder source = new StringBuilder();
		
		for(String part : htmlParts) {
			source.append(part);
		}
		
		html = source.toString();
	}
	
	private void processScriptBlocks() throws Exception {
		if(compressJavaScript) {
			for(int i = 0; i < scriptBlocks.size(); i++) {
				scriptBlocks.set(i, compressJavaScript(scriptBlocks.get(i)));
			}
		}
	}
	
	private void processStyleBlocks() throws Exception {
		if(compressCss) {
			for(int i = 0; i < styleBlocks.size(); i++) {
				styleBlocks.set(i, compressCssStyles(styleBlocks.get(i)));
			}
		}
	}
	
	private String compressJavaScript(String source) throws Exception {
		
		//check if block is not empty
		Matcher scriptMatcher = scriptPatternNonEmpty.matcher(source);
		if(scriptMatcher.find()) {
			
			//call YUICompressor
			StringWriter result = new StringWriter();
			JavaScriptCompressor compressor = new JavaScriptCompressor(new StringReader(scriptMatcher.group(1)), null);
			compressor.compress(result, yuiJsLineBreak, !yuiJsNoMunge, false, yuiJsPreserveAllSemiColons, yuiJsDisableOptimizations);
			
			return (new StringBuilder(source.substring(0, scriptMatcher.start(1))).append(result.toString()).append(source.substring(scriptMatcher.end(1)))).toString();
		
		} else {
			return source;
		}
	}
	
	private String compressCssStyles(String source) throws Exception {
		
		//check if block is not empty
		Matcher styleMatcher = stylePatternNonEmpty.matcher(source);
		if(styleMatcher.find()) {
			
			//call YUICompressor
			StringWriter result = new StringWriter();
			CssCompressor compressor = new CssCompressor(new StringReader(styleMatcher.group(1)));
			compressor.compress(result, yuiCssLineBreak);
			
			return (new StringBuilder(source.substring(0, styleMatcher.start(1))).append(result.toString()).append(source.substring(styleMatcher.end(1)))).toString();
		
		} else {
			return source;
		}
	}
	
	private class CompressorTask implements Runnable  {
		private Task task;
		private int index;
		
		public CompressorTask(Task task, int index) {
			this.task = task;
			this.index = index;
		}
		
		public void run() {
			String source = null;
			
			if(task.equals(Task.HTML)) {
				synchronized(HtmlCompressor.this.htmlParts) {
					source = HtmlCompressor.this.htmlParts.get(index);
				}
				try {
					source = HtmlCompressor.this.processHtml(source);
				} catch (Exception e) {}
				synchronized(HtmlCompressor.this.htmlParts) {
					HtmlCompressor.this.htmlParts.set(index, source);
				}
			} else if(task.equals(Task.SCRIPT)) {
				synchronized(HtmlCompressor.this.scriptBlocks) {
					source = HtmlCompressor.this.scriptBlocks.get(index);
				}
				try {
					source = HtmlCompressor.this.compressJavaScript(source);
				} catch (Exception e) {}
				synchronized(HtmlCompressor.this.scriptBlocks) {
					HtmlCompressor.this.scriptBlocks.set(index, source);
				}
			} else if(task.equals(Task.STYLE)) {
				synchronized(HtmlCompressor.this.styleBlocks) {
					source = HtmlCompressor.this.styleBlocks.get(index);
				}
				try {
					source = HtmlCompressor.this.compressCssStyles(source);
				} catch (Exception e) {}
				synchronized(HtmlCompressor.this.styleBlocks) {
					HtmlCompressor.this.styleBlocks.set(index, source);
				}
			}
		}
	}
	
	/**
	 * Returns <code>true</code> if JavaScript compression is enabled.
	 * 
	 * @return current state of JavaScript compression.
	 */
	public boolean isCompressJavaScript() {
		return compressJavaScript;
	}

	/**
	 * Enables JavaScript compression within &lt;script> tags using 
	 * <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a> 
	 * if set to <code>true</code>. Default is <code>false</code> for performance reasons.
	 *  
	 * <p><b>Note:</b> Compressing JavaScript is not recommended if pages are 
	 * compressed dynamically on-the-fly because of performance impact. 
	 * You should consider putting JavaScript into a separate file and
	 * compressing it using standalone YUICompressor for example.</p>
	 * 
	 * @param compressJavaScript set <code>true</code> to enable JavaScript compression. 
	 * Default is <code>false</code>
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 * 
	 */
	public void setCompressJavaScript(boolean compressJavaScript) {
		this.compressJavaScript = compressJavaScript;
	}

	/**
	 * Returns <code>true</code> if CSS compression is enabled.
	 * 
	 * @return current state of CSS compression.
	 */
	public boolean isCompressCss() {
		return compressCss;
	}

	/**
	 * Enables CSS compression within &lt;style> tags using 
	 * <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a> 
	 * if set to <code>true</code>. Default is <code>false</code> for performance reasons.
	 *  
	 * <p><b>Note:</b> Compressing CSS is not recommended if pages are 
	 * compressed dynamically on-the-fly because of performance impact. 
	 * You should consider putting CSS into a separate file and
	 * compressing it using standalone YUICompressor for example.</p>
	 * 
	 * @param compressCss set <code>true</code> to enable CSS compression. 
	 * Default is <code>false</code>
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 * 
	 */
	public void setCompressCss(boolean compressCss) {
		this.compressCss = compressCss;
	}

	/**
	 * Returns <code>true</code> if Yahoo YUI Compressor
	 * will only minify javascript without obfuscating local symbols. 
	 * This corresponds to <code>--nomunge</code> command line option.  
	 *   
	 * @return <code>nomunge</code> parameter value used for JavaScript compression.
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public boolean isYuiJsNoMunge() {
		return yuiJsNoMunge;
	}

	/**
	 * Tells Yahoo YUI Compressor to only minify javascript without obfuscating 
	 * local symbols. This corresponds to <code>--nomunge</code> command line option. 
	 * This option has effect only if JavaScript compression is enabled. 
	 * Default is <code>false</code>.
	 * 
	 * @param yuiJsNoMunge set <code>true<code> to enable <code>nomunge</code> mode
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public void setYuiJsNoMunge(boolean yuiJsNoMunge) {
		this.yuiJsNoMunge = yuiJsNoMunge;
	}

	/**
	 * Returns <code>true</code> if Yahoo YUI Compressor
	 * will preserve unnecessary semicolons during JavaScript compression. 
	 * This corresponds to <code>--preserve-semi</code> command line option.
	 *   
	 * @return <code>preserve-semi</code> parameter value used for JavaScript compression.
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public boolean isYuiJsPreserveAllSemiColons() {
		return yuiJsPreserveAllSemiColons;
	}

	/**
	 * Tells Yahoo YUI Compressor to preserve unnecessary semicolons 
	 * during JavaScript compression. This corresponds to 
	 * <code>--preserve-semi</code> command line option. 
	 * This option has effect only if JavaScript compression is enabled.
	 * Default is <code>false</code>.
	 * 
	 * @param yuiJsPreserveAllSemiColons set <code>true<code> to enable <code>preserve-semi</code> mode
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public void setYuiJsPreserveAllSemiColons(boolean yuiJsPreserveAllSemiColons) {
		this.yuiJsPreserveAllSemiColons = yuiJsPreserveAllSemiColons;
	}

	/**
	 * Returns <code>true</code> if Yahoo YUI Compressor
	 * will disable all the built-in micro optimizations during JavaScript compression. 
	 * This corresponds to <code>--disable-optimizations</code> command line option.
	 *   
	 * @return <code>disable-optimizations</code> parameter value used for JavaScript compression.
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public boolean isYuiJsDisableOptimizations() {
		return yuiJsDisableOptimizations;
	}
	
	/**
	 * Tells Yahoo YUI Compressor to disable all the built-in micro optimizations
	 * during JavaScript compression. This corresponds to 
	 * <code>--disable-optimizations</code> command line option. 
	 * This option has effect only if JavaScript compression is enabled.
	 * Default is <code>false</code>.
	 * 
	 * @param yuiJsDisableOptimizations set <code>true<code> to enable 
	 * <code>disable-optimizations</code> mode
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public void setYuiJsDisableOptimizations(boolean yuiJsDisableOptimizations) {
		this.yuiJsDisableOptimizations = yuiJsDisableOptimizations;
	}
	
	/**
	 * Returns number of symbols per line Yahoo YUI Compressor
	 * will use during JavaScript compression. 
	 * This corresponds to <code>--line-break</code> command line option.
	 *   
	 * @return <code>line-break</code> parameter value used for JavaScript compression.
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public int getYuiJsLineBreak() {
		return yuiJsLineBreak;
	}

	/**
	 * Tells Yahoo YUI Compressor to break lines after the specified number of symbols 
	 * during JavaScript compression. This corresponds to 
	 * <code>--line-break</code> command line option. 
	 * This option has effect only if JavaScript compression is enabled.
	 * Default is <code>-1</code> to disable line breaks.
	 * 
	 * @param yuiJsLineBreak set number of symbols per line
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public void setYuiJsLineBreak(int yuiJsLineBreak) {
		this.yuiJsLineBreak = yuiJsLineBreak;
	}
	
	/**
	 * Returns number of symbols per line Yahoo YUI Compressor
	 * will use during CSS compression. 
	 * This corresponds to <code>--line-break</code> command line option.
	 *   
	 * @return <code>line-break</code> parameter value used for CSS compression.
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public int getYuiCssLineBreak() {
		return yuiCssLineBreak;
	}
	
	/**
	 * Tells Yahoo YUI Compressor to break lines after the specified number of symbols 
	 * during CSS compression. This corresponds to 
	 * <code>--line-break</code> command line option. 
	 * This option has effect only if CSS compression is enabled.
	 * Default is <code>-1</code> to disable line breaks.
	 * 
	 * @param yuiCssLineBreak set number of symbols per line
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public void setYuiCssLineBreak(int yuiCssLineBreak) {
		this.yuiCssLineBreak = yuiCssLineBreak;
	}

	/**
	 * Returns <code>true</code> if all unnecessary quotes will be removed 
	 * from tag attributes. 
	 *   
	 */
	public boolean isRemoveQuotes() {
		return removeQuotes;
	}

	/**
	 * If set to <code>true</code> all unnecessary quotes will be removed  
	 * from tag attributes. Default is <code>false</code>.
	 * 
	 * <p><b>Note:</b> Even though quotes are removed only when it is safe to do so, 
	 * it still might break strict HTML validation. Turn this option on only if 
	 * a page validation is not very important or to squeeze the most out of the compression.
	 * This option has no performance impact. 
	 * 
	 * @param removeQuotes set <code>true</code> to remove unnecessary quotes from tag attributes
	 */
	public void setRemoveQuotes(boolean removeQuotes) {
		this.removeQuotes = removeQuotes;
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
	 * Returns <code>true</code> if all HTML comments will be removed.
	 * 
	 * @return <code>true</code> if all HTML comments will be removed
	 */
	public boolean isRemoveComments() {
		return removeComments;
	}

	/**
	 * If set to <code>true</code> all HTML comments will be removed.   
	 * Default is <code>true</code>.
	 * 
	 * @param removeComments set <code>true</code> to remove all HTML comments
	 */
	public void setRemoveComments(boolean removeComments) {
		this.removeComments = removeComments;
	}

	/**
	 * Returns <code>true</code> if all multiple whitespace characters will be replaced with single spaces.
	 * 
	 * @return <code>true</code> if all multiple whitespace characters will be replaced with single spaces.
	 */
	public boolean isRemoveMultiSpaces() {
		return removeMultiSpaces;
	}

	/**
	 * If set to <code>true</code> all multiple whitespace characters will be replaced with single spaces.
	 * Default is <code>true</code>.
	 * 
	 * @param removeMultiSpaces set <code>true</code> to replace all multiple whitespace characters 
	 * will single spaces.
	 */
	public void setRemoveMultiSpaces(boolean removeMultiSpaces) {
		this.removeMultiSpaces = removeMultiSpaces;
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
	 * Default is <code>false</code>.
	 * 
	 * <p><b>Note:</b> It is fairly safe to turn this option on unless you 
	 * rely on spaces for page formatting. Even if you do, you can always preserve 
	 * required spaces with <code>&amp;nbsp;</code>. This option has no performance impact.    
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
	 * large files or if using javascript/style inline compression (as it is time consuming). 
	 * Usually optimal number of threads equals to a number of processor cores in the system.
	 * Default value is 1 (no threading, everything is done in the main thread).
	 * 
	 * @param threads number of threads that are used during a compression 
	 */
	public void setThreads(int threads) {
		this.threads = threads;
	}
	
}