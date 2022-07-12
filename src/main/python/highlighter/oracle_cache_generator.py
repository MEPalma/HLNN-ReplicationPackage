import sys
import utils as utils


def generate_folds(lang_name: str):
    c = utils.Config(lang_name=lang_name)
    c.generate_folds()


def generate_snippets(lang_name: str):
    c = utils.Config(lang_name=lang_name)
    c.generate_folds_snippets()


def generate_cache(lang_name: str):
    c = utils.Config(lang_name=lang_name)
    c.generate_cache()


if __name__ == '__main__':
    task = sys.argv[1]
    if task == 'generate_folds':
        generate_folds(sys.argv[2])
    elif task == 'generate_snippets':
        generate_snippets(sys.argv[2])
    elif task == 'generate_cache':
        generate_cache(sys.argv[2])
    elif task == 'all':
        generate_folds(sys.argv[2])
        generate_snippets(sys.argv[2])
        generate_cache(sys.argv[2])
    else:
        print(f"Unknown command sequence {sys.argv[1:]}")
