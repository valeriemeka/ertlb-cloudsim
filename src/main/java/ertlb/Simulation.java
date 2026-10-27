package ertlb;

import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.provisioners.*;

import java.util.*;

/** Une exécution CloudSim : 1 Datacenter, m VM, n cloudlets, une stratégie. */
public final class Simulation {

    private Simulation() { }

    public static Metrics run(Strategy strategy, List<TaskSpec> tasks, double[] cap, int[] assignment)
            throws Exception {

        int m = cap.length, n = tasks.size();
        CloudSim.init(1, Calendar.getInstance(), false);

        // --- Datacenter unique : hôtes assez nombreux pour héberger toutes les VM ---
        int nbHosts = m; // un hôte par VM : la politique VmAllocationPolicySimple ne peut alors pas refuser de VM
        List<Host> hosts = new ArrayList<>();
        for (int h = 0; h < nbHosts; h++) {
            List<Pe> pes = new ArrayList<>();
            for (int p = 0; p < 4; p++) pes.add(new Pe(p, new PeProvisionerSimple(2000)));
            hosts.add(new Host(h, new RamProvisionerSimple(32768), new BwProvisionerSimple(100000),
                    1_000_000, pes, new VmSchedulerSpaceShared(pes)));
        }
        DatacenterCharacteristics ch = new DatacenterCharacteristics("x86", "Linux", "Xen", hosts,
                0.0, 3.0, 0.05, 0.001, 0.0);
        Datacenter dc = new Datacenter("DC_1", ch, new VmAllocationPolicySimple(hosts),
                new LinkedList<Storage>(), 0);

        DatacenterBroker broker = new DatacenterBroker("Broker");
        int uid = broker.getId();

        // --- m VM hétérogènes (1 coeur, MIPS = 1000 x Cap(v)), ordonnanceur espace-partagé (file FIFO) ---
        List<Vm> vms = new ArrayList<>();
        for (int v = 0; v < m; v++)
            vms.add(new Vm(v, uid, cap[v] * Workload.MIPS_PER_CAP, 1, 512, 1000, 10000, "Xen",
                    new CloudletSchedulerSpaceShared()));
        broker.submitGuestList(vms);

        // --- n cloudlets, liés à la VM choisie par la stratégie ---
        List<Cloudlet> cls = new ArrayList<>();
        UtilizationModel full = new UtilizationModelFull();
        for (int t = 0; t < n; t++) {
            Cloudlet c = new Cloudlet(t, tasks.get(t).complexity * Workload.MI_PER_UNIT, 1, 300, 300,
                    full, full, full);
            c.setUserId(uid);
            cls.add(c);
        }
        broker.submitCloudletList(cls);
        for (int t = 0; t < n; t++) broker.bindCloudletToVm(t, assignment[t]);

        CloudSim.startSimulation();
        List<Cloudlet> done = broker.getCloudletReceivedList();
        CloudSim.stopSimulation();

        if (done.size() != n)
            throw new IllegalStateException(strategy + " : " + done.size() + "/" + n + " cloudlets terminés");

        double sumResp = 0, makespan = 0;
        double[] busy = new double[m];
        for (Cloudlet c : done) {
            double resp = c.getFinishTime() - c.getSubmissionTime();
            sumResp += resp;
            makespan = Math.max(makespan, c.getFinishTime());
            busy[c.getGuestId()] += c.getActualCPUTime();
        }
        double totalBusy = 0; for (double b : busy) totalBusy += b;
        double mean = totalBusy / m, var = 0;
        for (double b : busy) var += (b - mean) * (b - mean);

        Metrics r = new Metrics();
        r.avgResponseTime = sumResp / n;
        r.makespan = makespan;
        r.utilization = 100.0 * totalBusy / (m * makespan);
        r.stdLoad = Math.sqrt(var / m);
        return r;
    }
}
