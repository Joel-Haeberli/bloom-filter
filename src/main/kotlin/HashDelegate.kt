import org.bouncycastle.crypto.macs.KMAC
import org.bouncycastle.crypto.params.KeyParameter
import java.nio.ByteBuffer
import java.util.*
import kotlin.math.abs

// if set to true will print calculated bucket indices
const val PRINT_INDICES = true

// Instead of HMAC we use KMAC (based on SHA-3/Keccak).
private val KMAC = KMAC(128, null)

/**
 * delegate for hash function, so that all implementation use the same hash and the hash is easy exchangeable
 */
fun <E> hash(o: E) = o.hashCode() // for our use-case we just use the objects hash function, which should be could enough.

/**
 * map maps a given input to k buckets of type B
 */
fun <E, B> map(o: E, k: Int, buckets: Set<B>): Set<B> =
    (1..k)
        .map { hash(o) + it }
        .map { doHash(it, prepareHash()) }
        .map { it % buckets.size }
        .map { if (PRINT_INDICES) println("MAP: bucket index=${it}"); it }
        .map { buckets.elementAt(it) }
        .toSet()

private fun prepareHash(): KMAC {

    val notRand = Random()
    notRand.setSeed(1234567890)
    val seed = ByteArray(16) { notRand.nextInt().toByte() }

    KMAC.init(KeyParameter(seed))
    KMAC.reset()

    return KMAC
}

private fun doHash(inp: Int, hash: KMAC): Int {

    val buf = ByteBuffer.allocate(Int.SIZE_BYTES).array()

    hash.update(inp.toByte())

    hash.doFinal(buf, 0, Int.SIZE_BYTES)

    return abs(buf.sumOf { it.toInt() })
}