HtmlCompressor is a small, fast and very easy to use Java library that minifies given HTML or XML source by removing extra whitespaces, comments and other unneeded characters without breaking the content structure. As a result pages become smaller in size and load faster. A command-line version of the compressor is also available.

Here is a few samples of HTML compression results with default settings:
| **Site Name** | **Original** | **Compressed** | **Decrease** |
|:--------------|:-------------|:---------------|:-------------|
|BBC            |77,054b       |55,324b         | **28.2%**    |
|CNet           |86,492b       |61,896b         | **28.4%**    |
|FOX News       |75,266b       |64,221b         | **14.7%**    |
|GameTrailers   |112,199b      |92,851b         | **17.2%**    |
|Kotaku         |134,938b      |116,280b        | **13.8%**    |
|National Post  |75,006b       |55,628b         | **25.8%**    |
|SlashDot       |158,137b      |142,346b        | **10.0%**    |
|StackOverflow  |116,032b      |100,478b        | **13.4%**    |

#### Table of Contents ####


## How it Works ##
During HTML compression the following is applied to the page source:
  * Any content within `<pre>`, `<textarea>`, `<script>` and `<style>` tags will be preserved and remain untouched (with the exception of `<script type="text/x-jquery-tmpl">` tags which are compressed as HTML). Inline javascript inside tags (`onclick="test()"`) will be preserved as well. You can wrap any part of the page in `<!-- {{{ -->...<!-- }}} -->` comments to preserve it, or provide a set of your own preservation rules (out of the box `<?php...?>`, `<%...%>`, and `<!--#... -->` are also supported)
  * Comments are removed (except IE conditional comments). Could be disabled.
  * Multiple spaces are replaced with a single space. Could be disabled.
  * Unneeded spaces inside tags (around `=` and before `/>`) are removed.
  * Quotes around tag attributes could be removed when safe (off by default).
  * All spaces between tags could be removed (off by default).
  * Spaces around selected tags could be removed (off by default).
  * Existing doctype declaration could be replaced with simple `<!DOCTYPE html>` declaration (off by default).
  * Default attributes from `<script>`, `<style>`, `<link>`, `<form>`, `<input>` tags could be removed (off by default).
  * Values from boolean tag attributes could be removed (off by default).
  * `javascript:` pseudo-protocol could be removed from inline event handlers (off by default).
  * `http://` and `https://` protocols could be replaced with `//` inside `href`, `src`, `cite`, and `action` tag attributes (tags marked with `rel="external"` are skipped).
  * Content inside `<style>` tags could be optionally compressed using YUI compressor or your own compressor implementation.
  * Content inside `<script>` could be optionally compressed using YUI compressor, Google Closure Compiler or your own compressor implementation.

With default settings your compressed HTML layout should be 100% identical to the original in all browsers (only characters that are completely safe to remove are removed). Optional settings (that should be safe in 99% cases) would give you extra savings.

You can optionally remove all unnecessary quotes from tag attributes (attributes that consist from a single word: `<div id="example">` would become `<div id=example>`). This usually gives around 3% pagesize decrease at no performance cost but might break strict HTML validation so this option is disabled by default.

About extra 3% pagesize can be saved by removing inter-tag spaces. It is fairly safe to turn this option on unless you rely on spaces for page formatting. Even if you do, you can always preserve required spaces with `&#20;` or `&nbsp;`. This option has no performance impact.

