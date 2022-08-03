---
title: 💪 Models training
weight: 4
---

# 💪 Models training

All training of neural networks is carried out from the Python's project, once the
cached training datasets have been created. The configuration of the training session
is to be set in `src/main/python/highlighter/main.py`. By default, the process
tests all possible combinations of testing parameters. Hence, training can be launched
by running the following from the Python's project root folder

```shell
python main.py
```

During training, per fold, the same neural network is trained, validated and tested
on each dataset fold (3). Hence, three PyTorch neural networks are save to disk
in `src/main/python/saved_models`, and logs for the whole session in
`src/main/python/save_model_losses`. Such generated files do also carry details
regarding the configuration of the training session and are necessary for the execution
of not only accuracy and speed tests, but also rendering of syntax highlighted files
using trained neural networks. Files in `saved_models` and `save_model_losses` are named
after the configuration they reference in particular, the following substructure may be
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

Please note that according to the syntax highlighting coverage tasks described in
the paper this replication package is linked to, task 1 through to 4 are `<task-id>`:
`28`, `37`, `55` and `66` respectively.
