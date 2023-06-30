import java.util.UUID

data class CountingBucket(var number: Int = 0, private val id: UUID = UUID.randomUUID())

class CountingBloomFilter<E>(private val config: BloomFilterConfiguration<E>) {

    private val buckets: Set<CountingBucket> = (1..config.numberOfBuckets).map { CountingBucket() }.toSet()

    init {
        config.elements.forEach(this::add)
    }

    fun add(e: E) =
        map(e, config.numberOfBucketsPerElement, buckets).forEach {
            it.number++
        }

    fun remove(e: E) =
        map(e, config.numberOfBucketsPerElement, buckets).forEach {
            it.number--
        }

    fun containsProbably(e: E) =
        map(e, config.numberOfBucketsPerElement, buckets)
            .map { if (it.number > 0) 1 else 0 }
            .sum() == config.numberOfBucketsPerElement
}