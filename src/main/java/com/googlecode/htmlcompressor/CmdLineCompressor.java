package com.googlecode.htmlcompressor;

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

import jargs.gnu.CmdLineParser;
import jargs.gnu.CmdLineParser.Option;
import jargs.gnu.CmdLineParser.OptionException;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.javascript.jscomp.CompilationLevel;
import com.google.javascript.jscomp.JSSourceFile;
import com.googlecode.htmlcompressor.analyzer.HtmlAnalyzer;
import com.googlecode.htmlcompressor.compressor.ClosureJavaScriptCompressor;
import com.googlecode.htmlcompressor.compressor.Compressor;
import com.googlecode.htmlcompressor.compressor.HtmlCompressor;
import com.googlecode.htmlcompressor.compressor.XmlCompressor;

/**
 * Wrapper for HTML and XML compressor classes that allows using them from a command line.
 * 
 * <p>Usage: <code>java -jar htmlcompressor.jar [options] [input]</code>
 * <p>To view a list of all available parameters please run with <code>-?</code> option:
 * <p><code>java -jar htmlcompressor.jar -?</code>
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 */
public class CmdLineCompressor {

	private static final Pattern urlPattern = Pattern.compile("^https?://.*$", Pattern.CASE_INSENSITIVE);
	
	private boolean helpOpt;
	private boolean analyzeOpt;
	private String charsetOpt;
	private String outputFilenameOpt;
	private String patternsFilenameOpt;
	private String typeOpt;
	private String filemaskOpt;
	private boolean recursiveOpt;
	private boolean preserveCommentsOpt;
	private boolean preserveIntertagSpacesOpt;
	private boolean preserveMultiSpacesOpt;
	private boolean removeIntertagSpacesOpt;
	private boolean removeQuotesOpt;
	private String removeSurroundingSpacesOpt;
	private boolean preserveLineBreaksOpt;
	private boolean preservePhpTagsOpt;
	private boolean preserveServerScriptTagsOpt;
	private boolean preserveSsiTagsOpt;
	private boolean compressJsOpt;
	private boolean compressCssOpt;
	private String jsCompressorOpt;

	private boolean simpleDoctypeOpt;
	private boolean removeScriptAttributesOpt;
	private boolean removeStyleAttributesOpt;
	private boolean removeLinkAttributesOpt;
	private boolean removeFormAttributesOpt;
	private boolean removeInputAttributesOpt;
	private boolean simpleBooleanAttributesOpt;
	private boolean removeJavaScriptProtocolOpt;
	private boolean removeHttpProtocolOpt;
	private boolean removeHttpsProtocolOpt;

	private boolean nomungeOpt;
	private int linebreakOpt;
	private boolean preserveSemiOpt;
	private boolean disableOptimizationsOpt;
	
	private String closureOptLevelOpt;
	private boolean closureCustomExternsOnlyOpt;
	private List<String> closureExternsOpt;
	
	private String[] fileArgsOpt;
	
	public static void main(String[] args) {
		CmdLineCompressor cmdLineCompressor = new CmdLineCompressor(args);
		cmdLineCompressor.process(args);
	}
	
