Android Quality Verifier
===============

Static code analysis plugin for Android projects.
This is a fork of [the original android-check plugin][1], which implements a really useful concept.

<!---
Build status
------------

### master [![master](https://travis-ci.org/stoyicker/android-check-2.svg?branch=master)](https://travis-ci.org/stoyicker/android-check-2)
### dev [![dev](https://travis-ci.org/stoyicker/android-check-2.svg?branch=dev)](https://travis-ci.org/stoyicker/android-check-2)
-->

Usage
-----

[ ![Download](https://api.bintray.com/packages/simdea/android-quality-verifier/pt.simdea.verifier/images/download.svg) ](https://bintray.com/simdea/android-quality-verifier/pt.simdea.verifier/_latestVersion)

This plugin is available in<!--- [the Gradle Plugin Portal](https://plugins.gradle.org/plugin/org.stoyicker.android-check) and --> jCenter. It attaches itself to the `check` task if it finds it (that is, you don't use the `plugins` block and you apply either the application or library Android plugins first) - otherwise you'll need to execute the corresponding tasks manually when desired: `androidCheckstyle` for CheckStyle, `androidFindbugs` for FindBugs and `androidPmd` for PMD.

Configuration
-------------

### Install

##### Add to main build.gradle:
```gradle
buildscript {
    ...
    dependencies {
        ...
        classpath 'pt.simdea.verifier:verifier:(last version)'
        ...
    }
    ...
}
```

##### add to app build.gradle:
```
apply plugin: 'pt.simdea.verifier'
```
### Recommended

The default one.

### Customized

```gradle
// Configuration is completely optional, defaults will be used if not present
check {
  // Do absolutely nothing, default: false
  skip true/false
  // Fails build if a violation is found, default: true
  abortOnError true/false. Ignored if all per-tool confs are set to abortOnError false (see below)
  // Checkstyle configuration
  checkstyle {
    // Completely skip CheckStyle, default: false
    skip true/false

    // Fails build if CheckStyle rule violation is found, default: false
    abortOnError true/false

    // Configuration file for CheckStyle, default: <project_path>/config/checkstyle.xml, if non-existent then <project_path>/<module_path>/config/checkstyle.xml, if non-existent then plugin/src/main/resources/checkstyle/conf-default.xml
    config 'path/to/checkstyle.xml'

    // Output file for XML reports, default: new File(project.buildDir, 'outputs/checkstyle/checkstyle.xml')
    reportXML new File(project.buildDir, 'path/where/you/want/checkstyle.xml')

    // Output file for HTML reports, default: not generated
    reportHTML new File(project.buildDir, 'path/where/you/want/checkstyle.html')
  }
  // FindBugs configuration
  findbugs {
    // Same options as Checkstyle
  }
  // PMD configuration
  pmd {
    // Same options as Checkstyle
  }
  
  // Lint configuration
    lint {
      // Same vars of android lint options
    }
}
```

Developed By
============

The original version of this plugin was developed by:

  - [Jorge Antonio Diaz-Benito Soriano](https://www.linkedin.com/in/jorgediazbenitosoriano)

This fork is owned and maintained by [Simdea][2].

License
=======

See [LICENSE.txt](LICENSE.txt).

Original work licensed under [MIT license](https://github.com/noveogroup/android-check/blob/master/LICENSE.txt).

[1]: https://github.com/stoyicker/android-check-2
[2]: http://simdea.pt/
