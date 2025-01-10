# SMPHook
A non-invasive management software for simple home Minecraft: Java Edition servers.

## IMPORTANT
You must have the [curl](https://github.com/curl/curl) package installed on your device.
If not, you prevent the network features of this software from operating correctly.

At the moment, the only server types supported are:
- Vanilla
- Fabric

More are planned in the future

## Getting Started
Download the jar file found in the releases page or build from source.

It is advised to run the jar file with the `setup` when using this for the first time as you need to configure your `hook.properties` file for this to work.

The default `hook.properties` looks like this:
```properties
#SMPHook Properties (v1.0)
#Fri Jan 10 23:24:07 GMT 2025
jar-file=
max-heap=
min-heap=
network-check-interval=3600000
pretty-print=true
webhook-url=
```

| Key                    | Description                                                              | Optional  | 
|------------------------|--------------------------------------------------------------------------|-----------|
| jar-file               | The Minecraft server jar file to hook to                                 | ❌        |
| mix-heap               | The minimum allocation pool for the server                               | ✅        |
| max-heap               | The maximum allocation pool for the server                               | ✅        |
| network-check-interval | The amount of time between subsequent checks of the network state        | ✅        |
| webhook-url            | The Discord webhook URL used to notify server members of an IP change    | ❌        |

# TODO
- Robust configuration system
- Custom Discord messages for certain events
- Server events
- Server Scripting
