package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.data.River

/**
 * @author Artem Strokov
 * поле mine - id шахты
 * поле riverNearMines - ссылка на изменяемое множество ближайших рек к шахте
 * riversCount() - количество ближайших к шахте рек
 * removeEnemyRivers - удалить вражеские ближайшие к шахте речки
 */
class MinesInfo(private val mine: Int,
                val riverNearMines: MutableSet<River>) {

    fun riversCount() = riverNearMines.size

    fun removeEnemyRivers(riversMap: Map<River, RiverState>) {
        val temp = riversMap.filter { (key, value) ->
            (key.target == mine || key.source == mine) && value == RiverState.Enemy }.keys
        riverNearMines.removeAll(temp)
    }

    //Это чисто для себя
    override fun toString(): String {
        return Pair(mine, riverNearMines.size).toString()
    }
}