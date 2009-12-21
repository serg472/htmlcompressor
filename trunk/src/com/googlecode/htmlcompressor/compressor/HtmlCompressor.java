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
	
	//YUICompressor settings
	private boolean yuiJsNoMunge = false;
	private boolean yuiJsPreserveAllSemiColons = false;
	private boolean yuiJsDisableOptimizations = false;
	private int yuiJsLineBreak = -1;
	private int yuiCssLineBreak = -1;
	
	//temp replacements for preserved blocks 
	private static final String tempPreBlock = "%%%COMPRESS~PRE~#%%%";
	private static final String tempTextAreaBlock = "%%%COMPRESS~TEXTAREA~#%%%";
	private static final String tempScriptBlock = "%%%COMPRESS~SCRIPT~#%%%";
	private static final String tempStyleBlock = "%%%COMPRESS~STYLE~#%%%";
	private static final String tempEventBlock = "%%%COMPRESS~EVENT~#%%%";
	
	//compiled regex patterns
	private static final Pattern commentPattern = Pattern.compile("<!--[^\\[].*?-->", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern intertagPattern = Pattern.compile(">\\s+?<", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern multispacePattern = Pattern.compile("\\s{2,}", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern tagEndSpacePattern = Pattern.compile("(<(?:[^>]+?))(?:\\s+?)(/?>)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern tagQuotePattern = Pattern.compile("\\s*=\\s*([\"'])([a-z0-9-_]+?)\\1(?=[^<]*?>)", Pattern.CASE_INSENSITIVE);
	private static final Pattern prePattern = Pattern.compile("(<pre[^>]*?>)(.*?)(</pre>)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern taPattern = Pattern.compile("(<textarea[^>]*?>)(.*?)(</textarea>)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern scriptPattern = Pattern.compile("(<script[^>]*?>)(.*?)(</script>)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern stylePattern = Pattern.compile("(<style[^>]*?>)(.*?)(</style>)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern tagPropertyPattern = Pattern.compile("(\\s\\w+)\\s=\\s(?=[^<]*?>)", Pattern.CASE_INSENSITIVE);
	
	//unmasked: \son[a-z]+\s*=\s*"[^"\\\r\n]*(?:\\.[^"\\\r\n]*)*"
	private static final Pattern eventPattern1 = Pattern.compile("(\\son[a-z]+\\s*=\\s*\")([^\"\\\\\\r\\n]*(?:\\\\.[^\"\\\\\\r\\n]*)*)(\")", Pattern.CASE_INSENSITIVE);
	private static final Pattern eventPattern2 = Pattern.compile("(\\son[a-z]+\\s*=\\s*')([^'\\\\\\r\\n]*(?:\\\\.[^'\\\\\\r\\n]*)*)(')", Pattern.CASE_INSENSITIVE);
	
	
	
	private static final Pattern tempPrePattern = Pattern.compile("%%%COMPRESS~PRE~(\\d+?)%%%", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern tempTextAreaPattern = Pattern.compile("%%%COMPRESS~TEXTAREA~(\\d+?)%%%", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern tempScriptPattern = Pattern.compile("%%%COMPRESS~SCRIPT~(\\d+?)%%%", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern tempStylePattern = Pattern.compile("%%%COMPRESS~STYLE~(\\d+?)%%%", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	private static final Pattern tempEventPattern = Pattern.compile("%%%COMPRESS~EVENT~(\\d+?)%%%", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
	
	
	/**
	 * The main method that compresses given HTML source and returns compressed result.
	 * 
	 * @param html HTML content to compress
	 * @return compressed content.
	 * @throws Exception
	 */
	@Override
	public String compress(String html) throws Exception {
		if(!enabled || html == null || html.length() == 0) {
			return html;
		}
		
		//preserved block containers
		List<String> preBlocks = new ArrayList<String>();
		List<String> taBlocks = new ArrayList<String>();
		List<String> scriptBlocks = new ArrayList<String>();
		List<String> styleBlocks = new ArrayList<String>();
		List<String> eventBlocks = new ArrayList<String>();
		
		//preserve blocks
		html = preserveBlocks(html, preBlocks, taBlocks, scriptBlocks, styleBlocks, eventBlocks);
		
		//process pure html
		html = processHtml(html);

		//process preserved blocks
		processScriptBlocks(scriptBlocks);
		processStyleBlocks(styleBlocks);
		
		//put blocks back
		html = returnBlocks(html, preBlocks, taBlocks, scriptBlocks, styleBlocks, eventBlocks);
		
		return html.trim();
	}

	private String preserveBlocks(String html, List<String> preBlocks, List<String> taBlocks, List<String> scriptBlocks, List<String> styleBlocks, List<String> eventBlocks) {
		//preserve inline events
		Matcher matcher = eventPattern1.matcher(html);
		int index = 0;
		StringBuffer sb = new StringBuffer();
		while(matcher.find()) {
			if(matcher.group(2).trim().length() > 0) {
				eventBlocks.add(matcher.group(2));
				matcher.appendReplacement(sb, "$1"+tempEventBlock.replaceFirst("#", Integer.toString(index++))+"$3");
			}
		}
		matcher.appendTail(sb);
		html = sb.toString();
		
		matcher = eventPattern2.matcher(html);
		sb = new StringBuffer();
		while(matcher.find()) {
			if(matcher.group(2).trim().length() > 0) {
				eventBlocks.add(matcher.group(2));
				matcher.appendReplacement(sb, "$1"+tempEventBlock.replaceFirst("#", Integer.toString(index++))+"$3");
			}
		}
		matcher.appendTail(sb);
		html = sb.toString();
		
		//preserve PRE tags
		matcher = prePattern.matcher(html);
		index = 0;
		sb = new StringBuffer();
		while(matcher.find()) {
			if(matcher.group(2).trim().length() > 0) {
				preBlocks.add(matcher.group(2));
				matcher.appendReplacement(sb, "$1"+tempPreBlock.replaceFirst("#", Integer.toString(index++))+"$3");
			}
		}
		matcher.appendTail(sb);
		html = sb.toString();
		
		//preserve SCRIPT tags
		matcher = scriptPattern.matcher(html);
		index = 0;
		sb = new StringBuffer();
		while(matcher.find()) {
			if(matcher.group(2).trim().length() > 0) {
				scriptBlocks.add(matcher.group(2));
				matcher.appendReplacement(sb, "$1"+tempScriptBlock.replaceFirst("#", Integer.toString(index++))+"$3");
			}
		}
		matcher.appendTail(sb);
		html = sb.toString();

		//preserve STYLE tags
		matcher = stylePattern.matcher(html);
		index = 0;
		sb = new StringBuffer();
		while(matcher.find()) {
			if(matcher.group(2).trim().length() > 0) {
				styleBlocks.add(matcher.group(2));
				matcher.appendReplacement(sb, "$1"+tempStyleBlock.replaceFirst("#", Integer.toString(index++))+"$3");
			}
		}
		matcher.appendTail(sb);
		html = sb.toString();
		
		//preserve TEXTAREA tags
		matcher = taPattern.matcher(html);
		index = 0;
		sb = new StringBuffer();
		while(matcher.find()) {
			if(matcher.group(2).trim().length() > 0) {
				taBlocks.add(matcher.group(2));
				matcher.appendReplacement(sb, "$1"+tempTextAreaBlock.replaceFirst("#", Integer.toString(index++))+"$3");
			}
		}
		matcher.appendTail(sb);
		html = sb.toString();

		return html;
	}
	
	private String returnBlocks(String html, List<String> preBlocks, List<String> taBlocks, List<String> scriptBlocks, List<String> styleBlocks, List<String> eventBlocks) {
		//put TEXTAREA blocks back
		Matcher matcher = tempTextAreaPattern.matcher(html);
		StringBuffer sb = new StringBuffer();
		while(matcher.find()) {
			matcher.appendReplacement(sb, Matcher.quoteReplacement(taBlocks.get(Integer.parseInt(matcher.group(1)))));
		}
		matcher.appendTail(sb);
		html = sb.toString();
		
		//put STYLE blocks back
		matcher = tempStylePattern.matcher(html);
		sb = new StringBuffer();
		while(matcher.find()) {
			matcher.appendReplacement(sb, Matcher.quoteReplacement(styleBlocks.get(Integer.parseInt(matcher.group(1)))));
		}
		matcher.appendTail(sb);
		html = sb.toString();
		
		//put SCRIPT blocks back
		matcher = tempScriptPattern.matcher(html);
		sb = new StringBuffer();
		while(matcher.find()) {
			matcher.appendReplacement(sb, Matcher.quoteReplacement(scriptBlocks.get(Integer.parseInt(matcher.group(1)))));
		}
		matcher.appendTail(sb);
		html = sb.toString();

		//put PRE blocks back
		matcher = tempPrePattern.matcher(html);
		sb = new StringBuffer();
		while(matcher.find()) {
			matcher.appendReplacement(sb, Matcher.quoteReplacement(preBlocks.get(Integer.parseInt(matcher.group(1)))));
		}
		matcher.appendTail(sb);
		html = sb.toString();
		
		//put event blocks back
		matcher = tempEventPattern.matcher(html);
		sb = new StringBuffer();
		while(matcher.find()) {
			matcher.appendReplacement(sb, Matcher.quoteReplacement(eventBlocks.get(Integer.parseInt(matcher.group(1)))));
		}
		matcher.appendTail(sb);
		html = sb.toString();
		
		return html;
	}
	
	private String processHtml(String html) {
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
			html = tagQuotePattern.matcher(html).replaceAll("=$2");
		}
		
		//remove spaces around equal sign inside tags
		html = tagPropertyPattern.matcher(html).replaceAll("$1=");
		
		//remove ending spaces inside tags
		html = tagEndSpacePattern.matcher(html).replaceAll("$1$2");
		
		return html;
	}
	
	private void processScriptBlocks(List<String> scriptBlocks) throws Exception {
		if(compressJavaScript) {
			for(int i = 0; i < scriptBlocks.size(); i++) {
				scriptBlocks.set(i, compressJavaScript(scriptBlocks.get(i)));
			}
		}
	}
	
	private void processStyleBlocks(List<String> styleBlocks) throws Exception {
		if(compressCss) {
			for(int i = 0; i < styleBlocks.size(); i++) {
				styleBlocks.set(i, compressCssStyles(styleBlocks.get(i)));
			}
		}
	}
	
	private String compressJavaScript(String source) throws Exception {
		//call YUICompressor
		StringWriter result = new StringWriter();
		JavaScriptCompressor compressor = new JavaScriptCompressor(new StringReader(source), null);
		compressor.compress(result, yuiJsLineBreak, !yuiJsNoMunge, false, yuiJsPreserveAllSemiColons, yuiJsDisableOptimizations);
		
		return result.toString();
	}
	
	private String compressCssStyles(String source) throws Exception {
		//call YUICompressor
		StringWriter result = new StringWriter();
		CssCompressor compressor = new CssCompressor(new StringReader(source));
		compressor.compress(result, yuiCssLineBreak);
		
		return result.toString();
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
	
}