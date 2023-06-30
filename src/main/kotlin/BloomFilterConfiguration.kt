data class BloomFilterConfiguration<E>(
    val elements: Set<E>,
    val numberOfBuckets: Int = 256,
    val numberOfBucketsPerElement: Int = 3
)