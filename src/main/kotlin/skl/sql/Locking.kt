package skl.sql

data class LockingClause(
  val strength: LockingStrength,
  val tables: List<QualifiedName> = emptyList(),
  val waitOption: LockWaitOption = LockWaitOption.WAIT
)

enum class LockingStrength {
  FOR_UPDATE,
  FOR_NO_KEY_UPDATE,
  FOR_SHARE,
  FOR_KEY_SHARE
}

enum class LockWaitOption {
  WAIT,
  NOWAIT,
  SKIP_LOCKED
}
