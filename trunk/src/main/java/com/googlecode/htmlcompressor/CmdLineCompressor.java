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
 * <p>Usage: <code>java -jar htmlcompressor.jar [options] [input file]</code>
 * <p>To view a list of all available parameters please run with <code>--help</code> option:
 * <p><code>java -jar htmlcompressor.jar --help</code>
 * 
 * @author <a href="mailto:serg472@gmail.com">Sergiy Kovalchuk</a>
 */
public class CmdLineCompressor {

	private static final Pattern urlPattern = Pattern.compile("^https?://.*$", Pattern.CASE_INSENSITIVE);
	
	private static CmdLineParser parser = new CmdLineParser();

	private static CmdLineParser.Option helpOpt = parser.addBooleanOption('h', "help");
	private static CmdLineParser.Option analyzeOpt = parser.addBooleanOption('a', "analyze");
	private static CmdLineParser.Option charsetOpt = parser.addStringOption('c', "charset");
	private static CmdLineParser.Option outputFilenameOpt = parser.addStringOption('o', "output");
	private static CmdLineParser.Option patternsFilenameOpt = parser.addStringOption('p', "preserve");
	private static CmdLineParser.Option typeOpt = parser.addStringOption('t', "type");
	private static CmdLineParser.Option filemaskOpt = parser.addStringOption('m', "mask");
	private static CmdLineParser.Option preserveCommentsOpt = parser.addBooleanOption("preserve-comments");
	private static CmdLineParser.Option preserveIntertagSpacesOpt = parser.addBooleanOption("preserve-intertag-spaces");
	private static CmdLineParser.Option preserveMultiSpacesOpt = parser.addBooleanOption("preserve-multi-spaces");
	private static CmdLineParser.Option removeIntertagSpacesOpt = parser.addBooleanOption("remove-intertag-spaces");
	private static CmdLineParser.Option removeQuotesOpt = parser.addBooleanOption("remove-quotes");
	private static CmdLineParser.Option preserveLineBreaksOpt = parser.addBooleanOption("preserve-line-breaks");
	private static CmdLineParser.Option preservePhpTagsOpt = parser.addBooleanOption("preserve-php");
	private static CmdLineParser.Option preserveServerScriptTagsOpt = parser.addBooleanOption("preserve-server-script");
	private static CmdLineParser.Option preserveSsiTagsOpt = parser.addBooleanOption("preserve-ssi");
	private static CmdLineParser.Option compressJsOpt = parser.addBooleanOption("compress-js");
	private static CmdLineParser.Option compressCssOpt = parser.addBooleanOption("compress-css");
	private static CmdLineParser.Option jsCompressorOpt = parser.addStringOption("js-compressor");
	
	private static CmdLineParser.Option simpleDoctypeOpt = parser.addBooleanOption("simple-doctype");
	private static CmdLineParser.Option removeScriptAttributesOpt = parser.addBooleanOption("remove-script-attr");
	private static CmdLineParser.Option removeStyleAttributesOpt = parser.addBooleanOption("remove-style-attr");
	private static CmdLineParser.Option removeLinkAttributesOpt = parser.addBooleanOption("remove-link-attr");
	private static CmdLineParser.Option removeFormAttributesOpt = parser.addBooleanOption("remove-form-attr");
	private static CmdLineParser.Option removeInputAttributesOpt = parser.addBooleanOption("remove-input-attr");
	private static CmdLineParser.Option simpleBooleanAttributesOpt = parser.addBooleanOption("simple-bool-attr");
	private static CmdLineParser.Option removeJavaScriptProtocolOpt = parser.addBooleanOption("remove-js-protocol");
	private static CmdLineParser.Option removeHttpProtocolOpt = parser.addBooleanOption("remove-http-protocol");
	private static CmdLineParser.Option removeHttpsProtocolOpt = parser.addBooleanOption("remove-https-protocol");

	private static CmdLineParser.Option nomungeOpt = parser.addBooleanOption("nomunge");
	private static CmdLineParser.Option linebreakOpt = parser.addStringOption("line-break");
	private static CmdLineParser.Option preserveSemiOpt = parser.addBooleanOption("preserve-semi");
	private static CmdLineParser.Option disableOptimizationsOpt = parser.addBooleanOption("disable-optimizations");
	
