package de.gyrosbande.dice.data.transfer

/** What an import did - shown to the user afterwards. */
data class MergeReport(
    val importedRounds: Int,
    val skippedRounds: Int,
    val newPlayers: List<String>,
)

/** The plan: which rounds to insert and which players to create. */
data class MergePlan(
    val roundsToImport: List<ExportRound>,
    val playersToCreate: List<String>,
    val report: MergeReport,
)

/**
 * Pure merge planning for history imports. Idempotent by design: rounds are
 * deduplicated by their uuid, so importing the same file twice (or exports
 * from several phones in any order) never creates duplicates.
 *
 * Players are matched by name, case-insensitively and trimmed - "Marcel"
 * and "marcel " are the same person. Missing players are created.
 */
object HistoryMerge {

    fun plan(
        import: HistoryExport,
        existingRoundUuids: Set<String>,
        existingPlayerNames: Collection<String>,
    ): MergePlan {
        val (newRounds, knownRounds) = import.rounds
            .distinctBy { it.uuid }
            .partition { it.uuid !in existingRoundUuids }

        val knownNames = existingPlayerNames.map { normalize(it) }.toMutableSet()
        val playersToCreate = mutableListOf<String>()
        // Only players that actually appear in imported rounds matter; the
        // player list in the file is a bonus for empty-round exports.
        val importedNames = newRounds.flatMap { round -> round.results.map { it.playerName } } +
            import.players.map { it.name }
        for (name in importedNames) {
            val trimmed = name.trim()
            if (trimmed.isNotEmpty() && knownNames.add(normalize(trimmed))) {
                playersToCreate += trimmed
            }
        }

        return MergePlan(
            roundsToImport = newRounds,
            playersToCreate = playersToCreate,
            report = MergeReport(
                importedRounds = newRounds.size,
                skippedRounds = knownRounds.size,
                newPlayers = playersToCreate.toList(),
            ),
        )
    }

    private fun normalize(name: String) = name.trim().lowercase()
}
