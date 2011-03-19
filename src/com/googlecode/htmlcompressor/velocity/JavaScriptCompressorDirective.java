package com.googlecode.htmlcompressor.velocity;

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
import java.io.StringWriter;
import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.exception.TemplateInitException;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.log.Log;
import org.apache.velocity.runtime.parser.node.Node;

import com.google.javascript.jscomp.CompilationLevel;
import com.googlecode.htmlcompressor.compressor.ClosureJavaScriptCompressor;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.googlecode.htmlcompressor.compressor.YuiJavaScriptCompressor;

/**
 * Velocity directive that compresses an JavaScript content within #compressJs ... #end block.
 * All JavaScript-related properties from {@link HtmlCompressor} are supported.
 * 
 * @see HtmlCompressor
 * @see <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>
 * @see <a href="http://code.google.com/closure/compiler/">Google Closure Compiler</a>
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 */
public class JavaScriptCompressorDirective extends Directive {
	
	private Log log;
	
	private boolean enabled = true;
	
	private String jsCompressor = HtmlCompressor.JS_COMPRESSOR_YUI;
	
	//YUICompressor settings
	private boolean yuiJsNoMunge = false;
	private boolean yuiJsPreserveAllSemiColons = false;
	private boolean yuiJsDisableOptimizations = false;
	private int yuiJsLineBreak = -1;
	
	//Closure compressor settings
	private String closureOptLevel = ClosureJavaScriptCompressor.COMPILATION_LEVEL_SIMPLE;

	public String getName() {
		return "compressJs";
	}

	public int getType() {
		return BLOCK;
	}
	
	@Override
	public void init(RuntimeServices rs, InternalContextAdapter context, Node node) throws TemplateInitException {
		super.init(rs, context, node);
		log = rs.getLog();
		
		//set compressor properties
		enabled = rs.getBoolean("userdirective.compressJs.enabled", true);
		jsCompressor = rs.getString("userdirective.compressHtml.jsCompressor", HtmlCompressor.JS_COMPRESSOR_YUI);
		yuiJsNoMunge = rs.getBoolean("userdirective.compressJs.yuiJsNoMunge", false);
		yuiJsPreserveAllSemiColons = rs.getBoolean("userdirective.compressJs.yuiJsPreserveAllSemiColons", false);
		yuiJsLineBreak = rs.getInt("userdirective.compressJs.yuiJsLineBreak", -1);
		closureOptLevel = rs.getString("userdirective.compressHtml.closureOptLevel", ClosureJavaScriptCompressor.COMPILATION_LEVEL_SIMPLE);
	}

    public boolean render(InternalContextAdapter context, Writer writer, Node node) 
    		throws IOException, ResourceNotFoundException, ParseErrorException, MethodInvocationException {
    	
    	//render content
    	StringWriter content = new StringWriter();
		node.jjtGetChild(0).render(context, content);
		
		//compress
		if(enabled) {
			try {
				String result = content.toString();
				
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
					
					result = closureCompressor.compress(result);
					
				} else {
					//call YUICompressor
					YuiJavaScriptCompressor yuiCompressor = new YuiJavaScriptCompressor();
					yuiCompressor.setDisableOptimizations(yuiJsDisableOptimizations);
					yuiCompressor.setLineBreak(yuiJsLineBreak);
					yuiCompressor.setNoMunge(yuiJsNoMunge);
					yuiCompressor.setPreserveAllSemiColons(yuiJsPreserveAllSemiColons);
					
					result = yuiCompressor.compress(result);
				}
				
				writer.write(result);
			} catch (Exception e) {
				writer.write(content.toString());
				String msg = "Failed to compress content: "+content.toString();
	            log.error(msg, e);
	            throw new RuntimeException(msg, e);
	            
			}
		} else {
			writer.write(content.toString());
		}
		
		return true;
    	
    }

}
