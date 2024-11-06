# AirJR
This is a Minecraft Java Plugin written in Kotlin. 

This Plugin allows it to play random jump and runs.

## Build the plugin
You need to set the environment variable `PLUGIN_DIR` to the directory the plugin should be exported.
This might be the plugins directory of your server.

To actually build the plugin run:
```bash
gradle build
```

## Commands
Create a new area for a jump and run:
```
/jr_create <name> <x1> <y1> <z1> <x2> <y2> <z2>
```

Show jump and run areas:
```
/jr_list
```

Deletes a jump and run area:
```
/jr_delete <name>
```

Play a jump and run:
```
/jr_play <name>
```

## Data
All of the data is stored as sqlite3 database in `./airJR.sqlite3`