![Dumbo](https://sweepyoface.github.io/dumbo-bukkit/logo.png)

![Build status](https://api.travis-ci.org/sweepyoface/dumbo-bukkit.svg?branch=master)
![Current release](https://img.shields.io/github/release/sweepyoface/dumbo-bukkit.svg)
![License](https://img.shields.io/github/license/sweepyoface/dumbo-bukkit.svg)

This plugin is a simple utility to dispense Qball quotes based on [this list](https://github.com/sweepyoface/dumbo/blob/master/quotes.yml).

| Command | Permission | Description
| --- | --- | --- |
| `/dumbo` or `.dumbo` in chat | dumbo.quote | Broadcasts a random Qball quote in the chat. |
| `/dumbo reload` | dumbo.reload | Reload the configuration file. |
| `/dumbo version` | dumbo.version | Get the plugin version. |


# Downloading
You can download the latest build from [Jenkins](https://ci.sweepy.pw/job/Dumbo-Bukkit/).

# Building
1. Install [Apache Maven](https://maven.apache.org/).
2. Clone this repository.
3. Run `mvn clean install`.
4. The compiled jar will be in the `target` directory.