	public CmdLineCompressor(String[] args) {
		CmdLineParser parser = new CmdLineParser();

		Option helpOpt = parser.addBooleanOption('h', "help");
		Option helpOptAlt = parser.addBooleanOption('?', "help_alt");
		Option analyzeOpt = parser.addBooleanOption('a', "analyze");
		Option recursiveOpt = parser.addBooleanOption('r', "recursive");
		Option charsetOpt = parser.addStringOption('c', "charset");
		Option outputFilenameOpt = parser.addStringOption('o', "output");
		Option patternsFilenameOpt = parser.addStringOption('p', "preserve");
		Option typeOpt = parser.addStringOption('t', "type");
		Option filemaskOpt = parser.addStringOption('m', "mask");
		Option preserveCommentsOpt = parser.addBooleanOption("preserve-comments");
		Option preserveIntertagSpacesOpt = parser.addBooleanOption("preserve-intertag-spaces");
		Option preserveMultiSpacesOpt = parser.addBooleanOption("preserve-multi-spaces");
		Option removeIntertagSpacesOpt = parser.addBooleanOption("remove-intertag-spaces");
		Option removeSurroundingSpacesOpt = parser.addStringOption("remove-surrounding-spaces");
		Option removeQuotesOpt = parser.addBooleanOption("remove-quotes");
		Option preserveLineBreaksOpt = parser.addBooleanOption("preserve-line-breaks");
		Option preservePhpTagsOpt = parser.addBooleanOption("preserve-php");
		Option preserveServerScriptTagsOpt = parser.addBooleanOption("preserve-server-script");
		Option preserveSsiTagsOpt = parser.addBooleanOption("preserve-ssi");
		Option compressJsOpt = parser.addBooleanOption("compress-js");
		Option compressCssOpt = parser.addBooleanOption("compress-css");
		Option jsCompressorOpt = parser.addStringOption("js-compressor");
		
		Option simpleDoctypeOpt = parser.addBooleanOption("simple-doctype");
		Option removeScriptAttributesOpt = parser.addBooleanOption("remove-script-attr");
		Option removeStyleAttributesOpt = parser.addBooleanOption("remove-style-attr");
		Option removeLinkAttributesOpt = parser.addBooleanOption("remove-link-attr");
		Option removeFormAttributesOpt = parser.addBooleanOption("remove-form-attr");
		Option removeInputAttributesOpt = parser.addBooleanOption("remove-input-attr");
		Option simpleBooleanAttributesOpt = parser.addBooleanOption("simple-bool-attr");
		Option removeJavaScriptProtocolOpt = parser.addBooleanOption("remove-js-protocol");
		Option removeHttpProtocolOpt = parser.addBooleanOption("remove-http-protocol");
		Option removeHttpsProtocolOpt = parser.addBooleanOption("remove-https-protocol");

		Option nomungeOpt = parser.addBooleanOption("nomunge");
		Option linebreakOpt = parser.addStringOption("line-break");
		Option preserveSemiOpt = parser.addBooleanOption("preserve-semi");
		Option disableOptimizationsOpt = parser.addBooleanOption("disable-optimizations");
		
		Option closureOptLevelOpt = parser.addStringOption("closure-opt-level");
		Option closureCustomExternsOnlyOpt = parser.addBooleanOption("closure-custom-externs-only");
		Option closureExternsOpt = parser.addStringOption("closure-externs");
		
		try {
			parser.parse(args);
			
			this.helpOpt = (Boolean)parser.getOptionValue(helpOpt, false) || (Boolean)parser.getOptionValue(helpOptAlt, false);
			this.analyzeOpt = (Boolean)parser.getOptionValue(analyzeOpt, false);
			this.recursiveOpt = (Boolean)parser.getOptionValue(recursiveOpt, false);
			this.charsetOpt = (String)parser.getOptionValue(charsetOpt, "UTF-8");
			this.outputFilenameOpt = (String)parser.getOptionValue(outputFilenameOpt);
			this.patternsFilenameOpt = (String)parser.getOptionValue(patternsFilenameOpt);
			this.typeOpt = (String)parser.getOptionValue(typeOpt);
			this.filemaskOpt = (String)parser.getOptionValue(filemaskOpt);
			this.preserveCommentsOpt = (Boolean)parser.getOptionValue(preserveCommentsOpt, false);
			this.preserveIntertagSpacesOpt = (Boolean)parser.getOptionValue(preserveIntertagSpacesOpt, false);
			this.preserveMultiSpacesOpt = (Boolean)parser.getOptionValue(preserveMultiSpacesOpt, false);
			this.removeIntertagSpacesOpt = (Boolean)parser.getOptionValue(removeIntertagSpacesOpt, false);
			this.removeQuotesOpt = (Boolean)parser.getOptionValue(removeQuotesOpt, false);
			this.preserveLineBreaksOpt = (Boolean)parser.getOptionValue(preserveLineBreaksOpt, false);
			this.preservePhpTagsOpt = (Boolean)parser.getOptionValue(preservePhpTagsOpt, false);
			this.preserveServerScriptTagsOpt = (Boolean)parser.getOptionValue(preserveServerScriptTagsOpt, false);
			this.preserveSsiTagsOpt = (Boolean)parser.getOptionValue(preserveSsiTagsOpt, false);
			this.compressJsOpt = (Boolean)parser.getOptionValue(compressJsOpt, false);
			this.compressCssOpt = (Boolean)parser.getOptionValue(compressCssOpt, false);
			this.jsCompressorOpt = (String)parser.getOptionValue(jsCompressorOpt, HtmlCompressor.JS_COMPRESSOR_YUI);
			
			this.simpleDoctypeOpt = (Boolean)parser.getOptionValue(simpleDoctypeOpt, false);
			this.removeScriptAttributesOpt = (Boolean)parser.getOptionValue(removeScriptAttributesOpt, false);
			this.removeStyleAttributesOpt = (Boolean)parser.getOptionValue(removeStyleAttributesOpt, false);
			this.removeLinkAttributesOpt = (Boolean)parser.getOptionValue(removeLinkAttributesOpt, false);
			this.removeFormAttributesOpt = (Boolean)parser.getOptionValue(removeFormAttributesOpt, false);
			this.removeInputAttributesOpt = (Boolean)parser.getOptionValue(removeInputAttributesOpt, false);
			this.simpleBooleanAttributesOpt = (Boolean)parser.getOptionValue(simpleBooleanAttributesOpt, false);
			this.removeJavaScriptProtocolOpt = (Boolean)parser.getOptionValue(removeJavaScriptProtocolOpt, false);
			this.removeHttpProtocolOpt = (Boolean)parser.getOptionValue(removeHttpProtocolOpt, false);
			this.removeHttpsProtocolOpt = (Boolean)parser.getOptionValue(removeHttpsProtocolOpt, false);

			this.nomungeOpt = (Boolean)parser.getOptionValue(nomungeOpt, false);
			this.linebreakOpt = (Integer)parser.getOptionValue(linebreakOpt, -1);
			this.preserveSemiOpt = (Boolean)parser.getOptionValue(preserveSemiOpt, false);
			this.disableOptimizationsOpt = (Boolean)parser.getOptionValue(disableOptimizationsOpt, false);

			this.closureOptLevelOpt = (String)parser.getOptionValue(closureOptLevelOpt, ClosureJavaScriptCompressor.COMPILATION_LEVEL_SIMPLE);
			this.closureCustomExternsOnlyOpt = (Boolean)parser.getOptionValue(closureCustomExternsOnlyOpt, false);
			
			this.closureExternsOpt = parser.getOptionValues(closureExternsOpt);
			
			this.removeSurroundingSpacesOpt = (String)parser.getOptionValue(removeSurroundingSpacesOpt);
			if(this.removeSurroundingSpacesOpt != null) {
				if(this.removeSurroundingSpacesOpt.equalsIgnoreCase("min")) {
					this.removeSurroundingSpacesOpt = HtmlCompressor.BLOCK_TAGS_MIN;
				} else if(this.removeSurroundingSpacesOpt.equalsIgnoreCase("max")) {
					this.removeSurroundingSpacesOpt = HtmlCompressor.BLOCK_TAGS_MAX;
				} else if(this.removeSurroundingSpacesOpt.equalsIgnoreCase("all")) {
					this.removeSurroundingSpacesOpt = HtmlCompressor.ALL_TAGS;
				}
			}
			
			//input file
			this.fileArgsOpt = parser.getRemainingArgs();
			
			//charset
			this.charsetOpt = Charset.isSupported(this.charsetOpt) ? this.charsetOpt : "UTF-8";
			
			//look for "/?"
			for(int i=0;i<args.length;i++) {
				if(args[i].equals("/?")) {
					this.helpOpt = true;
					break;
				}
			}
			
		} catch (OptionException e) {
			System.out.println("ERROR: " + e.getMessage());
			printUsage();
		}
		
	}
	
