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
class Path (private val nearestMine: NearestMine, path: List<Int>) {
            private var path = ArrayDeque<Int>()
            private var currentPosition: Int = -1
            private var next: Int = -1

    data class NearestMine(val name: Int, val nearest: Int) {
        override fun hashCode(): Int {
            return super.hashCode()
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other?.javaClass != javaClass) return false
            other as NearestMine
            if (other.nearest == this.name && other.name == this.nearest
            || other.nearest == this.nearest && other.name == this.name)
                return true
            return super.equals(other)
        }

    }

    init {
        setPathNew(path)
        //currentPosition = this.path.poll() ?: -1
        //next = this.path.poll() ?: -1
    }

    fun checkPath() = path.isEmpty()

    fun getStart(): Int = currentPosition

    fun getFinal(): Int = nearestMine.nearest

    fun getPathCost() = path.size

    fun getNearest() = nearestMine.nearest

    fun getNextRiver(): Pair<Int, Int> {
        if (currentPosition == -1 && next == -1 && path.isNotEmpty()) {
            currentPosition = path.poll()
            next = path.poll() ?: -1
            return Pair(currentPosition, next)
        }
        currentPosition = next
        next = path.poll() ?: -1
        return Pair(currentPosition, next)
    }

    /**
     * Получить текущую речку, которую нужно захватить
     */
    fun getCurrentRiver() = Pair(currentPosition, next)

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
        return "\nMine ${nearestMine.name} NearestMine ${nearestMine.nearest} path: $path"
    }
}