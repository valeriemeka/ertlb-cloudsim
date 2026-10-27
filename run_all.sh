#!/usr/bin/env bash
# Lance toutes les expériences puis l'analyse. Prérequis : JDK 17+, Maven, Python 3 (numpy, pandas, scipy, matplotlib).
set -e
mvn -q compile
CP=$(mvn -q dependency:build-classpath -Dmdep.outputFile=/dev/stdout):target/classes
java -cp "$CP" ertlb.Main 30 20 1000 5000 results/results_main.csv
for v in 5 10 20 40;       do java -cp "$CP" ertlb.Main 30 $v 1000 5000 results/sens_vm_$v.csv;   done
for t in 250 500 1000 2000; do java -cp "$CP" ertlb.Main 30 20 $t 5000 results/sens_task_$t.csv; done
python3 analysis/analyze.py results/results_main.csv analysis/out
