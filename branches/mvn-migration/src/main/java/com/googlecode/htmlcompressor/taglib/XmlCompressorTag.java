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

import com.googlecode.htmlcompressor.compressor.XmlCompressor;

/**
 * JSP tag that compresses an XML content within &lt;compress:xml>.
 * Compression parameters are set by default.
 * 
 * @see XmlCompressor
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 */
@SuppressWarnings("serial")
public class XmlCompressorTag extends BodyTagSupport {
	
	private boolean enabled = true;
	
	//default settings
	private boolean removeComments = true;
	private boolean removeIntertagSpaces = true;
	
	@Override
	public int doEndTag() throws JspException {
		
		BodyContent bodyContent = getBodyContent();
		String content = bodyContent.getString();
		
		XmlCompressor compressor = new XmlCompressor();
		compressor.setEnabled(enabled);
		compressor.setRemoveComments(removeComments);
		compressor.setRemoveIntertagSpaces(removeIntertagSpaces);
		
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
	 * @see XmlCompressor#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * @see XmlCompressor#setRemoveComments(boolean)
	 */
	public void setRemoveComments(boolean removeComments) {
		this.removeComments = removeComments;
	}

	/**
	 * @see XmlCompressor#setRemoveIntertagSpaces(boolean)
	 */
	public void setRemoveIntertagSpaces(boolean removeIntertagSpaces) {
		this.removeIntertagSpaces = removeIntertagSpaces;
	}
	
}