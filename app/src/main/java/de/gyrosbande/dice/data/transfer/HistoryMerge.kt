package de.gyrosbande.dice.data.transfer

/** What an import did - shown to the user afterwards. */
data class MergeReport(
    val importedRounds: Int,
    /** Rounds that were already here, but corrected on the other phone. */
    val updatedRounds: Int,
    val skippedRounds: Int,
    val newPlayers: List<String>,
)

/** The plan: which rounds to insert, which to replace, which players to create. */
data class MergePlan(
    val roundsToImport: List<ExportRound>,
    /** Already here in an older version - replaced wholesale. */
    val roundsToReplace: List<ExportRound>,
    val playersToCreate: List<String>,
    val report: MergeReport,
)

/**
 * Pure merge planning for history imports. Idempotent by design: rounds are
 * deduplicated by their uuid, so importing the same file twice (or exports
 * from several phones in any order) never creates duplicates.
 *
 * Rounds can be corrected after the fact (someone joined late), so a known
 * uuid is not automatically a reason to skip: if the incoming round was
 * changed more recently than the local one, it replaces it. Without that,
 * a correction would silently never reach the other phones.
 *
 * Players are matched by name, case-insensitively and trimmed - "Marcel"
 * and "marcel " are the same person. Missing players are created.
 */
object HistoryMerge {

    fun plan(
        import: HistoryExport,
        /** uuid -> updatedAt of the rounds already on this device. */
        existingRounds: Map<String, Long?>,
        existingPlayerNames: Collection<String>,
    ): MergePlan {
        val newRounds = mutableListOf<ExportRound>()
        val replacedRounds = mutableListOf<ExportRound>()
        var skipped = 0

        for (round in import.rounds.distinctBy { it.uuid }) {
            when {
                round.uuid !in existingRounds -> newRounds += round
                isNewerThanLocal(round, existingRounds[round.uuid]) -> replacedRounds += round
                else -> skipped++
            }
        }

        val knownNames = existingPlayerNames.map { normalize(it) }.toMutableSet()
        val playersToCreate = mutableListOf<String>()
        // Only players that actually appear in imported rounds matter; the
        // player list in the file is a bonus for empty-round exports. Replaced
        // rounds count too - the correction may be exactly the new player.
        val importedNames = (newRounds + replacedRounds)
            .flatMap { round -> round.results.map { it.playerName } } +
            import.players.map { it.name }
        for (name in importedNames) {
            val trimmed = name.trim()
            if (trimmed.isNotEmpty() && knownNames.add(normalize(trimmed))) {
                playersToCreate += trimmed
            }
        }

        return MergePlan(
            roundsToImport = newRounds,
            roundsToReplace = replacedRounds,
            playersToCreate = playersToCreate,
            report = MergeReport(
                importedRounds = newRounds.size,
                updatedRounds = replacedRounds.size,
                skippedRounds = skipped,
                newPlayers = playersToCreate.toList(),
            ),
        )
    }

    /**
     * Only a strictly newer round wins. Equal timestamps mean the same
     * version - that keeps re-importing the same file a no-op. A round
     * without a timestamp (older app version) never overwrites a local one.
     */
    private fun isNewerThanLocal(incoming: ExportRound, localUpdatedAt: Long?): Boolean {
        val incomingUpdatedAt = incoming.updatedAt ?: return false
        return localUpdatedAt == null || incomingUpdatedAt > localUpdatedAt
    }

    private fun normalize(name: String) = name.trim().lowercase()
}
