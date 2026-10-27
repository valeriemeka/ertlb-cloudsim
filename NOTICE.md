# Notice
Les fichiers sous src/main/java/org/cloudbus proviennent du projet CloudSim (The University of Melbourne et contributeurs), https://github.com/Cloudslab/cloudsim, version 7.0.1. Ils sont inclus sans modification, sous leur licence d'origine (voir les en-têtes de chaque fichier). Le code sous src/main/java/ertlb est celui de ce projet.

## Modification pour Java 17
CloudSim 7.0.1 utilise des méthodes de Java 21 (List.getFirst, getLast, removeFirst). Pour compiler avec Java 17, 17 appels ont été remplacés par leurs équivalents (get(0), get(size() - 1), remove(0)) dans Cloudlet, CloudletScheduler, DatacenterBroker, HostDynamicWorkload, VmScheduler et core/GuestEntity. Le comportement est inchangé ; les résultats sont identiques à ceux obtenus avec Java 21.
