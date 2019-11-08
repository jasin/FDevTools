# FDevTools
__The first devtools plugin for Nukkit!__

# Features
* You can load plugin using source code (e.g. *.java).
* If you don't have JDK,just download tools.jar from [Here](https://www.dropbox.com/s/vjvcebljpk6qlmj/tools.jar?dl=0) and put it into FDevTools folder then it works well too.
* You can pack the plugin into jar and relese it easily.

## Attention
This version of FDevTools follows the Maven standard directory layout. [Here](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html)

Your source plugin should follow this format:

1. Make a dir in plugins folder (e.g. MyPlugin_src).
2. Plugin.yml goes into resources (e.g. src/main/resources/plugin.yml)
3. Source code goe into java (e.g. src/main/java/your/package/*.java)

Now your plugin should looks like this:
>MyPlugin_src
├src
    ├main
        ├java
        └resources

# Commands
* `makeplugin <PluginName>` - Compress the plugin into jar format(It must loaded by FDevTools).
