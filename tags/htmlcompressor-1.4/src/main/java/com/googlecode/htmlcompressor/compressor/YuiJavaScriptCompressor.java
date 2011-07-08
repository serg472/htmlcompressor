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

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

import com.yahoo.platform.yui.compressor.JavaScriptCompressor;

/**
 * Basic JavaScript compressor implementation using <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a> 
 * that could be used by {@link HtmlCompressor} for inline JavaScript compression.
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 * 
 * @see HtmlCompressor#setJavaScriptCompressor(Compressor)
 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
 */
public class YuiJavaScriptCompressor implements Compressor {

	//YUICompressor default settings
	private boolean noMunge = false;
	private boolean preserveAllSemiColons = false;
	private boolean disableOptimizations = false;
	private int lineBreak = -1;
	
	private ErrorReporter errorReporter = new DefaultErrorReporter();
	
	public YuiJavaScriptCompressor() {
	}
	
	@Override
	public String compress(String source) {
		
		StringWriter result = new StringWriter();
		try {
			JavaScriptCompressor compressor = new JavaScriptCompressor(new StringReader(source), errorReporter);
			compressor.compress(result, lineBreak, !noMunge, false, preserveAllSemiColons, disableOptimizations);
		} catch (IOException e) {
			result.write(source);
			e.printStackTrace();
		}
		return result.toString();
		
	}
	
	/**
	 * Default <code>ErrorReporter</code> implementation that uses <code>System.err</code> 
	 * stream for error reporting. Used by YUI Compressor to log errors during JavaScript compression.
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 * @see <a href="http://www.mozilla.org/rhino/apidocs/org/mozilla/javascript/ErrorReporter.html">ErrorReporter Interface</a>
	 * 
	 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
	 * 
	 */
	public static class DefaultErrorReporter implements ErrorReporter{
		
		public void warning(String message, String sourceName, int line, String lineSource, int lineOffset) {
			if (line < 0) {
				System.err.println("[WARNING] HtmlCompressor: \"" + message + "\" during JavaScript compression");
			} else {
				System.err.println("[WARNING] HtmlCompressor: \"" + message + "\" at line [" + line + ":" + lineOffset + "] during JavaScript compression" + (lineSource != null ? ": " + lineSource : ""));
			}
		}

		public void error(String message, String sourceName, int line, String lineSource, int lineOffset) {
			if (line < 0) {
				System.err.println("[ERROR] HtmlCompressor: \"" + message + "\" during JavaScript compression");
			} else {
				System.err.println("[ERROR] HtmlCompressor: \"" + message + "\" at line [" + line + ":" + lineOffset + "] during JavaScript compression" + (lineSource != null ? ": " + lineSource : ""));
			}
		}

		public EvaluatorException runtimeError(String message, String sourceName, int line, String lineSource, int lineOffset) {
			error(message, sourceName, line, lineSource, lineOffset);
			return new EvaluatorException(message);
		}
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
	public boolean isNoMunge() {
		return noMunge;
	}

	/**
	 * Tells Yahoo YUI Compressor to only minify javascript without obfuscating 
	 * local symbols. This corresponds to <code>--nomunge</code> command line option. 
	 * This option has effect only if JavaScript compression is enabled. 
	 * Default is <code>false</code>.
	 * 
	 * @param noMunge set <code>true<code> to enable <code>nomunge</code> mode
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public void setNoMunge(boolean noMunge) {
		this.noMunge = noMunge;
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
	public boolean isPreserveAllSemiColons() {
		return preserveAllSemiColons;
	}

	/**
	 * Tells Yahoo YUI Compressor to preserve unnecessary semicolons 
	 * during JavaScript compression. This corresponds to 
	 * <code>--preserve-semi</code> command line option. 
	 * This option has effect only if JavaScript compression is enabled.
	 * Default is <code>false</code>.
	 * 
	 * @param preserveAllSemiColons set <code>true<code> to enable <code>preserve-semi</code> mode
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public void setPreserveAllSemiColons(boolean preserveAllSemiColons) {
		this.preserveAllSemiColons = preserveAllSemiColons;
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
	public boolean isDisableOptimizations() {
		return disableOptimizations;
	}

	/**
	 * Tells Yahoo YUI Compressor to disable all the built-in micro optimizations
	 * during JavaScript compression. This corresponds to 
	 * <code>--disable-optimizations</code> command line option. 
	 * This option has effect only if JavaScript compression is enabled.
	 * Default is <code>false</code>.
	 * 
	 * @param disableOptimizations set <code>true<code> to enable 
	 * <code>disable-optimizations</code> mode
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public void setDisableOptimizations(boolean disableOptimizations) {
		this.disableOptimizations = disableOptimizations;
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
	public int getLineBreak() {
		return lineBreak;
	}

	/**
	 * Tells Yahoo YUI Compressor to break lines after the specified number of symbols 
	 * during JavaScript compression. This corresponds to 
	 * <code>--line-break</code> command line option. 
	 * This option has effect only if JavaScript compression is enabled.
	 * Default is <code>-1</code> to disable line breaks.
	 * 
	 * @param lineBreak set number of symbols per line
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 */
	public void setLineBreak(int lineBreak) {
		this.lineBreak = lineBreak;
	}

	/**
	 * Returns <code>ErrorReporter</code> used by YUI Compressor to log error messages 
	 * during JavasSript compression 
	 * 
	 * @return <code>ErrorReporter</code> used by YUI Compressor to log error messages 
	 * during JavasSript compression
	 * 
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 * @see <a href="http://www.mozilla.org/rhino/apidocs/org/mozilla/javascript/ErrorReporter.html">Error Reporter Interface</a>
	 */
	public ErrorReporter getErrorReporter() {
		return errorReporter;
	}

	/**
	 * Sets <code>ErrorReporter</code> that YUI Compressor will use for reporting errors during 
	 * JavaScript compression. If no <code>ErrorReporter</code> was provided 
	 * {@link YuiJavaScriptCompressor.DefaultErrorReporter} will be used 
	 * which reports errors to <code>System.err</code> stream. 
	 * 
	 * @param errorReporter <code>ErrorReporter<code> that will be used by YUI Compressor
	 * 
	 * @see YuiJavaScriptCompressor.DefaultErrorReporter
	 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
	 * @see <a href="http://www.mozilla.org/rhino/apidocs/org/mozilla/javascript/ErrorReporter.html">ErrorReporter Interface</a>
	 */
	public void setErrorReporter(ErrorReporter errorReporter) {
		this.errorReporter = errorReporter;
	}

}
