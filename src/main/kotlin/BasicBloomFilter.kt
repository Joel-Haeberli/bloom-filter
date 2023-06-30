import java.util.UUID

data class Bucket(var isOne: Boolean = false, private val id: UUID = UUID.randomUUID())

class BloomFilter<E>(private val config: BloomFilterConfiguration<E>) {

    private val buckets: Set<Bucket> = (1..config.numberOfBuckets).map { Bucket() }.toSet()

    init {
        config.elements.forEach(this::add)
    }

    fun add(e: E) =
        map(e, config.numberOfBucketsPerElement, buckets).forEach { b ->
            b.isOne = true
        }

    fun containsProbably(e: E) =
        map(e, config.numberOfBucketsPerElement, buckets)
            .map { if (it.isOne) 1 else 0 }
            .sum() == config.numberOfBucketsPerElement
}