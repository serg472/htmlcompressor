package com.googlecode.htmlcompressor;

import org.mozilla.javascript.ErrorReporter;
import org.mozilla.javascript.EvaluatorException;

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
public class DefaultErrorReporter implements ErrorReporter{
	
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
