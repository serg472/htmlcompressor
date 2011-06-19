
PREREQUISITES:
	- install JDK6
	- install Maven2 or Maven3
	- install GnuPG (http://www.gnupg.org/download/), required to sign up project binaries (before you start with GPG and Maven2 please refer to https://docs.sonatype.org/display/Repository/How+To+Generate+PGP+Signatures+With+Maven)

PROJECT BUILD:
  I've created shell scripts to build project quickly. Use *.bat for windows, *.sh for linux.
  The build proces generates binaries, javadocs, sources, site, md5 and sha1 files.
	- build-local: build and install project binaries to the local repo (~/.m2)
	- build-central: build and install project binaries to maven central repository (NOTE: read before you deploy your project to maven central repo https://docs.sonatype.org/display/Repository/Sonatype+OSS+Maven+Repository+Usage+Guide)
	- build-release: build and install project binaries to maven central repository (NOTE: this required you to have committed to svn your pom file and project code like maven structure)

Project binaries (can be found under target folder or ~/.m2/repository/com/googlecode/htmlcompressor/htmlcompressor/1.3.1):
	- htmlcompressor-1.3.1.jar: contains compiled classes, main class for jar, manifest (just like the original jar file)
	- htmlcompressor-1.3.1-javadoc.jar: contains source javadocs
	- htmlcompressor-1.3.1-site.jar: contains project site (includes project description, dependencies, documentation like javadocs and reports, project license, source code repository, etc.)
	- htmlcompressor-1.3.1-sources.jar: contains project sources
	- htmlcompressor-1.3.1-test-javadoc.jar: contains test classes javadocs
	- htmlcompressor-1.3.1-test-sources.jar: contains test classes sources
	- every file has md5 and sha1 hashes (available only under ~/.m2)

USEFULL INFORMATION:
	- mvn idea:idea to create IntellijIDEA project files
	- mvn eclipse:eclipse to create Eclipse project files
	- you can find a lot of other usefull maven plugins for your project
	
CHANGELOG:
	- migrated to maven2 (didn't try it on maven3);
	- libraries version updated;
	- fixed tests as there was non-UTF8 char that prevents from compilation;
	- fixed tests:  changed resource path to relative one;
