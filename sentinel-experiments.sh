#!/usr/bin/env bash
mvn clean package
java -cp //home/jerome/Documents/Assistant/Recherche/joular-scripts/sentinel-experiments/target/sentinel-experiments-1.0-SNAPSHOT-jar-with-dependencies.jar be.unamur.snail.Main "$@"
