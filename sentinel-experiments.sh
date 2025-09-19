#!/usr/bin/env bash
export JAVA_HOME=/usr/lib/jvm/java-19-openjdk-amd64/
export PATH=$JAVA_HOME/bin:$PATH

mvn clean package
java -cp //home/jerome/Documents/Assistant/Recherche/joular-scripts/sentinel-experiments/target/sentinel-experiments-1.0-SNAPSHOT-jar-with-dependencies.jar be.unamur.snail.Main "$@"
