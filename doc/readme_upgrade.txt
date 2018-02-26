=======================================
NanoDefiner e-tool upgrade instructions
=======================================

- stop the application (simply hit ctrl-c in the shell of the running process
	or use stop_server.sh – the latter only works if the WAR is called
	NanoDefiner.war)
- make a backup of the files in the installation directory
- copy the contents of the new ZIP archive into the installation directory
- make sure to follow version-specific upgrade instructions
- start the application using start_server.sh

==========================
Upgrading to version 1.0.0
==========================

- make sure to check the configuration file, many options were added
- nanodefiner.ANALYSIS_FILE_DIRECTORY was renamed to server.data_directory
