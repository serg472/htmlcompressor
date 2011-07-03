package com.googlecode.htmlcompressor.taglib;

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

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.google.javascript.jscomp.CompilationLevel;
import com.googlecode.htmlcompressor.compressor.ClosureJavaScriptCompressor;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.googlecode.htmlcompressor.compressor.YuiJavaScriptCompressor;

/**
 * JSP tag that compresses an HTML content within &lt;compress:html>.
 * Compression parameters are set by default (no JavaScript and CSS compression).
 * 
 * @see HtmlCompressor
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 */
@SuppressWarnings("serial")
public class HtmlCompressorTag extends BodyTagSupport {
	
	private boolean enabled = true;
	
	//default settings
	private boolean removeComments = true;
	private boolean removeMultiSpaces = true;
	
	//optional settings
	private boolean removeIntertagSpaces = false;
	private boolean removeQuotes = false;
	private boolean preserveLineBreaks = false;
	private boolean simpleDoctype = false;
	private boolean removeScriptAttributes = false;
	private boolean removeStyleAttributes = false;
	private boolean removeLinkAttributes = false;
	private boolean removeFormAttributes = false;
	private boolean removeInputAttributes = false;
	private boolean simpleBooleanAttributes = false;
	private boolean removeJavaScriptProtocol = false;
	private boolean removeHttpProtocol = false;
	private boolean removeHttpsProtocol = false;
	private boolean compressJavaScript = false;
	private boolean compressCss = false;
	
	private String jsCompressor = HtmlCompressor.JS_COMPRESSOR_YUI;
	
	//YUICompressor settings
	private boolean yuiJsNoMunge = false;
	private boolean yuiJsPreserveAllSemiColons = false;
	private boolean yuiJsDisableOptimizations = false;
	private int yuiJsLineBreak = -1;
	private int yuiCssLineBreak = -1;
	
	//Closure compressor settings
	private String closureOptLevel = ClosureJavaScriptCompressor.COMPILATION_LEVEL_SIMPLE;

