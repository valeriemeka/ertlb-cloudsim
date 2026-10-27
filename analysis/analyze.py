"""Analyse statistique des résultats CloudSim.
Usage : python analysis/analyze.py results/results_main.csv [dossier_sortie]
"""
import sys, glob, os
import numpy as np, pandas as pd
from scipy import stats
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt

METRICS = {"avg_response_time": "Temps de réponse moyen (s)", "makespan": "Makespan (s)",
           "utilization_pct": "Utilisation CPU (%)", "std_load": "Écart-type de la charge (s)"}
ALGOS = ["ROUND_ROBIN", "ERTLB", "ERTLB_PLUS"]

def cohen_dz(d):  # d de Cohen pour échantillons appariés
    return d.mean() / d.std(ddof=1) if d.std(ddof=1) > 0 else float("nan")

def main(path, out):
    os.makedirs(out, exist_ok=True)
    df = pd.read_csv(path)
    rows, desc = [], []
    for k, lab in METRICS.items():
        w = df.pivot(index="seed", columns="algorithm", values=k)
        for a in ALGOS:
            desc.append(dict(metrique=lab, algorithme=a, n=len(w), moyenne=w[a].mean(),
                             ecart_type=w[a].std(ddof=1),
                             ic95=1.96 * w[a].std(ddof=1) / np.sqrt(len(w))))
        for a, b in [("ERTLB_PLUS", "ROUND_ROBIN"), ("ERTLB_PLUS", "ERTLB"), ("ERTLB", "ROUND_ROBIN")]:
            d = w[a] - w[b]
            t, p = stats.ttest_rel(w[a], w[b])
            try: wp = stats.wilcoxon(w[a], w[b]).pvalue
            except ValueError: wp = float("nan")
            rows.append(dict(metrique=lab, comparaison=f"{a} vs {b}", diff_moyenne=d.mean(),
                             gain_pct=100 * d.mean() / w[b].mean(), t=t, p_t=p, p_wilcoxon=wp,
                             cohen_dz=cohen_dz(d)))
    pd.DataFrame(desc).round(4).to_csv(f"{out}/descriptif.csv", index=False)
    pd.DataFrame(rows).to_csv(f"{out}/tests.csv", index=False, float_format="%.6g")
    print(pd.DataFrame(desc).round(2).to_string(index=False))
    print(pd.DataFrame(rows).round(4).to_string(index=False))

    # Boîtes à moustaches
    fig, ax = plt.subplots(1, 4, figsize=(14, 3.4))
    for axi, (k, lab) in zip(ax, METRICS.items()):
        w = df.pivot(index="seed", columns="algorithm", values=k)[ALGOS]
        axi.boxplot([w[a] for a in ALGOS], tick_labels=["RR", "ERTLB", "ERTLB+"])
        axi.set_title(lab, fontsize=9)
    fig.tight_layout(); fig.savefig(f"{out}/boxplots.png", dpi=200); plt.close(fig)

    # Sensibilité
    d = os.path.dirname(path)
    for tag, col, xl in [("sens_vm", "vms", "Nombre de VM"), ("sens_task", "tasks", "Nombre de tâches")]:
        fs = sorted(glob.glob(f"{d}/{tag}_*.csv"))
        if not fs: continue
        s = pd.concat(map(pd.read_csv, fs))
        g = s.groupby([col, "algorithm"])[["makespan", "utilization_pct"]].mean().reset_index()
        g.round(3).to_csv(f"{out}/{tag}.csv", index=False)
        fig, ax = plt.subplots(1, 2, figsize=(9, 3.2))
        for a in ALGOS:
            x = g[g.algorithm == a]
            ax[0].plot(x[col], x.makespan, marker="o", label=a); ax[1].plot(x[col], x.utilization_pct, marker="o", label=a)
        ax[0].set_ylabel("Makespan (s)"); ax[1].set_ylabel("Utilisation (%)")
        for axi in ax: axi.set_xlabel(xl)
        ax[0].legend(fontsize=8); fig.tight_layout(); fig.savefig(f"{out}/{tag}.png", dpi=200); plt.close(fig)

if __name__ == "__main__":
    main(sys.argv[1], sys.argv[2] if len(sys.argv) > 2 else "analysis/out")
