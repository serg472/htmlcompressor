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

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import com.yahoo.platform.yui.compressor.CssCompressor;

/**
 * Basic CSS compressor implementation using <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>   
 * that could be used by {@link HtmlCompressor} for inline CSS compression.
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 * 
 * @see HtmlCompressor#setCssCompressor(Compressor)
 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
 */
public class YuiCssCompressor implements Compressor {
	
	private int lineBreak = -1;
    
    public YuiCssCompressor() {
    }
    
    @Override
	public String compress(String source) {
    	StringWriter result = new StringWriter();
		
		try {
			CssCompressor compressor = new CssCompressor(new StringReader(source));
			compressor.compress(result, lineBreak);
		} catch (IOException e) {
			result.write(source);
			e.printStackTrace();
		}
		
		return result.toString();
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
	public int getLineBreak() {
		return lineBreak;
	}
	/**
	 * Tells Yahoo YUI Compressor to break lines after the specified number of symbols 
	 * during CSS compression. This corresponds to 
	 * <code>--line-break</code> command line option. 
	 * This option has effect only if CSS compression is enabled.
	 * Default is <code>-1</code> to disable line breaks.
	 * 
	 * @param lineBreak set number of symbols per line
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public void setLineBreak(int lineBreak) {
		this.lineBreak = lineBreak;
	}

}
