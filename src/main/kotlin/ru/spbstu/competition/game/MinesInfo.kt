package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.data.River

class MinesInfo(private val mine: Int,
                val riverNearMines: MutableSet<River>) {

    private var nearMine = -1

    fun riversCount() = riverNearMines.size

    fun removeEnemyRivers(riversMap: Map<River, RiverState>) {
        riverNearMines.removeAll(riversMap.filter { (_, value) -> value == RiverState.Enemy }.keys)
    }

    fun setNearMine(nearMine: Int) {
        this.nearMine = nearMine
    }

    fun getRivers() = riverNearMines

    override fun toString(): String {
        return nearMine.toString()
    }
}