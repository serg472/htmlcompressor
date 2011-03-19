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
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Basic CSS compressor implementation using <a href="http://developer.yahoo.com/yui/compressor/">Yahoo YUI Compressor</a>   
 * that could be used by {@link HtmlCompressor} for inline CSS compression.
 * 
 * <p>This code is a port of Julien Lecomte's CssCompressor which is largely based on Isaac Schlueter's cssmin utility.
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
	public String compress(String content) throws Exception {
    	StringWriter result = new StringWriter();
		
		process(new StringReader(content), result, lineBreak);
		
		return result.toString();
	}

    private void process(Reader in, Writer out, int linebreakpos)
            throws IOException {

        Pattern p;
        Matcher m;
        String css;
        StringBuffer sb;
        int startIndex, endIndex;
        
        // Read the stream...
        StringBuffer srcsb = new StringBuffer();
        int c1;
        while ((c1 = in.read()) != -1) {
            srcsb.append((char) c1);
        }

        // Remove all comment blocks...
        startIndex = 0;
        boolean iemac = false;
        boolean preserve = false;
        sb = new StringBuffer(srcsb.toString());
        while ((startIndex = sb.indexOf("/*", startIndex)) >= 0) {
            preserve = sb.length() > startIndex + 2 && sb.charAt(startIndex + 2) == '!';
            endIndex = sb.indexOf("*/", startIndex + 2);
            if (endIndex < 0) {
                if (!preserve) {
                    sb.delete(startIndex, sb.length());
                }
            } else if (endIndex >= startIndex + 2) {
                if (sb.charAt(endIndex-1) == '\\') {
                    // Looks like a comment to hide rules from IE Mac.
                    // Leave this comment, and the following one, alone...
                    startIndex = endIndex + 2;
                    iemac = true;
                } else if (iemac) {
                    startIndex = endIndex + 2;
                    iemac = false;
                } else if (!preserve) {
                    sb.delete(startIndex, endIndex + 2);
                } else {
                    startIndex = endIndex + 2;
                }
            }
        }

        css = sb.toString();

        // Normalize all whitespace strings to single spaces. Easier to work with that way.
        css = css.replaceAll("\\s+", " ");

        // Make a pseudo class for the Box Model Hack
        css = css.replaceAll("\"\\\\\"}\\\\\"\"", "___PSEUDOCLASSBMH___");

        // Remove the spaces before the things that should not have spaces before them.
        // But, be careful not to turn "p :link {...}" into "p:link{...}"
        // Swap out any pseudo-class colons with the token, and then swap back.
        sb = new StringBuffer();
        p = Pattern.compile("(^|\\})(([^\\{:])+:)+([^\\{]*\\{)");
        m = p.matcher(css);
        while (m.find()) {
            String s = m.group();
            s = s.replaceAll(":", "___PSEUDOCLASSCOLON___");
            m.appendReplacement(sb, s);
        }
        m.appendTail(sb);
        css = sb.toString();
        css = css.replaceAll("\\s+([!{};:>+\\(\\)\\],])", "$1");
        css = css.replaceAll("___PSEUDOCLASSCOLON___", ":");

        // Remove the spaces after the things that should not have spaces after them.
        css = css.replaceAll("([!{}:;>+\\(\\[,])\\s+", "$1");

        // Add the semicolon where it's missing.
        css = css.replaceAll("([^;\\}])}", "$1;}");

        // Replace 0(px,em,%) with 0.
        css = css.replaceAll("([\\s:])(0)(px|em|%|in|cm|mm|pc|pt|ex)", "$1$2");

        // Replace 0 0 0 0; with 0.
        css = css.replaceAll(":0 0 0 0;", ":0;");
        css = css.replaceAll(":0 0 0;", ":0;");
        css = css.replaceAll(":0 0;", ":0;");
        // Replace background-position:0; with background-position:0 0;
        css = css.replaceAll("background-position:0;", "background-position:0 0;");

        // Replace 0.6 to .6, but only when preceded by : or a white-space
        css = css.replaceAll("(:|\\s)0+\\.(\\d+)", "$1.$2");

        // Shorten colors from rgb(51,102,153) to #336699
        // This makes it more likely that it'll get further compressed in the next step.
        p = Pattern.compile("rgb\\s*\\(\\s*([0-9,\\s]+)\\s*\\)");
        m = p.matcher(css);
        sb = new StringBuffer();
        while (m.find()) {
            String[] rgbcolors = m.group(1).split(",");
            StringBuffer hexcolor = new StringBuffer("#");
            for (int i = 0; i < rgbcolors.length; i++) {
                int val = Integer.parseInt(rgbcolors[i]);
                if (val < 16) {
                    hexcolor.append("0");
                }
                hexcolor.append(Integer.toHexString(val));
            }
            m.appendReplacement(sb, hexcolor.toString());
        }
        m.appendTail(sb);
        css = sb.toString();

        // Shorten colors from #AABBCC to #ABC. Note that we want to make sure
        // the color is not preceded by either ", " or =. Indeed, the property
        //     filter: chroma(color="#FFFFFF");
        // would become
        //     filter: chroma(color="#FFF");
        // which makes the filter break in IE.
        p = Pattern.compile("([^\"'=\\s])(\\s*)#([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])([0-9a-fA-F])");
        m = p.matcher(css);
        sb = new StringBuffer();
        while (m.find()) {
            // Test for AABBCC pattern
            if (m.group(3).equalsIgnoreCase(m.group(4)) &&
                    m.group(5).equalsIgnoreCase(m.group(6)) &&
                    m.group(7).equalsIgnoreCase(m.group(8))) {
                m.appendReplacement(sb, m.group(1) + m.group(2) + "#" + m.group(3) + m.group(5) + m.group(7));
            } else {
                m.appendReplacement(sb, m.group());
            }
        }
        m.appendTail(sb);
        css = sb.toString();

        // Remove empty rules.
        css = css.replaceAll("[^\\}]+\\{;\\}", "");

        if (linebreakpos >= 0) {
            // Some source control tools don't like it when files containing lines longer
            // than, say 8000 characters, are checked in. The linebreak option is used in
            // that case to split long lines after a specific column.
            int i = 0;
            int linestartpos = 0;
            sb = new StringBuffer(css);
            while (i < sb.length()) {
                char c = sb.charAt(i++);
                if (c == '}' && i - linestartpos > linebreakpos) {
                    sb.insert(i, '\n');
                    linestartpos = i;
                }
            }

            css = sb.toString();
        }

        // Replace the pseudo class for the Box Model Hack
        css = css.replaceAll("___PSEUDOCLASSBMH___", "\"\\\\\"}\\\\\"\"");

        // Replace multiple semi-colons in a row by a single one
        // See SF bug #1980989
        css = css.replaceAll(";;+", ";");

        // Trim the final string (for any leading or trailing white spaces)
        css = css.trim();

        // Write the output...
        out.write(css);
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
