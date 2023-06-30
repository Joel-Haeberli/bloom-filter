/**
 * To understand bloom filters better I decided to implement them in a very basic version.
 *
 * This work is based on https://datatracker.ietf.org/doc/html/draft-summermatter-set-union
 */
fun main() {

    val testSet = (1..10).map { Bucket() }.toSet()
    assert(testSet.size == 10)

    // BASIC BLOOM FILTER
    val elements = setOf(1,2,3,4,5,6,7,8,9,10)
    val config = BloomFilterConfiguration(elements, 128, 3)
    val basicFilter = BloomFilter(config)

    basicFilter.add(11)

    (1..11).forEach {
        assert(basicFilter.containsProbably(it))
    }


    // COUNTING BLOOM FILTER
    val countingElements = setOf(1,2,3)
    val countingConfig = BloomFilterConfiguration(countingElements, 128, 3)
    val cbf = CountingBloomFilter(countingConfig)

    cbf.add(4)
    (1..4).forEach {
        assert(cbf.containsProbably(it))
        cbf.remove(it)
        assert(!cbf.containsProbably(it))
    }


}