package com.solanamobile.mintyfresh.mintycore.ipld

import com.solanamobile.mintyfresh.mintycore.util.asVarint

// TODO: cleanup byte array concat + (seemingly) magic numbers + document
class IpdlDirectory(links: List<PBLink>)
    : PBNode(byteArrayOf(8, 1), links)

class IpdlFile(links: List<PBLink>)
    : PBNode(
    byteArrayOf(8, 2) +
            byteArrayOf(24) + links.fold(0) { t, s -> t + s.size}.asVarint() +
            links.map { byteArrayOf(32) + it.size.asVarint() }.reduce { acc, bytes -> acc + bytes },
        links
    )