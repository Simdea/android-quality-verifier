Android Quality Verifier
===============
[![Build Status][11]][13] [![Download][14]][15] [![gitcheese.com][16]][17]

Static code analysis plugin for Android projects.
This is a fork of [the original android-check plugin][1], which implements a really useful concept.

Usage
-----
This plugin is available in jCenter. It attaches itself to the `check` task if it finds it (that is, you don't use the `plugins` block and you apply either the application or library Android plugins first) - otherwise you'll need to execute the corresponding tasks manually when desired: `androidCheckstyle` for [CheckStyle][3], `androidSpotbugs` for [SpotBugs][4], `CPDTask` for [CPD][5], `androidPmd` for [PMD][6], `androidDetekt` for [Detekt][detekt-link], and `androidKtlint` for [Ktlint][ktlint-link].

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
  
  // Detekt configuration (for Kotlin)
  detekt {
    // Completely skip Detekt, default: false
    skip true/false

    // Fails build if Detekt rule violation is found, default: true
    abortOnError true/false

    // Configuration file for Detekt (YML). 
    // Default lookup: <project_path>/config/detekt.yml, 
    // then <project_path>/<module_path>/config/detekt.yml, 
    // then plugin's internal 'conf-default.yml'.
    // To use Detekt's own defaults, ensure no custom file is found and 
    // the plugin's 'conf-default.yml' is removed or made inaccessible.
    config 'path/to/your/detekt.yml'

    // Output file for XML reports (Checkstyle format).
    // Default: build/outputs/detekt/detekt.xml in the module.
    reportXML new File(project.buildDir, 'path/where/you/want/detekt.xml')

    // Output file for HTML reports.
    // Default: build/outputs/detekt/detekt.html in the module.
    reportHTML new File(project.buildDir, 'path/where/you/want/detekt.html')
  }
  
  // Ktlint configuration (for Kotlin)
  ktlint {
    // Completely skip Ktlint, default: false
    skip true/false

    // Fails build if Ktlint rule violation is found, default: true
    abortOnError true/false

    // Ktlint uses .editorconfig files for rule settings, discovered automatically 
    // from the project structure. This 'config' property is for compatibility 
    // with the plugin's structure but is not the primary way to configure Ktlint rules.
    // config 'path/to/.editorconfig' // Generally not needed.

    // Output file for XML reports (Checkstyle format).
    // Default: build/outputs/ktlint/ktlint.xml (points to the main source set report).
    reportXML new File(project.buildDir, 'path/where/you/want/ktlint-main.xml')

    // Output file for HTML reports.
    // Default: build/outputs/ktlint/ktlint.html (points to the main source set report).
    reportHTML new File(project.buildDir, 'path/where/you/want/ktlint-main.html')
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
[detekt-link]: https://detekt.dev/
[ktlint-link]: https://ktlint.github.io/