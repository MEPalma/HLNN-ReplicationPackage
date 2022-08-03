---
title: ⚙️ Project setup
weight: 1
---

# ⚙️ Project Setup

This project is written in Kotlin and Python. For the most part Python is utilised for:
GitHub files mining, neural networks training and evaluation, test folds and snippets creation,
and utilizing the [Pygments](https://pygments.org) syntax highlighting library.
Kotlin instead implements logic for: preprocessing of GitHub's raw files, brute force algorithms, accuracy
and speed testing for all approaches and HTML or console rendering of syntax highlighted files.

Gradle is used for building and running of the project -- with exception to some Python tasks,
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

[Conda](https://conda.io) is used for the creation of the python environment.
All of the packages and versions required are listed in the Conda environment file `environment.yml`.

It should be noted that the project uses the machine's default Conda/Python environment.
Furthermore, [PyTorch](https://pytorch.org)'s Cuda integration is installed by default. If this should not be available,
this can be safely changed to PyTorch's non-Cuda installation of the same version; likewise, for any other
Cuda related packages.

The Python's root workspace is located in `src/main/python/highlighter`.

Finally, some automation routines uses the Bash shell (v3.*), and the ZIP v3.0) command-line tool.