You can quickly test how each of the compressor settings would affect filesize of your page by running command line [HTML Analyzer](#HTML_Analyzer.md).

During XML compression:
  * Any content inside `<![CDATA[...]]>` is preserved.
  * All comments are removed. Could be disabled.
  * All spaces between tags are removed. Could be disabled.
  * Unneeded spaces inside tags (multiple spaces, spaces around `=`, spaces before `/>`) are removed.

## How to Use ##
Before reading further, if you are not serving your HTML/XML content using GZip compression, you should really look into that first, as it would give you very significant compression ratio (sometimes up to 10 times) and usually very easy to implement. For further reading on GZip compression please see [this article](http://betterexplained.com/articles/how-to-optimize-your-site-with-gzip-compression/) for example.

If you want to reach further size decrease, the next step would be removing insignificant, from browser's perspective, characters from your pages, that's where this library comes in handy.

Juriy Zaytsev did an excellent detailed research on HTML minification techniques, which you can use as a guide to what HTML compression settings would work best for your project. Please see his [Optimizing HTML](http://perfectionkills.com/optimizing-html/) and [Experimenting with html minifier](http://perfectionkills.com/experimenting-with-html-minifier/) articles.

### For Java Projects ###
If you are generating static HTML files on the server, the most flexible solution would be calling html compression before writing output to the file. If you are generating HTML files once in a while and then uploading them to a production server, the easiest solution that doesn't require any code modifications would be using ANT task that calls the command line version of the library and rewrites files with their compressed versions.

For dynamic sites that are using JSP, the best way of compressing the output would be using compressor taglib.

For dynamic sites using Velocity, you can either wrap your templates with compressor directives or call compressor manually after merging the template.

For other dynamic cases you will probably have to call compressors directly from the code before serving a page to the client.

`HtmlCompressor` and `XmlCompressor` classes are considered thread safe`*` and can be used in multi-thread environment (the only unsafe part is setting compression options, so it would be a good idea to initialize a compressor with required settings once per application and then use it to compress different pages in parallel in multiple threads. Please note that enabling statistics generation makes compressor not safe).

For Maven projects HtmlCompressor library is available as a [Maven artifact](#Maven_Artifact.md).

### For Non-Java Projects ###
If you are generating HTML for your site (or have simple site in pure HTML) you can use a command line version of the library (Java still must be installed). For dynamic sites it other languages the only option would be programmatically executing shell command that runs command line compressor.

### Bad Practices ###
  * Don't feed the compressor actual templates (php, jsp, etc). This most likely won't work, even if it does it would be a bad idea anyway as you will lose their readability and further development will be very inconvenient. Instead of compressing templates you should consider compressing resulting html after a template is merged. If you absolutely have to compress templates, you need to set custom block preservation rules for HTML Compressor.
  * If your site is in pure HTML, always keep original files and only compress their copies that will be served to the client. If you compress your only sources, again your further development will be very hard and there is no easy way to decompress pages back.

### Known Issues ###
  * When `<script>` tag contains some custom preserved block (for example `<?php>`), enabling inline javascript compression will fail. Such `<script>` tags could be skipped by wrapping them with `<!-- {{{ -->...<!-- }}} -->` comments (skip blocks).
  * Removing intertag spaces might break text formatting, for example spaces between  words surrounded with `<b>` will be removed. Such spaces might be preserved by replacing them with `&#20;` or `&nbsp;`.

## Dependencies ##
XML compressor doesn't rely on any external libraries.

HTML compressor with default settings doesn't require any dependencies.

Inline CSS compression requires [YUI compressor](http://developer.yahoo.com/yui/compressor/) library.

Inline JavaScript compression requires either [YUI compressor](http://developer.yahoo.com/yui/compressor/) library (by default) or [Google Closure Compiler](http://code.google.com/closure/compiler/) library.

All dependencies could be found in `/lib/` folder of the package.

Please note that if using command line compressor, there are strict restrictions on jar filenames for dependencies (more details in the command line compressor section below).


## Compressing HTML and XML files from a command line ##
If you have [Java](http://www.oracle.com/technetwork/java/javase/downloads/index.html) installed, you can run HTML and XML compressors from a command line.

For inline JavaScript compression you can choose between YUI Compressor (default) and Google Closure Compiler. If YUI compressor is used, jar file `yuicompressor-2.4.6.jar` (or `yuicompressor-2.4.*.jar` or `yuicompressor.jar`) must be present at the same directory as HtmlCompressor jar. For Closure Compiler `compiler.jar` must be present. (Please note that jar filenames cannot be changed).

Usage: `java -jar htmlcompressor.jar [options] [input]`
```
[input]                        URL, filename, directory, or space separated list
                               of files and directories to compress.
                               If none provided reads from <stdin>

Global Options:
 -?, /?, -h, --help            Displays this help screen
 -t, --type <html|xml>         If not provided autodetects from file extension
 -r, --recursive               Process files inside subdirectories
 -c, --charset <charset>       Charset for reading files, UTF-8 by default
 -m, --mask <filemask>         Filter input files inside directories by mask
 -o, --output <path>           Filename or directory for compression results.
                               If none provided outputs result to <stdout>
 -a, --analyze                 Tries different settings and displays report.
                               All settings except --js-compressor are ignored

XML Compression Options:
 --preserve-comments           Preserve comments
 --preserve-intertag-spaces    Preserve intertag spaces

HTML Compression Options:
 --preserve-comments           Preserve comments
 --preserve-multi-spaces       Preserve multiple spaces
 --preserve-line-breaks        Preserve line breaks
 --remove-intertag-spaces      Remove intertag spaces
 --remove-quotes               Remove unneeded quotes
 --simple-doctype              Change doctype to <!DOCTYPE html>
 --remove-style-attr           Remove TYPE attribute from STYLE tags
 --remove-link-attr            Remove TYPE attribute from LINK tags
 --remove-script-attr          Remove TYPE and LANGUAGE from SCRIPT tags
 --remove-form-attr            Remove METHOD="GET" from FORM tags
 --remove-input-attr           Remove TYPE="TEXT" from INPUT tags
 --simple-bool-attr            Remove values from boolean tag attributes
 --remove-js-protocol          Remove "javascript:" from inline event handlers
 --remove-http-protocol        Remove "http:" from tag attributes
 --remove-https-protocol       Remove "https:" from tag attributes
 --remove-surrounding-spaces <min|max|all|custom_list>
                               Predefined or custom comma separated list of tags
 --compress-js                 Enable inline JavaScript compression
 --compress-css                Enable inline CSS compression using YUICompressor
 --js-compressor <yui|closure> Switch inline JavaScript compressor between
                               YUICompressor (default) and Closure Compiler

JavaScript Compression Options for YUI Compressor:
 --nomunge                     Minify only, do not obfuscate
 --preserve-semi               Preserve all semicolons
 --disable-optimizations       Disable all micro optimizations
 --line-break <column num>     Insert a line break after the specified column

JavaScript Compression Options for Google Closure Compiler:
 --closure-opt-level <simple|advanced|whitespace>
                               Sets level of optimization (simple by default)
 --closure-externs <file>      Sets custom externs file, repeat for each file
 --closure-custom-externs-only Disable default built-in externs

CSS Compression Options for YUI Compressor:
 --line-break <column num>     Insert a line break after the specified column

Custom Block Preservation Options:
 --preserve-php                Preserve <?php ... ?> tags
 --preserve-server-script      Preserve <% ... %> tags
 --preserve-ssi                Preserve <!--# ... --> tags
 -p, --preserve <path>         Read regular expressions that define
                               custom preservation rules from a file

Please note that if you enable CSS or JavaScript compression, additional
YUI Compressor or Google Closure Compiler jar files must be present
in the same directory as this jar.
```

Input and output files could be the same, in this case a file will be overwritten with its compressed version.

If `--type` parameter is not set, the compressor would try to guess it based on a file extension. If a file extension is not recognized, it defaults to html compression. When compressing [multiple files at once](#Compressing_whole_directories_and_multiple_files_at_once.md), the type is not guessed and defaults to HTML compression if it wasn't set manually.

If charset is not provided, `UTF-8` will be used by default. List of all supported charsets could be found  [here](http://download.oracle.com/javase/1.5.0/docs/guide/intl/encoding.doc.html) (required name is specified in the first column, for example: `ISO-8859-1`, `windows-1251`).

Examples:
```
java -jar htmlcompressor.jar --preserve-comments --type html -o /path/compressed/test-compressed.html /path/original/test.html

java -jar htmlcompressor.jar --compress-js /path/original/test.html > /path/compressed/test-compressed.html

java -jar htmlcompressor.jar --compress-js --js-compressor closure --closure-opt-level advanced --closure-externs /path/externs1.js --closure-externs /path/externs2.js original.html > compressed.html
```


### Defining custom preservation rules ###
If you need to set custom tag preservation rules, you can put regular expressions defining them into a text file (one expression per line) and pass path to this file as `-p /path/to/regexps.txt` parameter. Regular expressions in a file can have so called "embedded flag expressions" macro options that set options for a regular expression compiler.

For example if you want to apply `CASE_INSENSITIVE` and `DOTALL` options to this expression that matches php blocks:
```
<\?php.+?\?>
```

you need to use this syntax:
```
(?si)<\?php.+?\?>
```

You can read more about regular expression compiler options and their corresponding embedded flags [here](http://download.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html)

Example:
```
java -jar htmlcompressor.jar --preserve-php -p /path/regexp.txt http://path/original/test.html > /path/compressed/test-compressed.html
```

### Compressing whole directories and multiple files at once ###
Command line compressor can take a whole directory as input. In this case output parameter should point to a directory as well.

For example:
```
java -jar htmlcompressor.jar --type html -o /to/ /from/
```

In this mode all files from the input directory will be compressed and placed into the output directory (with the same filenames). Sub-directories are not included by default, use `--recursive` option to include sub-directories.

Please note that `--type` parameter in this case plays important role as it not only defines which compressor mode to use, but also which files to compress. For HTML type only `*.html` and `*.htm` files are processed, for XML - only `*.xml`. If type is not explicitly set, it defaults to HTML.

You can overwrite default file masks by setting `--mask <mask>` parameter. Mask is a semicolon (`;`) separated filename filter, with the following special characters:

  * Star (`*`) - any character zero or more times
  * Question mark (`?`) - any character exactly one time

For example to compress only `.php` files, files which names start with `test-`, and files with two letter extensions:
```
java -jar htmlcompressor.jar --type html --recursive --mask *.php;test-*;*.?? -o /to/ /from/
```

(Please note that mask shouldn't contain any spaces)

Another way to compress multiple files at once is providing a space separated list of files (or even directories) as input:

```
java -jar htmlcompressor.jar -o /to/ /source1/ /source2/ file1.html file2.html
```

Please note that in this mode all files will be placed into the same output folder (`/to/`). If it is not a desired behavior, please compress each folder separately.

### HTML Analyzer ###
To run a command line compressor in analyzer mode you only need to specify input file or URL:
```
java -jar htmlcompressor.jar --js-compressor closure -a http://www.cnn.com/
```

In this mode HTML compressor will try to compress provided page with all possible compression parameters and display result in a report:
```
================================================================================
         Setting          | Incremental Gain |    Total Gain    |  Page Size   |
================================================================================
Compression disabled      |         0 (0.0%) |         0 (0.0%) |      103,568 |
All settings disabled     |        27 (0.0%) |        27 (0.0%) |      103,541 |
Comments removed          |     5,517 (5.3%) |     5,544 (5.4%) |       98,024 |
Multiple spaces removed   |     4,234 (4.3%) |     9,778 (9.4%) |       93,790 |
No spaces between tags    |       838 (0.9%) |   10,616 (10.3%) |       92,952 |
No surround spaces (min)  |         4 (0.0%) |   10,620 (10.3%) |       92,948 |
No surround spaces (max)  |         0 (0.0%) |   10,620 (10.3%) |       92,948 |
No surround spaces (all)  |       116 (0.1%) |   10,736 (10.4%) |       92,832 |
Quotes removed from tags  |     2,072 (2.2%) |   12,808 (12.4%) |       90,760 |
<link> attr. removed      |       112 (0.1%) |   12,920 (12.5%) |       90,648 |
<style> attr. removed     |         0 (0.0%) |   12,920 (12.5%) |       90,648 |
<script> attr. removed    |     1,040 (1.1%) |   13,960 (13.5%) |       89,608 |
<form> attr. removed      |        22 (0.0%) |   13,982 (13.5%) |       89,586 |
<input> attr. removed     |        40 (0.0%) |   14,022 (13.5%) |       89,546 |
Simple boolean attributes |        36 (0.0%) |   14,058 (13.6%) |       89,510 |
Simple doctype            |        86 (0.1%) |   14,144 (13.7%) |       89,424 |
Remove js pseudo-protocol |         0 (0.0%) |   14,144 (13.7%) |       89,424 |
Remove http protocol      |     1,095 (1.2%) |   15,239 (14.7%) |       88,329 |
Remove https protocol     |         0 (0.0%) |   15,239 (14.7%) |       88,329 |
Compress inline CSS (YUI) |       354 (0.4%) |   15,593 (15.1%) |       87,975 |
Compress JS (Closure)     |     2,095 (2.4%) |   17,688 (17.1%) |       85,880 |
================================================================================

Each consecutive compressor setting is applied on top of previous ones.
In order to see JS and CSS compression results, YUI jar file must be present.
All sizes are in bytes.
```

Having this information might help you decide which compression parameters to use.

## Using HTML Compressor from Java API ##
Create [HtmlCompressor](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/HtmlCompressor.html) instance and pass HTML content to [compress(String source)](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/HtmlCompressor.html#compress(java.lang.String)) method, which would return compressed result:
```
String html = getHtml(); //your external method to get html from memory, file, url etc.
HtmlCompressor compressor = new HtmlCompressor();
String compressedHtml = compressor.compress(html);
```
By default it:
  * Preserves any content within `<pre>`, `<textarea>`, `<script>` and `<style>` tags
  * Removes HTML comments
  * Replaces multiple whitespace characters and line breaks with spaces

If you need to set additional compression parameters:
```
HtmlCompressor compressor = new HtmlCompressor();

compressor.setEnabled(true);                   //if false all compression is off (default is true)
compressor.setRemoveComments(true);            //if false keeps HTML comments (default is true)
compressor.setRemoveMultiSpaces(true);         //if false keeps multiple whitespace characters (default is true)
compressor.setRemoveIntertagSpaces(true);      //removes iter-tag whitespace characters
compressor.setRemoveQuotes(true);              //removes unnecessary tag attribute quotes
compressor.setSimpleDoctype(true);             //simplify existing doctype
compressor.setRemoveScriptAttributes(true);    //remove optional attributes from script tags
compressor.setRemoveStyleAttributes(true);     //remove optional attributes from style tags
compressor.setRemoveLinkAttributes(true);      //remove optional attributes from link tags
compressor.setRemoveFormAttributes(true);      //remove optional attributes from form tags
compressor.setRemoveInputAttributes(true);     //remove optional attributes from input tags
compressor.setSimpleBooleanAttributes(true);   //remove values from boolean tag attributes
compressor.setRemoveJavaScriptProtocol(true);  //remove "javascript:" from inline event handlers
compressor.setRemoveHttpProtocol(true);        //replace "http://" with "//" inside tag attributes
compressor.setRemoveHttpsProtocol(true);       //replace "https://" with "//" inside tag attributes
compressor.setPreserveLineBreaks(true);        //preserves original line breaks
compressor.setRemoveSurroundingSpaces("br,p"); //remove spaces around provided tags

compressor.setCompressCss(true);               //compress inline css 
compressor.setCompressJavaScript(true);        //compress inline javascript
compressor.setYuiCssLineBreak(80);             //--line-break param for Yahoo YUI Compressor 
compressor.setYuiJsDisableOptimizations(true); //--disable-optimizations param for Yahoo YUI Compressor 
compressor.setYuiJsLineBreak(-1);              //--line-break param for Yahoo YUI Compressor 
compressor.setYuiJsNoMunge(true);              //--nomunge param for Yahoo YUI Compressor 
compressor.setYuiJsPreserveAllSemiColons(true);//--preserve-semi param for Yahoo YUI Compressor 

//use Google Closure Compiler for javascript compression
compressor.setJavaScriptCompressor(new ClosureJavaScriptCompressor(CompilationLevel.SIMPLE_OPTIMIZATIONS));

//use your own implementation of css comressor
compressor.setCssCompressor(new MyOwnCssCompressor());

String compressedHtml = compressor.compress(html);
```

If JavaScript compression is enabled, corresponding compressor libraries should be included into project's classpath. See [Dependencies](#Dependencies.md) section for more details.

### Creating your own block preservation rules ###
If you are going to compress HTML mixed with some non-HTML tags (like PHP, JSP, etc), you most likely would want to keep those tags preserved. You can set custom preservation rules for HTML compressor by passing a list of regular expression `Pattern` objects.

[HtmlCompressor](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/HtmlCompressor.html) class currently has 3 predefined patterns for most often used custom preservation rules: [PHP\_TAG\_PATTERN](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/HtmlCompressor.html#PHP_TAG_PATTERN), [SERVER\_SCRIPT\_TAG\_PATTERN](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/HtmlCompressor.html#SERVER_SCRIPT_TAG_PATTERN) and [SERVER\_SIDE\_INCLUDE\_PATTERN](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/HtmlCompressor.html#SERVER_SIDE_INCLUDE_PATTERN). Other patterns could be created manually:

```
List<Pattern> preservePatterns = new ArrayList<Pattern>();
preservePatterns.add(HtmlCompressor.PHP_TAG_PATTERN); //<?php ... ?> blocks
preservePatterns.add(HtmlCompressor.SERVER_SCRIPT_TAG_PATTERN); //<% ... %> blocks
preservePatterns.add(HtmlCompressor.SERVER_SIDE_INCLUDE_PATTERN); //<!--# ... --> blocks
preservePatterns.add(Pattern.compile("<jsp:.*?>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE)); //<jsp: ... > tags

HtmlCompressor compressor = new HtmlCompressor();
compressor.setPreservePatterns(preservePatterns);
compressor.compress(source);
```

Custom preservation rules have the highest priority.

### Using different JavaScript and CSS compressor implementations ###
Out of the box HTML Compressor comes with two JavaScript implementations that use YUI and Google Closure compressors for compressing inline `<script>` tags, and one implementation that uses YUI compressor for compressing inline `<style>` tags. You can additionally create your own implementations using any third party library.

CSS compressor implementation based on YUI is called [YuiCssCompressor](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/YuiCssCompressor.html). In most cases you won't need to use this class directly as HTML compressor would initialize it behind the scenes for you if no other implementation is provided for CSS compression.

```
YuiCssCompressor cssCompressor = new YuiCssCompressor();
cssCompressor.setLineBreak(-1);
htmlCompressor.setCssCompressor(cssCompressor);
```

This would be exactly the same as:
```
htmlCompressor.setYuiCssLineBreak(-1);
```


JavaScript compressor implementation based on YUI is called [YuiJavaScriptCompressor](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/YuiJavaScriptCompressor.html). You usually won't need to use it directly either, as HTML Compressor will use it behind the scenes by default.

JavaScript compressor based on Google Closure Compiler implementation is called [ClosureJavaScriptCompressor](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/ClosureJavaScriptCompressor.html):

```
//default settings
htmlCompressor.setJavaScriptCompressor(new ClosureJavaScriptCompressor());

//you can set different level of compilation in a constructor
htmlCompressor.setJavaScriptCompressor(new ClosureJavaScriptCompressor(CompilationLevel.WHITESPACE_ONLY));

//or fine tune all settings
ClosureJavaScriptCompressor jsCompressor = new ClosureJavaScriptCompressor();
jsCompressor.setCompilationLevel(CompilationLevel.SIMPLE_OPTIMIZATIONS);
jsCompressor.setCompilerOptions(new CompilerOptions());
jsCompressor.setExterns(new ArrayList<JSSourceFile>());
jsCompressor.setCustomExternsOnly(false);
jsCompressor.setLoggingLevel(Level.SEVERE);
jsCompressor.setWarningLevel(WarningLevel.DEFAULT);

htmlCompressor.setJavaScriptCompressor(jsCompressor);
```

Please see [javadocs](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/ClosureJavaScriptCompressor.html) for details.

If you would like to create your own compressor, you need to create a class that  implements simple [Compressor](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/Compressor.html) interface:

```
public interface Compressor {
	public abstract String compress(String source);
}
```

You can take current [YuiJavaScriptCompressor](http://code.google.com/p/htmlcompressor/source/browse/trunk/src/com/googlecode/htmlcompressor/compressor/YuiJavaScriptCompressor.java) and [ClosureJavaScriptCompressor](http://code.google.com/p/htmlcompressor/source/browse/trunk/src/com/googlecode/htmlcompressor/compressor/ClosureJavaScriptCompressor.java) implementations as examples.

### Retrieving HTML compression statistics ###
HTML Compressor can optionally collect compression statistics:

```
HtmlCompressor compressor = new HtmlCompressor();
compressor.setGenerateStatistics(true);
compressor.compress(html);

System.out.println(String.format(
	"Compression time: %,d ms, Original size: %,d bytes, Compressed size: %,d bytes", 
	compressor.getStatistics().getTime(), 
	compressor.getStatistics().getOriginalMetrics().getFilesize(), 	
	compressor.getStatistics().getCompressedMetrics().getFilesize()
));
```

Please note that enabling statistics generation makes HTML compressor not thread safe.

For a full list of all statistics parameters collected please see [HtmlCompressorStatistics](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/HtmlCompressorStatistics.html) javadocs.

## Using XML Compressor from Java API ##
Create [XmlCompressor](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/XmlCompressor.html) instance and pass XML content to [compress(String source)](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/compressor/XmlCompressor.html#compress(java.lang.String)) method, which would return a compressed source:
```
String xml = getXml(); //your external method to get xml from memory, file, url etc.
XmlCompressor compressor = new XmlCompressor();
String compressedXml = compressor.compress(xml);
```
By default it:
  * Preserves any content within tags and `CDATA` blocks.
  * Removes XML comments
  * Removes all whitespace characters outside of the tags

If you need to set additional compression parameters:
```
XmlCompressor compressor = new XmlCompressor();

compressor.setEnabled(true);             //if false all compression is off (default is true)
compressor.setRemoveComments(true);      //if false keeps XML comments (default is true)
compressor.setRemoveIntertagSpaces(true);//removes iter-tag whitespace characters  (default is true)
String compressedXml = compressor.compress(xml);
```

## Compressing selective content in JSP pages ##
If you install the compressor taglib, you will be able to use `<compress:html>`, `<compress:xml>`, `<compress:js>` and `<compress:css>` tags in your JSP pages to mark selective code blocks that need to be compressed.

### Taglib installation procedure ###
  1. Download `.jar` file of the current release and put it into your lib directory
  1. Add the following taglib directive to your JSP pages:
```
<%@ taglib uri="http://htmlcompressor.googlecode.com/taglib/compressor" prefix="compress" %>
```

Please note that JSP 2.0 or above is required.

### Using compressor taglib ###
Now you can wrap parts of JSP pages that need to be compressed with corresponding tags:
```
<%@ taglib uri="http://htmlcompressor.googlecode.com/taglib/compressor" prefix="compress" %>
<html>
	<head><title>Compressor Example</title></head>
	<body>
		<compress:html enabled="${param.enabled != 'false'}" compressJavaScript="false">
			<h1> Header </h1>
			<a href="/"> Link </a>
			<script>
				alert("javascript"); //comment
			</script>
		</compress:html>
		
		<script>
			<compress:js jsCompressor="closure">
				alert("javascript"); //comment
			</compress:js>
		</script>
	</body>
</html>
```

Each tag supports all attributes from corresponding compressors, so you have full control over their options, for example:
```
<compress:html enabled="true" removeComments="false" compressJavaScript="true" yuiJsDisableOptimizations="true"></compress:html>
<compress:html compressJavaScript="true" jsCompressor="closure" closureOptLevel="simple"></compress:html>
<compress:js enabled="true" yuiJsLineBreak="80" yuiJsPreserveAllSemiColons="true"></compress:js>
```

`<compress:js>` and `<compress:css>` tags call corresponding compressors directly bypassing HtmlCompressor, so they should be used only for actual JavaScript and Css content. If you need to wrap mixed content use `<compress:html>` with required attributes.  For the complete list of available attributes see taglib or [javadocs](http://htmlcompressor.googlecode.com/svn/trunk/doc/com/googlecode/htmlcompressor/taglib/package-summary.html).

## Compressing selective content in Velocity templates ##
After installing Velocity compressor directives you will be able to use `#compressHtml`, `#compressXml`, `#compressJs` and `#compressCss` directives in your Velocity templates to mark selective blocks that need to be compressed.

### Compressor directive installation procedure ###
First you need to set `userdirective` Velocity property and provide a list of  directive classes. You can do this by either adding the following line into your `velocity.properties`:
```
userdirective=com.googlecode.htmlcompressor.velocity.HtmlCompressorDirective, \
    com.googlecode.htmlcompressor.velocity.XmlCompressorDirective, \ 
    com.googlecode.htmlcompressor.velocity.JavaScriptCompressorDirective, \
    com.googlecode.htmlcompressor.velocity.CssCompressorDirective
```
Or using runtime configuration:
```
VelocityEngine velocity = new VelocityEngine();
velocity.setProperty("userdirective", "com.googlecode.htmlcompressor.velocity.HtmlCompressorDirective," + 
    "com.googlecode.htmlcompressor.velocity.XmlCompressorDirective," + 
    "com.googlecode.htmlcompressor.velocity.JavaScriptCompressorDirective," + 
    "com.googlecode.htmlcompressor.velocity.CssCompressorDirective");
velocity.init();
```
You can include only compressors you would need.

All compressors are now ready to be used with default configurations. You can change configuration properties either in `velocity.properties` or using runtime configuration by setting `userdirective.{directive}.{property_name}` values. Here is a list of all supported parameters with their default values:
```
userdirective.compressHtml.enabled = true
userdirective.compressHtml.removeComments = true
userdirective.compressHtml.removeMultiSpaces = true
userdirective.compressHtml.removeIntertagSpaces = false
userdirective.compressHtml.removeQuotes = false
userdirective.compressHtml.preserveLineBreaks = false
userdirective.compressHtml.simpleDoctype = false
userdirective.compressHtml.removeScriptAttributes = false
userdirective.compressHtml.removeStyleAttributes = false
userdirective.compressHtml.removeLinkAttributes = false
userdirective.compressHtml.removeFormAttributes = false
userdirective.compressHtml.removeInputAttributes = false
userdirective.compressHtml.simpleBooleanAttributes = false
userdirective.compressHtml.removeJavaScriptProtocol = false
userdirective.compressHtml.removeHttpProtocol = false
userdirective.compressHtml.removeHttpsProtocol = false
userdirective.compressHtml.compressJavaScript = false
userdirective.compressHtml.compressCss = false
userdirective.compressHtml.jsCompressor = yui #(or "closure")
userdirective.compressHtml.yuiJsNoMunge = false
userdirective.compressHtml.yuiJsPreserveAllSemiColons = false
userdirective.compressHtml.yuiJsLineBreak = -1
userdirective.compressHtml.yuiCssLineBreak = -1
userdirective.compressHtml.closureOptLevel = simple #(or "advanced", "whitespace")

userdirective.compressXml.enabled = true
userdirective.compressXml.removeComments = true
userdirective.compressXml.removeIntertagSpaces = true

userdirective.compressJs.enabled = true
userdirective.compressJs.jsCompressor = yui #(or "closure")
userdirective.compressJs.yuiJsNoMunge = false
userdirective.compressJs.yuiJsPreserveAllSemiColons = false
userdirective.compressJs.yuiJsLineBreak = -1
userdirective.compressJs.closureOptLevel = simple #(or "advanced", "whitespace")

userdirective.compressCss.enabled = true
userdirective.compressCss.yuiCssLineBreak = -1
```


### Using compressor directives ###
Now you can wrap parts of Velocity templates that need to be compressed with corresponding directives (please note that directives must end with empty parentheses):
```
<html>
	<head><title>Compressor Example</title></head>
	<body>
		#compressHtml()
			<h1> Header </h1>
			<a href="/"> Link </a>
			<script>
				alert("javascript"); //comment
			</script>
		#end
		
		<script>
		#compressJs()
                      	alert("javascript"); //comment
		#end
		</script>

	</body>
</html>
```

`#compressJs` and `#compressCss` directives call corresponding YUI or Closure compressor classes directly bypassing HtmlCompressor, so they should be used only for actual JavaScript and CSS content. If you need to wrap mixed content use `#compressHtml` with required properties.


## Setting up Ant task to compress files ##
If you are using Ant for project builds you can setup a task that will compress provided files automatically during a build.

For example:
```
    <target name="compress">
    	<apply executable="java" parallel="false">
	        <fileset dir="/test/" includes="*.html">
	        	<exclude name="**/leave/**" />
	        </fileset>
	        <arg value="-jar"/>
	        <arg path="/path/to/htmlcompressor-0.8.jar"/>
	 	<arg line="--type html"/>
	 	<arg value="--preserve-comments"/>
	        <srcfile/>
	        <arg value="-o"/>
	        <mapper type="glob" from="*" to="/compressed/*"/>
	        <targetfile/>
	    </apply>
    </target>
```

This will compress all html files from `/test/` excluding `/leave/` subfolders and put results to `/compressed/` folder using `--preserve-comments` and `--type html` compression parameters.

Another example:
```
    <target name="compress">
    	<apply executable="java" parallel="false" force="true" dest="/test/">
	        <fileset dir="/test/" includes="*.xml"/>
	        <arg value="-jar"/>
	        <arg path="/path/to/htmlcompressor-0.8.jar"/>
	        <srcfile/>
	        <arg value="-o"/>
	        <mapper type="identity"/>
	        <targetfile/>
	    </apply>
    </target>
```
This will overwrite all xml files within `/test/` folder with their compressed versions.

## Maven Integration ##

### Maven Artifact ###
HtmlCompressor is available as a maven artifact:

```
<dependency>
  <groupId>com.googlecode.htmlcompressor</groupId>
  <artifactId>htmlcompressor</artifactId>
  <version>1.4</version>
</dependency>
```

Please note, that HtmlCompressor uses YUI Compressor and/or Google Closure libraries if inline javascript or css compression is enabled. These dependencies were marked as optional, so you would have to declare them explicitly in your project if corresponding functionality is required:

```
<-- Optional dependencies if js/css compression is enabled -->
<dependency>
  <groupId>com.google.javascript</groupId>
  <artifactId>closure-compiler</artifactId>
  <version>r1043</version>
</dependency>
<dependency>
  <groupId>com.yahoo.platform.yui</groupId>
  <artifactId>yuicompressor</artifactId>
  <version>2.4.6</version>
</dependency>
```

### Maven Plugin ###
For compressing HTML and XML files during the build process [Maven HTMLCompressor Plugin](http://code.google.com/p/htmlcompressor-maven-plugin/) created by Alex Tunyk is available:
```
<build>
  <plugins>
    <plugin>
      <groupId>com.tunyk.mvn.plugins.htmlcompressor</groupId>
      <artifactId>htmlcompressor-maven-plugin</artifactId>
      <version>1.0</version>
      <configuration>
        <goalPrefix>htmlcompressor</goalPrefix>
      </configuration>
    </plugin>
  </plugins>
</build>
```

For plugin configuration options and usage details please see the [project homepage](http://code.google.com/p/htmlcompressor-maven-plugin/).

## Who Uses it ##
  * [HTML5 Boilerplate](http://html5boilerplate.com/)
  * [SpaFinder](http://www.spafinder.com/)
  * [Erik's Weblog](http://erik.thauvin.net/blog/)
  * [CyberMonitor.ru](http://cybermonitor.ru/)
  * [Smaller](http://smallerapp.com/)


If you would like to add your project to the list, please contact me at: serg472@gmail.com