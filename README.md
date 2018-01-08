# jTerm - the java serial terminal

Java Serial Terminal V 0.12


Working both Linux (Mint 18.2) and Windows (Windows 10) with java version "1.8.0_151". This terminal program is inspired from hterm <http://www.der-hammer.info/terminal/index.html> (great program but closed source)

### features
- continuous scan of available Serial Ports to update list of Ports.
- clean and small GUI overhead
- ASCII and HEX view of transmitted and received data
- ASCII and HEX input for transmission
- transmitted bytes could be appended by LF, CR+LF, CR, or modified by a script written in javascript language (used for example for checksum calculation)
- time measurement between received bytes
- shortcuts with individual line endings/scripts

# Screenshot
![Screenshot](jTerm_screenshot.png?raw=true "Screenshot V0.12")

# Changelog
- 0.10
  - initial Commit
- 0.11
  - font change to bold for visibility
  - CR+LF linebreak option working
  - Input textfield for ASCII and HEX input modified
- 0.12
  - javascript editor added for custom behaviour on enter commands
  - treeview added for shortcut commands with import export
  - window size stored and restored in properties file

# Used libraries
Connection to serial interfaces by JSSC v2.8.0 (<https://github.com/scream3r/java-simple-serial-connector>).<br />
Using udev library for hotplug detection (<https://github.com/Zubnix/udev-java-bindings>).<br />
Formatted text window uses RitchtextFX (<https://github.com/FXMisc/RichTextFX>).<br />
Icons from (<http://www.iconarchive.com/show/leaf-mimes-icons-by-untergunter.html>).<br />


