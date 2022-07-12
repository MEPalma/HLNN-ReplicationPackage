#!/bin/bash

FILES="src/main/python/saved_model_losses/*"
for f in $FILES
do
    fname="$(basename -- $f)"
    fnamenoext="${fname%.*}"

    echo "$fnamenoext"

        if [[ $fname == java* ]];
            then
            ./gradlew JavaEvaluator -Pargs="perFileAcc ../saved_model_losses/$fname"
            echo "$fname"

        elif [[ $fname == kotlin* ]];
            then
            ./gradlew KotlinEvaluator -Pargs="perFileAcc ../saved_model_losses/$fname"
            echo "$fname"

        elif [[ $fname == python* ]];
            then
            ./gradlew Python3Evaluator -Pargs="perFileAcc ../saved_model_losses/$fname"
            echo "$fname"

        else
            echo "Invalid $fname"
        fi

done

