# Bloom Filter

A Bloom-Filter is a datastructure which helps to find differences in a set. The datastructure
is probabilistic and was invented by Burton Howard Bloom in 1970. The most 
important building block is a good fitting hash function (number of collisions should be low enough for the use case).

This repo contains simple implementations for the Bloom Filters
specified in RFC-Draft about [Byzantine Fault Tolerant Set Reconciliation](https://datatracker.ietf.org/doc/html/draft-summermatter-set-union)

Three different level of the Bloom-Filter datastructure are implemented:
1. Basic Bloom Filter: Most basic
2. Counting Bloom Filter: Counts number of elements
3. Invertible Bloom Filter: Most advanced implementation allowing decoding and set differences

More information on [Wikipedia about Bloom filter](https://en.wikipedia.org/wiki/Bloom_filter)

Be aware that I implemented the structure to get a grasp of how bloom filters work and that I made some simplifications.
This could lead to inaccuracies regarding the spec or simply lead to bugs ;) 