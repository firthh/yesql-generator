# yesql-generator

Creates sql script for basic CRUD operations for a database. The SQL script is intended for use with YeSQL.


## TODO
 - Dispatch to different SQL queries to describe tables based on the database
 - Support more than just MySQL

## Usage

Put a `:database` key within you `project.clj` file.

Run:

`$lein yesql-generator`

OR

`$lein yesql-generator filename.sql`

## Install
User-level plugins:

Put `[yesql-generator "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your `:user`
profile.

Project-level plugins:

Put `[yesql-generator "0.1.0-SNAPSHOT"]` into the `:plugins` vector of your project.clj.


## License

Copyright © 2016 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
