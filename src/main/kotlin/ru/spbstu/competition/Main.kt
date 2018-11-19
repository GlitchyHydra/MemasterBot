package ru.spbstu.competition

import org.kohsuke.args4j.CmdLineParser
import org.kohsuke.args4j.Option
import ru.spbstu.competition.game.Graph
import ru.spbstu.competition.game.Intellect
import ru.spbstu.competition.game.State
import ru.spbstu.competition.protocol.Protocol
import ru.spbstu.competition.protocol.data.*

object Arguments {
    @Option(name = "-u", usage = "Specify server url")
    var url: String = "kotoed.icc.spbstu.ru"

    @Option(name = "-p", usage = "Specify server port")
    var port: Int = 50006

    fun use(args: Array<String>): Arguments =
            CmdLineParser(this).parseArgument(*args).let{ this }
}

fun main(args: Array<String>) {
    Arguments.use(args)

    println("Hello")

    // Протокол обмена с сервером
    val protocol = Protocol(Arguments.url, Arguments.port)
    // Состояние игрового поля
    val gameState = State()

    protocol.handShake("CordOfGlitches")

    val setupData = protocol.setup()
    gameState.init(setupData)

    println("Received id = ${setupData.punter}")

    protocol.ready()

    println("make graph")
    // Джо очень умный чувак, вот его ум
    val intellect = Intellect(gameState, protocol, Graph(gameState))
    println(intellect.graph.getV())
    println("graph maded")

    gameloop@ while(true) {
        val message = protocol.serverMessage()
        when(message) {
            is GameResult -> {
                println("The game is over!")
                val myScore = message.stop.scores[protocol.myId]
                println("I'm scored ${myScore.score} points!")
                break@gameloop
            }
            is Timeout -> {
                println("Connection Timeout")
            }
            is GameTurnMessage -> {
                for(move in message.move.moves) {
                    when(move) {
                        is PassMove -> {}
                        is ClaimMove -> gameState.update(move.claim)
                    }
                }
            }
        }
        println("Make glitches")
        intellect.makeMove()
        println("Made")
    }
}
