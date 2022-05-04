This repo hosts the source code of Privacy-sensitive Behaviour Miner (PriBM) for Java/Android applications.

Privacy-sensitive Behaviour Miner (PriBM) is a semi-automatic tool to find privacy-relevant behaviours in programs.

Samples used for the experiments are listed under folder `data\`.

The manually annotated BOM and EOM files are located under `basic\`.

Main algorithm for this detector is described under [`src\pribm\ReadClasses.java`](https://github.com/feiyangtang97/PriBM/blob/main/src/pribm/ReadClasses.java).

This algorithm aims to perform the following tasks:
1. Output a list of COIs (class of interests, where source methods are found in its invocations)
2. Output the call-graphs stemming from each source methods in the COIs
    - If one of the edges in the call-graph built points to a method that was decalred in another class, also as known as a global flow, we move to build a call-graph stemming from the caller in another class. This allows us to build a inter-component call-graph and provides more information.
    - If one of the edges in the call-graph built points to a source method, we do not build a call-graph on it but print this out. When another source method appears in a call-graph that stems from a source method, this might indicate that there is a data aggregation/accumulation happening.