	@Override
	public int doEndTag() throws JspException {
		
		BodyContent bodyContent = getBodyContent();
		String content = bodyContent.getString();
		
		HtmlCompressor htmlCompressor = new HtmlCompressor();
		htmlCompressor.setEnabled(enabled);
		htmlCompressor.setRemoveComments(removeComments);
		htmlCompressor.setRemoveMultiSpaces(removeMultiSpaces);
		htmlCompressor.setRemoveIntertagSpaces(removeIntertagSpaces);
		htmlCompressor.setRemoveQuotes(removeQuotes);
		htmlCompressor.setPreserveLineBreaks(preserveLineBreaks);
		htmlCompressor.setCompressJavaScript(compressJavaScript);
		htmlCompressor.setCompressCss(compressCss);
		htmlCompressor.setYuiJsNoMunge(yuiJsNoMunge);
		htmlCompressor.setYuiJsPreserveAllSemiColons(yuiJsPreserveAllSemiColons);
		htmlCompressor.setYuiJsDisableOptimizations(yuiJsDisableOptimizations);
		htmlCompressor.setYuiJsLineBreak(yuiJsLineBreak);
		htmlCompressor.setYuiCssLineBreak(yuiCssLineBreak);

		htmlCompressor.setSimpleDoctype(simpleDoctype);
		htmlCompressor.setRemoveScriptAttributes(removeScriptAttributes);
		htmlCompressor.setRemoveStyleAttributes(removeStyleAttributes);
		htmlCompressor.setRemoveLinkAttributes(removeLinkAttributes);
		htmlCompressor.setRemoveFormAttributes(removeFormAttributes);
		htmlCompressor.setRemoveInputAttributes(removeInputAttributes);
		htmlCompressor.setSimpleBooleanAttributes(simpleBooleanAttributes);
		htmlCompressor.setRemoveJavaScriptProtocol(removeJavaScriptProtocol);
		htmlCompressor.setRemoveHttpProtocol(removeHttpProtocol);
		htmlCompressor.setRemoveHttpsProtocol(removeHttpsProtocol);
		
		if(compressJavaScript && jsCompressor.equalsIgnoreCase(HtmlCompressor.JS_COMPRESSOR_CLOSURE)) {
			ClosureJavaScriptCompressor closureCompressor = new ClosureJavaScriptCompressor();
			if(closureOptLevel.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_ADVANCED)) {
				closureCompressor.setCompilationLevel(CompilationLevel.ADVANCED_OPTIMIZATIONS);
			} else if(closureOptLevel.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_WHITESPACE)) {
				closureCompressor.setCompilationLevel(CompilationLevel.WHITESPACE_ONLY);
			} else {
				closureCompressor.setCompilationLevel(CompilationLevel.SIMPLE_OPTIMIZATIONS);
			}
			htmlCompressor.setJavaScriptCompressor(closureCompressor);
		}
		
		try {
			bodyContent.clear();
			bodyContent.append(htmlCompressor.compress(content));
			bodyContent.writeOut(pageContext.getOut());
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return super.doEndTag();
	}
	
	/**
	 * @see HtmlCompressor#setCompressJavaScript(boolean)
	 */
	public void setCompressJavaScript(boolean compressJavaScript) {
		this.compressJavaScript = compressJavaScript;
	}

	/**
	 * @see HtmlCompressor#setCompressCss(boolean)
	 */
	public void setCompressCss(boolean compressCss) {
		this.compressCss = compressCss;
	}

	/**
	 * @see HtmlCompressor#setYuiJsNoMunge(boolean)
	 */
	public void setYuiJsNoMunge(boolean yuiJsNoMunge) {
		this.yuiJsNoMunge = yuiJsNoMunge;
	}

	/**
	 * @see HtmlCompressor#setYuiJsPreserveAllSemiColons(boolean)
	 */
	public void setYuiJsPreserveAllSemiColons(boolean yuiJsPreserveAllSemiColons) {
		this.yuiJsPreserveAllSemiColons = yuiJsPreserveAllSemiColons;
	}

	/**
	 * @see HtmlCompressor#setYuiJsDisableOptimizations(boolean)
	 */
	public void setYuiJsDisableOptimizations(boolean yuiJsDisableOptimizations) {
		this.yuiJsDisableOptimizations = yuiJsDisableOptimizations;
	}
	
	/**
	 * @see HtmlCompressor#setYuiJsLineBreak(int)
	 */
	public void setYuiJsLineBreak(int yuiJsLineBreak) {
		this.yuiJsLineBreak = yuiJsLineBreak;
	}
	
	/**
	 * @see HtmlCompressor#setYuiCssLineBreak(int)
	 */
	public void setYuiCssLineBreak(int yuiCssLineBreak) {
		this.yuiCssLineBreak = yuiCssLineBreak;
	}

	/**
	 * @see HtmlCompressor#setRemoveQuotes(boolean)
	 */
	public void setRemoveQuotes(boolean removeQuotes) {
		this.removeQuotes = removeQuotes;
	}

	/**
	 * @see HtmlCompressor#setPreserveLineBreaks(boolean)
	 */
	public void setPreserveLineBreaks(boolean preserveLineBreaks) {
		this.preserveLineBreaks = preserveLineBreaks;
	}

	/**
	 * @see HtmlCompressor#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see HtmlCompressor#setRemoveComments(boolean)
	 */
	public void setRemoveComments(boolean removeComments) {
		this.removeComments = removeComments;
	}

	/**
	 * @see HtmlCompressor#setRemoveMultiSpaces(boolean)
	 */
	public void setRemoveMultiSpaces(boolean removeMultiSpaces) {
		this.removeMultiSpaces = removeMultiSpaces;
	}

	/**
	 * @see HtmlCompressor#setRemoveIntertagSpaces(boolean)
	 */
	public void setRemoveIntertagSpaces(boolean removeIntertagSpaces) {
		this.removeIntertagSpaces = removeIntertagSpaces;
	}
	
	/**
	 * Sets JavaScript compressor implementation that will be used 
	 * to compress inline JavaScript in HTML. 
	 * 
	 * @param jsCompressor Could be either <code>"yui"</code> for using {@link YuiJavaScriptCompressor} (used by default if none provided) or
	 * <code>"closure"</code> for using {@link ClosureJavaScriptCompressor}
	 * 
	 * @see YuiJavaScriptCompressor
 	 * @see ClosureJavaScriptCompressor
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 * @see <a href="http://code.google.com/closure/compiler/">Google Closure Compiler</a>

	 */
	public void setJsCompressor(String jsCompressor) {
		this.jsCompressor = jsCompressor;
	}
	
	/**
	 * Sets level of optimization if <a href="http://code.google.com/closure/compiler/">Google Closure Compiler</a> is used 
	 * for compressing inline JavaScript.
	 * 
	 * @param closureOptLevel Could be either <code>"simple"</code> (used by default), <code>"whitespace"</code> or <code>"advanced"</code>
	 * 
	 * @see ClosureJavaScriptCompressor#setCompilationLevel(CompilationLevel)
	 */
	public void setClosureOptLevel(String closureOptLevel) {
		this.closureOptLevel = closureOptLevel;
	}

	/**
	 * @see HtmlCompressor#setSimpleDoctype(boolean)
	 */
	public void setSimpleDoctype(boolean simpleDoctype) {
		this.simpleDoctype = simpleDoctype;
	}

	/**
	 * @see HtmlCompressor#setRemoveScriptAttributes(boolean)
	 */
	public void setRemoveScriptAttributes(boolean removeScriptAttributes) {
		this.removeScriptAttributes = removeScriptAttributes;
	}

	/**
	 * @see HtmlCompressor#setRemoveStyleAttributes(boolean)
	 */
	public void setRemoveStyleAttributes(boolean removeStyleAttributes) {
		this.removeStyleAttributes = removeStyleAttributes;
	}

	/**
	 * @see HtmlCompressor#setRemoveLinkAttributes(boolean)
	 */
	public void setRemoveLinkAttributes(boolean removeLinkAttributes) {
		this.removeLinkAttributes = removeLinkAttributes;
	}

	/**
	 * @see HtmlCompressor#setRemoveFormAttributes(boolean)
	 */
	public void setRemoveFormAttributes(boolean removeFormAttributes) {
		this.removeFormAttributes = removeFormAttributes;
	}

	/**
	 * @see HtmlCompressor#setRemoveInputAttributes(boolean)
	 */
	public void setRemoveInputAttributes(boolean removeInputAttributes) {
		this.removeInputAttributes = removeInputAttributes;
	}
	
	/**
	 * @see HtmlCompressor#setSimpleBooleanAttributes(boolean)
	 */
	public void setSimpleBooleanAttributes(boolean simpleBooleanAttributes) {
		this.simpleBooleanAttributes = simpleBooleanAttributes;
	}

	/**
	 * @see HtmlCompressor#setRemoveJavaScriptProtocol(boolean)
	 */
	public void setRemoveJavaScriptProtocol(boolean removeJavaScriptProtocol) {
		this.removeJavaScriptProtocol = removeJavaScriptProtocol;
	}

	/**
	 * @see HtmlCompressor#setRemoveHttpProtocol(boolean)
	 */
	public void setRemoveHttpProtocol(boolean removeHttpProtocol) {
		this.removeHttpProtocol = removeHttpProtocol;
	}

	/**
	 * @see HtmlCompressor#setRemoveHttpsProtocol(boolean)
	 */
	public void setRemoveHttpsProtocol(boolean removeHttpsProtocol) {
		this.removeHttpsProtocol = removeHttpsProtocol;
	}

}