# YALTA project backend based on play-scala-slick-example

YALTA stands for Yet Another Logistics Tracking App

<https://github.com/playframework/play-scala-isolated-slick-example/>

Contains git submodule with common data model code, to properly clone this use
`git clone --recurse-submodules`

## deployment
Latest version available at heroku: https://yalta-backend.herokuapp.com/

Autodeploy is not available yet since GH integration does not work with submodules and it requires workarounds

## how to run 
To run in dev mode use `gradlew runPlay` to run tests `gradlew test` or `gradlew build`

To run in prod mode: 
1) Heroku-style: 
 prepare `$DATABASE_URL` as `postgres://<username>:<password>@<host>/<dbname>`
 prepare `$APP_SECRET` as described [here](https://www.playframework.com/documentation/2.8.x/ApplicationSecret)
 do `gradlew stage` and run `build/stage/main/bin/proper-main`
2) manually set stuff up
 adjust `conf\application.conf` with your db data 
 prepare $APP_SECRET 
 then set `$MAIN_OPTS` to `-Dconfig.resource=production.conf -Dplay.http.secret.key=$APP_SECRET` 
 do `gradlew stage` and run `build/stage/main/bin/main`

## issues

List of all damn issues met during development is [here] (https://github.com/maiksaray/YALTA-backend/wiki/External-issues)
 


 
