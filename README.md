[![Build Status](https://travis-ci.org/MichaelRocks/colonist.svg?branch=develop)](https://travis-ci.org/MichaelRocks/colonist)

Colonist
==========

Compile time object discovery framework for JVM languages. Early alpha! Documentation is coming…

Usage
-----

### Configuration

```groovy
buildscript {
  repositories {
    jcenter()
  }

  dependencies {
    classpath 'io.michaelrocks:colonist:colonist-gradle-plugin:0.1.0'
  }
}

// For Android projects.
apply plugin: 'com.android.application'
apply plugin: 'io.michaelrocks.colonist.android'

// For other projects.
apply plugin: 'java'
apply plugin: 'io.michaelrocks.colonist'
```

License
-------

    Copyright 2019 Michael Rozumyanskiy

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
