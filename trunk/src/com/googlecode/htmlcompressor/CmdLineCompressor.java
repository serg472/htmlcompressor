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

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import com.google.javascript.jscomp.CompilationLevel;
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

	public static void main(String[] args) {

		CmdLineParser parser = new CmdLineParser();

		CmdLineParser.Option helpOpt = parser.addBooleanOption('h', "help");
		CmdLineParser.Option analyzeOpt = parser.addBooleanOption('a', "analyze");
		CmdLineParser.Option charsetOpt = parser.addStringOption("charset");
		CmdLineParser.Option outputFilenameOpt = parser.addStringOption('o', "output");
		CmdLineParser.Option patternsFilenameOpt = parser.addStringOption('p', "preserve-patterns");
		CmdLineParser.Option typeOpt = parser.addStringOption("type");
		CmdLineParser.Option preserveCommentsOpt = parser.addBooleanOption("preserve-comments");
		CmdLineParser.Option preserveIntertagSpacesOpt = parser.addBooleanOption("preserve-intertag-spaces");
		CmdLineParser.Option preserveMultiSpacesOpt = parser.addBooleanOption("preserve-multi-spaces");
		CmdLineParser.Option removeIntertagSpacesOpt = parser.addBooleanOption("remove-intertag-spaces");
		CmdLineParser.Option removeQuotesOpt = parser.addBooleanOption("remove-quotes");
		CmdLineParser.Option preservePhpTagsOpt = parser.addBooleanOption("preserve-php");
		CmdLineParser.Option preserveServerScriptTagsOpt = parser.addBooleanOption("preserve-server-script");
		CmdLineParser.Option compressJsOpt = parser.addBooleanOption("compress-js");
		CmdLineParser.Option compressCssOpt = parser.addBooleanOption("compress-css");
		CmdLineParser.Option jsCompressorOpt = parser.addStringOption("js-compressor");
		
		CmdLineParser.Option simpleDoctypeOpt = parser.addBooleanOption("simple-doctype");
		CmdLineParser.Option removeScriptAttributesOpt = parser.addBooleanOption("remove-script-attr");
		CmdLineParser.Option removeStyleAttributesOpt = parser.addBooleanOption("remove-style-attr");
		CmdLineParser.Option removeLinkAttributesOpt = parser.addBooleanOption("remove-link-attr");
		CmdLineParser.Option removeFormAttributesOpt = parser.addBooleanOption("remove-form-attr");
		CmdLineParser.Option removeInputAttributesOpt = parser.addBooleanOption("remove-input-attr");
		CmdLineParser.Option simpleBooleanAttributesOpt = parser.addBooleanOption("simple-bool-attr");
		CmdLineParser.Option removeJavaScriptProtocolOpt = parser.addBooleanOption("remove-js-protocol");
		CmdLineParser.Option removeHttpProtocolOpt = parser.addBooleanOption("remove-http-protocol");
		CmdLineParser.Option removeHttpsProtocolOpt = parser.addBooleanOption("remove-https-protocol");

		CmdLineParser.Option nomungeOpt = parser.addBooleanOption("nomunge");
		CmdLineParser.Option linebreakOpt = parser.addStringOption("line-break");
		CmdLineParser.Option preserveSemiOpt = parser.addBooleanOption("preserve-semi");
		CmdLineParser.Option disableOptimizationsOpt = parser.addBooleanOption("disable-optimizations");
		
		CmdLineParser.Option closureOptLevelOpt = parser.addStringOption("closure-opt-level");

		Reader in = null;
		BufferedReader patternsIn = null;
		Writer out = null;
		
		boolean useClosureCompressor = false;
		
		try {

			parser.parse(args);

			// help
			Boolean help = (Boolean) parser.getOptionValue(helpOpt);
			if (help != null && help.booleanValue()) {
				printUsage();
				System.exit(0);
			}

			// charset
			String charset = (String) parser.getOptionValue(charsetOpt);
			if (charset == null || !Charset.isSupported(charset)) {
				charset = "UTF-8";
			}

			// input file
			String[] fileArgs = parser.getRemainingArgs();

			// type
			String type = (String) parser.getOptionValue(typeOpt);
			if (type != null && !type.equalsIgnoreCase("html") && !type.equalsIgnoreCase("xml")) {
				printUsage();
				System.exit(1);
			}

			if (fileArgs.length == 0) {

				// html by default for stdin
				if (type == null) {
					type = "html";
				}

				in = new InputStreamReader(System.in, charset);

			} else {

				String inputFilename = fileArgs[0];

				// detect type from extension
				if (type == null) {
					if(inputFilename.toLowerCase().endsWith(".xml")) {
						type = "xml";
					} else {
						type = "html";
					}
				}
				
				//detect url
				if(inputFilename.toLowerCase().startsWith("http://") || inputFilename.toLowerCase().startsWith("https://")) {
					in = new InputStreamReader((new URL(inputFilename)).openConnection().getInputStream());
				} else {
					in = new InputStreamReader(new FileInputStream(inputFilename), charset);
				}

				
			}
			
			if((parser.getOptionValue(analyzeOpt) != null) && type.equalsIgnoreCase("html")) {
				BufferedReader input =  new BufferedReader(in);
				
				//analyze HTML
				try {
					
					//read input file
					StringBuilder source = new StringBuilder();
					
					String line = null;
					while ((line = input.readLine()) != null){
						source.append(line);
						source.append(System.getProperty("line.separator"));
					}
	
					// Close the input stream first, and then open the output
					// stream,
					// in case the output file should override the input file.
					input.close();
					input = null;
					in.close();
					
					HtmlAnalyzer analyzer = new HtmlAnalyzer();
					analyzer.analyze(source.toString());
	
				} catch (Exception e) {
	
					e.printStackTrace();
					System.exit(1);
	
				}
				
			} else {
				
				//HTML compression

				//line break
				int linebreakpos = -1;
				String linebreakstr = (String) parser.getOptionValue(linebreakOpt);
				if (linebreakstr != null) {
					try {
						linebreakpos = Integer.parseInt(linebreakstr, 10);
					} catch (NumberFormatException e) {
						printUsage();
						System.exit(1);
					}
				}
	
				//output file
				String outputFilename = (String) parser.getOptionValue(outputFilenameOpt);
				
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
				
				String patternsFilename = (String) parser.getOptionValue(patternsFilenameOpt);
				if(patternsFilename != null) {
	
					//read input file
					patternsIn = new BufferedReader(new InputStreamReader(new FileInputStream(patternsFilename), charset));
	
					String line = null;
					while ((line = patternsIn.readLine()) != null){
						if(line.length() > 0) {
							try {
								preservePatterns.add(Pattern.compile(line));
							} catch (PatternSyntaxException  e) {
								System.err.println("ERROR: Regular expression compilation error: " + e.getMessage());
							}
						}
					}
				}
	
				//set compressor options
				Compressor compressor = null;
				if (type.equalsIgnoreCase("html")) {
	
					HtmlCompressor htmlCompressor = new HtmlCompressor();
					htmlCompressor.setRemoveComments(parser.getOptionValue(preserveCommentsOpt) == null);
					htmlCompressor.setRemoveMultiSpaces(parser.getOptionValue(preserveMultiSpacesOpt) == null);
					htmlCompressor.setRemoveIntertagSpaces(parser.getOptionValue(removeIntertagSpacesOpt) != null);
					htmlCompressor.setRemoveQuotes(parser.getOptionValue(removeQuotesOpt) != null);
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
						} else if(closureOptLevel != null && closureOptLevel.equalsIgnoreCase(ClosureJavaScriptCompressor.COMPILATION_LEVEL_WHITESPACE)) {
							closureCompressor.setCompilationLevel(CompilationLevel.WHITESPACE_ONLY);
						} else {
							closureCompressor.setCompilationLevel(CompilationLevel.SIMPLE_OPTIMIZATIONS);
						}
						
						htmlCompressor.setJavaScriptCompressor(closureCompressor);
					}
					
					compressor = htmlCompressor;
	
				} else {
	
					XmlCompressor xmlCompressor = new XmlCompressor();
					xmlCompressor.setRemoveComments(parser.getOptionValue(preserveCommentsOpt) == null);
					xmlCompressor.setRemoveIntertagSpaces(parser.getOptionValue(preserveIntertagSpacesOpt) == null);
	
					compressor = xmlCompressor;
	
				}
	
				BufferedReader input =  new BufferedReader(in);
				
				//compress
				try {
					
					//read input file
					StringBuilder source = new StringBuilder();
					
					String line = null;
					while ((line = input.readLine()) != null){
						source.append(line);
						source.append(System.getProperty("line.separator"));
					}
	
					// Close the input stream first, and then open the output
					// stream,
					// in case the output file should override the input file.
					input.close();
					input = null;
					in.close();
					in = null;
	
					if (outputFilename == null) {
						out = new OutputStreamWriter(System.out, charset);
					} else {
						out = new OutputStreamWriter(new FileOutputStream(outputFilename), charset);
					}
	
					String result = compressor.compress(source.toString());
					out.write(result);
	
				} catch (Exception e) {
	
					e.printStackTrace();
					System.exit(1);
	
				} finally {
					if (input != null) {
						try {
							input.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}

		} catch (NoClassDefFoundError e){
			if(useClosureCompressor) {
				System.err.println("ERROR: For JavaScript compression using Google Closure Compiler\n" +
						"additional jar file called compiler.jar must be present\n" +
						"in the same directory as HtmlCompressor jar");
			} else {
				System.err.println("ERROR: For CSS or JavaScript compression using YUICompressor additional jar file \n" +
					"called yuicompressor-2.4.*.jar or yuicompressor.jar must be present\n" +
					"in the same directory as HtmlCompressor jar");
			}
			System.exit(1);
		} catch (CmdLineParser.OptionException e) {

			printUsage();
			System.exit(1);

		} catch (IOException e) {

			e.printStackTrace();
			System.exit(1);

		} finally {

			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (patternsIn != null) {
				try {
					patternsIn.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		System.exit(0);
	}

	private static void printUsage() {
		System.err.println("Usage: java -jar htmlcompressor.jar [options] [input file]\n\n"

				+ "<input file or URL>            If not provided reads from stdin\n\n"
	
				+ "Global Options:\n"
				+ " --type <html|xml>             If not provided autodetects from file extension\n"
				+ " --charset <charset>           Read the input file using <charset>\n"
				+ " -h, --help                    Display this screen\n"
				+ " -a, --analyze                 Compression report (all settings below ignored)\n"
				+ " -o <output file>              If not provided outputs result to stdout\n\n"
	
				+ "XML Compression Options:\n"
				+ " --preserve-comments           Preserve comments\n"
				+ " --preserve-intertag-spaces    Preserve intertag spaces\n\n"
	
				+ "HTML Compression Options:\n"
				+ " --preserve-comments           Preserve comments\n"
				+ " --preserve-multi-spaces       Preserve multiple spaces\n"
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
				+ "                               Sets level of optimization (simple by default)\n\n"
				
				+ "CSS Compression Options for YUI Compressor:\n"
				+ " --line-break <column num>     Insert a line break after the specified column\n\n"
	
				+ "Custom Block Preservation Options:\n"
				+ " --preserve-php                Preserve <?php ... ?> tags\n"
				+ " --preserve-server-script      Preserve <% ... %> tags\n"
				+ " -p <custom_patterns.txt>      Read regular expressions that define\n"
				+ "                               custom preservation rules from a file\n\n"
				
				+ "Please note that if you enable CSS or JavaScript compression, additional\n"
				+ "YUI Compressor or Google Closure Compiler jar files must be present\n"
				+ "in the same directory as this jar."

		);
	}

}