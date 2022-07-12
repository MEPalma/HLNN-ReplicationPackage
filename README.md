# On-the-Fly Syntax Highlighting Using Neural Networks -- Replication Package
Replication Package for the paper
[On-the-Fly Syntax Highlighting Using Neural Networks](https://2022.esec-fse.org/details/fse-2022-research-papers/31/On-the-Fly-Syntax-Highlighting-Using-Neural-Networks)
published at [ESEC/FSE 2022](https://2022.esec-fse.org)

## Abstract
With the presence of online collaborative tools for software developers, 
source code is shared and consulted frequently, from code viewers to 
merge requests and code snippets. Typically, code highlighting quality 
in such scenarios is sacrificed in favor of system responsiveness. 
In these on-the-fly settings, performing a formal grammatical analysis 
of the source code is expensive and intractable for the many times the 
input is an invalid derivation of the language. Indeed, current popular 
highlighters heavily rely on a system of regular expressions, typically 
far from the specification of the language's lexer. Due to their 
complexity, regular expressions need to be periodically updated as more 
feedback is collected from the users and their design unwelcome the 
detection of more complex language formations. This paper delivers a 
deep learning-based approach suitable for on-the-fly grammatical code 
highlighting of correct and incorrect language derivations, such as code 
viewers and snippets. It focuses on alleviating the burden on the 
developers, who can reuse the language's parsing strategy to produce the 
desired highlighting specification. Moreover, this approach is compared 
to nowadays online syntax highlighting tools and formal methods in terms 
of accuracy and execution time, across different levels of grammatical 
coverage, for three mainstream programming languages. The results obtained
show how the proposed approach can consistently achieve near-perfect 
accuracy in its predictions, thereby outperforming regular 
expression-based strategies.

## Project Setup
This project is part written in Kotlin and Python. For the most part, Python is utilised for:
GitHub files mining, neural networks training and evaluation, test folds and snippets creation,
and utilising the [Pygments](https://pygments.org) syntax highlighting library. Kotlin instead
implements logic for: preprocessing of GitHub's raw files, brute force algorithms, accuracy
and speed testing for all approaches, and HTML or console rendering of syntax highlighted files.

Gradle is utilised to for building and running of the project -- with exception of some Python tasks,
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
and versions required are listed in the CondaList file `CondaList`, hence the compatible environment
can be created by running in the project's root directory:
```shell
conda install --file CondaList
```
It should be noted that the project utilises the machine's default Conda/Python environment. Furthermore,
[PyTorch](https://pytorch.org)'s Cuda integration is installed by default. If this should not be available,
this can be safely changed to PyTorch's non Cuda installation of the same version; likewise for any other
Cuda related packages.

The Python's root workspace is located in `src > main > python > highlighter`.

Finally, some automation routines utilise the Bash shell (v 3.*), and the ZIP v 3.0)
command line tool.


## Rebuilding Split Archives
Please first refer to the [project's website](https://hlnn.netlify.app) to download the original assets,
and add them to the `src/main/resources/` subdirectory.

Some files have been broken down into multiple parts, in order to comply with the maximum file size of the platform used
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

## GitHub Mining
This release of the project already includes GitHub's mined resource files, and logs of the events that took
place during the mining process, in `src > main > resources > <land-name> > raw.zip`, where `<land-name>`
is one of the 3 target programming languages:`java`, `kotlin` and `python3`.

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
which through skipping token-wise duplicate patters, takes the first 20k samples in the dataset.

This can be performed for Kotlin and Python datasets, by changing the target Gradle task to `KotlinPreprocessor` and
`Python3Preprocessor` respectively.

Note that this project already provides oracles for all languages, which are stored in
`src > main > resources > <land-name> > oracle.zip`


## NNs' Training Datasets

### NNs' Training Datasets -- Folds & Snippets
Having completed the preprocessing steps to generate an oracle, one must generate the training 66-33
cross-validation folds and snippets. Once again, this is already provided by this distribution in
`src > main > resources > <land-name> > folds.zip`. Such task is carried out by the Python's implementation of the project, hence the following commands are
intended to be run within the this subproject's root directory given above.

For this purpose the following command should be run:
```shell
python oracle_cache_generator.py generate_folds <land-name>
```
where `<land-name>` is one of the three languages analysed, as given above.

### NNs' Training Datasets -- Cache
Cache is a more efficient representation of the training datasets, in the form of collections of PyTorch tensors,
especially performed for the training of Neural Networks. Although these are already provided in
`src > main > resources > <land-name> > foldscache.zip`
one can generate them by running:
```shell
python oracle_cache_generator.py generate_cache python3
```
Please note that for this to work, the folds need to be generated first.


## NN Training
All training of neural networks is carried out from the Python's project, once the
cached training datasets have been created. The configuration of the training session
is to be set in `src > main > python > highlighter > main.py`. By default, the process
tests all possible combination of testing parameters. Hence, training can be launched
by running the following from the Python's project root folder
```shell
python main.py
```
During training, per fold, the same neural network is trained, validates and tested
on each dataset fold (3). Hence, three PyTorch neural networks are save to disk
in `src > main > python > saved_models`, and logs for the whole session in
`src > main > python > save_model_losses`. Such generated files do also carry details
regarding the configuration of the training session, and are necessary for the execution
of not only accuracy and speed tests, but also rendering of syntax highlighted files
using trained neural networks. Files in `saved_models` and `save_model_losses` are named
after the configuration they reference, in particular the following substructure may be
found:
```
<lang-name>_<execution-number>_\
<task-id>_<nn-model>_<embedding-layer-dim>embs_\
<input-dim>id_<width-hidden-layers>hd_\
<num-hidden-layers>hl_\
<is-bidirectional-network>bid
```
hence, for example:
```
java_1_28_RNNClassifier1_128embs_109id_32hd_1hl_Falsebid
```

Please note that, according to the syntax highlighting coverage tasks described in
the paper this replication package is linked to, task 1 through to 4 are `<task-id>`:
`28`, `37`, `55` and `66` respectively.

## Accuracy Testing
Accuracy testing refers to the accuracy of producing syntax highlighting for each of
the files in the test dataset of each training dataset folds, and the respective
synthetic snippets. As the accuracy of the Brute Force method is always 1 (100%),
accuracy per file is collected with regards to the neural networks approach and Pygments.

### Accuracy Testing -- Neural Networks & Pygments
For each tested language there exists an accuracy test task, which needs to be instructed
which trained model session to load. This information enables it to test each model
trained on each fold with its set of unseen test cases. For Java, the following may be run
```shell
./gradlew JavaEvaluator -Pargs="runPerFileApproachAcc ../saved_model_losses/<model-training-logs-filename>"
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

## Speed Testing
Speed tests record the time elapsed for the three approaches to compute syntax highlighting
for any given input file. Although this could be performed per each file in the oracle,
given the high computational costs of the Brute Force solutions, an alternative is to
pre-sample 1k files of increasing computation costs as per the BF approach, averaged across
multiple runs.

### Speed Testing -- Brute Force
Brute Force approaches per language can be launched from Gradle, with the following
command, here testing the Java BF strategy
```shell
./gradlew JavaEvaluator -Pargs="runBruteTimeBenchmarker"
```
with the Gradle tasks for `Kotlin` and `Python3` being `KotlinEvaluator` and
`Python3Evaluator` respectively.

### Speed Testing -- Neural Networks
Speed testing for trained neural networks require a completed training sessions, hence
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

### Speed Testing -- Pygments
Speed tests for the Pygments library can be launched in a similar manner to the other
speed test
```shell
./gradlew JavaEvaluator -Pargs="runPygmentBenchmarker"
```
with the Gradle tasks for `Kotlin` and `Python3` being `KotlinEvaluator` and
`Python3Evaluator` respectively.

## Results Structure
For each language investigated, its results in terms of accuracy and speed are stored in the language's directory: `src/main/resources/<lang>/`.
These metrics are available for each trained DL model, the brute force approach and Pygments.

### Accuracy
For all normalised and filtered test files and snippets stored in each fold of the model's training process, test results list the accuracy of each model (3) with respect to each syntax highlighting coverage task (of which there are four of them: 28, 37, 55 and 66 respectively).

File names are well formed and carry information about the configuration of the model and coverage task.
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

### Speed

All models are tested on all normalised and filtered files of the entire dataset (20000), and all the snippets of all the folds.
The prediction delays are recorded in nanoseconds, and repeated 30 times per file or snippet.

- **Brute Force Model**

    Speed benchmarks for the brute force implementation are stored in the single json file: `perFileTimeBrute.json`.
    The structure of this file is a list of file benchmarks, of the form:
    - `fileId`: the `MD5` encoding of the file's raw content
    - `nss`: the list of 30 nanoseconds computation delays for producing the highlighting of the file or snippet

- **Pygments**

    Speed benchmarks for the brute force implementation are stored in the single json file: `perFileTimePygments.json`.
    The structure of this file is a list of file benchmarks, of the form:
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
