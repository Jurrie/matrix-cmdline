# matrix-cmdline

This is a simple command-line application that allows for sending messages to a room on a Matrix server.
It is useful for scripts, as the standard input is forwarded to the Matrix server.
It is not possible to *receive* messages, only to send them.

Please see the `--help` command-line option for a list of available command-line parameters.

## Configuration file
Support for a configuration file exists. It saves the user from specifying all connection settings every time.
The default location is `~/.matrix-cmdline.conf`, though a different configuration file can be specified using `-c`.
All command-line parameters that start with `--` are also available as configuration options.
Just use the same name, but the leading `--` should be stripped.

### Example configuration file
Below could be the contents of `~/.matrix-cmdline.conf`. An invocation of `matrix-cmdline` would then not need any parameters, and would send the contents of the standard input into `#MI6:matrix.org`.

```
server=https://matrix.org
username=jamesbond
password=shakennotstirred
room=#MI6:matrix.org
verbose=true
```

## The `--max-chars` option
In order not to flood the room with a large message, there is a maximum number of characters count for a message.
The default value is (a rather arbitrary) 512 characters. Please note that newlines also count as characters.

If you would like to send arbitrary long messages, you could set the `--max-chars` option to `-1`.
This would disable the maximum number of characters for a message, and will send a message on every newline read.

## Joining a room
This client will not create a room. You will first have to create it manually, and either make the room public, or invite the account that's used with matrix-cmdline.
If the room is public, matrix-cmdline will automatically join it when sending the first message.
If the room is invite-only, matrix-cmdline will automatically accept an invite for the room when sending the first message. 
