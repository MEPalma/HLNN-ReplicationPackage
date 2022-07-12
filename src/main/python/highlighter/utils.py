import json

import torch
import numpy as np
import models as models
import os.path
import pickle
import random
import copy
import math
from sklearn.model_selection import KFold
import random

# constants
# JAVA
# ---------------------------------
JAVA_LANG_NAME: str = 'java'
JAVA_LEXER_MAX_TOKEN_VAL: int = 107
JAVA_LEXER_NORMALISED_MAX_TOKEN_VAL: int = 65
# ---------------------------------
#
# KOTLIN
# ---------------------------------
KOTLIN_LANG_NAME: str = 'kotlin'
KOTLIN_LEXER_MAX_TOKEN_VAL: int = 176
KOTLIN_LEXER_NORMALISED_MAX_TOKEN_VAL: int = 113
#
# PYTHON3
# ---------------------------------
PYTHON3_LANG_NAME: str = 'python3'
PYTHON3_LEXER_MAX_TOKEN_VAL: int = 100
PYTHON3_LEXER_NORMALISED_MAX_TOKEN_VAL: int = 68

# Model names.
LSTMClassifier1 = 'LSTMClassifier1'
GRUClassifier1 = 'GRUClassifier1'
RNNClassifier1 = 'RNNClassifier1'

# StackOverflow snippets size.
SO_JAVA_MIN: int = 1
SO_JAVA_MAX: int = 1117
SO_JAVA_MEAN: float = 17.00
SO_JAVA_STD: float = 28.75
#
SO_KOTLIN_MIN: int = 1
SO_KOTLIN_MAX: int = 703
SO_KOTLIN_MEAN: float = 15.00
SO_KOTLIN_STD: float = 22.05
#
SO_PYTHON_MIN: int = 1
SO_PYTHON_MAX: int = 1341
SO_PYTHON_MEAN: float = 14.00
SO_PYTHON_STD: float = 20.39

# From Kotlin's implementation:
ANY: (int, str) = (0, 'ANY')
#
KEYWORD: (int, str) = (1, 'KEYWORD')
LITERAL: (int, str) = (2, 'LITERAL')
CHAR_STRING_LITERAL: (int, str) = (3, 'CHAR_STRING_LITERAL')
COMMENT: (int, str) = (4, 'COMMENT')
#
CLASS_DECLARATOR: (int, str) = (5, 'CLASS_DECLARATOR')
FUNCTION_DECLARATOR: (int, str) = (6, 'FUNCTION_DECLARATOR')
VARIABLE_DECLARATOR: (int, str) = (7, 'VARIABLE_DECLARATOR')
#
TYPE_IDENTIFIER: (int, str) = (8, 'TYPE_IDENTIFIER')
FUNCTION_IDENTIFIER: (int, str) = (9, 'FUNCTION_IDENTIFIER')
FIELD_IDENTIFIER: (int, str) = (10, 'FIELD_IDENTIFIER')
#
ANNOTATION_DECLARATOR: (int, str) = (11, 'ANNOTATION_DECLARATOR')

__TASK_L__: [(int, str)] = (ANY, KEYWORD, LITERAL, CHAR_STRING_LITERAL, COMMENT)
__TASK_D__: [(int, str)] = (CLASS_DECLARATOR, FUNCTION_DECLARATOR, VARIABLE_DECLARATOR)
__TASK_I__: [(int, str)] = (TYPE_IDENTIFIER, FUNCTION_IDENTIFIER, FIELD_IDENTIFIER)
__TASK_A__: [(int, str)] = (ANNOTATION_DECLARATOR,)

TASK_L_D: [(int, str)] = (*__TASK_L__, *__TASK_D__)
TASK_L_I: [(int, str)] = (*__TASK_L__, *__TASK_I__)
TASK_L_D_I: [(int, str)] = (*__TASK_L__, *__TASK_D__, *__TASK_I__)
TASK_L_D_I_A: [(int, str)] = (*__TASK_L__, *__TASK_D__, *__TASK_I__, *__TASK_A__)

NAME_MAP: [(int, str)] = {i: n for i, n in TASK_L_D_I_A}


def task_code_of(task: [(int, str)]) -> int:
    return sum([i for (i, _) in task])


def get_task_adapter_of(tasks: [(int, str)]) -> ({int, int}, {int, int}):
    s = set([k[0] for k in tasks])
    sv = {k: v for k, v in zip(sorted(s), range(0, len(s)))}  # NY ANY (0) is always a member of task.
    a = {i: sv[i] if i in s else ANY[0] for i in range(ANY[0], ANNOTATION_DECLARATOR[0] + 1)}
    ainv = {v: k if v != 0 else 0 for k, v in a.items()}
    return a, ainv