	private static CmdLineParser.Option closureOptLevelOpt = parser.addStringOption("closure-opt-level");
	private static CmdLineParser.Option closureCustomExternsOnly = parser.addBooleanOption("closure-custom-externs-only");
	private static CmdLineParser.Option closureExterns = parser.addStringOption("closure-externs");
	
	public static void main(String[] args) {
		CmdLineCompressor cmdLineCompressor = new CmdLineCompressor();
		cmdLineCompressor.init(args);
	}
	
	public void init(String[] args) {
		try {
			parser.parse(args);

			// help
			Boolean help = (Boolean) parser.getOptionValue(helpOpt);
			if (help != null && help.booleanValue()) {
				printUsage();
				return;
			} else {
				//look for "/?" and "-?"
				for(int i=0;i<args.length;i++) {
					if(args[i].equals("/?") || args[i].equals("-?")) {
						printUsage();
						return;
					}
				}
			}

			// input file
			String[] fileArgs = parser.getRemainingArgs();

			// type
			String type = (String) parser.getOptionValue(typeOpt);
			if (type != null && !type.equalsIgnoreCase("html") && !type.equalsIgnoreCase("xml")) {
				throw new IllegalArgumentException("Unknown type: " + type);
			}

			if (fileArgs.length == 0) {
				// html by default for stdin
				if (type == null) {
					type = "html";
				}
			} else {
				// detect type from extension
				if (type == null) {
					if(fileArgs[0].toLowerCase().endsWith(".xml")) {
						type = "xml";
					} else {
						type = "html";
					}
				}
			}
			
			if((parser.getOptionValue(analyzeOpt) != null)) {
				//analyzer mode
				HtmlAnalyzer analyzer = new HtmlAnalyzer();
				analyzer.analyze(readResource(buildReader(fileArgs.length > 0 ? fileArgs[0] : null)));
			} else {
				//compression mode
				Compressor compressor = type.equals("xml") ? createXmlCompressor() : createHtmlCompressor();
				Map<String, String> ioMap = buildInputOutputMap();
				for (Map.Entry<String, String> entry : ioMap.entrySet()) {
					writeResource(compressor.compress(readResource(buildReader(entry.getKey()))), buildWriter(entry.getValue()));
				}
			}	

		} catch (NoClassDefFoundError e){
			boolean useClosureCompressor = HtmlCompressor.JS_COMPRESSOR_CLOSURE.equalsIgnoreCase((String) parser.getOptionValue(jsCompressorOpt));
			if(useClosureCompressor) {
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
		
		boolean useClosureCompressor = false;
		
		// charset
		String charset = getCharset();

		//line break
		int linebreakpos = -1;
		String linebreakstr = (String) parser.getOptionValue(linebreakOpt);
		if (linebreakstr != null) {
			try {
				linebreakpos = Integer.parseInt(linebreakstr, 10);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException();
			}
		}

		boolean compressJavaScript = (parser.getOptionValue(compressJsOpt) != null);
		boolean compressCss = parser.getOptionValue(compressCssOpt) != null;
		useClosureCompressor = HtmlCompressor.JS_COMPRESSOR_CLOSURE.equalsIgnoreCase((String) parser.getOptionValue(jsCompressorOpt));
		
		//custom preserve patterns
		List<Pattern> preservePatterns = new ArrayList<Pattern>();
		
		//predefined
		if(parser.getOptionValue(preservePhpTagsOpt) != null) {
			preservePatterns.add(HtmlCompressor.PHP_TAG_PATTERN);
		}
		
		if(parser.getOptionValue(preserveServerScriptTagsOpt) != null) {
			preservePatterns.add(HtmlCompressor.SERVER_SCRIPT_TAG_PATTERN);
		}

		if(parser.getOptionValue(preserveSsiTagsOpt) != null) {
			preservePatterns.add(HtmlCompressor.SERVER_SIDE_INCLUDE_PATTERN);
		}
		
		String patternsFilename = (String) parser.getOptionValue(patternsFilenameOpt);
		if(patternsFilename != null) {
			
			BufferedReader patternsIn = null;
			try {

				//read input file
				patternsIn = new BufferedReader(new InputStreamReader(new FileInputStream(patternsFilename), charset));
	
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
		
		htmlCompressor.setRemoveComments(parser.getOptionValue(preserveCommentsOpt) == null);
		htmlCompressor.setRemoveMultiSpaces(parser.getOptionValue(preserveMultiSpacesOpt) == null);
		htmlCompressor.setRemoveIntertagSpaces(parser.getOptionValue(removeIntertagSpacesOpt) != null);
		htmlCompressor.setRemoveQuotes(parser.getOptionValue(removeQuotesOpt) != null);
		htmlCompressor.setPreserveLineBreaks(parser.getOptionValue(preserveLineBreaksOpt) != null);
		htmlCompressor.setCompressJavaScript(compressJavaScript);
		htmlCompressor.setCompressCss(compressCss);

		htmlCompressor.setSimpleDoctype(parser.getOptionValue(simpleDoctypeOpt) != null);
		htmlCompressor.setRemoveScriptAttributes(parser.getOptionValue(removeScriptAttributesOpt) != null);
		htmlCompressor.setRemoveStyleAttributes(parser.getOptionValue(removeStyleAttributesOpt) != null);
		htmlCompressor.setRemoveLinkAttributes(parser.getOptionValue(removeLinkAttributesOpt) != null);
		htmlCompressor.setRemoveFormAttributes(parser.getOptionValue(removeFormAttributesOpt) != null);
		htmlCompressor.setRemoveInputAttributes(parser.getOptionValue(removeInputAttributesOpt) != null);
		htmlCompressor.setSimpleBooleanAttributes(parser.getOptionValue(simpleBooleanAttributesOpt) != null);
		htmlCompressor.setRemoveJavaScriptProtocol(parser.getOptionValue(removeJavaScriptProtocolOpt) != null);
		htmlCompressor.setRemoveHttpProtocol(parser.getOptionValue(removeHttpProtocolOpt) != null);
		htmlCompressor.setRemoveHttpsProtocol(parser.getOptionValue(removeHttpsProtocolOpt) != null);
		
		htmlCompressor.setPreservePatterns(preservePatterns);

		htmlCompressor.setYuiJsNoMunge(parser.getOptionValue(nomungeOpt) != null);
		htmlCompressor.setYuiJsPreserveAllSemiColons(parser.getOptionValue(preserveSemiOpt) != null);
		htmlCompressor.setYuiJsDisableOptimizations(parser.getOptionValue(disableOptimizationsOpt) != null);
		htmlCompressor.setYuiJsLineBreak(linebreakpos);
		htmlCompressor.setYuiCssLineBreak(linebreakpos);
		
		//switch js compressor to closure
		if(compressJavaScript && useClosureCompressor) {
			ClosureJavaScriptCompressor closureCompressor = new ClosureJavaScriptCompressor();

			String closureOptLevel = (String) parser.getOptionValue(closureOptLevelOpt);
			if(closureOptLevel != null && closureOptLevel.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_ADVANCED)) {
				closureCompressor.setCompilationLevel(CompilationLevel.ADVANCED_OPTIMIZATIONS);
				closureCompressor.setCustomExternsOnly(parser.getOptionValue(closureCustomExternsOnly) != null);
				
				//get externs
				List<String> externFiles = parser.getOptionValues(closureExterns);
				if(externFiles.size() > 0) {
					List<JSSourceFile> externs = new ArrayList<JSSourceFile>();
					for(String externFile : externFiles) {
						externs.add(JSSourceFile.fromFile(externFile));
					}
					closureCompressor.setExterns(externs);
				}
			} else if(closureOptLevel != null && closureOptLevel.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_WHITESPACE)) {
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
		xmlCompressor.setRemoveComments(parser.getOptionValue(preserveCommentsOpt) == null);
		xmlCompressor.setRemoveIntertagSpaces(parser.getOptionValue(preserveIntertagSpacesOpt) == null);
			
		return xmlCompressor;
	}
	
	private Map<String, String> buildInputOutputMap() throws IllegalArgumentException, IOException {
		Map<String, String> map = new HashMap<String, String>();
		
		String type = (String) parser.getOptionValue(typeOpt);
		String filemask = (String) parser.getOptionValue(filemaskOpt);
		
		
		String[] fileArgs = parser.getRemainingArgs();

		File outpuFile = null;
		String outputFilename = (String) parser.getOptionValue(outputFilenameOpt);
		if(outputFilename != null) {
			outpuFile = new File(outputFilename);
			
			//make dirs
			if(outputFilename.endsWith("/") || outputFilename.endsWith("\\")) {
				outpuFile.mkdirs();
			} else {
				(new File(outpuFile.getCanonicalFile().getParent())).mkdirs();
			}
		}
		
		if(fileArgs.length > 1 && (outpuFile == null || !outpuFile.isDirectory())) {
			throw new IllegalArgumentException("Output must be a directory");
		}
		
		if(fileArgs.length == 0) {
			map.put(null, outputFilename);
		} else {
			for(int i=0; i<fileArgs.length; i++) {
				if(!urlPattern.matcher(fileArgs[i]).matches()) {
					File inputFile = new File(fileArgs[i]);
					if(inputFile.isDirectory()) {
						//is dir
						if(outpuFile != null && outpuFile.isDirectory()) {
							for(File file : inputFile.listFiles(new CompressorFileFilter(type, filemask))) {
								if(!file.isDirectory()) {
									String from = file.getCanonicalPath();
									String to = from.replaceFirst(escRegEx(inputFile.getCanonicalPath()), Matcher.quoteReplacement(outpuFile.getCanonicalPath())); 
									map.put(from, to);
								}
							}
						} else {
							throw new IllegalArgumentException("Output must be a directory");
						}
					} else {
						//is file
						if(outpuFile != null && outpuFile.isDirectory()) {
							String from = inputFile.getCanonicalPath();
							String to = from.replaceFirst(escRegEx(inputFile.getCanonicalFile().getParentFile().getCanonicalPath()), Matcher.quoteReplacement(outpuFile.getCanonicalPath())); 
							map.put(fileArgs[i], to);
						} else {
							map.put(fileArgs[i], outputFilename);
						}
						
					}
				} else {
					//is url
					if(fileArgs.length == 1 && (outpuFile == null || !outpuFile.isDirectory())) {
						map.put(null, outputFilename);
					} else {
						throw new IllegalArgumentException("Input from URL cannot have directory as output");
					}
				}
			}
		}
		
		return map;
	}
	
	private String getCharset() {
		String charset = (String) parser.getOptionValue(charsetOpt);
		if (charset == null || !Charset.isSupported(charset)) {
			charset = "UTF-8";
		}
		return charset;
	}
	
	private BufferedReader buildReader(String filename) throws IOException {
		String charset = getCharset();
		
		if (filename == null) {
			return new BufferedReader(new InputStreamReader(System.in, charset));
		} else if(urlPattern.matcher(filename).matches()) {
			return new BufferedReader(new InputStreamReader((new URL(filename)).openConnection().getInputStream()));
		} else {
			return new BufferedReader(new InputStreamReader(new FileInputStream(filename), charset));
		}
	}

	private Writer buildWriter(String filename) throws IOException {
		String charset = getCharset();
		
		if (filename == null) {
			return new OutputStreamWriter(System.out, charset);
		} else {
			return  new OutputStreamWriter(new FileOutputStream(filename), charset);
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
				+ " -c, --charset <charset>       Read the input files using <charset>\n"
				+ " -a, --analyze                 Compression report (all settings below ignored)\n"
				+ " -m, --mask <filemask>         Filter input files inside directories by mask\n"
				+ " -o, --output <path>           Filename or directory for compression results.\n"
				+ "                               If none provided outputs result to <stdout>\n\n"
	
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
		
		public CompressorFileFilter(String type, String filemask) {
			if(filemask == null) {
				if(type != null && type.equals("xml")) {
					filemaskPattern = Pattern.compile("^.*\\.xml$", Pattern.CASE_INSENSITIVE);
				} else {
					filemaskPattern = Pattern.compile("^.*\\.html?$", Pattern.CASE_INSENSITIVE);
				}
			} else {
				//turn mask into regexp
				filemask = filemask.replaceAll(escRegEx("."), Matcher.quoteReplacement("\\.")); 	// "." -> "\."
				filemask = filemask.replaceAll(escRegEx("*"), Matcher.quoteReplacement(".*"));		// "*" -> ".*"
				filemask = filemask.replaceAll(escRegEx("?"), Matcher.quoteReplacement("."));		// "?" -> "."
				filemask = filemask.replaceAll(escRegEx(";"), Matcher.quoteReplacement("$|^"));		// "," -> "$|^"
				filemask = "^" + filemask + "$";
				
				filemaskPattern = Pattern.compile(filemask, Pattern.CASE_INSENSITIVE);
			}
		}
		
		@Override
		public boolean accept(File file) {
			//take only matching non-dirs
			if(!file.isDirectory()) {
				return filemaskPattern.matcher(file.getName()).matches();
			} 
			return false;
		}
		
	}

}