import java.util.*

class IBFDecodeException : Exception("Invertible Bloom Filter Decode Exception. Decoding failed because involved buckets were not pure.")

data class InvertibleBucket(
    var number: Int = 0,
    var idSum: Int = 0,
    var hashSum: Int = 0,
    private val id: UUID = UUID.randomUUID())

/**
 * For our use-case we assume that the element-id is
 * the hashcode produced by calling Object.hashcode().
 * If we need the hash of the id (for the HASHSUM) we
 * just call hashcode on the hashcode itself. This is
 * not perfect, but it works for the sake of the showcase
 */
class InvertibleBloomFilter<E>(private val config: BloomFilterConfiguration<E>) {

    private val buckets: Set<InvertibleBucket> = (1..config.numberOfBuckets).map { InvertibleBucket() }.toSet()

    init {
        config.elements.forEach(this::add)
    }

    fun add(e: E) =
        map(e, config.numberOfBucketsPerElement, buckets).forEach {
            it.number++
            it.idSum = it.idSum xor e.hashCode()
            it.hashSum = it.hashSum xor e.hashCode().hashCode()
        }

    fun remove(e: E) =
        map(e, config.numberOfBucketsPerElement, buckets).forEach {
            it.number--
            it.idSum = it.idSum xor e.hashCode()
            it.hashSum = it.hashSum xor e.hashCode().hashCode()
        }

    fun decode(e: E): Int {

        map(e, config.numberOfBucketsPerElement, buckets).forEach {
            if (!isPure(it)) {
                throw IBFDecodeException()
            }
        }

        return e.hashCode()
    }

    fun diff(ibf: InvertibleBloomFilter<E>) : InvertibleBloomFilter<E> {

        if (ibf.buckets.size != buckets.size) {
            throw IllegalArgumentException("IBF must be of same length (same size of buckets)")
        }

        if (ibf.config.numberOfBucketsPerElement != config.numberOfBucketsPerElement) {
            throw IllegalArgumentException("IBF must have same number of buckets per element")
        }

        // we don't check the map function here since this is static due to our implementation

        // TODO calculate DIFF IBF here

        return InvertibleBloomFilter(config)
    }

    private fun isPure(b: InvertibleBucket) : Boolean {

        if (b.number != 1 && b.number != -1) {
            return false
        }

        return b.hashSum == b.idSum.hashCode()
    }

    fun containsProbably(e: E) =
        map(e, config.numberOfBucketsPerElement, buckets)
            .map { if (it.number != 0) 1 else 0 }
            .sum() == config.numberOfBucketsPerElement
}