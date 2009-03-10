package com.googlecode.htmlcompressor.taglib;

import java.io.IOException;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyContent;
import javax.servlet.jsp.tagext.BodyTagSupport;

import com.googlecode.htmlcompressor.compressor.Compressor;
import com.googlecode.htmlcompressor.compressor.XmlCompressor;

@SuppressWarnings("serial")
public class XmlCompressorTag extends BodyTagSupport {

	public int doEndTag() throws JspException {
		
		BodyContent bodyContent = getBodyContent();
		String content = bodyContent.getString();
		
		Compressor compressor = new XmlCompressor();
		
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
}