	public void process(String[] args) {
		try {

			// help
			if (helpOpt) {
				printUsage();
				return;
			}
			
			// type
			String type = typeOpt;
			if (type != null && !type.equalsIgnoreCase("html") && !type.equalsIgnoreCase("xml")) {
				throw new IllegalArgumentException("Unknown type: " + type);
			}

			if (fileArgsOpt.length == 0) {
				// html by default for stdin
				if (type == null) {
					type = "html";
				}
			} else {
				// detect type from extension
				if (type == null) {
					if(fileArgsOpt[0].toLowerCase().endsWith(".xml")) {
						type = "xml";
					} else {
						type = "html";
					}
				}
			}
			
			if(analyzeOpt) {
				//analyzer mode
				HtmlAnalyzer analyzer = new HtmlAnalyzer(HtmlCompressor.JS_COMPRESSOR_CLOSURE.equalsIgnoreCase(jsCompressorOpt) ? HtmlCompressor.JS_COMPRESSOR_CLOSURE : HtmlCompressor.JS_COMPRESSOR_YUI);
				analyzer.analyze(readResource(buildReader(fileArgsOpt.length > 0 ? fileArgsOpt[0] : null)));
			} else {
				//compression mode
				Compressor compressor = type.equals("xml") ? createXmlCompressor() : createHtmlCompressor();
				Map<String, String> ioMap = buildInputOutputMap();
				for (Map.Entry<String, String> entry : ioMap.entrySet()) {
					writeResource(compressor.compress(readResource(buildReader(entry.getKey()))), buildWriter(entry.getValue()));
				}
			}	

		} catch (NoClassDefFoundError e){
			if(HtmlCompressor.JS_COMPRESSOR_CLOSURE.equalsIgnoreCase(jsCompressorOpt)) {
				System.out.println("ERROR: For JavaScript compression using Google Closure Compiler\n" +
						"additional jar file called compiler.jar must be present\n" +
						"in the same directory as HtmlCompressor jar");
			} else {
				System.out.println("ERROR: For CSS or JavaScript compression using YUICompressor additional jar file \n" +
					"called yuicompressor.jar must be present\n" +
					"in the same directory as HtmlCompressor jar");
			}
		} catch (OptionException e) {
			System.out.println("ERROR: " + e.getMessage());
			printUsage();
		} catch (IOException e) {
			System.out.println("ERROR: " + e.getMessage());
		} catch (IllegalArgumentException e) {
			System.out.println("ERROR: " + e.getMessage());
		} 

	}
	
