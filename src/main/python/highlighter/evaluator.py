import torch
import json
from sklearn.metrics import accuracy_score
import utils as utils


def acc_of(model: torch.nn.Module, seq_in: torch.Tensor, seq_t: torch.Tensor, loss_function) -> (float, [(int, int)], torch.Tensor):
    p_logs = model(seq_in)
    seq_p = torch.argmax(p_logs, dim=1)
    acc_sc = accuracy_score(seq_t.cpu(), seq_p.cpu())
    errs = [(p.item(), t.item()) for p, t in zip(seq_p, seq_t) if p != t]
    return acc_sc, errs, seq_p.tolist(), loss_function(p_logs, seq_t).item()


def acc_of_all(model: torch.nn.Module, seqs_in: [torch.Tensor], seqs_t: [torch.Tensor], loss_function, msg: str = 'Eval') -> (float, {(int, int): int}, [int], [int], [torch.Tensor]):
    N = len(seqs_in)
    #
    accs_sum = 0
    errs_sum = 0
    loss_sum = 0
    #
    errs_map = {}
    errs_obs = []
    errs_hist = []
    #
    seqs_p = []
    #
    for i, (seq_in, seq_t) in enumerate(zip(seqs_in, seqs_t)):
        (acc_sc, errs, seq_p, loss) = acc_of(model, seq_in, seq_t, loss_function)
        #
        accs_sum += acc_sc
        errs_sum += len(errs)
        loss_sum += loss
        errs_obs.append(errs)
        errs_hist.append(len(errs))
        #
        for er_t, er_c in errs:
            tmp_err_bindings = errs_map.get(er_t, {er_c: 0})
            tmp_err_bindings[er_c] = tmp_err_bindings.get(er_t, 0) + 1
            errs_map[er_t] = tmp_err_bindings
        #
        seqs_p.append(seq_p)
        #
        if i % 100 == 0:
            print('\r' + msg + ' | Progress:', ('%.2f' % ((i + 1) * 100 / N)) + '%', '| Loss:', loss_sum, '| Acc:', '%.2f' % ((accs_sum / (i + 1)) * 100) + '%', end='')
    print()

    return (accs_sum / N) * 100, loss_sum, errs_map, errs_obs, errs_hist, seqs_p


@DeprecationWarning
def eval_to_jheta(model, color_map, jeta):
    etas = jeta['etas']
    seq = torch.tensor([it['tokenRule'] + 1 for it in etas], dtype=torch.long)
    hcodes = torch.argmax(model(seq), dim=1).tolist()
    return {
        'source': jeta['source'],
        'hetas': [
            { 'eta': eta
            , 'highlightCode': int(hcode)
            , 'highlightColor': str(color_map[hcode])
            } for eta, hcode in zip(etas, hcodes)
        ]
    }
