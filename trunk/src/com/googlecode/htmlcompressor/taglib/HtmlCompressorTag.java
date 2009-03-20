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

import com.googlecode.htmlcompressor.compressor.HtmlCompressor;

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
	private boolean compressJavaScript = false;
	private boolean compressCss = false;
	
	//YUICompressor settings
	private boolean yuiJsNoMunge = false;
	private boolean yuiJsPreserveAllSemiColons = false;
	private boolean yuiJsDisableOptimizations = false;
	private int yuiJsLineBreak = -1;
	private int yuiCssLineBreak = -1;

	@Override
	public int doEndTag() throws JspException {
		
		BodyContent bodyContent = getBodyContent();
		String content = bodyContent.getString();
		
		HtmlCompressor compressor = new HtmlCompressor();
		compressor.setEnabled(enabled);
		compressor.setRemoveComments(removeComments);
		compressor.setRemoveMultiSpaces(removeMultiSpaces);
		compressor.setRemoveIntertagSpaces(removeIntertagSpaces);
		compressor.setRemoveQuotes(removeQuotes);
		compressor.setCompressJavaScript(compressJavaScript);
		compressor.setCompressCss(compressCss);
		compressor.setYuiJsNoMunge(yuiJsNoMunge);
		compressor.setYuiJsPreserveAllSemiColons(yuiJsPreserveAllSemiColons);
		compressor.setYuiJsDisableOptimizations(yuiJsDisableOptimizations);
		compressor.setYuiJsLineBreak(yuiJsLineBreak);
		compressor.setYuiCssLineBreak(yuiCssLineBreak);
		
		try {
			bodyContent.clear();
			bodyContent.append(compressor.compress(content));
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
	
}