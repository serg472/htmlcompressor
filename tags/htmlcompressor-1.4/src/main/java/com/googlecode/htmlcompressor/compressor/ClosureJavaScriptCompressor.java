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
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import com.google.common.collect.Lists;
import com.google.common.io.LimitInputStream;
import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.Compiler;
import com.google.javascript.jscomp.CompilerOptions;
import com.google.javascript.jscomp.JSSourceFile;
import com.google.javascript.jscomp.Result;
import com.google.javascript.jscomp.WarningLevel;

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
	private WarningLevel warningLevel = WarningLevel.DEFAULT;
	private boolean customExternsOnly = false;
	private List<JSSourceFile> externs = null;

	public ClosureJavaScriptCompressor() {
	}

	public ClosureJavaScriptCompressor(CompilationLevel compilationLevel) {
		this.compilationLevel = compilationLevel;
	}

	@Override
	public String compress(String source) {
		
		StringWriter writer = new StringWriter();
		
		//prepare source
		List<JSSourceFile> input = new ArrayList<JSSourceFile>();
		input.add(JSSourceFile.fromCode("source.js", source));
		
		//prepare externs
		List<JSSourceFile> externsList = new ArrayList<JSSourceFile>();
		if(compilationLevel.equals(CompilationLevel.ADVANCED_OPTIMIZATIONS)) {
			//default externs
			if(!customExternsOnly) {
				try {
					externsList = getDefaultExterns();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//add user defined externs
			if(externs != null) {
				for(JSSourceFile extern : externs) {
					externsList.add(extern);
				}
			}
			//add empty externs
			if(externsList.size() == 0) {
				externsList.add(JSSourceFile.fromCode("externs.js", ""));
			}
		} else {
			//empty externs
			externsList.add(JSSourceFile.fromCode("externs.js", ""));
		}
		
		Compiler.setLoggingLevel(loggingLevel);
		
		Compiler compiler = new Compiler();
		compiler.disableThreads();
		
		compilationLevel.setOptionsForCompilationLevel(compilerOptions);
		warningLevel.setOptionsForWarningLevel(compilerOptions);
		
		Result result = compiler.compile(externsList, input, compilerOptions);
		
		if (result.success) {
			writer.write(compiler.toSource());
		} else {
			writer.write(source);
		}

		return writer.toString();

	}
	
	//read default externs from closure.jar
	private List<JSSourceFile> getDefaultExterns() throws IOException {
		InputStream input = ClosureJavaScriptCompressor.class.getResourceAsStream("/externs.zip");
		ZipInputStream zip = new ZipInputStream(input);
		List<JSSourceFile> externs = Lists.newLinkedList();
		for (ZipEntry entry = null; (entry = zip.getNextEntry()) != null;) {
			LimitInputStream entryStream = new LimitInputStream(zip, entry.getSize());
			externs.add(JSSourceFile.fromInputStream(entry.getName(), entryStream));
		}
		return externs;
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
	 * 
	 * <p><b>Warning:</b> Using <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code> could 
	 * break inline JavaScript if externs are not set properly.
	 * 
	 * @param compilationLevel Optimization level to use, could be set to <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code>, <code>CompilationLevel.SIMPLE_OPTIMIZATIONS</code>, <code>CompilationLevel.WHITESPACE_ONLY</code> 
	 * 
	 * @see <a href="http://code.google.com/closure/compiler/docs/api-tutorial3.html">Advanced Compilation and Externs</a>
	 * @see <a href="http://code.google.com/closure/compiler/docs/compilation_levels.html">Closure Compiler Compilation Levels</a>
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
	public List<JSSourceFile> getExterns() {
		return externs;
	}

	/**
	 * Sets external JavaScript files that are used as a reference for function declarations if 
	 * <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code> compression level is used. 
	 * 
	 * <p>A number of default externs defined inside Closure's jar will be used besides user defined ones, 
	 * to use only user defined externs set {@link #setCustomExternsOnly(boolean) setCustomExternsOnly(true)}
	 * 
	 * <p><b>Warning:</b> Using <code>CompilationLevel.ADVANCED_OPTIMIZATIONS</code> could 
	 * break inline JavaScript if externs are not set properly.
	 * 
	 * @param externs <code>JSSourceFile</code> to use as a reference during compression
	 * 
	 * @see #setCompilationLevel(CompilationLevel)
	 * @see #setCustomExternsOnly(boolean)
	 * @see <a href="http://code.google.com/closure/compiler/docs/api-tutorial3.html">Advanced Compilation and Externs</a>
	 * @see <a href="http://closure-compiler.googlecode.com/svn/trunk/javadoc/com/google/javascript/jscomp/JSSourceFile.html">JSSourceFile</a>
	 */
	public void setExterns(List<JSSourceFile> externs) {
		this.externs = externs;
	}

	/**
	 * Returns <code>WarningLevel</code> used by the Closure compiler
	 *  
	 * @return <code>WarningLevel</code> used by the Closure compiler
	 */
	public WarningLevel getWarningLevel() {
		return warningLevel;
	}

	/**
	 * Indicates the amount of information you want from the compiler about possible problems in your code. 
	 * 
	 * @param warningLevel <code>WarningLevel</code> to use
	 * 
	 * @see <a href="http://code.google.com/closure/compiler/docs/api-ref.html">
	 */
	public void setWarningLevel(WarningLevel warningLevel) {
		this.warningLevel = warningLevel;
	}

	/**
	 * Returns <code>true</code> if default externs defined inside Closure's jar are ignored 
	 * and only user defined ones are used.
	 * 
	 * @return <code>true</code> if default externs defined inside Closure's jar are ignored 
	 * and only user defined ones are used
	 */
	public boolean isCustomExternsOnly() {
		return customExternsOnly;
	}

	/**
	 * If set to <code>true</code>, default externs defined inside Closure's jar will be ignored 
	 * and only user defined ones will be used.
	 *  
	 * @param customExternsOnly <code>true</code> to skip default externs and use only user defined ones
	 * 
	 * @see #setExterns(List)
	 * @see #setCompilationLevel(CompilationLevel)
	 */
	public void setCustomExternsOnly(boolean customExternsOnly) {
		this.customExternsOnly = customExternsOnly;
	}

}
