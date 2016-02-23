[![Build Status](https://travis-ci.org/haffla/stream-compare.svg?branch=master)](https://travis-ci.org/haffla/stream-compare)
[![Codacy Badge](https://api.codacy.com/project/badge/grade/9929064cb5a64dcfa343de348204f8fe)](https://www.codacy.com/app/jakobpupke_2054/stream-mashup)
##stream-mashup

What you need:

- Java 8 (It should work with 7, too, but I did not test it)
- [sbt](http://www.scala-sbt.org/)
- [Node.js](https://nodejs.org/en/)
- PostgreSQL

Create a database called `playdb` with user `play` and `somepassword` (see `conf/db.txt`).

`export PLAY_DB_PASS='somepassword'`

Install Node dependencies and start the Node development server with react-hot-reload:

```
npm install
npm start
```

Compile and run the Play application:

```
sbt run
```

Point your browser to `http://localhost:9000`
