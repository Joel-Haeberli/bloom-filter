
// if set to true, will print calculated bucket indices to stdout
const val PRINT_INDICES = false

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

    val stringElements = setOf("Hello", "World")
    val stringConfig = BloomFilterConfiguration(stringElements, 128, 16)
    val stringBasicFilter = BloomFilter(stringConfig)

    stringElements.forEach {
        assert(stringBasicFilter.containsProbably(it))
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


    // INVERTIBLE BLOOM FILTER
    val invElements = setOf(1,2,3,4,5)
    val invConfig = BloomFilterConfiguration(invElements, 128, 3)
    val ibfBase = InvertibleBloomFilter(invConfig)

    ibfBase.add(6)
    (1..6).forEach {
        assert(cbf.containsProbably(it))
    }

    val emptyIbf = ibfBase.diff(ibfBase)
    emptyIbf.getBuckets().forEach {
        assert(it.number == 0)
        assert(it.idSum == 0L)
        assert(it.hashSum == 0)
    }

    val invElements2 = setOf(2,3,4,5,6,7)
    val invConfig2 = BloomFilterConfiguration(invElements2, 128, 3)
    val ibf2 = InvertibleBloomFilter(invConfig2)

    val diffIbf = ibfBase.diff(ibf2)

    val difference = diffIbf.getBuckets().map {

        var differenceInBucket = 0

        if (it.number != 0) differenceInBucket++
        if (it.idSum != 0L) differenceInBucket++
        if (it.hashSum != 0) differenceInBucket++

        differenceInBucket

    }.sum()

    println("difference score: $difference (lower score -> less different)")
    assert(difference != 0)
}