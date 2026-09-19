package org.paeddeadspace.android.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json


@Serializable
data class CoreReleaseMetadata(
    @SerialName("package_name") val packageName: String,
    @SerialName("package_version") val packageVersion: String,
    @SerialName("git_tag") val gitTag: String,
    val repository: String,
    @SerialName("concept_doi") val conceptDoi: String,
    @SerialName("release_doi") val releaseDoi: String,
    @SerialName("source_manifest") val sourceManifest: String,
) {
    companion object {
        private val strictJson = Json { ignoreUnknownKeys = false }

        fun parse(jsonText: String): CoreReleaseMetadata =
            strictJson.decodeFromString(jsonText)
    }
}
