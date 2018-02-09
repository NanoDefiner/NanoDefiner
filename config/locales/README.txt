To add/change languanges, add/change a file called messages_lang_variant (valid file types are xml,
properties and csv), e.g. messages_en_US.csv. When a locale string is not found for a language, it
will be taken from messages.csv (therefore, the en_GB file is empty since it is equivalent to the
messages.csv, but is required to display the language in the user profile).

Ignore the empty .properties files which are generated, these are necessary for the CSV files to be
picked up. If both a CSV and properties/XML file exist for a language, the CSV file takes
precedence.

The messages in these files follow the message format described here:
https://docs.oracle.com/javase/8/docs/api/java/text/MessageFormat.html

This format requires you to:

- escape single quotes (whenever you need a single quote, add two single quotes)
- escape curly braces not meant to be pattern strings (e.g. to write "{", include "'{'" in the string)
