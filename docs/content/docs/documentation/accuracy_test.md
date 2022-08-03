---
title: 🔍 Accuracy testing
weight: 5
---

# Accuracy Testing

Accuracy testing refers to the accuracy of producing syntax highlighting for each of
the files in the test dataset of each training dataset folds, and the respective
synthetic snippets. As the accuracy of the Brute Force method is always 1 (100%),
accuracy per file is collected with regard to the neural networks approach and Pygments.

## Accuracy Testing -- Neural Networks & Pygments

For each tested language there exists an accuracy test task, which needs to be instructed
which trained model session to load. This information enables it to test each model
trained on each fold with its set of unseen test cases. For Java, the following may be run

```shell
./gradlew JavaEvaluator -Pargs="runPerFileApproachAccNoRam ../saved_model_losses/<model-training-logs-filename>"
```

whereas for Kotlin and Python3 the task name should be changed from `JavaEvaluator` to
`KotlinEvaluator` and `Python3Evaluator`, ensuring the target model logs file
is of a model trained on the same language.

In order to automate this process, a shell script was created to perform accuracy testing
for each trained model, for any of the languages, if it wasn't already tested. This can be
launched by running from the project's root directory:

```shell
./RunAllModelAccs.sh
```

# Accuracy Results Structure

For each language investigated, its results in terms of accuracy and speed are stored in the language's directory: `src/main/resources/<lang>/`.
These metrics are available for each trained DL model, the brute force approach and Pygments.

## Accuracy

For all normalized and filtered test files and snippets stored in each fold of the model's training process, test results list the accuracy of each model (3) with respect to each syntax highlighting coverage task (of which there are four of them: 28, 37, 55 and 66 respectively).

File names are well-formed and carry information about the configuration of the model and coverage task.
Filenames are of the form:

```txt
perFileAcc_<lang>\
    _1_<task-id>\
    _RNNClassifier1\
    _<lang-embs>embs\
    _<lang-id>id\
    _<hidden-units>hd\
    _1hl_<is-bidirectional>bid\
    _<fold>.json
```

Such files consist of a json list of per file accuracy reports. Such reports are of the form:

- `fileId: str`: the `MD5` encoding of the file's raw content
- `isSnippet: bool`: `true` if the file was a snippet, false otherwise
- `bruteAcc: float`: the `[0..1]` accuracy of the brute force model on this file or snippet and coverage task
- `modelAcc: float`: the `[0..1]` accuracy of the DL model on this file or snippet and coverage task
- `pygAcc: float`: the `[0..1]` accuracy of Pygments model on this file or snippet and coverage task
