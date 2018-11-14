package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River
import java.util.*

class MinesAndRivers {
    companion object {
        val mapOfMines: TreeMap<Int, MutableSet<River>> = TreeMap()
    }
}

class Intellect(val state: State, val protocol: Protocol) {

    private val setOfMines = mutableSetOf<Int>()
    private var nextMoveEnable = false

    fun makeMove() {


        if (MinesAndRivers.mapOfMines.isEmpty()) {
            state.mines.map { m ->
                val tryToFindNearRivers = state.rivers.filter { (river, riverState) ->
                    (river.source == m || river.target == m) && riverState == RiverState.Neutral
                }
                val k = mutableSetOf<River>()
                k.addAll(tryToFindNearRivers.keys)
                MinesAndRivers.mapOfMines[m] = k
            }
        } else {
            MinesAndRivers.mapOfMines.values.map { riverMap ->
                riverMap.removeIf { state.rivers[it] != RiverState.Neutral }
            }
        }

        val river = nextMove()
        if (river != null && nextMoveEnable) move(river)

        println("ya vibralsya")
        // Если река между двумя шахтами - берём
        val try0 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in state.mines && river.target in state.mines)
        }
        if (try0 != null) return move(try0.key.source, try0.key.target)

        // Если есть свободная река около шахты - берём
        val try1 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in state.mines || river.target in state.mines)
        }
        if (try1 != null) return move(try1.key.source, try1.key.target)

        // Здеся храняться данные о наших и чужих точках
        val ourSites = state.our.sites
        val enemySites = state.enemy.sites

        // Если есть свободная река между нашими точками - берём
        val try2 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in ourSites && river.target in ourSites)
        }
        if (try2 != null) return move(try2.key.source, try2.key.target)

        // Если есть свободная река около нашеё точки - берём (с учётом тупика!)
        val try3 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in ourSites || river.target in ourSites)
        }
        if (try3 != null && !deadEnd(try3)) return move(try3.key.source, try3.key.target)

        // Если есть река между двух вражеских точек - берём
        val try4 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && river.source in enemySites && river.target in enemySites
        }
        if (try4 != null) return move(try4.key.source, try4.key.target)

        // Если есть точка около вражеской точки - берём
        val try5 = state.rivers.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source in enemySites || river.target in enemySites)
        }
        if (try5 != null) return move(try5.key.source, try5.key.target)

        // Берём любую реку (с учётом тупика!)
        val try6 = state.rivers.entries.find { (_, riverState) ->
            riverState == RiverState.Neutral
        }
        if (try6 != null && !deadEnd(try6)) return move(try6.key.source, try6.key.target)

        // (╯°□°)╯ ┻━┻
        protocol.passMove()
    }

    private fun nextMove(): River? {
        var source = -1
        var min = 10000
        if (setOfMines.size == state.mines.size) nextMoveEnable = false
        for (mine in MinesAndRivers.mapOfMines) {
            if (mine.value.size < min && mine.key !in setOfMines) {
                source = mine.key
                min = mine.value.size
            }
        }
        if (source != -1) {
            val river = MinesAndRivers.mapOfMines.getValue(source).first()
            setOfMines.add(source)
            nextMoveEnable = true
            return river
        }
        return null
    }

    // функция, которая проверяет реку на тупиковость, то есть нет иного нейтрального выхода
    private fun deadEnd(river: MutableMap.MutableEntry<River, RiverState>): Boolean {
        val end = river.key.target
        val filtered = state.rivers.filter { it.key.source == end || it.key.target == end }
        val ourTry = filtered.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source != end || river.target != end)
        }
        return ourTry == null
    }

    // функция ради функции
    private fun move(source: Int, target: Int) {
        protocol.claimMove(source, target)
    }

    private fun move(river: River) {
        protocol.claimMove(river.source, river.target)
    }
}
