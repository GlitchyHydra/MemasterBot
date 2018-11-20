package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.River

class Intellect(val state: State, val protocol: Protocol) {

    private val listOfMines = mutableListOf<MinesInfo>()
    private var firstTime = true
    private val listOfMadeMoves = mutableListOf<River>()

    fun makeMove() {

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
            for (mine in listOfMines) mine.removeEnemyRivers(state.rivers)
            listOfMines.removeIf { it.riversCount() == 0 }
            listSort()
        }

        if (listOfMines.isNotEmpty()) return conquer()

        // If there is a free river near a mine, take it!
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

    private fun conquer() {
        if (listOfMines.size == 1) firstTime = false
        if (listOfMines.first().riversCount() == 0 && listOfMines.size > 1) listOfMines.remove(listOfMines.first())
        val temp = listOfMines.first().riverNearMines.first()
        listOfMines.removeAt(0)
        listOfMadeMoves.add(temp)
        println(temp)
        protocol.claimMove(temp.source, temp.target)
    }

}
