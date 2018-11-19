package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.data.River
import java.util.ArrayDeque

/**
 * Класс Path нужен для перестроения пути, если какие-то реки на возможном
 * пути захватили враги, а также для того чтоб узнать, где мы находимся
 * Для каждой шахты создается отдельный объект
 * currentPosition - последняя захваченная точка(в начале это шахта)
 * nearestMine - ближайшая шахта, -1, если нет шахты куда можно прийти
 * rivers - ссылка на все реки
 * path - возможный путь по вершинам
 * next - следующая вершина в пути
 */
class Path (private var currentPosition:Int,
            private val nearestMine: NearestMine, path: List<Int>,
            private val rivers: Map<River, RiverState>) {
            private var path = ArrayDeque<Int>()
            private var next = this.path.poll()

    data class NearestMine(val name: Int, val nearest: Int)

    init {
        setPathNew(path)
    }

    fun getPathCost() = path.size

    fun getNearest() = nearestMine.nearest
    /**
     * Получить следующую речку
     */
    fun getNextRiver(): River {
        val river = rivers.keys.find { it.source == currentPosition
        && it.target == next || it.source == next && it.target == currentPosition}
        currentPosition = path.poll()
        next = path.poll()
        return river ?: throw IllegalArgumentException("Null pointer in getNextRiver")
    }

    /**
     * Получить текущую речку, которую нужно захватить
     */
    fun getCurrentRiver() = rivers.keys.find { it.source == currentPosition
            && it.target == next || it.source == next && it.target == currentPosition}

    /**
     * Перестроить путь
     */
    fun setPathNew(newPathList: List<Int>){
        val newPath = ArrayDeque<Int>()
        newPath.addAll(newPathList)
        path = newPath
    }

    /**
     * Узнать есть ли шахта к которой можно построить путь
     */
    private fun checkPossibilities(): Boolean = nearestMine.nearest != -1

    override fun toString(): String {
        return """
            Mine ${nearestMine.nearest} NearestMine ${nearestMine.nearest}
            path: $path
        """.trimIndent()
    }
}