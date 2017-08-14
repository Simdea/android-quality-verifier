Android Quality Verifier
===============
[ ![Download](https://api.bintray.com/packages/simdea/android-quality-verifier/pt.simdea.verifier/images/download.svg?version=3.5.8) ](https://bintray.com/simdea/android-quality-verifier/pt.simdea.verifier/3.5.8/link) [![gitcheese.com](https://s3.amazonaws.com/gitcheese-ui-master/images/badge.svg)](https://www.gitcheese.com/donate/users/1757083/repos/87924699)

Static code analysis plugin for Android projects.
This is a fork of [the original android-check plugin][1], which implements a really useful concept.

Build status
------------

| Master   | [![Build Status](https://travis-ci.org/Simdea/android-quality-verifier.svg?branch=master)](https://travis-ci.org/Simdea/android-quality-verifier) |
|----------|-------------|
| **Dev** | [![dev](https://travis-ci.org/Simdea/android-quality-verifier.svg?branch=dev)](https://travis-ci.org/Simdea/android-quality-verifier) |

Usage
-----
This plugin is available in jCenter. It attaches itself to the `check` task if it finds it (that is, you don't use the `plugins` block and you apply either the application or library Android plugins first) - otherwise you'll need to execute the corresponding tasks manually when desired: `androidCheckstyle` for [CheckStyle][3], `androidFindbugs` for [FindBugs][4], `CPDTask`  for [CPD][5] and `androidPmd` for [PMD][6].

Configuration
-------------

### Install

##### Add to main build.gradle:
```gradle
buildscript {
    ...
    dependencies {
        ...
        classpath 'pt.simdea.verifier:verifier:3.5.8'
        ...
    }
    ...
}
```

##### add to app build.gradle:
```gradle
apply plugin: 'pt.simdea.verifier'
```
### Recommended

The default one.

### Customized

```js
// Configuration is completely optional, defaults will be used if not present
check {
  // Do absolutely nothing, default: false
  skip true/false
  // Fails build if a violation is found, default: true
  abortOnError true/false. //Ignored if all per-tool confs are set to abortOnError false (see below)
  
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

  - [Jorge Antonio Diaz-Benito Soriano][9]

This fork is owned and maintained by [Simdea][2].

License
=======

See [LICENSE.txt][7].

Original work licensed under [MIT license][8].

[1]: https://github.com/stoyicker/android-check-2
[2]: http://simdea.pt/
[3]: http://checkstyle.sourceforge.net/
[4]: http://findbugs.sourceforge.net/
[5]: https://pmd.github.io/pmd-5.7.0/usage/cpd-usage.html
[6]: https://pmd.github.io/
[7]: LICENSE.txt
[8]: https://github.com/noveogroup/android-check/blob/master/LICENSE.txt
[9]: https://www.linkedin.com/in/jorgediazbenitosoriano
