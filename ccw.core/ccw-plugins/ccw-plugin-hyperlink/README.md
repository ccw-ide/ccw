# ccw-plugin-hyperlink

This Counterclockwise plugin adds hyperlinks to the Clojure Editor for String literals whose content represent:
- java resources (relative to the current file, or absolute from the start of the classpath) in the project sources/resources/tests folders
- absolute or relative (to the current file) filesystem URLs

If several hyperlink candidates, a choice list is displayed.

This plugin's state is ready for use.

## Install

The `~/.ccw/` folder is where Counterclockwise searches for User Plugins.

It is recommended to layout User Plugins inside this folder by mirroring Github's namespacing. So if you clone ccw-ide/ccw-plugin-hyperlink, you should do the following:

- Create a folder named `~/.ccw/ccw-ide/`
- Clone this project from `~/.ccw/ccw-ide/`

        mkdir -p ~/.ccw/ccw-ide
        cd ~/.ccw/ccw-ide
        git clone https://github.com/ccw-ide/ccw-plugin-hyperlink.git

- If you have already installed ccw-plugin-manager (https://github.com/ccw-ide/ccw-plugin-manager.git), then type `Alt+U S` to re[S]tart User Plugins (and thus ccw-plugin-hyperlink will be found and loaded)
- If you have not already installed ccw-plugin-manager, restart your Eclipse / Counterclockwise/Standalone instance.

## Usage

You can discover Hyperlinks by typing `Cmd` in a clojure editor and moving your mouse. Hyperlink candidates should be underlined when you hover over them.

- `Cmd` + mouse move to discover hyperlinks
- `Cmd` + mouse click to open hyperlink
- F3 when cursor is in the hyperlink text to open it

## License

Copyright © 2009-2015 Laurent Petit

Distributed under the Eclipse Public License, the same as Clojure.

