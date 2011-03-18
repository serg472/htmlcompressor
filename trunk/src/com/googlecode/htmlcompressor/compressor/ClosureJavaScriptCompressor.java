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

import java.io.StringWriter;
import java.util.logging.Level;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;

/**
 * Basic JavaScript compressor implementation using <a href="http://code.google.com/closure/compiler/">Google Closure Compiler</a> 
 * that could be used by {@link HtmlCompressor} for inline JavaScript compression.
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 * 
 * @see HtmlCompressor#setJavaScriptCompressor(Compressor)
 * @see <a href="http://code.google.com/closure/compiler/">Google Closure Compiler</a>
 */
public class ClosureJavaScriptCompressor implements Compressor {
	
	public static final String COMPILATION_LEVEL_SIMPLE = "simple";
	public static final String COMPILATION_LEVEL_ADVANCED = "advanced";
	public static final String COMPILATION_LEVEL_WHITESPACE = "whitespace";

	//Closure compiler default settings
	private CompilerOptions compilerOptions = new CompilerOptions();
	private CompilationLevel compilationLevel = CompilationLevel.SIMPLE_OPTIMIZATIONS;
	private Level loggingLevel = Level.SEVERE;
	
	private JSSourceFile externs = JSSourceFile.fromCode("externs.js", "");

	public ClosureJavaScriptCompressor() {
	}

	public ClosureJavaScriptCompressor(CompilationLevel compilationLevel) {
		this.compilationLevel = compilationLevel;
	}

	@Override
	public String compress(String source) throws Exception {
		
		StringWriter writer = new StringWriter();
		
		JSSourceFile input = JSSourceFile.fromCode("", source);
		
		Compiler.setLoggingLevel(loggingLevel);
		
		Compiler compiler = new Compiler();
		compiler.disableThreads();
		
		compilationLevel.setOptionsForCompilationLevel(compilerOptions);
		
		Result result = compiler.compile(externs, input, compilerOptions);
		
		if (result.success) {
			writer.write(compiler.toSource());
		} else {
			writer.write(source);
		}

		return writer.toString();

	}

	/**
	 * Returns level of optimization that is applied when compiling JavaScript code.
	 * 
	 * @return <code>CompilationLevel</code> that is applied when compiling JavaScript code.
	 * 
	 * @see <a href="http://closure-compiler.googlecode.com/svn/trunk/javadoc/com/google/javascript/jscomp/CompilationLevel.html">CompilationLevel</a>
	 */
	public CompilationLevel getCompilationLevel() {
		return compilationLevel;
	}

	/**
	 * Sets level of optimization that should be applied when compiling JavaScript code. 
	 * If none is provided, <code>CompilationLevel.SIMPLE_OPTIMIZATIONS</code> will be used by default. 

	 * <p><b>Warning:</b> You should avoid <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code> as 
	 * it would most likely break inline JavaScript.
	 * 
	 * @param compilationLevel Optimization level to use, could be set to <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code>, <code>CompilationLevel.SIMPLE_OPTIMIZATIONS</code>, <code>CompilationLevel.WHITESPACE_ONLY</code> 
	 * 
	 * @see <a href="http://code.google.com/closure/compiler/docs/api-tutorial3.html">Advanced Compilation and Externs</a>
	 * @see <a href="http://closure-compiler.googlecode.com/svn/trunk/javadoc/com/google/javascript/jscomp/CompilationLevel.html">CompilationLevel</a>
	 */
	public void setCompilationLevel(CompilationLevel compilationLevel) {
		this.compilationLevel = compilationLevel;
	}

	/**
	 * Returns options that are used by the Closure compiler.
	 * 
	 * @return <code>CompilerOptions</code> that are used by the compiler
	 * 
	 * @see <a href="http://closure-compiler.googlecode.com/svn/trunk/javadoc/com/google/javascript/jscomp/CompilerOptions.html">CompilerOptions</a>
	 */
	public CompilerOptions getCompilerOptions() {
		return compilerOptions;
	}

	/**
	 * Sets options that will be used by the Closure compiler. 
	 * If none is provided, default options constructor will be used: <code>new CompilerOptions()</code>.
	 *   
	 * @param compilerOptions <code>CompilerOptions</code> that will be used by the compiler
	 * 
	 * @see <a href="http://closure-compiler.googlecode.com/svn/trunk/javadoc/com/google/javascript/jscomp/CompilerOptions.html">CompilerOptions</a>
	 */
	public void setCompilerOptions(CompilerOptions compilerOptions) {
		this.compilerOptions = compilerOptions;
	}

	/**
	 * Returns logging level used by the Closure compiler.
	 * 
	 * @return <code>Level</code> of logging used by the Closure compiler
	 */
	public Level getLoggingLevel() {
		return loggingLevel;
	}

	/**
	 * Sets logging level for the Closure compiler.
	 * 
	 * @param loggingLevel logging level for the Closure compiler.
	 * 
	 * @see java.util.logging.Level
	 */
	public void setLoggingLevel(Level loggingLevel) {
		this.loggingLevel = loggingLevel;
	}

	/**
	 * Returns <code>JSSourceFile</code> used as a reference during the compression 
	 * at <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code> level.
	 * 
	 * @return <code>JSSourceFile</code> used as a reference during compression
	 */
	public JSSourceFile getExterns() {
		return externs;
	}

	/**
	 * Sets external JavaScript files that are used as a reference for function declarations if 
	 * <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code> compression level is used.
	 * 
	 * <p><b>Warning:</b> You should avoid <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code> as 
	 * it would most likely break inline JavaScript.
	 * 
	 * @param externs <code>JSSourceFile</code> to use as a reference during compression
	 * 
	 * @see <a href="http://code.google.com/closure/compiler/docs/api-tutorial3.html">Advanced Compilation and Externs</a>
	 * @see <a href="http://closure-compiler.googlecode.com/svn/trunk/javadoc/com/google/javascript/jscomp/JSSourceFile.html">JSSourceFile</a>
	 */
	public void setExterns(JSSourceFile externs) {
		this.externs = externs;
	}

}
