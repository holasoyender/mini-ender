package plugins.giveaway

class WinnerChooser(winners: Int, clickers: Array<String>, oldWinnerIds: Array<String>) {

    val result: List<String>

    init {
        result = if(winners > clickers.size) {
            listOf()
        } else {

            val winnerIds = mutableListOf<String>()
            val allClickers = clickers.toMutableList()
            val filteredClickers = allClickers.filter { !oldWinnerIds.contains(it) }.toMutableList()

            if (winners > filteredClickers.size) {
                listOf()
            } else {
                for (i in 0 until winners) {
                    val winner = filteredClickers.random()
                    winnerIds.add(winner)
                    filteredClickers.remove(winner)
                }

                winnerIds
            }
        }
    }
}