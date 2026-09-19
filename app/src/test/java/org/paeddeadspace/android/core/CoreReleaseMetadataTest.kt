package org.paeddeadspace.android.core

import org.junit.Assert.assertEquals
import org.junit.Test


class CoreReleaseMetadataTest {
    @Test
    fun releaseMetadataPinsThePublishedCore() {
        val metadata = CoreReleaseMetadata.parse(
            """
            {
              "package_name": "paeddeadspace",
              "package_version": "1.0.0",
              "git_tag": "v1.0.0",
              "repository": "https://github.com/taffache-hash/PaedDeadSpace-Core",
              "concept_doi": "10.5281/zenodo.22838224",
              "release_doi": "10.5281/zenodo.22838225",
              "source_manifest": "MANIFEST_SHA256.txt"
            }
            """.trimIndent(),
        )

        assertEquals("paeddeadspace", metadata.packageName)
        assertEquals("1.0.0", metadata.packageVersion)
        assertEquals("v1.0.0", metadata.gitTag)
        assertEquals("10.5281/zenodo.22838225", metadata.releaseDoi)
    }
}
