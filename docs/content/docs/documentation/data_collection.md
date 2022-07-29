---
title: ⛏️ Data collection
weight: 2
---

# ⛏️ Data collection

## Data mining

This release of the project already includes GitHub's mined resource files, and logs of the events that took
place during the mining process, in `src > main > resources > <lang-name> > raw.zip`, where `<lang-name>`
is one of the 3 target programming languages: `java`, `kotlin` and `python3`.

In order to repeat the mining process for any of the languages, one must obtain a GitHub API Token, and insert
it into the file `src > main > python > github_miner.py` as indicated (`API_TOKEN` field), and hence run the
script from within its directory.


## Preprocessing

Preprocessing is the task of obtaining an oracle of 20k files with correct language derivations, for each one of the three languages.
Most of the logic is contained in `src > main > kotlin > preprocessor`, whereas the formal grammatical
syntax highlighters are contained in `src > main > kotlin > highlighter`.

The process of oracle generation is divided into two step: generation and cleaning. These are run for each
language, hence, for example, with regards to Java, one would run the following two Gradle tasks:

```shell
./gradlew JavaPreprocessor -Pargs="generateOracle"
```

which filters and converts GitHub's raw files sequentially into valid `ETA` and  `HETA` representations;

```shell
./gradlew JavaPreprocessor -Pargs="cleanOracle"
```

which through skipping token-wise duplicate patterns, takes the first 20k samples in the dataset.

This can be performed for Kotlin and Python datasets by changing the target Gradle task to `KotlinPreprocessor` and
`Python3Preprocessor` respectively.

Note that this project already provides oracles for all languages, which are stored in
`src > main > resources > <lang-name> > oracle.zip`
