package ertlb;

import org.cloudbus.cloudsim.Log;

import java.io.PrintWriter;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Usage : java ertlb.Main [runs=30] [vms=20] [tasks=1000] [seed0=5000] [out=results/results.csv]
 * Les trois stratégies sont évaluées sur la MÊME charge et la MÊME infrastructure pour chaque graine
 * (plan apparié -> test t apparié).
 */
public class Main {
    public static void main(String[] args) throws Exception {
        int runs  = args.length > 0 ? Integer.parseInt(args[0]) : 30;
        int vms   = args.length > 1 ? Integer.parseInt(args[1]) : 20;
        int tasks = args.length > 2 ? Integer.parseInt(args[2]) : 1000;
        int seed0 = args.length > 3 ? Integer.parseInt(args[3]) : 5000;
        String out = args.length > 4 ? args[4] : "results/results.csv";

        Log.setDisabled(true);
        Locale.setDefault(Locale.ROOT);
        java.io.File f = new java.io.File(out);
        if (f.getParentFile() != null) f.getParentFile().mkdirs();

        try (PrintWriter w = new PrintWriter(f, "UTF-8")) {
            w.println("seed,vms,tasks,algorithm,avg_response_time,makespan,utilization_pct,std_load");
            for (int r = 0; r < runs; r++) {
                int seed = seed0 + r;
                Random rnd = new Random(seed);
                double[] cap = Workload.capacities(vms, rnd);
                List<TaskSpec> list = Workload.tasks(tasks, rnd);
                for (Strategy s : Strategy.values()) {
                    int[] a = Allocator.allocate(s, list, cap);
                    Metrics m = Simulation.run(s, list, cap, a);
                    w.printf("%d,%d,%d,%s,%.4f,%.4f,%.4f,%.4f%n", seed, vms, tasks, s,
                            m.avgResponseTime, m.makespan, m.utilization, m.stdLoad);
                }
                w.flush();
                System.out.printf("Graine %d terminée (%d/%d)%n", seed, r + 1, runs);
            }
        }
        System.out.println("Résultats écrits dans " + out);
    }
}