	private Compressor createHtmlCompressor() throws IllegalArgumentException, OptionException {
		
		boolean useClosureCompressor = HtmlCompressor.JS_COMPRESSOR_CLOSURE.equalsIgnoreCase(jsCompressorOpt);
		
		//custom preserve patterns
		List<Pattern> preservePatterns = new ArrayList<Pattern>();
		
		//predefined
		if(preservePhpTagsOpt) {
			preservePatterns.add(HtmlCompressor.PHP_TAG_PATTERN);
		}
		
		if(preserveServerScriptTagsOpt) {
			preservePatterns.add(HtmlCompressor.SERVER_SCRIPT_TAG_PATTERN);
		}

		if(preserveSsiTagsOpt) {
			preservePatterns.add(HtmlCompressor.SERVER_SIDE_INCLUDE_PATTERN);
		}
		
		if(patternsFilenameOpt != null) {
			
			BufferedReader patternsIn = null;
			try {

				//read input file
				patternsIn = new BufferedReader(new InputStreamReader(new FileInputStream(patternsFilenameOpt), charsetOpt));
	
				String line = null;
				while ((line = patternsIn.readLine()) != null){
					if(line.length() > 0) {
						try {
							preservePatterns.add(Pattern.compile(line));
						} catch (PatternSyntaxException  e) {
							throw new IllegalArgumentException("Regular expression compilation error: " + e.getMessage());
						}
					}
				}
			} catch (IOException e) {
				throw new IllegalArgumentException("Unable to read custom pattern definitions file: " + e.getMessage());
			} finally {
				closeStream(patternsIn);
			}
		}
		
		//set compressor options
		HtmlCompressor htmlCompressor = new HtmlCompressor();
		
		htmlCompressor.setRemoveComments(!preserveCommentsOpt);
		htmlCompressor.setRemoveMultiSpaces(!preserveMultiSpacesOpt);
		htmlCompressor.setRemoveIntertagSpaces(removeIntertagSpacesOpt);
		htmlCompressor.setRemoveQuotes(removeQuotesOpt);
		htmlCompressor.setPreserveLineBreaks(preserveLineBreaksOpt);
		htmlCompressor.setCompressJavaScript(compressJsOpt);
		htmlCompressor.setCompressCss(compressCssOpt);

		htmlCompressor.setSimpleDoctype(simpleDoctypeOpt);
		htmlCompressor.setRemoveScriptAttributes(removeScriptAttributesOpt);
		htmlCompressor.setRemoveStyleAttributes(removeStyleAttributesOpt);
		htmlCompressor.setRemoveLinkAttributes(removeLinkAttributesOpt);
		htmlCompressor.setRemoveFormAttributes(removeFormAttributesOpt);
		htmlCompressor.setRemoveInputAttributes(removeInputAttributesOpt);
		htmlCompressor.setSimpleBooleanAttributes(simpleBooleanAttributesOpt);
		htmlCompressor.setRemoveJavaScriptProtocol(removeJavaScriptProtocolOpt);
		htmlCompressor.setRemoveHttpProtocol(removeHttpProtocolOpt);
		htmlCompressor.setRemoveHttpsProtocol(removeHttpsProtocolOpt);
		htmlCompressor.setRemoveSurroundingSpaces(removeSurroundingSpacesOpt);
		
		htmlCompressor.setPreservePatterns(preservePatterns);

		htmlCompressor.setYuiJsNoMunge(nomungeOpt);
		htmlCompressor.setYuiJsPreserveAllSemiColons(preserveSemiOpt);
		htmlCompressor.setYuiJsDisableOptimizations(disableOptimizationsOpt);
		htmlCompressor.setYuiJsLineBreak(linebreakOpt);
		htmlCompressor.setYuiCssLineBreak(linebreakOpt);
		
		//switch js compressor to closure
		if(compressJsOpt && useClosureCompressor) {
			ClosureJavaScriptCompressor closureCompressor = new ClosureJavaScriptCompressor();

			if(closureOptLevelOpt.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_ADVANCED)) {
				closureCompressor.setCompilationLevel(CompilationLevel.ADVANCED_OPTIMIZATIONS);
				closureCompressor.setCustomExternsOnly(closureCustomExternsOnlyOpt);
				
				//get externs
				if(closureExternsOpt.size() > 0) {
					List<JSSourceFile> externs = new ArrayList<JSSourceFile>();
					for(String externFile : closureExternsOpt) {
						externs.add(JSSourceFile.fromFile(externFile));
					}
					closureCompressor.setExterns(externs);
				}
			} else if(closureOptLevelOpt.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_WHITESPACE)) {
				closureCompressor.setCompilationLevel(CompilationLevel.WHITESPACE_ONLY);
			} else {
				closureCompressor.setCompilationLevel(CompilationLevel.SIMPLE_OPTIMIZATIONS);
			}
			
