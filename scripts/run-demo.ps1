$jar = Resolve-Path "$PSScriptRoot\..\metrics-backend\target\metrics-backend-0.0.1-SNAPSHOT.jar"
java -jar $jar
