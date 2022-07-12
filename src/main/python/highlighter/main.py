import sys
import json
import itertools
import torch
import time
import os.path
#
from pygments import highlight
from pygments.lexers import Python3Lexer, JavaLexer, KotlinLexer
#
import utils as utils
import trainer as trainer
import pygments_utils as pygments_utils


def training_seq():
    all_configs = list(itertools.product(
        # lang_names:
        [utils.PYTHON3_LANG_NAME],
        # task:
        [utils.TASK_L_D_I_A, utils.TASK_L_D_I, utils.TASK_L_D, utils.TASK_L_I],
        # model_name:
        [utils.RNNClassifier1],
        # embs_dim:
        [128],
        # hidden_dim:
        [16, 32],
        # hidden_layers:
        [1],
        # is_bidirectional
        [False, True]
    ))
    tot_num_configs = len(all_configs)

    for i, config in enumerate(all_configs):
        print('On config', (i + 1), 'of', tot_num_configs)
        print(config)

        lang_name, task, model_name, embs_dim, hidden_dim, hidden_layers, is_bid = config
        config = utils.Config(
            lang_name=lang_name,
            run_code=1,
            #
            task=task,
            model_name=model_name,
            embs_dim=embs_dim,
            hidden_dim=hidden_dim,
            hidden_layers=hidden_layers,
            is_bidirectional=is_bid,
            #
            lr_step_size=2,
            max_epochs=4,
            #
            is_load_module_from_path=False,
            is_save_module_to_path=True,
        )
        print(f"Using device {config.device}")
        if not os.path.isfile(config.session_loss_evo_path):
            trainer.debug_training(config)
        else:
            print('Configuration already carried out:', config.session_loss_evo_path)


def use(log_path: str, model_index: int = 0):

    # Load config of trained model.
    log = utils.load_json(log_path)

    # Rebuild original config.
    config = utils.Config()
    config.apply_config_of(log['config'])

    device = config.device

    # Retrieve the last trained model.
    model = config.get_model_of_iter(model_index)
    model.eval()

    # Decodes model's task-specific hcodes to generic hcodes.
    task_decoder = {}
    for i, j in config.task_adapter[1].items():
        task_decoder[int(i)] = int(j)

    with torch.no_grad():
        print("[ READY ]", flush=True)

        for line in sys.stdin:
            if line[0] == 'e':
                break
            else:
                # Create non negative input.
                token_rules = list(map(lambda x: int(x) + 1, line.split()))
                #
                t0 = time.time_ns()
                tens_token_rules = torch.tensor(token_rules, dtype=torch.long).to(device)
                ps = torch.argmax(model(tens_token_rules), dim=1)
                t1 = time.time_ns()
                #
                model_cmp_time_ns = round(t1 - t0)
                print(model_cmp_time_ns)
                print(*[thc.item() for thc in ps], sep=" ", flush=True)


def usepygments(lang: str):
    if lang == 'java':
        lang_lexer = JavaLexer()
        bindings = pygments_utils.JAVA_ORACLE_BINDINGS
    elif lang == 'kotlin':
        lang_lexer = KotlinLexer()
        bindings = pygments_utils.KOTLIN_ORACLE_BINDINGS
    elif lang == 'python3':
        lang_lexer = Python3Lexer()
        bindings = pygments_utils.PYTHON3_ORACLE_BINDINGS
    else:
        raise ValueError(lang + 'is not a valid language')

    drop_formatter = pygments_utils.DropFormatter()
    json_formatter = pygments_utils.JSONFormatter(bindings)

    print("[ READY ]", flush=True)

    for line in sys.stdin:
        if line[0] == 'e':
            break
        else:
            sourcecode = json.loads(line)['source']
            t0 = time.time_ns()
            highlight(sourcecode, lang_lexer, drop_formatter)
            t1 = time.time_ns()
            print(str(t1 - t0) + "\n" + highlight(sourcecode, lang_lexer, json_formatter), flush=True)


if __name__ == '__main__':
    if len(sys.argv) >= 3:
        if sys.argv[1] == 'use':
            log_path = sys.argv[2]
            if len(sys.argv) == 4:
                model_index = int(sys.argv[3])
                use(log_path, model_index=model_index)
            else:
                use(log_path)
        elif sys.argv[1] == 'usepygments':
            usepygments(sys.argv[2])
    else:
        training_seq()
