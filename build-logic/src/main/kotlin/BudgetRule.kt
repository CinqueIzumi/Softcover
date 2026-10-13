internal data class BudgetRule(
    val glob: String,
    val token: String,
    val limit: Long,
    val unit: BudgetUnit,
)