def task_max_val(adapter: ({int, int}, {int, int})) -> int:
    return np.max(list(adapter[1].keys())).item()


def as_adapted_target_of(target: torch.Tensor, adapter: ({int, int}, {int, int})):
    return torch.tensor(list(map(lambda x: adapter[0][x], target.tolist())))


def as_hcodes_with(target: torch.Tensor, adapter: {int, int}) -> torch.Tensor:
    return torch.tensor(list(map(lambda x: adapter[1][x], target.tolist())))


def view_names_of(target: torch.Tensor) -> [str]:
    return [NAME_MAP[it] for it in target.tolist()]


def load_colormap(filename: str):
    color_map = {}
    with open(filename) as f:
        tmp = json.load(f)
        for i, j in tmp.items():
            color_map[int(i)] = int(j)
    return color_map


def to_cache(data: ([torch.Tensor], [torch.Tensor]), name: str):
    with open(name, 'wb') as f:
        pickle.dump(data, f)


def from_cache(name: str) -> ([torch.Tensor], [torch.Tensor]):
    tmp = None
    with open(name, 'rb') as f:
        tmp = pickle.load(f)
    return tmp


def load_json(filename: str):
    tmp = None
    with open(filename, 'r') as f:
        tmp = json.load(f)
    return tmp


def dump_json(filename: str, obj):
    with open(filename, 'w') as f:
        json.dump(obj, f)


def __jhetas_to_cache__(source_jhetas_filepath: str, destination_jhetas_filepath: str):
    jhetas = load_json(source_jhetas_filepath)

    input_seqs = []
    target_seqs = []
    for jh in jhetas:
        iseq = []
        tseq = []
        for heta in jh['hetas']:
            iseq.append(int(heta['eta']['tokenRule']) + 1)  # EOF=-1, hence make input non negative.
            tseq.append(int(heta['highlightCode']))
        #
        input_seqs.append(torch.tensor(iseq, dtype=torch.long))
        target_seqs.append(torch.tensor(tseq, dtype=torch.long))

    data = (input_seqs, target_seqs)
    to_cache(data, destination_jhetas_filepath)


def __lookup_indexes_of_source__(source: str):
    lookup_indexes = {}
    tmp_index = 0
    for line_index, line in enumerate(source.split('\n')):
        lsi = tmp_index
        lei = lsi + len(line)
        lookup_indexes[line_index] = (line, lsi, lei)
        tmp_index = lei + 1
    return lookup_indexes


def __remap_hetas_to_start__(hetas, line_start_index):
    offset = line_start_index
    for heta in hetas:
        heta['eta']['startIndex'] -= offset
        heta['eta']['stopIndex'] -= offset
    return hetas


def __sample_lines_as__(lookup_indexes, tlln, mloc, stdloc, minloc, maxloc):
    res = None
    # Sample gaussian random number of lines of its kind.
    num_loc = round(np.random.normal(mloc, stdloc))
    if not(tlln - num_loc < 0 or not(minloc <= num_loc <= maxloc)):
        # File is at least of such length.
        tail_end_index = tlln - num_loc
        line_start_index = random.randint(0, tail_end_index)
        line_end_index = line_start_index + num_loc - 1
        # Find start and end char indexes.
        char_start_index = lookup_indexes[line_start_index][1]
        char_stop_index = lookup_indexes[line_end_index][2]
        #
        res = char_start_index, char_stop_index
    return res

