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
 * JSP tag that compresses an JavaScript content within &lt;compress:js> tags.
 * All JavaScript-related properties from {@link HtmlCompressor} are supported.
 * 
 * @see HtmlCompressor
 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
 * @see <a href="http://code.google.com/closure/compiler/">Google Closure Compiler</a>
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 */
@SuppressWarnings("serial")
public class JavaScriptCompressorTag extends BodyTagSupport {
	
	private boolean enabled = true;
	
	private String jsCompressor = HtmlCompressor.JS_COMPRESSOR_YUI;
	
	//YUICompressor settings
	private boolean yuiJsNoMunge = false;
	private boolean yuiJsPreserveAllSemiColons = false;
	private boolean yuiJsDisableOptimizations = false;
	private int yuiJsLineBreak = -1;
	
	//Closure compressor settings
	private String closureOptLevel = ClosureJavaScriptCompressor.COMPILATION_LEVEL_SIMPLE;

	@Override
	public int doEndTag() throws JspException {
		
		BodyContent bodyContent = getBodyContent();
		String content = bodyContent.getString();
	
		try {
			if(enabled) {
				
				String result = content;
				
				if(jsCompressor.equalsIgnoreCase(HtmlCompressor.JS_COMPRESSOR_CLOSURE)) {
					//call Closure compressor
					ClosureJavaScriptCompressor closureCompressor = new ClosureJavaScriptCompressor();
					if(closureOptLevel.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_ADVANCED)) {
						closureCompressor.setCompilationLevel(CompilationLevel.ADVANCED_OPTIMIZATIONS);
					} else if(closureOptLevel.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_WHITESPACE)) {
						closureCompressor.setCompilationLevel(CompilationLevel.WHITESPACE_ONLY);
					} else {
						closureCompressor.setCompilationLevel(CompilationLevel.SIMPLE_OPTIMIZATIONS);
					}
					
					result = closureCompressor.compress(content);
					
				} else {
					//call YUICompressor
					YuiJavaScriptCompressor yuiCompressor = new YuiJavaScriptCompressor();
					yuiCompressor.setDisableOptimizations(yuiJsDisableOptimizations);
					yuiCompressor.setLineBreak(yuiJsLineBreak);
					yuiCompressor.setNoMunge(yuiJsNoMunge);
					yuiCompressor.setPreserveAllSemiColons(yuiJsPreserveAllSemiColons);
					
					result = yuiCompressor.compress(content);
				}
				
				bodyContent.clear();
				bodyContent.append(result);
				bodyContent.writeOut(pageContext.getOut());
			} else {
				bodyContent.clear();
				bodyContent.append(content);
				bodyContent.writeOut(pageContext.getOut());
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return super.doEndTag();
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
	 * @see HtmlCompressor#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
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

}