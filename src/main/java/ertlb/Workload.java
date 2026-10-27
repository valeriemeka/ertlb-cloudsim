package ertlb;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Génération reproductible (graine) de la charge et de l'infrastructure. */
public final class Workload {

    /** MI par unité de complexité : length(t) = c(t) * MI_PER_UNIT. */
    public static final long MI_PER_UNIT = 100;
    /** MIPS correspondant à une capacité relative Cap(v) = 1,0. */
    public static final double MIPS_PER_CAP = 1000.0;

    private Workload() { }

    /** Tâches : simple 10-25 (45 %), composée 26-50 (35 %), complexe 51-100 (20 %). */
    public static List<TaskSpec> tasks(int n, Random rnd) {
        List<TaskSpec> list = new ArrayList<>(n);
        for (int i = 0; i < n; i++) {
            double u = rnd.nextDouble();
            if (u < 0.45)      list.add(new TaskSpec(i, 10 + rnd.nextInt(16), "simple"));
            else if (u < 0.80) list.add(new TaskSpec(i, 26 + rnd.nextInt(25), "composee"));
            else               list.add(new TaskSpec(i, 51 + rnd.nextInt(50), "complexe"));
        }
        return list;
    }

    /** Capacités relatives hétérogènes Cap(v) dans [0,5 ; 1,3]. */
    public static double[] capacities(int m, Random rnd) {
        double[] cap = new double[m];
        for (int v = 0; v < m; v++) cap[v] = Math.round((0.5 + 0.8 * rnd.nextDouble()) * 100.0) / 100.0;
        return cap;
    }
}
