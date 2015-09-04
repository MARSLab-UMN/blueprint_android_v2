#ifndef   DIJKSTRA_H
#define   DIJKSTRA_H

#include <stdio.h>
#include <stdlib.h>
#include <limits.h>
#include <iostream>
#include <vector>
#include <algorithm>

#include <libdijkstra.h>

using namespace std;

class Dijkstra {
  public:
    // A structure to represent a node in adjacency list
    struct AdjListNode
    {
        int dest;
        float weight;
        struct AdjListNode* next;
    };
     
    // A structure to represent an adjacency liat
    struct AdjList
    {
        struct AdjListNode *head;  // pointer to head node of list
    };
     
    // A structure to represent a graph. A graph is an array of adjacency lists.
    // Size of array will be V (number of vertices in graph)
    struct Graph
    {
        int V;
        struct AdjList* array;
    };

    // Structure to represent a min heap node
    struct MinHeapNode
    {
        int  v;
        int dist;
    };
     
    // Structure to represent a min heap
    struct MinHeap
    {
        int size;      // Number of heap nodes present currently
        int capacity;  // Capacity of min heap
        int *pos;     // This is needed for decreaseKey()
        struct MinHeapNode **array;
    };
 
     
    Graph* graph;
    std::vector<int> path;

    Dijkstra(int V);

    // A utility function to create a new adjacency list node
    struct AdjListNode* newAdjListNode(int dest, float weight);
     
    // A utility function that creates a graph of V vertices
    void createGraph(int V);
     
    // Adds an edge to an undirected graph
    void addEdge(int src, int dest, float weight);
     
    // A utility function to create a new Min Heap Node
    struct MinHeapNode* newMinHeapNode(int v, int dist);
     
    // A utility function to create a Min Heap
    struct MinHeap* createMinHeap(int capacity);
     
    // A utility function to swap two nodes of min heap. Needed for min heapify
    void swapMinHeapNode(struct MinHeapNode** a, struct MinHeapNode** b);
     
    // A standard function to heapify at given idx
    // This function also updates position of nodes when they are swapped.
    // Position is needed for decreaseKey()
    void minHeapify(struct MinHeap* &minHeap, int idx);
     
    // A utility function to check if the given minHeap is ampty or not
    int isEmpty(struct MinHeap* &minHeap);
     
    // Standard function to extract minimum node from heap
    struct MinHeapNode* extractMin(struct MinHeap* &minHeap);
     
    // Function to decreasy dist value of a given vertex v. This function
    // uses pos[] of min heap to get the current index of node in min heap
    void decreaseKey(struct MinHeap* &minHeap, int v, int dist);
     
    // A utility function to check if a given vertex
    // 'v' is in min heap or not
    bool isInMinHeap(struct MinHeap* &minHeap, int v);
     
    // A utility function to print the constructed MST stored in parent[]
    int printPath(int parent[], float cost[], int n, int src, int dst);
     
    // The main function that calulates distances of shortest paths from src to all
    // vertices. It is a O(ELogV) function
    void dijkstra(int src, int dst);

};
#endif /* !DIJKSTRA_H */