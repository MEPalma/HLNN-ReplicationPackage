---
title: ⚙️ Project setup
weight: 1
---

## Project Setup

This project is written in Kotlin and Python. For the most part Python is utilised for:
GitHub files mining, neural networks training and evaluation, test folds and snippets creation,
and utilising the [Pygments](https://pygments.org) syntax highlighting library. Kotlin instead
implements logic for: preprocessing of GitHub's raw files, brute force algorithms, accuracy
and speed testing for all approaches and HTML or console rendering of syntax highlighted files.

Gradle is utilised for building and running of the project -- with exception of some Python tasks,
and some test automations. The Gradle Wrapper of the project is already supplied, hence this can be
build by running in the project's root directory:

```shell
./gradlew build
```

which will fetch the required dependencies and run all the compilation-required tasks.

Although many JVM versions might produce similar results to the ones obtained, the
recommended install is the following, available on
[Amazon Corretto's GitHub repository](https://github.com/corretto/corretto-jdk/releases)

```
openjdk 16.0.2 2021-07-20
OpenJDK Runtime Environment Corretto-16.0.2.7.1 (build 16.0.2+7)
```

[Conda](https://conda.io) is utilised for the creation of the python environment. All of the packages
and versions required are listed in the Conda environment file `environment.yml`.

It should be noted that the project utilises the machine's default Conda/Python environment. Furthermore,
[PyTorch](https://pytorch.org)'s Cuda integration is installed by default. If this should not be available,
this can be safely changed to PyTorch's non-Cuda installation of the same version; likewise, for any other
Cuda related packages.

The Python's root workspace is located in `src > main > python > highlighter`.

Finally, some automation routines utilise the Bash shell (v 3.*), and the ZIP v 3.0) command-line tool.

## Rebuilding Split Archives

Some files have been broken down into multiple parts in order to comply with the maximum file size of the platform used
for the distribution of this replication package. Such files have the naming structure of the form
```
<file-name>.<extension>__seg*
```
these should be reconstructed before use, which can be achieved by concatenating their contents:

```shell
cat <file-name>.<extension>__seg* > <file-name>.<extension>
```

hence, for example, to rebuild the oracle archive, the following should be run:

```shell
cat oracle.tar.xz_seg* > oracle.tar.xz
```

Files can be split by running:

```shell
split -b 98m <file-name>.<extension> <file-name>.<extension>__seg
```

Please ensure the following sub-structures are kept after rebuilding:

```shell
src/main/resources/<lang-name>/raw/
src/main/resources/<lang-name>/raw/files.json
src/main/resources/<lang-name>/raw/repos.json
src/main/resources/<lang-name>/raw/skipped_files.json
src/main/resources/<lang-name>/raw/skipped_repos.json

src/main/resources/<lang-name>/oracle/
src/main/resources/<lang-name>/oracle/jhetas.json
src/main/resources/<lang-name>/oracle/jhetas_clean.json
src/main/resources/<lang-name>/oracle/jhetas_filtered.json
src/main/resources/<lang-name>/oracle/logs.txt

src/main/resources/<lang-name>/folds/
src/main/resources/<lang-name>/folds/fold0_snippets.json
src/main/resources/<lang-name>/folds/fold0_testing.json
src/main/resources/<lang-name>/folds/fold0_training.json
src/main/resources/<lang-name>/folds/fold0_validation.json
src/main/resources/<lang-name>/folds/fold1_snippets.json
src/main/resources/<lang-name>/folds/fold1_testing.json
src/main/resources/<lang-name>/folds/fold1_training.json
src/main/resources/<lang-name>/folds/fold1_validation.json
src/main/resources/<lang-name>/folds/fold2_snippets.json
src/main/resources/<lang-name>/folds/fold2_testing.json
src/main/resources/<lang-name>/folds/fold2_training.json
src/main/resources/<lang-name>/folds/fold2_validation.json

src/main/resources/<lang-name>/foldscache/
src/main/resources/<lang-name>/foldscache/fold0_snippets.pickle
src/main/resources/<lang-name>/foldscache/fold0_testing.pickle
src/main/resources/<lang-name>/foldscache/fold0_training.pickle
src/main/resources/<lang-name>/foldscache/fold0_validation.pickle
src/main/resources/<lang-name>/foldscache/fold1_snippets.pickle
src/main/resources/<lang-name>/foldscache/fold1_testing.pickle
src/main/resources/<lang-name>/foldscache/fold1_training.pickle
src/main/resources/<lang-name>/foldscache/fold1_validation.pickle
src/main/resources/<lang-name>/foldscache/fold2_snippets.pickle
src/main/resources/<lang-name>/foldscache/fold2_testing.pickle
src/main/resources/<lang-name>/foldscache/fold2_training.pickle
src/main/resources/<lang-name>/foldscache/fold2_validation.pickle
```