			htmlCompressor.setJavaScriptCompressor(closureCompressor);
		}

		return htmlCompressor;
	}
	
	private Compressor createXmlCompressor() throws IllegalArgumentException, OptionException {
		XmlCompressor xmlCompressor = new XmlCompressor();
		xmlCompressor.setRemoveComments(!preserveCommentsOpt);
		xmlCompressor.setRemoveIntertagSpaces(!preserveIntertagSpacesOpt);
			
		return xmlCompressor;
	}
	
	private Map<String, String> buildInputOutputMap() throws IllegalArgumentException, IOException {
		Map<String, String> map = new HashMap<String, String>();
		
		File outpuFile = null;
		if(outputFilenameOpt != null) {
			outpuFile = new File(outputFilenameOpt);
			
			//make dirs
			if(outputFilenameOpt.endsWith("/") || outputFilenameOpt.endsWith("\\")) {
				outpuFile.mkdirs();
			} else {
				(new File(outpuFile.getCanonicalFile().getParent())).mkdirs();
			}
		}
		
		if(fileArgsOpt.length > 1 && (outpuFile == null || !outpuFile.isDirectory())) {
			throw new IllegalArgumentException("Output must be a directory and end with a slash (/)");
		}
		
		if(fileArgsOpt.length == 0) {
			map.put(null, outputFilenameOpt);
		} else {
			for(int i=0; i<fileArgsOpt.length; i++) {
				if(!urlPattern.matcher(fileArgsOpt[i]).matches()) {
					File inputFile = new File(fileArgsOpt[i]);
					if(inputFile.isDirectory()) {
						//is dir
						if(outpuFile != null && outpuFile.isDirectory()) {
							if(!recursiveOpt) {
								//non-recursive
								for(File file : inputFile.listFiles(new CompressorFileFilter(typeOpt, filemaskOpt, false))) {
									if(!file.isDirectory()) {
										String from = file.getCanonicalPath();
										String to = from.replaceFirst(escRegEx(inputFile.getCanonicalPath()), Matcher.quoteReplacement(outpuFile.getCanonicalPath())); 
										map.put(from, to);
									}
								}
							} else {
								//recursive
								Stack<File> fileStack = new Stack<File>();
								fileStack.push(inputFile);
								while(!fileStack.isEmpty()) {
									File child = fileStack.pop();
									if (child.isDirectory()) {
										for(File f : child.listFiles(new CompressorFileFilter(typeOpt, filemaskOpt, true))) {
											fileStack.push(f);
										}
									} else if (child.isFile()) {
										String from = child.getCanonicalPath();
										String to = from.replaceFirst(escRegEx(inputFile.getCanonicalPath()), Matcher.quoteReplacement(outpuFile.getCanonicalPath())); 
										map.put(from, to);
										//make dirs
										(new File((new File(to)).getCanonicalFile().getParent())).mkdirs();
									}
								}								
							}
						} else {
							throw new IllegalArgumentException("Output must be a directory and end with a slash (/)");
						}
					} else {
						//is file
						if(outpuFile != null && outpuFile.isDirectory()) {
							String from = inputFile.getCanonicalPath();
							String to = from.replaceFirst(escRegEx(inputFile.getCanonicalFile().getParentFile().getCanonicalPath()), Matcher.quoteReplacement(outpuFile.getCanonicalPath())); 
							map.put(fileArgsOpt[i], to);
						} else {
							map.put(fileArgsOpt[i], outputFilenameOpt);
						}
						
					}
				} else {
					//is url
					if(fileArgsOpt.length == 1 && (outpuFile == null || !outpuFile.isDirectory())) {
						map.put(fileArgsOpt[i], outputFilenameOpt);
					} else {
						throw new IllegalArgumentException("Input URL should be single and cannot have directory as output");
					}
				}
			}
		}
		
		return map;
	}
	
	private BufferedReader buildReader(String filename) throws IOException {
		
		if (filename == null) {
			return new BufferedReader(new InputStreamReader(System.in, charsetOpt));
		} else if(urlPattern.matcher(filename).matches()) {
			return new BufferedReader(new InputStreamReader((new URL(filename)).openConnection().getInputStream()));
		} else {
			return new BufferedReader(new InputStreamReader(new FileInputStream(filename), charsetOpt));
		}
	}

	private Writer buildWriter(String filename) throws IOException {
		if (filename == null) {
			return new OutputStreamWriter(System.out, charsetOpt);
		} else {
			return  new OutputStreamWriter(new FileOutputStream(filename), charsetOpt);
		}
	}
	
	private String readResource(BufferedReader input) throws IOException {
		
		StringBuilder source = new StringBuilder();
		try {
			String line = null;
			while ((line = input.readLine()) != null){
				source.append(line);
				source.append(System.getProperty("line.separator"));
			}

		} finally {
			closeStream(input);
		}
		
		return source.toString();
	}

	private void writeResource(String content, Writer output) throws IOException {
		try {
			output.write(content);
		} finally {
			closeStream(output);
		}
	}
	
	private void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException ignore) {}
		}
	}
	
	private String escRegEx(String inStr) {
		return inStr.replaceAll("([\\\\*+\\[\\](){}\\$.?\\^|])", "\\\\$1");
	}

	private void printUsage() {
		System.out.println("Usage: java -jar htmlcompressor.jar [options] [input]\n\n"

				+ "[input]                        URL, filename, directory, or space separated list\n"
				+ "                               of files and directories to compress.\n"
				+ "                               If none provided reads from <stdin>\n\n"
	
				+ "Global Options:\n"
				+ " -?, /?, -h, --help            Displays this help screen\n"
				+ " -t, --type <html|xml>         If not provided autodetects from file extension\n"
				+ " -r, --recursive               Process files inside subdirectories\n"
				+ " -c, --charset <charset>       Charset for reading files, UTF-8 by default\n"
				+ " -m, --mask <filemask>         Filter input files inside directories by mask\n"
				+ " -o, --output <path>           Filename or directory for compression results.\n"
				+ "                               If none provided outputs result to <stdout>\n"
				+ " -a, --analyze                 Tries different settings and displays report.\n"
				+ "                               All settings except --js-compressor are ignored\n\n"
	
				+ "XML Compression Options:\n"
				+ " --preserve-comments           Preserve comments\n"
				+ " --preserve-intertag-spaces    Preserve intertag spaces\n\n"
	
				+ "HTML Compression Options:\n"
				+ " --preserve-comments           Preserve comments\n"
				+ " --preserve-multi-spaces       Preserve multiple spaces\n"
				+ " --preserve-line-breaks        Preserve line breaks\n"
				+ " --remove-intertag-spaces      Remove intertag spaces\n"
				+ " --remove-quotes               Remove unneeded quotes\n"
				+ " --simple-doctype              Change doctype to <!DOCTYPE html>\n"
				+ " --remove-style-attr           Remove TYPE attribute from STYLE tags\n"
				+ " --remove-link-attr            Remove TYPE attribute from LINK tags\n"
				+ " --remove-script-attr          Remove TYPE and LANGUAGE from SCRIPT tags\n"
				+ " --remove-form-attr            Remove METHOD=\"GET\" from FORM tags\n"
				+ " --remove-input-attr           Remove TYPE=\"TEXT\" from INPUT tags\n"
				+ " --simple-bool-attr            Remove values from boolean tag attributes\n"
				+ " --remove-js-protocol          Remove \"javascript:\" from inline event handlers\n"
				+ " --remove-http-protocol        Remove \"http:\" from tag attributes\n"
				+ " --remove-https-protocol       Remove \"https:\" from tag attributes\n"
				+ " --remove-surrounding-spaces <min|max|all|custom_list>\n" 
				+ "                               Predefined or custom comma separated list of tags\n"
				+ " --compress-js                 Enable inline JavaScript compression\n"
				+ " --compress-css                Enable inline CSS compression using YUICompressor\n"
				+ " --js-compressor <yui|closure> Switch inline JavaScript compressor between\n"
				+ "                               YUICompressor (default) and Closure Compiler\n\n"
				
				+ "JavaScript Compression Options for YUI Compressor:\n"
				+ " --nomunge                     Minify only, do not obfuscate\n"
				+ " --preserve-semi               Preserve all semicolons\n"
				+ " --disable-optimizations       Disable all micro optimizations\n"
				+ " --line-break <column num>     Insert a line break after the specified column\n\n"
	
				+ "JavaScript Compression Options for Google Closure Compiler:\n"
				+ " --closure-opt-level <simple|advanced|whitespace>\n"
				+ "                               Sets level of optimization (simple by default)\n"
				+ " --closure-externs <file>      Sets custom externs file, repeat for each file\n"
				+ " --closure-custom-externs-only Disable default built-in externs\n\n"
				
				+ "CSS Compression Options for YUI Compressor:\n"
				+ " --line-break <column num>     Insert a line break after the specified column\n\n"
	
				+ "Custom Block Preservation Options:\n"
				+ " --preserve-php                Preserve <?php ... ?> tags\n"
				+ " --preserve-server-script      Preserve <% ... %> tags\n"
				+ " --preserve-ssi                Preserve <!--# ... --> tags\n"
				+ " -p, --preserve <path>         Read regular expressions that define\n"
				+ "                               custom preservation rules from a file\n\n"
				
				+ "Please note that if you enable CSS or JavaScript compression, additional\n"
				+ "YUI Compressor or Google Closure Compiler jar files must be present\n"
				+ "in the same directory as this jar."

		);
	}
	
	private class CompressorFileFilter implements FileFilter {
		
		private Pattern filemaskPattern;
		private boolean withDirs;
		
		public CompressorFileFilter(String type, String filemask, boolean withDirs) {
			
			this.withDirs = withDirs;
			
			if(filemask == null) {
				if(type != null && type.equals("xml")) {
					filemaskPattern = Pattern.compile("^.*\\.xml$", Pattern.CASE_INSENSITIVE);
				} else {
					filemaskPattern = Pattern.compile("^.*\\.html?$", Pattern.CASE_INSENSITIVE);
				}
			} else {
				//turn mask into regexp
				filemask = filemask.replaceAll(escRegEx("."), Matcher.quoteReplacement("\\."));
				filemask = filemask.replaceAll(escRegEx("*"), Matcher.quoteReplacement(".*"));
				filemask = filemask.replaceAll(escRegEx("?"), Matcher.quoteReplacement("."));
				filemask = filemask.replaceAll(escRegEx(";"), Matcher.quoteReplacement("$|^"));
				filemask = "^" + filemask + "$";
				
				filemaskPattern = Pattern.compile(filemask, Pattern.CASE_INSENSITIVE);
			}
		}
		
		@Override
		public boolean accept(File file) {
			if(!withDirs) {
				//take only matching non-dirs
				if(!file.isDirectory()) {
					return filemaskPattern.matcher(file.getName()).matches();
				}
			} else {
				//take matching files and dirs
				return file.isDirectory() || filemaskPattern.matcher(file.getName()).matches();
			}
			return false;
		}
		
	}

}