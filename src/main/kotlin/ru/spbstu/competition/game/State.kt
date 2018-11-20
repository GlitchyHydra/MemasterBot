package ru.spbstu.competition.game

import ru.spbstu.competition.protocol.data.Claim
import ru.spbstu.competition.protocol.data.River
import ru.spbstu.competition.protocol.data.Setup

enum class RiverState { Our, Enemy, Neutral }

class State {
    val rivers = mutableMapOf<River, RiverState>()
    var dotsCount: Int = 0
    var mines = listOf<Int>()
    var myId = -1
    val our = OurSites(setOf())
    val enemy = EnemySites(setOf())


    fun init(setup: Setup) {
        myId = setup.punter
        for (river in setup.map.rivers) {
            rivers[river] = RiverState.Neutral
        }

        for (mine in setup.map.mines) {
            mines += mine
        }

        dotsCount = rivers
                .entries
                .filter { it.value == RiverState.Neutral }
                .flatMap { listOf(it.key.source) }.size
    }

    fun findRiver(source: Int, target: Int): River = rivers.keys.find {
            it.source == source && it.target == target ||
                    it.source == target && it.target == source
        }!!


    fun update(claim: Claim) {
        rivers[River(claim.source, claim.target)] = when (claim.punter) {
            myId -> {
                our.sites += listOf(claim.source, claim.target)
                RiverState.Our
            }
            else -> {
                enemy.sites += setOf(claim.source, claim.target)
                RiverState.Enemy
            }
        }
    }
}

data class OurSites(var sites: Set<Int>)
data class EnemySites(var sites: Set<Int>)