class Config:
    def __init__(
            self,
            lang_name: str = JAVA_LANG_NAME,
            run_code: int = 1,
            #
            task: [(int, str)] = TASK_L_D_I_A,
            model_name: str = LSTMClassifier1,
            embs_dim: int = 1,
            hidden_dim: int = 128,
            hidden_layers: int = 1,
            is_bidirectional: bool = False,
            #
            lr_start: float = 0.001,
            lr_gamma: float = 0.1,
            lr_step_size: int = 4,
            max_epochs: int = 8,
            #
            is_load_module_from_path: bool = False,
            is_save_module_to_path: bool = True,
            #
            is_seeded: bool = True,
            seed_code: int = 1,
            #
            device=torch.device('cuda:0' if torch.cuda.is_available() else 'cpu')
    ):
        self.is_seeded: bool = is_seeded
        self.seed_code: int = seed_code
        if is_seeded:
            self.seed_pytorch(torch)
            random.seed(seed_code)
        self.device = device
        #
        self.lang_name: str = lang_name
        self.run_code: int = run_code
        #
        self.root_path: str = '../../resources/'
        #
        self.jhetas_clean_filepath: str = f"{self.root_path}{self.lang_name}/oracle/jhetas_clean.json"
        #
        self.jhetas_folds_dir: str = f"{self.root_path}{self.lang_name}/folds/"
        #
        self.pickle_folds_dir: str = f"{self.root_path}{self.lang_name}/foldscache/"
        #
        self.task: [(int, str)] = task
        #
        # FLAGS
        self.is_load_module_from_path: bool = is_load_module_from_path
        self.is_save_module_to_path: bool = is_save_module_to_path
        #
        # DERIVATIVES.
        self.task_adapter: ({int, int}, {int, int}) = get_task_adapter_of(self.task)
        self.task_max_val: int = task_max_val(self.task_adapter)
        #
        if self.lang_name == JAVA_LANG_NAME:
            self.input_dim = JAVA_LEXER_MAX_TOKEN_VAL
        elif self.lang_name == KOTLIN_LANG_NAME:
            self.input_dim = KOTLIN_LEXER_MAX_TOKEN_VAL
        elif self.lang_name == PYTHON3_LANG_NAME:
            self.input_dim = PYTHON3_LEXER_MAX_TOKEN_VAL
        # '+2' refers to 'EOF' always '-1': all were added +1 before this stage.
        self.input_dim += 2
        #
        self.embs_dim: int = embs_dim
        self.hidden_dim: int = hidden_dim
        self.hidden_layers: int = hidden_layers
        self.is_bidirectional: bool = is_bidirectional
        #
        self.lr_start: float = lr_start
        self.lr_gamma: float = lr_gamma
        self.lr_step_size: int = lr_step_size
        self.max_epochs: int = max_epochs
        #
        self.model_name = model_name
        self.config_name = \
            self.model_name + '_' + \
            str(self.embs_dim) + 'embs_' + \
            str(self.input_dim) + 'id_' + \
            str(self.hidden_dim) + 'hd_' + \
            str(self.hidden_layers) + 'hl_' + \
            str(self.is_bidirectional) + 'bid'
        #
        runc: str = str(self.run_code)
        taskc: str = str(task_code_of(self.task))
        self.session_prefix_path: str = f"{self.lang_name}_{runc}_{taskc}_"
        self.module_path: str = f"../saved_models/{self.session_prefix_path}{self.config_name}.pt"
        self.session_loss_evo_path: str = f"../saved_model_losses/{self.session_prefix_path}{self.config_name}.json"

    def apply_config_of(self, config):
        tmp_device = self.device
        self.__dict__ = config
        self.device = tmp_device
        if self.is_seeded:
            self.seed_pytorch(torch)
            random.seed(self.seed_code)

    def json_encode_config(self):
        tmp = self.__dict__.copy()
        del tmp['device']
        return tmp

    def get_cache_training_path_of_fold(self, fold_num: int) -> str:
        return f"{self.pickle_folds_dir}fold{fold_num}_training.pickle"

    def get_cache_validation_path_of_fold(self, fold_num: int) -> str:
        return f"{self.pickle_folds_dir}fold{fold_num}_validation.pickle"

    def get_cache_testing_path_of_fold(self, fold_num: int) -> str:
        return f"{self.pickle_folds_dir}fold{fold_num}_testing.pickle"

    def get_cache_snippets_path_of_fold(self, fold_num: int) -> str:
        return f"{self.pickle_folds_dir}fold{fold_num}_snippets.pickle"

    def get_jhetas_training_path_of_fold(self, fold_num: int) -> str:
        return f"{self.jhetas_folds_dir}fold{fold_num}_training.json"

    def get_jhetas_validation_path_of_fold(self, fold_num: int) -> str:
        return f"{self.jhetas_folds_dir}fold{fold_num}_validation.json"

    def get_jhetas_testing_path_of_fold(self, fold_num: int) -> str:
        return f"{self.jhetas_folds_dir}fold{fold_num}_testing.json"

    def get_jhetas_snippets_path_of_fold(self, fold_num: int) -> str:
        return f"{self.jhetas_folds_dir}fold{fold_num}_snippets.json"

    def get_snippets_class_ids_path_of_fold(self, fold_num: int) -> str:
        return f"{self.jhetas_folds_dir}fold{fold_num}_snippets_class_ids.json"

    def get_cache_training_of_fold(self, fold_num: int) -> ([torch.Tensor], [torch.Tensor]):
        return self.__cache_adapt_and_push_to_device__(from_cache(self.get_cache_training_path_of_fold(fold_num)))

    def get_cache_validation_of_fold(self, fold_num: int) -> ([torch.Tensor], [torch.Tensor]):
        return self.__cache_adapt_and_push_to_device__(from_cache(self.get_cache_validation_path_of_fold(fold_num)))

    def get_cache_testing_of_fold(self, fold_num: int) -> ([torch.Tensor], [torch.Tensor]):
        return self.__cache_adapt_and_push_to_device__(from_cache(self.get_cache_testing_path_of_fold(fold_num)))

    def get_cache_snippets_of_fold(self, fold_num: int) -> ([torch.Tensor], [torch.Tensor]):
        return self.__cache_adapt_and_push_to_device__(from_cache(self.get_cache_snippets_path_of_fold(fold_num)))

    def get_snippets_class_ids_of_fold(self, fold_num: int):
        return load_json(self.get_snippets_class_ids_path_of_fold(fold_num))

    def generate_folds(self):
        clean_jhetas = load_json(self.jhetas_clean_filepath)
        assert(len(clean_jhetas) == 20_000)
        #
        val_len = 1333 # 0.1*(0.66666..*20000)
        kfold = KFold(n_splits=3, shuffle=True, random_state=self.seed_code)
        for foldid, (trainval_is, test_is) in enumerate(kfold.split(range(20_000))):
            val_is = trainval_is[0:val_len]
            train_is = trainval_is[val_len:]
            #
            dump_json(self.get_jhetas_training_path_of_fold(foldid), [clean_jhetas[i] for i in train_is])
            dump_json(self.get_jhetas_validation_path_of_fold(foldid), [clean_jhetas[i] for i in val_is])
            dump_json(self.get_jhetas_testing_path_of_fold(foldid), [clean_jhetas[i] for i in test_is])
            #
            print(f"Fold {foldid}: Training:', {len(train_is)}, 'Validation:', {len(val_is)}, 'Test size:', {len(test_is)}")

    def generate_folds_snippets(self, number_of_snippets=5000):
        if self.lang_name == JAVA_LANG_NAME:
            LOC_mean: float = SO_JAVA_MEAN
            LOC_std: float = SO_JAVA_STD
            LOC_min: int = SO_JAVA_MIN
            LOC_max: int = SO_JAVA_MAX
        elif self.lang_name == KOTLIN_LANG_NAME:
            LOC_mean: float = SO_KOTLIN_MEAN
            LOC_std: float = SO_KOTLIN_STD
            LOC_min: int = SO_KOTLIN_MIN
            LOC_max: int = SO_KOTLIN_MAX
        elif self.lang_name == PYTHON3_LANG_NAME:
            LOC_mean: float = SO_PYTHON_MEAN
            LOC_std: float = SO_PYTHON_STD
            LOC_min: int = SO_PYTHON_MIN
            LOC_max: int = SO_PYTHON_MAX

        for foldid in range(3):
            jhetas_testing = load_json(self.get_jhetas_testing_path_of_fold(foldid))
            jhetas_indexes = range(len(jhetas_testing))
            per_file_lookup_indexes = {}

            this_fold_snips = []
            while len(this_fold_snips) < number_of_snippets:
                # Choose a random file.
                i = random.choice(jhetas_indexes)
                jh = jhetas_testing[i]
                source = str(jh['source']['source'])
                hetas = jh['hetas']
                tlln = source.count('\n') - 1
                if not i in per_file_lookup_indexes.keys():
                    per_file_lookup_indexes[i] = __lookup_indexes_of_source__(source)
                lookup_indexes = per_file_lookup_indexes[i]

                # Sample snippet.
                maybe_start_end = __sample_lines_as__(lookup_indexes, tlln, LOC_mean, LOC_std, LOC_min, LOC_max)
                if maybe_start_end is not None:
                    (char_start_index, char_stop_index) = maybe_start_end
                    #
                    new_hetas = []
                    for heta in hetas:
                        eta = heta['eta']
                        etasi = eta['startIndex']
                        etaei = eta['stopIndex']
                        if (char_start_index <= etasi <= char_stop_index < etaei) or (etasi < char_start_index <= etaei <= char_stop_index):
                            # Multiline token outside range: abandon this sample.
                            new_hetas = []
                            break
                        elif etasi >= char_start_index and etaei <= char_stop_index:
                            new_hetas.append(copy.deepcopy(heta))
                    #
                    if len(new_hetas) > 0:
                        jh_cp = copy.deepcopy(jh)
                        jh_cp['source']['source'] = source[char_start_index:(char_stop_index + 1)]
                        jh_cp['hetas'] = __remap_hetas_to_start__(new_hetas, char_start_index)
                        #
                        this_fold_snips.append(jh_cp)
                        print('\rFound', len(this_fold_snips), 'snippets', end='')

            print('\rFound', len(this_fold_snips), 'snippets', end='')
            print()

            dump_json(self.get_jhetas_snippets_path_of_fold(foldid), this_fold_snips)

    def generate_cache(self):
        for foldid in range(3):
            __jhetas_to_cache__(
                self.get_jhetas_training_path_of_fold(foldid),
                self.get_cache_training_path_of_fold(foldid)
            )
            __jhetas_to_cache__(
                self.get_jhetas_validation_path_of_fold(foldid),
                self.get_cache_validation_path_of_fold(foldid)
            )
            __jhetas_to_cache__(
                self.get_jhetas_testing_path_of_fold(foldid),
                self.get_cache_testing_path_of_fold(foldid)
            )
            __jhetas_to_cache__(
                self.get_jhetas_snippets_path_of_fold(foldid),
                self.get_cache_snippets_path_of_fold(foldid)
            )

    def new_model(self):
        if self.model_name == LSTMClassifier1:
            model = models.LSTMClassifier1(
                embedding_dim=self.embs_dim,
                vocab_size=self.input_dim,
                hidden_dim=self.hidden_dim,
                tagset_size=self.task_max_val + 1,  # task_max_val is last index, hence + 1
                num_lstm_layers=self.hidden_layers,
                is_bidirectional=self.is_bidirectional
            )
        elif self.model_name == GRUClassifier1:
            model = models.GRUClassifier1(
                embedding_dim=self.embs_dim,
                vocab_size=self.input_dim,
                hidden_dim=self.hidden_dim,
                tagset_size=self.task_max_val + 1,  # task_max_val is last index, hence + 1
                num_gru_layers=self.hidden_layers,
                is_bidirectional=self.is_bidirectional
            )
        elif self.model_name == RNNClassifier1:
            model = models.RNNClassifier1(
                embedding_dim=self.embs_dim,
                vocab_size=self.input_dim,
                hidden_dim=self.hidden_dim,
                tagset_size=self.task_max_val + 1,  # task_max_val is last index, hence + 1
                num_layers=self.hidden_layers,
                is_bidirectional=self.is_bidirectional
            )
        else:
            raise ValueError(self.model_name + ' is an invalid model name.')
        #
        if self.is_load_module_from_path:
            model.load_state_dict(torch.load(self.module_path, map_location='cpu'))
        #
        return model.to(self.device)

    def new_model_training_session(self):
        model = self.new_model()
        optimiser = torch.optim.Adam(model.parameters(), lr=self.lr_start)
        scheduler = torch.optim.lr_scheduler.StepLR(
                                                optimiser,
                                                step_size=self.lr_step_size,
                                                gamma=self.lr_gamma,
                                                verbose=True)
        loss_func = torch.nn.CrossEntropyLoss()
        return model, optimiser, scheduler, loss_func

    def seed_pytorch(self, t):
        if self.is_seeded:
            t.manual_seed(self.seed_code)

    def __model_path_of_iter__(self, iter: int):
        return self.module_path.replace('.pt', '_' + str(iter) + '.pt')

    def save_model_iter(self, model: torch.nn.Module, iter: int):
        torch.save(model.state_dict(), self.__model_path_of_iter__(iter))

    def get_model_of_iter(self, iter: int) -> torch.nn.Module:
        org_module_path = self.module_path
        org_is_load_module_from_path = self.is_load_module_from_path
        #
        ##
        self.module_path = self.__model_path_of_iter__(iter)
        self.is_load_module_from_path = True
        #
        model = self.new_model()
        #
        ##
        self.module_path = org_module_path
        self.is_load_module_from_path = org_is_load_module_from_path
        #
        return model

    def __cache_adapt_and_push_to_device__(self, cache: ([torch.Tensor], [torch.Tensor])) -> ([torch.Tensor], [torch.Tensor]):
        for i in range(len(cache[0])):
            cache[0][i] = cache[0][i].to(self.device)
            cache[1][i] = as_adapted_target_of(cache[1][i], self.task_adapter).to(self.device)
        return cache

