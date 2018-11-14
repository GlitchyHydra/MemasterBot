package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.data.River

class MinesInfo(val mine: Int, val riverNearMines: MutableSet<River>) {
    fun riversCount() = riverNearMines.size

    fun removeEnemyRivers(riversMap: Map<River, RiverState>) {
        val temp = riversMap.filter { (key, value) ->
            (key.target == mine || key.source == mine) && value == RiverState.Enemy }.keys
        riverNearMines.removeAll(temp)
    }

    fun getRivers() = riverNearMines

    override fun toString(): String {
        return Pair(mine, riverNearMines.size).toString()
    }
}