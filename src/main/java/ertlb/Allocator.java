package ertlb;

import java.util.List;

/**
 * Calcule l'affectation tâche -> VM pour chaque stratégie.
 * Les tâches sont traitées dans leur ordre d'arrivée (allocation en ligne) :
 * seule l'information disponible à l'arrivée de la tâche t est utilisée.
 *
 * Notation (voir l'article) : Cap(v) = capacité relative de la VM v,
 * c(t) = complexité de la tâche t, L(v) = charge estimée de la VM v.
 */
public final class Allocator {

    private Allocator() { }

    /** @return tableau a[t] = indice (0..m-1) de la VM choisie pour la tâche t. */
    public static int[] allocate(Strategy s, List<TaskSpec> tasks, double[] cap) {
        int n = tasks.size(), m = cap.length;
        int[] a = new int[n];
        switch (s) {
            case ROUND_ROBIN:
                // Statique : v = t mod m, aveugle à l'état et à la complexité.
                for (int t = 0; t < n; t++) a[t] = t % m;
                break;

            case ERTLB: {
                // Dynamique, sensible à la charge mais pas à la complexité :
                // score(v) = N(v) / Cap(v), N(v) = nombre de tâches déjà affectées à v.
                int[] count = new int[m];
                for (int t = 0; t < n; t++) {
                    int best = 0; double bestScore = Double.MAX_VALUE;
                    for (int v = 0; v < m; v++) {
                        double score = count[v] / cap[v];
                        if (score < bestScore) { bestScore = score; best = v; }
                    }
                    a[t] = best; count[best]++;
                }
                break;
            }

            case ERTLB_PLUS: {
                // Proposé : score(v) = L(v) + c(t)/Cap(v) ; L(v) <- L(v) + c(t)/Cap(v).
                double[] load = new double[m];
                for (int t = 0; t < n; t++) {
                    double c = tasks.get(t).complexity;
                    int best = 0; double bestScore = Double.MAX_VALUE;
                    for (int v = 0; v < m; v++) {
                        double score = load[v] + c / cap[v];
                        if (score < bestScore) { bestScore = score; best = v; }
                    }
                    a[t] = best; load[best] += c / cap[best];
                }
                break;
            }
        }
        return a;
    }
}
