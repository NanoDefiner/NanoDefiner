=======================================================
NanoDefiner e-tool standalone installation instructions
=======================================================

------------
Requirements
------------

- Java 8
- MySQL (using other DBMS may be possible but is currently not supported)

------------
Installation
------------

- extract files into directory
- adjust configuration files in config/:
	- application.properties: general properties, make sure to adjust mail, server
		and path settings
	- hibernate.properties: You should only have to change this file if you are
		not using MySQL
	- hikari.properties: database settings, at least the username and password
		should be changed

-----
Start
-----

- run start_server.sh to start the application
- access the application at the location specified in server.baseName (by
	default http://localhost:8080/NanoDefiner)
- you can login using the username admin and password admin (please change the
	password after first login!)

---------------
Troubleshooting
---------------

Can't access NanoDefiner e-tool after start:
	Check the log output and make sure the settings server.contextPath,
	server.port and server.baseName are correct; to bind to certain ports you will
	need admin privileges in your OS

Problem with special characters:
	Make sure your DBMS is properly configured to work with UTF-8, for MySQL make sure you have the
	following in your configuration file:
	[mysqld]
  collation-server = utf8_general_ci
  init-connect='SET NAMES utf8'
  character-set-server = utf8
