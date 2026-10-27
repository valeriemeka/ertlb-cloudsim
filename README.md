# ERTLB amélioré sous CloudSim — projet de reproduction

Compare trois stratégies d'allocation dans **1 Datacenter, 20 VM hétérogènes, 1000 tâches**, 30 exécutions indépendantes (graines 5000–5029) :
- `ROUND_ROBIN` : v = t mod m (statique) ;
- `ERTLB` : score(v) = N(v)/Cap(v), N(v) = nombre de tâches déjà affectées (dynamique, ignore la complexité) ;
- `ERTLB_PLUS` : score(v) = L(v) + c(t)/Cap(v), puis L(v) ← L(v) + c(t)/Cap(v) (proposé).

## Prérequis
JDK 17 ou plus, Maven 3.8+ (Internet au premier lancement, pour télécharger commons-math3), Python 3 avec numpy, pandas, scipy, matplotlib.

## Lancer
    ./run_all.sh            # Linux/macOS/Git Bash
ou, dans IntelliJ/Eclipse : importer le dossier comme projet Maven, exécuter `ertlb.Main` (arguments facultatifs : runs vms tasks seed0 fichier_sortie), puis
    python analysis/analyze.py results/results_main.csv analysis/out

## Architecture
| Fichier | Rôle |
|---|---|
| `Workload` | génère tâches (simple 10–25 : 45 %, composée 26–50 : 35 %, complexe 51–100 : 20 %) et capacités Cap(v) ∈ [0,5 ; 1,3] à partir de la graine |
| `Allocator` | calcule l'affectation de chaque stratégie (allocation en ligne) |
| `Simulation` | construit Datacenter, hôtes, VM (1 cœur, MIPS = 1000×Cap), cloudlets (MI = 100×c) ; lie chaque cloudlet à sa VM par `bindCloudletToVm` ; ordonnanceur `CloudletSchedulerSpaceShared` (file FIFO par VM) |
| `Main` | boucle sur les graines ; les 3 stratégies reçoivent la même charge (plan apparié) |

## Métriques
Temps de réponse moyen = moyenne de (fin − soumission) ; makespan = fin de la dernière tâche ; utilisation = Σ temps CPU / (m × makespan) ; écart-type de charge = écart-type des temps d'occupation par VM.

## À savoir avant de citer les résultats
- Version CloudSim : 7.0.1. Les 72 classes CloudSim nécessaires sont incluses telles quelles dans `src/main/java/org/cloudbus` (copiées de https://github.com/Cloudslab/cloudsim, version 7.0.1) car cette version n'est pas publiée sur Maven Central ; leurs licences sont indiquées dans les en-têtes de fichiers. API utilisée : `submitGuestList`. Pour CloudSim 3.0.3, remplacer `submitGuestList`/`getGuestId` par `submitVmList`/`getVmId`.
- Toutes les tâches arrivent à t = 0 ; les affectations sont calculées hors ligne dans l'ordre d'arrivée puis imposées au courtier. Le gain en temps de réponse moyen tient compte de cette hypothèse.
- La définition de l'ERTLB de base (N(v)/Cap(v)) est celle retenue ici ; l'article doit la formuler de la même façon.
- Les résultats de l'article précédent venaient d'un modèle Python : **remplacez-les tous** par `analysis/out/*.csv`.
