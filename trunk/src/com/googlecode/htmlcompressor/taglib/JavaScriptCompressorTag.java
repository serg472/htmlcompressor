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
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * JSP tag that compresses an JavaScript content within &lt;compress:js> using <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>.
 * All JavaScript-related properties from {@link HtmlCompressor} are supported.
 * 
 * @see HtmlCompressor
 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 */
@SuppressWarnings("serial")
public class JavaScriptCompressorTag extends BodyTagSupport {
	
	private boolean enabled = true;
	
	//YUICompressor settings
	private boolean yuiJsNoMunge = false;
	private boolean yuiJsPreserveAllSemiColons = false;
	private boolean yuiJsDisableOptimizations = false;
	private int yuiJsLineBreak = -1;

	@Override
	public int doEndTag() throws JspException {
		
		if(enabled) {
			BodyContent bodyContent = getBodyContent();
			String content = bodyContent.getString();
		
			try {
				//call YUICompressor
				StringWriter result = new StringWriter();
				JavaScriptCompressor compressor = new JavaScriptCompressor(new StringReader(content), null);
				compressor.compress(result, yuiJsLineBreak, !yuiJsNoMunge, false, yuiJsPreserveAllSemiColons, yuiJsDisableOptimizations);
				
				bodyContent.clear();
				bodyContent.append(result.toString());
				bodyContent.writeOut(pageContext.getOut());
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
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

}