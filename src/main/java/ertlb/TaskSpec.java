package ertlb;

/** Description d'une tâche : complexité c(t) et type (composition). */
public final class TaskSpec {
    public final int id;
    public final int complexity;   // c(t), sans unité
    public final String type;      // simple | composee | complexe

    public TaskSpec(int id, int complexity, String type) {
        this.id = id; this.complexity = complexity; this.type = type;
    }
}
