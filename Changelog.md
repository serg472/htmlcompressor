**1.5.3** (March 6, 2012)
  * `<script>` tags with unknown `type` attribute are getting preserved but not compressed. See [Issue #67](http://code.google.com/p/htmlcompressor/issues/detail?id=67) for details.
  * Space between final tag attribute and a trailing slash is preserved now if attribute value is unquoted (`<input value=x />`). See [Issue #60](http://code.google.com/p/htmlcompressor/issues/detail?id=60) for details.

**1.5.2** (September 18, 2011)
  * Added `--recursive` option to command line compressor that will process files inside subdirectories as well (when in directory mode).
  * Added `setRemoveSurroundingSpaces(tagList)` option to HtmlCompressor that will remove surrounding spaces from listed tags. See [Issue #54](http://code.google.com/p/htmlcompressor/issues/detail?id=54) for details.
  * HtmlAnalyzer looks at `--js-compressor` option to determine which js compressor to use (YUI by default).
  * Fixed bug with command line compressor not being able to take URL as input.

**1.5.1** (August 31, 2011)
  * Fixed a bug with not detecting the end of an empty comment correctly `<!---->`

**1.5** (August 28, 2011)
  * Command line compressor was rewritten from scratch and is now able to compress multiple files and directories at once. Please see the [front page](http://code.google.com/p/htmlcompressor/#Compressing_whole_directories_and_multiple_files_at_once) for details.
  * When `setRemoveHttpProtocol(true)` is enabled, tags that are marked with `rel="external"` will be skipped. This could be useful if you want to keep protocol for external resources. See [Issue #50](http://code.google.com/p/htmlcompressor/issues/detail?id=50) for details.
  * Spaces between custom preserved tags are collapsed again if `setRemoveIntertagSpaces(true)` is enabled. Spaces weren't removed in this case due to changes in 1.4.2 release. See [Issue #52](http://code.google.com/p/htmlcompressor/issues/detail?id=52) for details.

**1.4.3** (August 3, 2011)
  * Fixed bug with incorrect handling of preservable blocks inside conditional comments. See [Issue #48](http://code.google.com/p/htmlcompressor/issues/detail?id=48) for details.

**1.4.2** (July 30, 2011)
  * Fixed bug with custom preserved blocks causing exception if located inside some html tags. See [Issue #47](http://code.google.com/p/htmlcompressor/issues/detail?id=47) for details.

**1.4.1** (July 30, 2011)
  * `<script type="text/x-jquery-tmpl">` tags (jQuery template containers) are not preserved and their content is compressed with the rest of HTML.

**1.4** (July 8, 2011)
  * The project is now completely migrated to Maven and available as a [Maven artifact](http://code.google.com/p/htmlcompressor/#Maven_Artifact). Thanks Alex Tunyk for outstanding work on this (and who is now a committer on the project)
  * Added option to preserve line breaks in the original HTML document during a compression for increased readability. See [Issue #42](http://code.google.com/p/htmlcompressor/issues/detail?id=42) for details
  * Added option to preserve server side includes `<!--# ... -->`. See [Issue #43](http://code.google.com/p/htmlcompressor/issues/detail?id=43) for details
  * Fixed problem with Closure compressor throwing an error in come cases
  * Improved space removal around equals sign inside tag attributes during XML compression
  * `compress()` method of `Compressor` interface doesn't throw a checked `Exception` anymore

**1.3.1** (April 30, 2011)
  * When using advanced level of Google Closure compilation, a set of default built-in into `compiler.jar` externs is used. Default externs could be skipped by setting `ClosureJavaScriptCompressor.setCustomExternsOnly(true)` or `--closure-custom-externs-only` command line parameter
  * A list of custom externs could be passed to the command line compressor using `[--closure-externs <file>], ...` parameters
  * `ClosureJavaScriptCompressor.setExterns(JSSourceFile)` method signature is changed to `ClosureJavaScriptCompressor.setExterns(List<JSSourceFile>)` to support several custom externs.

**1.3** (April 24, 2011)
  * Added ability to replace `http://` or `https://` protocols with the  `//` (current protocol) inside `href`, `src`, `cite`, and `action` tag attributes by using `setRemoveHttpProtocol(true)` and `setRemoveHttpsProtocol(true)` setting accordingly. For example: `<a href="http://example.com">` would become `<a href="//example.com">`.   Corresponding settings were also added to command line compressor, HTML Analyzer, Velocity and JSP taglibs.
  * Added ability to programmatically retreive HTML compression statistics. See [front page](http://code.google.com/p/htmlcompressor/#Retrieving_HTML_compression_statistics) for details.
  * Improved space removal around equals sign inside tag attributes.
  * Deprecated `DefaultErrorReporter` class was removed (use `YuiJavaScriptCompressor.DefaultErrorReporter` instead)

**1.2** (April 10, 2011)
  * JSP compressor taglib is now supporting [Expression Language](http://download.oracle.com/javaee/1.4/tutorial/doc/JSPIntro7.html). TLD file is not provided anymore as a separated download and available only inside JAR file (see [Taglib installation procedure](http://code.google.com/p/htmlcompressor/#Taglib_installation_procedure)). Thanks to Erik for providing code samples and extensive testing of this feature.
  * Command line HTML and XML compressors now can read input source from URLs besides local files (only `http://` and `https://` protocols are currently supported).
  * Added ability to easily preserve any blocks inside HTML by wrapping them in `<!-- {{{ -->...<-- }}} -->` comments. Such skip blocks have the highest priority after user defined blocks. Please leave your feedback on this feature in [this thread](http://code.google.com/p/htmlcompressor/issues/detail?id=35).
  * Added command line [HTML analyzer](http://code.google.com/p/htmlcompressor/#HTML_Analyzer) which tries to compress provided source with different settings and displays results in a report table. Please leave your feedback on this feature in [this thread](http://code.google.com/p/htmlcompressor/issues/detail?id=35).

**1.1** (Mar 26, 2011)
  * YUI CSS compressor is depending again on external YUI library as it is still under active development.
  * Added HTML compressor option to replace existing doctype declaration with simple `<!DOCTYPE html>` declaration
  * Added HTML compressor options to remove default attributes from `<script>`, `<style>`, `<link>`, `<form>`, `<input>` tags
  * Added HTML compressor option to remove values from boolean attributes such as `checked`, `selected`, `disabled`, `readonly` (`checked="checked"` would become `checked`)
  * Added compressor option to remove `javascript:` pseudo-protocol from inline event handlers
  * Further inheritance-oriented improvements in HtmlCompressor class

**1.0** (Mar 19, 2011)
  * Added ability to provide custom implementations of JavaScript and CSS compressors using  `setJavaScriptCompressor()` and `setCssCompressor()` HtmlCompressor methods. YUI compressor is now treated as just one of possible implementations (if none provided YUI will still be used as a default JavaScript compressor). Custom compressors are currently supported only through Java API.
  * Added [Google Closure Compiler](http://code.google.com/closure/compiler/) implementation for inline JavaScript compression (also supported in command line compressor and velocity/jsp tags). Thanks to Lexius for providing code samples.
  * Small part of YUI compressor responsible for CSS was integrated into HTML compressor, so compressing inline CSS now doesn't require any external dependencies.
  * HTML and XML compressors were made much more inheritance-friendly by making all private methods protected and splitting the whole compression process into smaller methods ready for overriding.
  * Newline characters in html and xml files on Linux systems are now replaced with spaces, to be consistent with Windows compression results (file size is not affected)

**0.9.9** (Feb 02, 2011)
  * Fixed bug in HtmlCompressor that causes incorrect compression of big files (if number of preserved blocks exceeds 1000). See [this issue](http://code.google.com/p/htmlcompressor/issues/detail?id=26) for details.


**0.9.8** (Dec 30, 2010)
  * Command line compressor supports YUI compressor v.2.4.4 (jar file must be named yuicompressor-2.4.4.jar and put at the same folder as HtmlCompressor jar)


**0.9.7** (Dec 12, 2010)
  * Fixed bug introduced in v.0.9.4 with command line compressor not processing inline JavaScript and CSS in HTML files.


**0.9.6** (Dec 11, 2010)
  * Added `--preserve-php`, `--preserve-server-script` and `-p <regexp patterns file>` optional parameters to a command line compressor that allow preserving `<?php ... ?>`, `<% ... %>` and custom blocks defined in a file. See front page for more details.
  * `HtmlCompresor` class currently has 2 predefined patterns for most often used custom preservation rules: `PHP_TAG_PATTERN` and `SERVER_SCRIPT_TAG_PATTERN` that can be passed to `setPreservePatterns()` method. See front page for details.
  * Spaces between custom preserved blocks will be collapsed if `setRemoveIntertagSpaces` option is set to `true`.


**0.9.5** (Dec 05, 2010)
  * Fixed problem caused by removing quotes from tag attributes before "`/>`". See [this issue](http://code.google.com/p/htmlcompressor/issues/detail?id=20) for details.
  * Added JUnit tests to the zip bundle


**0.9.4** (Nov 13, 2010)
  * Improved extra space removal around conditional comments. See [this issue](http://code.google.com/p/htmlcompressor/issues/detail?id=16#c4) for details.
  * Compressor taglib TLD file is now bundled with JAR file to eliminate unnecessary web.xml configuration in JSP 2.0 and above (TLD can still be downloaded as separated file for older versions). See [this issue](http://code.google.com/p/htmlcompressor/issues/detail?id=19) for details.
  * Command line compressor now has more clear error message if required YUI compressor jar file is missing. See [this issue](http://code.google.com/p/htmlcompressor/issues/detail?id=17#c3) for details.


**0.9.3** (Sep 02, 2010)
  * Fixed issue with conditional comments. See [this issue](http://code.google.com/p/htmlcompressor/issues/detail?id=16) for details.
  * Because of the changes in v.0.9.2 related to `ErrorReporter`, YUI compressor dependency has become mandatory. In this release this dependency was made optional again,  but at a cost of moving default `ErrorReporter` implementation into external class `DefaultErrorReporter` (see front page for usage example). If no `ErrorReporter` is provided, a null pointer exception will be thrown in case of an error in JavaScript.


**0.9.2** (Aug 27, 2010)
  * Added ability to set custom preservation rules for HTML compressor by using `setPreservePatterns()` method (see the front page for usage examples)
  * Implemented default `ErrorReporter` for YUI compressor that would output any errors during JavaScript compression to `System.err` stream (it used to throw null pointer exceptions in case of JavaScript error). You can also provide your own `ErrorReporter` implementation by using `setYuiErrorReporter()` method (see javadocs for details)


**0.9.1** (Feb 06, 2010)
  * Fixed problem with JavaScript blocks wrapped with CDATA not being compressed. See [this issue](http://code.google.com/p/htmlcompressor/issues/detail?id=8) for details.


**0.9** (Dec 20, 2009)
  * Inline event handlers (`onclick`,`onload`, etc) now are getting preserved similar to `<pre>`,`<script>` and others, as they might contain javascript that will break tag boundaries detection.
  * Added space removal around equals sign inside tags (`<p id = "p1">` => `<p id="p1">`) to default behavior for HTML and XML compressors.
  * Multiple spaces inside XML tags replaced with single spaces as default behavior.
  * Improved tag preserving algorithm for `<pre>`,`<script>` and others to preserve only their content, so these tags themselves are treated as regular tags and getting compressed where appropriate (it used to preserve both content and tags).


**0.8.2** (Dec 05, 2009)
  * Added ending space removal inside tags (`<br /><p >` => `<br/><p>`) to default behavior for HTML and XML compressors.


**0.8** (Nov 11, 2009)
  * Added ability to call HTML and XML compressors from a command line. Please see the front page for details.


**0.7.2** (Oct 01, 2009)
  * [Bug fix](http://code.google.com/p/htmlcompressor/issues/detail?id=3)


**0.7.1** (Sep 23, 2009)
  * [Bug fix](http://code.google.com/p/htmlcompressor/issues/detail?id=2)


**0.7** (Aug 10, 2009)
  * Performance tweaks


**0.6** (Jul 10, 2009)
  * Improved algorithm of removing unnecessary quotes from tag attributes for `HtmlCompressor` so there is no performance impact when enabled (`setRemoveQuotes(true)`)


**0.5** (Jul 09, 2009)
  * Added Velocity compressor directives `#compressHtml`, `#compressXml`, `#compressJs`, `#compressCss` that allow compressing selected blocks within Velocity templates. Directives support all attributes from corresponding compressor classes.
  * Fixed bug with not preserving conditional IE comments in html (`<!--[if IE]><![endif]-->`) when `removeComments` is `true`


**0.4** (Mar 20, 2009)
  * Added `removeIntertagSpaces` parameter to HTML compressor that will remove inter-tag whitespaces (default is `false`)
  * Added `removeComments` parameter to HTML and XML compressors to control comments removal (default is `true`, was always on in previous releases)
  * Added `removeMultiSpaces` parameter to HTML compressor to control removal of multiple whitespace characters (default is `true`, was always on in previous releases)
  * Taglib compressor tags now support all attributes from corresponding compressor classes, so you have full control over compression parameters from both java and jsp.
  * Added new jsp tags `<compress:js>` and `<compress:css>` that will call YUI Compressor directly (these tags also support attributes)
  * Added `enabled` parameter to HTML and XML compressors which will completely bypass any compression if set to `false` (default is `true`)
  * Dependency libs are now included in the source package, JAR file is made Java 5 compatible.


**0.3** (Mar 13, 2009)
  * Added `removeQuotes` parameter to HTML compressor that will remove unnecessary quotes from tag attributes (dafault is `false`)
  * Performance tweaks


**0.2** (Mar 09, 2009)
  * Added XmlCompressor
  * Created JSP taglib for `<compress:html>` and `<compress:xml>` tags


**0.1** (Mar 07, 2009)
  * Initial release