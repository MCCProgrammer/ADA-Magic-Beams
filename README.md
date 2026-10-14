# ✨ Magic Beams — Analysis and Design of Algorithms

<p>
  <img alt="Java" src="https://img.shields.io/badge/Java-1f2328?style=flat-square&logo=openjdk&logoColor=white">
  <img alt="Course" src="https://img.shields.io/badge/Course-Analysis%20and%20Design%20of%20Algorithms%20(ADA)-1f2328?style=flat-square">
  <img alt="Year" src="https://img.shields.io/badge/2025%2F26-3rd%20year%2C%202nd%20semester-1f2328?style=flat-square">
</p>

Second assignment of *Análise e Desenho de Algoritmos* (Analysis and Design of Algorithms), Computer Science degree at **NOVA School of Science and Technology (FCT NOVA)**.

## About

A grid is crossed by **beams** — straight segments that, once released, slide forever in one direction (north, south, east or west). A beam can only be released if its whole path to the edge of the grid is empty. Given a band of consecutive columns that must be cleared, the program decides **which beams have to be released and in what order**:

- `Disaster` if it's impossible (some beams block each other in a cycle);
- `False alarm` if no beam is in the way;
- otherwise, the ids of the beams to release, in order (smallest id first when there's a choice).

## Solution

It's a **graph problem**:

1. Find the beams lying inside the chosen columns — they must be released.
2. With a **BFS**, follow each of those beams' exit paths: any beam in the way must be released first, which adds it (and its own path) to the search and creates a **dependency edge**.
3. Order the dependencies with **Kahn's topological sort** using a priority queue for the smallest-id tie-break; if a cycle remains, it's a `Disaster`.

Total time is **O(B · max(R, C))** for B beams on an R × C grid — every cell of every exit path has to be looked at once, so this is optimal.

`src/MagicBeamSystem.java` is the optimised version (the BFS and the graph construction are done in a single pass, with no per-beam allocations); `alternative/MagicBeamSystem.java` is my first, more straightforward version. Both give the same answers.

## Running it

Requires Java 8 or newer.

```bash
javac -d bin src/*.java
java -cp bin Main < input.txt
```

The input has the number of test cases, then for each one: `rows columns`, `numberOfColumnsToClear leftmostColumn`, the number of beams, and one line per beam with `row column length direction` (the beam starts at that cell and extends in its direction). Example:

```text
2
6 7
2 3
4
1 3 2 E
4 4 2 N
0 1 2 S
5 0 3 E
6 6
2 2
2
1 2 1 E
1 3 1 W
```

```text
1 2
Disaster
```

## What I learned

- Turning a simulation into a dependency graph
- BFS, topological sorting with Kahn's algorithm and cycle detection
- Complexity analysis and optimising the constant factors of a solution

## Author

**Miguel Carmo** ([@MCCProgrammer](https://github.com/MCCProgrammer)) — pair assignment with Tomás Ramos Alves ([@Alvesss04](https://github.com/Alvesss04)); this repository has my implementation.

---

<sub>The assignment statement is not included in this repository, as it belongs to the course's teaching staff. This is a student project shared for portfolio purposes.</sub>
