package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River
import java.util.*

class Intellect(val state: State, val protocol: Protocol) {

    fun makeMove() {
        // Joe is like super smart!
        // Da best strategy ever!
        val b = state.rivers.keys
        val a = findMinimalRoad(b.first().source, b.last().source)
        println("${b.first().source}:${b.last().source} [${a.values.forEach { print("[${it.prev}, ${it.vertex}, ${it.distance}], ") }}]")
        val try0 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in state.mines && river.target in state.mines)
        }
        if (try0 != null) return protocol.claimMove(try0.key.source, try0.key.target)

        // If there is a free river near a mine, take it!
        val try1 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in state.mines || river.target in state.mines)
        }
        if (try1 != null) return protocol.claimMove(try1.key.source, try1.key.target)

        // Look at all our pointsees
        val ourSites = state.our.sites
//        val enemySites = state.enemy.sites

        // If there is a river between two our pointsees, take it!
        val try2 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in ourSites && river.target in ourSites)
        }
        if (try2 != null) return protocol.claimMove(try2.key.source, try2.key.target)

        // If there is a river near our pointsee, take it!
        val try3 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in ourSites || river.target in ourSites)
        }
        if (try3 != null && !deadEnd(try3)) return protocol.claimMove(try3.key.source, try3.key.target)

//        val try4 = state.rivers.entries.find { (river, riverState) ->
//            riverState == RiverState.Neutral && river.source in enemySites && river.target in enemySites
//        }
//        if (try4 != null) return protocol.claimMove(try4.key.source, try4.key.target)
//
//        val try5 = state.rivers.entries.find { (river, riverState) ->
//            riverState == RiverState.Neutral && (river.source in enemySites || river.target in enemySites)
//        }
//        if (try5 != null) return protocol.claimMove(try5.key.source, try5.key.target)

        // Bah, take anything left
        val try6 = state.rivers.entries.find { (_, riverState) ->
            riverState == RiverState.Neutral
        }
        if (try6 != null && !deadEnd(try6)) return protocol.claimMove(try6.key.source, try6.key.target)

        // (╯°□°)╯ ┻━┻
        protocol.passMove()
    }

    private fun minePriority(): Int {
        val data = mutableMapOf<Int, Int>()
        for (mine in state.mines) {
            val rivers = state.rivers.entries.filter {
                (it.key.source == mine || it.key.target == mine) && it.value == RiverState.Neutral
            }
            data[mine] = rivers.size
        }
        var min = 10000
        var target = -1
        for (obj in data)
            if (obj.value < min) {
                min = obj.value
                target = obj.key
            }
        return target
    }

    private fun deadEnd(river: MutableMap.MutableEntry<River, RiverState>): Boolean {
        val end = river.key.target
        val begin = river.key.target

        val filtered = state.rivers.filter { it.key.source == end || it.key.target == end }
        val ourTry = filtered.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source != begin || river.target != begin)
        }

        return ourTry == null
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

    private fun findMinimalRoad(begin: Int, end: Int): Map<Int, VertexInfo> {
        val info = mutableMapOf<Int, VertexInfo>()
        for (vertex in getNeighbors(begin)) {
            info[vertex.target] = VertexInfo(vertex.target, 0, null)
        }
        val fromInfo = VertexInfo(begin, 0, null)
        val queue = PriorityQueue<VertexInfo>()
        queue.add(fromInfo)
        info[begin] = fromInfo
        var stop = false
        while (queue.isNotEmpty()) {
            val currentInfo = queue.poll()
            val currentVertex = currentInfo.vertex
            for (vertex in getNeighbors(currentVertex)) {
                if (vertex.source == end) stop = true
                val newDistance = info[currentVertex]!!.distance + 1
                if (info[vertex.source]!!.distance > newDistance) {
                    val newInfo = VertexInfo(vertex.source, newDistance, currentVertex)
                    queue.add(newInfo)
                    info[vertex.source] = newInfo
                }

            }
            if (stop) return info
        }
        return info
    }

    private fun getNeighbors(vertex: Int): List<River> = state
            .rivers
            .entries
            .filter { it.key.source == vertex && it.value == RiverState.Neutral }
            .flatMap { listOf(it.key) }

}

//ret Int
