Android Quality Verifier
===============
[![Build Status][11]][13] [![Download][14]][15] [![gitcheese.com][16]][17]

Static code analysis plugin for Android projects.
This is a fork of [the original android-check plugin][1], which implements a really useful concept.

Usage
-----
This plugin is available in jCenter. It attaches itself to the `check` task if it finds it (that is, you don't use the `plugins` block and you apply either the application or library Android plugins first) - otherwise you'll need to execute the corresponding tasks manually when desired: `androidCheckstyle` for [CheckStyle][3], `androidSpotbugs` for [SpotBugs][4], `CPDTask` for [CPD][5] and `androidPmd` for [PMD][6].

Configuration
-------------

### Install

##### Add to main build.gradle:
```gradle
buildscript {
    ...
    dependencies {
        ...
        classpath 'pt.simdea.verifier:verifier:3.6.0'
        ...
    }
    ...
}
```

##### add to app build.gradle:
```gradle
apply plugin: 'pt.simdea.verifier'
apply plugin: 'com.android.application'
...
dependencies {
    ...
    implementation 'pt.simdea.verifier:verifier-annotations:0.0.3'
    ...
}

```
### Recommended

The default one.

### Customized

```groovy
// Configuration is completely optional, defaults will be used if not present
check {
  // Do absolutely nothing, default: false
  skip true/false
  // Fails build if a violation is found, default: true
  abortOnError true/false //Ignored if all per-tool confs are set to abortOnError false (see below)
  
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
  
  // Cpd configuration
  cpd {
    // Same options as Checkstyle
  }
  
  // FindBugs configuration
  spotbugs {
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

  - [Jorge Antonio Diaz-Benito Soriano][9]

This fork is owned and maintained by [Simdea][2].

License
=======

See [LICENSE.txt][7].

Original work licensed under [MIT license][8].

[1]: https://github.com/stoyicker/android-check-2
[2]: http://simdea.pt/
[3]: http://checkstyle.sourceforge.net/
[4]: https://spotbugs.github.io
[5]: https://pmd.github.io/pmd-5.7.0/usage/cpd-usage.html
[6]: https://pmd.github.io/
[7]: LICENSE.txt
[8]: https://github.com/noveogroup/android-check/blob/master/LICENSE.txt
[9]: https://www.linkedin.com/in/jorgediazbenitosoriano
[10]: https://spotbugs.github.io
[11]: https://travis-ci.org/Simdea/android-quality-verifier.svg?branch=master
[12]: https://travis-ci.org/Simdea/android-quality-verifier.svg?branch=dev
[13]: https://travis-ci.org/Simdea/android-quality-verifier
[14]: https://api.bintray.com/packages/simdea/android-quality-verifier/pt.simdea.verifier/images/download.svg
[15]: https://bintray.com/simdea/android-quality-verifier/pt.simdea.verifier/_latestVersion
[16]: https://s3.amazonaws.com/gitcheese-ui-master/images/badge.svg
[17]: https://www.gitcheese.com/donate/users/1757083/repos/87924699