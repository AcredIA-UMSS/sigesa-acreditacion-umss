from __future__ import annotations


def cohen_kappa(human: list[bool], judge: list[bool]) -> float:
    """Cohen κ para etiquetas binarias PASS (True) / FAIL (False)."""
    if len(human) != len(judge) or not human:
        return 0.0
    n = len(human)
    agree = sum(1 for h, j in zip(human, judge, strict=True) if h == j)
    p_o = agree / n
    p_yes_h = sum(human) / n
    p_yes_j = sum(judge) / n
    p_e = p_yes_h * p_yes_j + (1 - p_yes_h) * (1 - p_yes_j)
    if p_e >= 1.0:
        return 1.0 if p_o >= 1.0 else 0.0
    return (p_o - p_e) / (1 - p_e)


def confusion_matrix(human: list[bool], judge: list[bool]) -> dict[str, int]:
    tp = fp = tn = fn = 0
    for h, j in zip(human, judge, strict=True):
        if h and j:
            tp += 1
        elif not h and j:
            fp += 1
        elif h and not j:
            fn += 1
        else:
            tn += 1
    return {"tp": tp, "fp": fp, "fn": fn, "tn": tn}
