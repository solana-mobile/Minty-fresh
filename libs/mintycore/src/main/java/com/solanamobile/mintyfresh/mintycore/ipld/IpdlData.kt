package com.solanamobile.mintyfresh.mintycore.ipld

// TODO: cleanup byte array concat + (seemingly) magic numbers + document
class IpdlDirectory(links: List<PBLink>)
    : PBNode(byteArrayOf(8, 1), links)

class IpdlFile(links: List<PBLink>)
    : PBNode(byteArrayOf(8, 2) +
        byteArrayOf(24) + Varint.encode(links.fold(0) { t, s -> t + s.size}) +
        links
            .map { byteArrayOf(32) + Varint.encode(it.size) }
            .reduce { acc, bytes -> acc + bytes },
        links)