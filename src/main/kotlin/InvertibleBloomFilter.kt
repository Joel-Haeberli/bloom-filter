import java.util.*

class IBFDecodeException : Exception("Invertible Bloom Filter Decode Exception. Decoding failed because involved buckets were not pure.")

data class InvertibleBucket(
    var number: Int = 0,
    var idSum: Long = 0,
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

    private var buckets: Set<InvertibleBucket> = (1..config.numberOfBuckets).map { InvertibleBucket() }.toSet()

    init {
        config.elements.forEach(this::add)
    }

    fun getBuckets() = buckets

    fun add(e: E) =
        map(e, config.numberOfBucketsPerElement, buckets).forEach {
            it.number++
            val idCalc = idCalculation(e)
            it.idSum = it.idSum xor idCalc
            it.hashSum = it.hashSum xor idHash(idCalc)
        }

    fun remove(e: E) =
        map(e, config.numberOfBucketsPerElement, buckets).forEach {
            it.number--
            val idCalc = idCalculation(e)
            it.idSum = it.idSum xor idCalc
            it.hashSum = it.hashSum xor idHash(idCalc)
        }

    fun decode(e: E): Int {

        val buckts = map(e, config.numberOfBucketsPerElement, buckets)

        buckts.forEach {
            if (!isPure(it)) {
                throw IBFDecodeException()
            }
        }

        return idHash(buckts.elementAt(0).idSum)
    }

    fun diff(ibf: InvertibleBloomFilter<E>) : InvertibleBloomFilter<E> {

        if (ibf.buckets.size != buckets.size) {
            throw IllegalArgumentException("IBF must be of same length (same size of buckets)")
        }

        if (ibf.config.numberOfBucketsPerElement != config.numberOfBucketsPerElement) {
            throw IllegalArgumentException("IBF must have same number of buckets per element")
        }

        // we don't check the map function here since
        // this is static due to our implementation

        val diffConf = BloomFilterConfiguration<E>(setOf(), config.numberOfBuckets, config.numberOfBucketsPerElement)
        val diffIbf = InvertibleBloomFilter(diffConf)
        diffIbf.buckets = setOf()

        for (bi in buckets.withIndex()) {

            val thisBucket = this.buckets.elementAt(bi.index)
            val otherBucket = ibf.buckets.elementAt(bi.index)

            val n = thisBucket.number - otherBucket.number
            val id = thisBucket.idSum xor otherBucket.idSum
            val hsh = thisBucket.hashSum xor otherBucket.hashSum

            val diffBucket = InvertibleBucket(n, id, hsh)

            diffIbf.buckets = diffIbf.buckets.plus(diffBucket)
        }

        return diffIbf
    }

    /**
     * ID Calculation according to [RFC](https://datatracker.ietf.org/doc/html/draft-summermatter-set-union#section-3.3.1)
     *
     * The Spec uses HMAC-SHA256 as hash, but we use KMAC.
     * In addition, we do not salt the key in this implementation.
     */
    private fun idCalculation(e: E) = doHash(longToByteArray(e.hashCode().toLong()), prepareHash())

    /**
     * HASH Calculation according to [RFC](https://datatracker.ietf.org/doc/html/draft-summermatter-set-union#name-hash-calculation-2)
     */
    private fun idHash(id: Long) = crc(id)

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