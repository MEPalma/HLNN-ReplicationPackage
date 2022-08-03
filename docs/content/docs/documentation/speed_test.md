---
title: 💨 Speed testing
weight: 6
---

# 💨 Speed testing

Speed tests record the time elapsed for the three approaches to compute syntax highlighting
for any given input file. Although this could be performed per each file in the oracle,
given the high computational costs of the Brute Force solutions, an alternative is to
pre-sample 1k files of increasing computation costs as per the BF approach averaged across
multiple runs.

## Speed Testing -- Brute Force

Brute Force approaches per language can be launched from Gradle, with the following
command, here testing the Java BF strategy

```shell
./gradlew JavaEvaluator -Pargs="runBruteTimeBenchmarker"
```

with the Gradle tasks for `Kotlin` and `Python3` being `KotlinEvaluator` and
`Python3Evaluator` respectively.

## Speed Testing -- Neural Networks

Speed testing for trained neural networks require a completed training sessions hence
having at disposal the log file in `saved_model_losses` and the respective trained
PyTorch models in `saved_models`. The routine will only test one of the folds'
models, as the change in weights of these otherwise static models is not expected
to have an impact on generic the prediction speed; the first fold is chosen. The
following command can be used, referencing the target language and trained model:

```shell
./gradlew JavaEvaluator -Pargs="runModelTimeBenchmarker ../saved_model_losses/<model-training-logs-filename> false"
```

with the Gradle tasks for `Kotlin` and `Python3` being `KotlinEvaluator` and
`Python3Evaluator` respectively.

Alternatively, speed tests for all models trained can be launched by running from
the project's root directory the automation script:

```shell
./RunAllModelTimes.sh
```

## Speed Testing -- Pygments

Speed tests for the Pygments library can be launched in a similar manner to the other
speed test

```shell
./gradlew JavaEvaluator -Pargs="runPygmentBenchmarker"
```

with the Gradle tasks for `Kotlin` and `Python3` being `KotlinEvaluator` and
`Python3Evaluator` respectively.

# Speed Results Structure

All models are tested on all normalized and filtered files of the entire dataset (20000) and all the snippets of all the folds.
The prediction delays are recorded in nanoseconds and repeated 30 times per file or snippet.

- **Brute Force Model**

    Speed benchmarks for the brute force implementation are stored in the single json file: `perFileTimeBrute.json`.
    The structure of this file is a list of file benchmarks of the form:
    - `fileId`: the `MD5` encoding of the file's raw content
    - `nss`: the list of 30 nanoseconds computation delays for producing the highlighting of the file or snippet

- **Pygments**

    Speed benchmarks for the brute force implementation are stored in the single json file: `perFileTimePygments.json`.
    The structure of this file is a list of file benchmarks of the form:
    - `fileId`: the `MD5` encoding of the file's raw content
    - `nss`: the list of 30 nanoseconds computation delays for producing the highlighting of the file or snippet

- **DL Models**

    All models are tested and stored in files named as:

    ```txt
    perFileTimeModel_<lang>\
        _1_<task-id>\
        _RNNClassifier1\
        _<lang-embs>embs\
        _<lang-id>id\
        _<hidden-units>hd\
        _1hl_<is-bidirectional>bid\
        _<fold>.json
    ```
    whilst the content of such files is a list of file benchmarks, of the form:
    - `fileId`: the `MD5` encoding of the file's raw content
    - `nss`: the list of 30 nanoseconds computation delays for producing the highlighting of the file or snippet
