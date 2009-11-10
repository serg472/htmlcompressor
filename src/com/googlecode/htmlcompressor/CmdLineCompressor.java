package com.googlecode.htmlcompressor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;

import jargs.gnu.CmdLineParser;

public class CmdLineCompressor {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CmdLineParser parser = new CmdLineParser();
        CmdLineParser.Option typeOpt = parser.addStringOption("type");
        CmdLineParser.Option verboseOpt = parser.addBooleanOption('v', "verbose");
        CmdLineParser.Option nomungeOpt = parser.addBooleanOption("nomunge");
        CmdLineParser.Option linebreakOpt = parser.addStringOption("line-break");
        CmdLineParser.Option preserveSemiOpt = parser.addBooleanOption("preserve-semi");
        CmdLineParser.Option disableOptimizationsOpt = parser.addBooleanOption("disable-optimizations");
        CmdLineParser.Option helpOpt = parser.addBooleanOption('h', "help");
        CmdLineParser.Option charsetOpt = parser.addStringOption("charset");
        CmdLineParser.Option outputFilenameOpt = parser.addStringOption('o', "output");

        Reader in = null;
        Writer out = null;

        try {

            parser.parse(args);

            Boolean help = (Boolean) parser.getOptionValue(helpOpt);
            if (help != null && help.booleanValue()) {
                printUsage();
                System.exit(0);
            }

            boolean verbose = parser.getOptionValue(verboseOpt) != null;

            String charset = (String) parser.getOptionValue(charsetOpt);
            if (charset == null || !Charset.isSupported(charset)) {
                charset = System.getProperty("file.encoding");
                if (charset == null) {
                    charset = "UTF-8";
                }
                if (verbose) {
                    System.err.println("\n[INFO] Using charset " + charset);
                }
            }

            String[] fileArgs = parser.getRemainingArgs();
            String type = (String) parser.getOptionValue(typeOpt);

            if (fileArgs.length == 0) {

                if (type == null || !type.equalsIgnoreCase("js") && !type.equalsIgnoreCase("css")) {
                	printUsage();
                    System.exit(1);
                }

                in = new InputStreamReader(System.in, charset);

            } else {

                if (type != null && !type.equalsIgnoreCase("js") && !type.equalsIgnoreCase("css")) {
                	printUsage();
                    System.exit(1);
                }

                String inputFilename = fileArgs[0];

                if (type == null) {
                    int idx = inputFilename.lastIndexOf('.');
                    if (idx >= 0 && idx < inputFilename.length() - 1) {
                        type = inputFilename.substring(idx + 1);
                    }
                }

                if (type == null || !type.equalsIgnoreCase("js") && !type.equalsIgnoreCase("css")) {
                	printUsage();
                    System.exit(1);
                }

                in = new InputStreamReader(new FileInputStream(inputFilename), charset);
            }

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

            String outputFilename = (String) parser.getOptionValue(outputFilenameOpt);

            if (type.equalsIgnoreCase("js")) {

            	/*
                try {
                    JavaScriptCompressor compressor = new JavaScriptCompressor(in, new ErrorReporter() {

                        public void warning(String message, String sourceName,
                                int line, String lineSource, int lineOffset) {
                            if (line < 0) {
                                System.err.println("\n[WARNING] " + message);
                            } else {
                                System.err.println("\n[WARNING] " + line + ':' + lineOffset + ':' + message);
                            }
                        }

                        public void error(String message, String sourceName,
                                int line, String lineSource, int lineOffset) {
                            if (line < 0) {
                                System.err.println("\n[ERROR] " + message);
                            } else {
                                System.err.println("\n[ERROR] " + line + ':' + lineOffset + ':' + message);
                            }
                        }

                        public EvaluatorException runtimeError(String message, String sourceName,
                                int line, String lineSource, int lineOffset) {
                            error(message, sourceName, line, lineSource, lineOffset);
                            return new EvaluatorException(message);
                        }
                    });

                    // Close the input stream first, and then open the output stream,
                    // in case the output file should override the input file.
                    in.close(); in = null;

                    if (outputFilename == null) {
                        out = new OutputStreamWriter(System.out, charset);
                    } else {
                        out = new OutputStreamWriter(new FileOutputStream(outputFilename), charset);
                    }

                    boolean munge = parser.getOptionValue(nomungeOpt) == null;
                    boolean preserveAllSemiColons = parser.getOptionValue(preserveSemiOpt) != null;
                    boolean disableOptimizations = parser.getOptionValue(disableOptimizationsOpt) != null;

                    compressor.compress(out, linebreakpos, munge, verbose,
                            preserveAllSemiColons, disableOptimizations);

                } catch (EvaluatorException e) {

                    e.printStackTrace();
                    // Return a special error code used specifically by the web front-end.
                    System.exit(2);

                }
*/
            } else if (type.equalsIgnoreCase("css")) {
/*
                CssCompressor compressor = new CssCompressor(in);

                // Close the input stream first, and then open the output stream,
                // in case the output file should override the input file.
                in.close(); in = null;

                if (outputFilename == null) {
                    out = new OutputStreamWriter(System.out, charset);
                } else {
                    out = new OutputStreamWriter(new FileOutputStream(outputFilename), charset);
                }

                compressor.compress(out, linebreakpos);
 */
            }
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

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}

	private static void printUsage() {
		System.out.println(
				"Usage: java -jar htmlcompressor-x.y.z.jar [options] [input file]\n\n"

						+ "<input file>                   If not provided reads from stdin\n\n"

						+ "Global Options:\n"
						+ "  -o <output file>             If not provided outputs result to stdout\n"
						+ "  --type <html|xml>            If not provided autodetects from file extension\n"
						+ "  --charset <charset>          Read the input file using <charset>\n"
						+ "  -h, --help                   Display this screen\n\n"

						+ "XML Options:\n"
						+ "  --preserve-comments          Preserve comments\n"
						+ "  --preserve-intertag-spaces   Preserve intertag spaces\n\n"

						+ "HTML Options:\n"
						+ "  --preserve-comments          Preserve comments\n"
						+ "  --preserve-multi-spaces      Preserve multiple spaces\n"
						+ "  --remove-intertag-spaces     Remove intertag spaces\n"
						+ "  --remove-quotes              Remove unneeded quotes\n"
						+ "  --compress-js                Enable JavaScript compression using YUICompressor\n"
						+ "  --compress-css               Enable CSS compression using YUICompressor\n\n"

						+ "JavaScript Options (for YUI Compressor):\n"
						+ "  --nomunge                    Minify only, do not obfuscate\n"
						+ "  --preserve-semi              Preserve all semicolons\n"
						+ "  --disable-optimizations      Disable all micro optimizations\n"
						+ "  --line-break <column num>    Insert a line break after the specified column\n\n"

						+ "CSS Options (for YUI Compressor):\n"
						+ "  --line-break <column num>    Insert a line break after the specified column\n\n"

				);
	}

}
