package ertlb;

/** Résultats d'une exécution (une stratégie, une graine). */
public final class Metrics {
    public double avgResponseTime;   // moyenne de (fin - soumission), en s de simulation
    public double makespan;          // date de fin de la dernière tâche
    public double utilization;       // % : somme des temps CPU / (m * makespan)
    public double stdLoad;           // écart-type des temps d'occupation par VM
}
