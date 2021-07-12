[![Build](https://github.com/joomcode/colonist/workflows/Build/badge.svg)](https://github.com/joomcode/colonist/actions)

Colonist
========

Compile time class discovery framework for JVM languages.

Why?
----

Sometimes you may want to find all classes that match some condition and perform
an action on every such class. On Android it could be quite a challenging task
since there's no documented way to enumerate all classes from an APK using
reflection. Moreover, solving this problem with reflection would affect 
application performance negatively.

Colonist offers a solution by moving class discovery step to the compile-time.
And at the runtime you just need to iterate through a precomputed list of
classes and do whatever you need.

Usage
-----

### Attention

The library is experimental. Its API isn't stable yet and may change dramatically.

### Configuration

```groovy
buildscript {
  repositories {
    mavenCentral()
  }

  dependencies {
    classpath 'com.joom.colonist:colonist-gradle-plugin:0.1.0-alpha12'
  }
}

// For Android projects.
apply plugin: 'com.android.application'
apply plugin: 'com.joom.colonist.android'

// For other projects.
apply plugin: 'java'
apply plugin: 'com.joom.colonist'
```

### Colonies

Colonist requires three steps to be defined in order to describe how to deal
with classes (settlers): selection, production, and acceptance. And that's what
a colony is for.

The colony is an annotation that, in its turn, is annotated with `@Colony`
meta-annotation. Moreover, the colony must be annotated with another three
annotations that define steps of settlers discovery.

#### Selection

The first step in class discovery is selection that specifies a common attribute
classes must have to be discovered. At the moment there're just two ways to
select classes:

- `@SelectSettlersByAnnotation` discovers all classes annotated with a specified
annotation class.
- `@SelectSettlersBySuperType` selects all classes that extend a given class. 

#### Production

The second step is production and it specifies what the discovered classes are
going to be converted to:

- `@ProduceSettlersAsClasses` is an identity conversion. In other words it
produces the same classes it receives.
- `@ProduceSettlersViaCallback` invokes a user-defined callback for every
discovered class and uses its return values as the result.
- `@ProduceSettlersViaConstructor` instantiates classes using their default
constructors. The default constructors for all discovered classes **must exist**. 

#### Acceptance

The third and the last step is acceptance and it's responsible for collecting
the results of the previous two steps (i.e. settlers):
- `@AcceptSettlersAndForget` just ignores all the settlers. This behavior may be
useful if settlers are autonomous and just need to be created in order to do
their stuff.
- `@AcceptSettlersViaCallback` invokes a user-defined callback for every 
produced settler.

#### All together

It's much easier to understand what's going on using a simple example. Let's
write a class that finds all plugins and initializes them. And we'll start with
a plugin interface:

```kotlin
interface Plugin {
  fun initialize(context: Context)
}
```

We'll be looking only for plugin classes annotated with a particular annotation
that we need to define:

```kotlin
@Target(AnnotationTarget.CLASS)
annotation class AutoPlugin
```

Then we need a couple of plugin implementations that we'll use in our sample.
Let's make it a bit more complex and also create a base plugin implementation: 

```kotlin
abstract class BasePlugin(private val name: String) : Plugin {
  override fun initialize(context: Context) = println("Initialize $name")
}

@AutoPlugin
class Plugin1 : BasePlugin("Plugin1")

@AutoPlugin
class Plugin2 : BasePlugin("Plugin2")
```

Now we need to define a colony annotation that will discover and instantiate all
existing plugins:

```kotlin
@Colony
@SelectSettlersByAnnotation(AutoPlugin::class)
@ProduceSettlersViaConstructor
@AcceptSettlersViaCallback
@Target(AnnotationTarget.CLASS)
annotation class PluginColony
```

Some entity should be responsible for plugin initialization so let's assume we
have a plugin manager:

```kotlin
@PluginColony
class PluginManager(private val context: Context) {
  init {
    Colonist.settle(this)
  }

  @OnAcceptSettler(colonyAnnotation = PluginColony::class)
  fun onAcceptPlugin(plugin: Plugin) {
    plugin.initialize(context)
  }
}
```

After running the application you'll the following lines in the log but the order may differ:
```
Initialize Plugin1
Initialize Plugin2
```

License
-------

    Copyright 2021 SIA Joom

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
