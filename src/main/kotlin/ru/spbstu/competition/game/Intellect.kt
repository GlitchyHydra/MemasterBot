package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River
import java.util.*

class Intellect(val state: State, val protocol: Protocol, val graph: Graph) {

    /**
     * setOfPath - множество путей из каждой в шахты к ближашей шахте
     * listOfMines - множество в котором хранится информация о каждой шахте,
     * которая включает в себя какие речки около шахты и ее название
     */

    private var listOfMines = mutableListOf<MinesInfo>()
    private var firstTime = true
    private var isMinesConnected = false
    private val setOfPaths = mutableSetOf<Path>()
    private var currentPath: Path? = null
    private val listOfMadeMoves = mutableListOf<River>()

    /**
     * Для каждой шахты ищем ближайшую и ищем кратчайшие до нее путь
     */
    private fun createPathsBetweenMines() {
        val existedPathBetween = mutableSetOf<Path.NearestMine>()

        state.mines.forEach { mine ->
            val nearestMine = Path.NearestMine(mine, graph.bfs(mine, state.mines))
            if (nearestMine.nearest != -1) {
                if (!existedPathBetween.contains(nearestMine)) {
                    val p = graph.shortestPath(mine).unrollPath(nearestMine.nearest)
                    val path = Path(nearestMine, p)
                    setOfPaths.add(path)
                    existedPathBetween.add(nearestMine)
                }
            }
        }
    }

    /**
     * по map в котором хранятся стоимость ближайшего пути до всех точек
     *
     */
    private fun Map<Int, Graph.VertexInfo>.unrollPath(to: Int): List<Int> {
        val result = mutableListOf<Int>()
        var current: Int? = to
        while (current != null) {
            result += current
            current = this[current]?.prev
        }
        result.reverse()
        return result
    }

    private fun captureOnPath() {

    }

    private fun removePath() {
        setOfPaths.remove(currentPath!!)
        if (setOfPaths.isNotEmpty())
            currentPath = setOfPaths.findWithMinPath()
        else isMinesConnected = true
    }

    private fun endOfPath(target: Int): Boolean {
        if (target == -1) {
            setOfPaths.remove(currentPath!!)
            if (setOfPaths.isNotEmpty())
                currentPath = setOfPaths.findWithMinPath()
            else {
                isMinesConnected = true
                currentPath = null
            }
            return true
        }
        return false
    }

    private fun repath(s: Int) {
        currentPath!!.setPathNew(
                graph.shortestPath(s).unrollPath(currentPath!!.getFinal())
        )
        println("новый путь ${currentPath.toString()}")
    }

    private fun conquer() {
        if (listOfMines.size == 1) firstTime = false
        if (listOfMines[0].riversCount() == 0 && listOfMines.size > 1) listOfMines.removeAt(0)
        val temp = listOfMines[0].riverNearMines.first()
        listOfMines.removeAt(0)
        listOfMadeMoves.add(temp)
        protocol.claimMove(temp.source, temp.target)
    }

    private fun listSort() {
        if (listOfMines.isNotEmpty()) {
            for (i in 1 until listOfMines.size) {
                val current = listOfMines[i]
                var j = i - 1
                while (j >= 0) {
                    if (listOfMines[j].riversCount() > current.riversCount())
                        listOfMines[j + 1] = listOfMines[j]
                    else
                        break
                    j--
                }
                listOfMines[j + 1] = current
            }
        }
    }


    private fun Graph.removeEnemyRivers() {
        state.rivers.filter { it.value == RiverState.Enemy }.keys
                .forEach { removeRiver(it.source, it.target) }
    }

    private fun Set<Path>.findWithMinPath(): Path {
        var min = Int.MAX_VALUE
        var path = this.first()
        for (p in this) {
            if (min > p.getPathCost()) {
                path = p
                min = p.getPathCost()
            }
        }
        return path
    }

    fun makeMove() {
        // Joe is like super smart!
        // Da best strategy ever!

        // If there is a free river near a mine, take it!

        when {
            /*mapOfMinesInfo.isEmpty() -> {
                for (mine in state.mines) {
                    val mi = MinesInfo(mine, state.rivers.keys)
                    mi.setNearMine(graph.bfs(mine, state.mines))
                    mapOfMinesInfo.add(mi)
                }
                println(mapOfMinesInfo)
            }*/
            setOfPaths.isEmpty() && !isMinesConnected -> {
                createPathsBetweenMines()
                if (setOfPaths.isEmpty()) isMinesConnected = true
                println(setOfPaths)
            }
            currentPath == null && !isMinesConnected -> {
                currentPath = setOfPaths.findWithMinPath()
            }
            else -> {
                println("before: ${graph.getConnections()}")
                graph.removeEnemyRivers()
                println("after1: ${graph.getConnections()}")
            }
        }

        if (listOfMines.isEmpty()) {
            if (firstTime) {
                for (mine in state.mines) {
                    val temp = state.rivers.filter { (river, type) ->
                        (river.target == mine || river.source == mine) && type == RiverState.Neutral
                    }.keys.toMutableSet()
                    listOfMines.add(MinesInfo(mine, temp))
                }
                listSort()
            }
        } else {
            for (it in listOfMines) {
                it.removeEnemyRivers(state.rivers)
                if (it.riversCount() == 0) listOfMines.remove(it)
            }
            listSort()
        }

        if (listOfMines.isNotEmpty()) return conquer()

        if (!setOfPaths.isEmpty() && currentPath != null) {

            val (s, t) = currentPath!!.getNextRiver()

            while (setOfPaths.isNotEmpty()) {
                if (isMinesConnected) break
                if (endOfPath(t)) {
                    println("end of path")
                    continue
                }

                var river = state.findRiver(s, t)
                var isCapturing = true

                while (state.rivers[river] != RiverState.Neutral) {
                    val (source, target) = currentPath!!.getNextRiver()
                    if (target == -1) {
                        isCapturing = false
                        removePath()
                        break
                    }
                    println("В цикле source: $source, target: $target")
                    if (state.rivers[river] == RiverState.Enemy) repath(source)
                    river = state.findRiver(source, target)
                }

                if (!isCapturing) continue

                println("capturing: $river")
                protocol.claimMove(river.source, river.target)
                return

            }
        }

        val try0 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in state.mines || river.target in state.mines)
        }
        if (try0 != null) return protocol.claimMove(try0.key.source, try0.key.target)

        // Look at all our pointsees
        val ourSites = state
                .rivers
                .entries
                .filter { it.value == RiverState.Our }
                .flatMap { listOf(it.key.source, it.key.target) }
                .toSet()

        // If there is a river between two our pointsees, take it!
        val try1 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in ourSites && river.target in ourSites)
        }
        if (try1 != null) return protocol.claimMove(try1.key.source, try1.key.target)

        // If there is a river near our pointsee, take it!
        val try2 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in ourSites || river.target in ourSites)
        }
        if (try2 != null) return protocol.claimMove(try2.key.source, try2.key.target)

        // Bah, take anything left
        val try3 = state.rivers.entries.find { (_, riverState) ->
            riverState == RiverState.Neutral
        }
        if (try3 != null) return protocol.claimMove(try3.key.source, try3.key.target)

        // (╯°□°)╯ ┻━┻
        protocol.passMove()
    }

}

//ret Int
