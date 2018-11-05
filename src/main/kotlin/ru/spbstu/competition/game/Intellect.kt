package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River
import java.util.*

class MinesAndRivers {
    companion object {
        val mapOfMines: TreeMap<Int, MutableMap<River, RiverState>> = TreeMap()
    }
}

class Intellect(val state: State, val protocol: Protocol) {

    private val setOfMines = mutableSetOf<Int>()
    // private var haveNext = true

    fun makeMove() {


        if (MinesAndRivers.mapOfMines.isEmpty()) {
            state.mines.map { m ->
                val tryToFindNearRivers = state.rivers.filter { (river, riverState) ->
                    (river.source == m || river.target == m) && riverState == RiverState.Neutral
                }
                val k = HashMap<River, RiverState>()
                k.putAll(tryToFindNearRivers)
                MinesAndRivers.mapOfMines[m] = k
            }
        } else {
            MinesAndRivers.mapOfMines.values.map { riverMap ->
                riverMap.entries.removeIf { state.rivers[it.key] != RiverState.Neutral }
            }
        }

        val next = nextTurn()
        if (next != -1) {
            val temp = state.rivers.entries.find { (river, type) ->
                type == RiverState.Neutral && (river.source == next || river.target == next)
            }!!
            setOfMines.add(next)
            move(temp.key.source, temp.key.target)

        }

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

    private fun nextTurn(): Int {
        var target = -1
        var i = 1
        while (target == -1) {
            for (mine in MinesAndRivers.mapOfMines.keys)
                if (MinesAndRivers.mapOfMines.getValue(mine).size == i)
                    target = mine
            if (i > 30) break
            i++
        }
        return target
    }

    // вроде как она должна считать сколько рек около шахт, но не задалось
    private fun minePriority(state: State): List<Int> {
        val data = mutableMapOf<Int, Int>()
        val result = mutableListOf<Int>()
        for (mine in state.mines) {
            val rivers = state.rivers.entries.filter {
                (it.key.source == mine || it.key.target == mine) && it.value == RiverState.Neutral
            }
            data[mine] = rivers.size
        }
        var i = 1
        while (result.size != data.size) {
            for (obj in data)
                if (obj.value == i) result.add(obj.key)
            i++
        }
        return result
    }

    // функция, которая проверяет реку на тупиковость, то есть нет иного нейтрального выхода
    private fun deadEnd(river: MutableMap.MutableEntry<River, RiverState>): Boolean {
        val end = river.key.target
        val begin = river.key.target
        val filtered = state.rivers.filter { it.key.source == end || it.key.target == end }
        val ourTry = filtered.entries.find { (river, riverState) ->
            riverState == RiverState.Neutral && (river.source != begin || river.target != begin)
        }
        return ourTry == null
    }

    // функция ради функции
    private fun move(source: Int, target: Int) {
        protocol.claimMove(source, target)
    }

}
