---
title: 💾 Evaluation datasets
weight: 3
---

# 💾 Evaluation datasets

## Folds

Having completed the preprocessing steps to generate an oracle, one must generate the training 66-33
cross-validation folds and snippets. Once again, this is already provided by this distribution in
`src > main > resources > <lang-name> > folds.zip`. The Python's subproject carries out such task, hence the following commands are
to be run within this subproject's root directory.

For this purpose, the following command should be run:

```shell
python oracle_cache_generator.py generate_folds <lang-name>
```

where `<lang-name>` is one of the three languages analysed, as given above.

## Snippets

Snippets for each fold are generated through the command below and only after folds have been produced.

```shell
python oracle_cache_generator.py generate_snippets <lang-name>
```

## Cache

Cache is a more efficient representation of the training datasets, in the form of collections of PyTorch Tensors,
especially performed for the training of Neural Networks. Although these are already provided in
`src > main > resources > <lang-name> > foldscache.zip`
one can generate them by running:
```shell
python oracle_cache_generator.py generate_cache <lang-name>
```
Please note that for this to work all folds need to be generated first.

## Run All

All of the pipelines above can be run in once with the command:
```shell
python oracle_cache_generator.py all <lang-name>
```
