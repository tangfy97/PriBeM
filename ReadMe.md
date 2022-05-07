**This repo hosts the source code of Privacy-sensitive Behaviour Miner (PriBeM) for Java/Android applications.**

Privacy-sensitive Behaviour Miner (PriBeM) is a semi-automatic tool to find privacy-relevant behaviours in programs.

Samples used for the experiments are listed under folder `data\` .

The manually annotated BOM and EOM files are located under `basic\` .

Core algorithm for this detector is described under [`src\pribem\Core.java` ](https://github.com/feiyangtang97/PriBeM/blob/main/src/pribem/Core.java).

This algorithm aims to perform the following tasks:
1. Output a list of COIs (class of interests, where source methods are found in its invocations)
2. Output the call-graphs stemming from each source methods in the COIs
    - If one of the edges in the call-graph built points to a method that was decalred in another class, also as known as a global flow, we move to build a call-graph stemming from the caller in another class. This allows us to build a inter-component call-graph and provides more information.
    - If one of the edges in the call-graph built points to a source method, we do not build a call-graph on it but print this out. When another source method appears in a call-graph that stems from a source method, this might indicate that there is a data aggregation/accumulation happening.

### Example Output for toyForTest.zip/toyForTest.jar under folder `data\` :

There are one sources here: `java.util.Scanner: int nextInt()>` . We aim to build the path `nextInt() -> getter() -> bar() -> foo() -> doStuff()` .

```
Start inspections for class: toyForTest.data
Source found in the callgraph: <java.util.Scanner: int nextInt()>...
Start traversal: 

Starting from method: <java.util.Scanner: int nextInt()>
<1: <java.util.Scanner: int nextInt()> -> <toyForTest.data: int getter()>>
Global flow detected: <java.util.Scanner: int nextInt()> -> <toyForTest.data: int getter()>

Adding connections to callgraphs in class: toyForTest.data

***************************
Now we build call graphs for class: toyForTest.data
Continue with method: <toyForTest.data: int getter()>

strict digraph G {
  1 [ label="<toyForTest.data: int getter()>" ];
  2 [ label="<toyForTest.A: void bar()>" ];
  1 -> 2;
}

<2: <toyForTest.data: int getter()> -> <toyForTest.A: void bar()>>
Global flow detected: <toyForTest.data: int getter()> -> <toyForTest.A: void bar()>

Adding connections to callgraphs in class: toyForTest.A

***************************
Now we build call graphs for class: toyForTest.A
Continue with method: <toyForTest.A: void bar()>

strict digraph G {
  1 [ label="<toyForTest.A: void bar()>" ];
  2 [ label="<toyForTest.A: void foo()>" ];
  3 [ label="<toyForTest.Main: void doStuff()>" ];
  1 -> 2;
  2 -> 3;
}

Flows from <java.util.Scanner: int nextInt()> is finished.
/////////////////////////////////////

strict digraph G {
  1 [ label="<java.util.Scanner: int nextInt()>" ];
  2 [ label="<toyForTest.data: int getter()>" ];
  3 [ label="<toyForTest.A: void bar()>" ];
  4 [ label="<toyForTest.A: void foo()>" ];
  5 [ label="<toyForTest.Main: void doStuff()>" ];
  1 -> 2;
  2 -> 3;
  3 -> 4;
  4 -> 5;
}

Loaded 8 methods from JAR files. 

Found 1 Source Methods.
Found 1 Sink Methods.
Sources and Sinks collected. 

Methods extraction finished.
All finished.
```
