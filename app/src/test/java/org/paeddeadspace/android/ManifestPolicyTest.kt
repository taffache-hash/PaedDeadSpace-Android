package org.paeddeadspace.android

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory


class ManifestPolicyTest {
    @Test
    fun manifestDeclaresNoInternetOrSensitiveDataPermission() {
        val permissions = ManifestPermissions.load("src/main/AndroidManifest.xml")

        assertFalse("android.permission.INTERNET" in permissions)
        assertTrue(permissions.isEmpty())
    }
}

object ManifestPermissions {
    fun load(path: String): Set<String> {
        val factory = DocumentBuilderFactory.newInstance().apply {
            isNamespaceAware = true
        }
        val document = factory.newDocumentBuilder().parse(Path.of(path).toFile())
        val permissionNodes = document.getElementsByTagName("uses-permission")
        return buildSet {
            for (index in 0 until permissionNodes.length) {
                val permission = permissionNodes.item(index).attributes
                    .getNamedItemNS(ANDROID_NAMESPACE, "name")
                    ?.nodeValue
                if (!permission.isNullOrBlank()) add(permission)
            }
        }
    }

    private const val ANDROID_NAMESPACE = "http://schemas.android.com/apk/res/android"
}
