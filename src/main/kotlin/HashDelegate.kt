import org.bouncycastle.crypto.macs.KMAC
import org.bouncycastle.crypto.params.KeyParameter
import java.nio.ByteBuffer
import java.util.*
import java.util.zip.CRC32
import kotlin.math.abs

// Instead of HMAC we use KMAC (based on SHA-3/Keccak).
private val KMAC = KMAC(256, null)

/**
 * delegate for hash function, so that all implementation use the same hash and the hash is easy exchangeable
 */
fun <E> hash(o: E) = o.hashCode() // for our use-case we just use the objects hash function, which should be could enough.

/**
 * map maps a given input to k buckets of type B
 *
 * Basically it's the implementation of the mapping function specified by the [RFC](https://datatracker.ietf.org/doc/html/draft-summermatter-set-union#section-3.3.2)
 * While the RFC suggests to return the id's of the bucket, here the buckets themselves are returned
 */
fun <E, B> map(o: E, k: Int, buckets: Set<B>): Set<B> =
    (1..k)
        .map { hash(o) + it } // needed to generate different hashes
        .map { doHash(longToByteArray(it.toLong()), prepareHash()) } // generates 'random' indices
        .map { (it % buckets.size).toInt() } // make indices fit into the valid range
        .map { if (PRINT_INDICES) println("MAP: bucket index=${it}"); it }
        .map { buckets.elementAt(it) }
        .toSet()

fun prepareHash(): KMAC {

    val notRand = Random()
    notRand.setSeed(1234567890)
    val key = ByteArray(32) { notRand.nextInt(256).toByte() }

    KMAC.init(KeyParameter(key))
    KMAC.reset()

    return KMAC
}

fun doHash(inp: ByteArray, hash: KMAC): Long {

    val buf = ByteBuffer.allocate(Long.SIZE_BYTES).array()

    hash.update(inp, 0, Long.SIZE_BYTES)

    hash.doFinal(buf, 0, Long.SIZE_BYTES)

    return abs(buf.sumOf { it.toLong() })
}

fun crc(inp: Long): Int {

    val crc = CRC32()
    crc.reset()
    crc.update(longToByteArray(inp), 0, Long.SIZE_BYTES)
    return crc.value.toInt()
}

fun longToByteArray(l: Long) : ByteArray {

    var convertable = l
    val buf = ByteBuffer.allocate(Long.SIZE_BYTES)

    // because Long::toByte fills 8 bit LSB ordered into resulting byte,
    // we shift through the entire long and read 8 LSB by calling Long::toByte
    for (chunk in (0 until Long.SIZE_BYTES)) {
        convertable = convertable shr (chunk*8)
        buf.put(convertable.toByte())
    }

    return buf.array()
}