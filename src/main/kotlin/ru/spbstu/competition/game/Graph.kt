package ru.spbstu.competition.game

import java.util.*

class Graph(gameState: State) {

    private val connections = mutableMapOf<Int, Set<Int>>()

    init {
        gameState.rivers.keys.forEach { addRiver(it.source, it.target) }
    }

    fun getConnections() = connections

    fun removeRiver(source: Int, target: Int) {
        connections[source] = connections[source]?.let { it - target } ?: setOf()
        connections[target] = connections[target]?.let { it - source } ?: setOf()
    }

    private fun addRiver(source: Int, target: Int) {
        connections[source] = connections[source]?.let { it + target } ?: setOf(target)
        connections[target] = connections[target]?.let { it + source } ?: setOf(source)
    }

    private fun getNeighbors(v: Int): Set<Int> {
        return connections[v] ?: return emptySet()
    }

    //operator fun get(name: Int) = vertices[name] ?: throw IllegalArgumentException()

    fun bfs(start: Int, mines: List<Int>): Int {
        val queue = ArrayDeque<Int>()
        queue.add(start)
        val visited = mutableSetOf(start)
        while (queue.isNotEmpty()) {
            val next = queue.poll()
            if (mines.contains(next) && next != start) return next
            for (neighbor in connections[next]!!) {
                if (neighbor in visited) continue
                visited.add(neighbor)
                queue.add(neighbor)
            }
        }
        return -1
    }

    class VertexInfo(
            val vertex: Int,
            val distance: Int,
            val prev: Int?
    ) : Comparable<VertexInfo> {
        override fun compareTo(other: VertexInfo): Int {
            return distance.compareTo(other.distance)
        }
    }

    fun shortestPath(from: Int): Map<Int, VertexInfo> {
        val info = mutableMapOf<Int, VertexInfo>()
        for (vertex in this.connections.keys) {
            info[vertex] = VertexInfo(vertex, Int.MAX_VALUE, null)
        }
        val fromInfo = VertexInfo(from, 0, null)
        val queue = PriorityQueue<VertexInfo>()
        queue.add(fromInfo)
        info[from] = fromInfo
        while (queue.isNotEmpty()) {
            val currentInfo = queue.poll()
            val currentVertex = currentInfo.vertex
            for (vertex in this.getNeighbors(currentVertex)) {
                val weight = if (connections[currentVertex]!!.contains(vertex)) 1
                else null
                if (weight != null) {
                    val newDistance = info[currentVertex]!!.distance + weight
                    if (info[vertex]!!.distance > newDistance) {
                        val newInfo = VertexInfo(vertex, newDistance, currentVertex)
                        queue.add(newInfo)
                        info[vertex] = newInfo
                    }
                }
            }
        }
        return info
    }
}