package com.wikidoc.data.smb

import jcifs.CIFSContext
import jcifs.config.PropertyConfiguration
import jcifs.context.BaseContext
import jcifs.smb.NtlmPasswordAuthenticator
import jcifs.smb.SmbFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.Properties
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SmbService @Inject constructor() {

    private var cifsContext: CIFSContext? = null
    private var baseUrl: String? = null

    suspend fun connect(serverAddress: String, username: String, password: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val props = Properties().apply {
                setProperty("jcifs.smb.client.responseTimeout", "30000")
                setProperty("jcifs.smb.client.soTimeout", "30000")
            }
            val baseContext = BaseContext(PropertyConfiguration(props))
            val domain = if (username.contains("@")) username.substringAfter("@") else ""
            val user = if (username.contains("@")) username.substringBefore("@") else username
            val auth = NtlmPasswordAuthenticator(domain, user, password)
            cifsContext = baseContext.withCredentials(auth)
            baseUrl = "smb://$serverAddress"
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listShares(): Result<List<SmbFileInfo>> = withContext(Dispatchers.IO) {
        try {
            val ctx = cifsContext ?: return@withContext Result.failure(Exception("Not connected"))
            val url = baseUrl ?: return@withContext Result.failure(Exception("Not connected"))

            val shares = mutableListOf<SmbFileInfo>()
            SmbFile(url, ctx).listFiles().forEach { file ->
                if (file.isDirectory && !file.name.endsWith("\$")) {
                    shares.add(SmbFileInfo(
                        name = file.name.trimEnd('/'),
                        path = file.url.path,
                        isDirectory = true,
                        size = 0
                    ))
                }
            }
            Result.success(shares)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun listDirectory(path: String): Result<List<SmbFileInfo>> = withContext(Dispatchers.IO) {
        try {
            val ctx = cifsContext ?: return@withContext Result.failure(Exception("Not connected"))
            val smbUrl = if (path.startsWith("smb://")) path else "smb://$path"

            val files = mutableListOf<SmbFileInfo>()
            SmbFile(smbUrl, ctx).listFiles().forEach { file ->
                val name = file.name.trimEnd('/')
                if (name != "." && name != "..") {
                    files.add(SmbFileInfo(
                        name = name,
                        path = file.url.path,
                        isDirectory = file.isDirectory,
                        size = if (file.isDirectory) 0 else file.length()
                    ))
                }
            }
            Result.success(files)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadFile(remotePath: String, localFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val ctx = cifsContext ?: return@withContext Result.failure(Exception("Not connected"))
            val smbUrl = if (remotePath.startsWith("smb://")) remotePath else "smb://$remotePath"

            SmbFile(smbUrl, ctx).inputStream.use { input ->
                FileOutputStream(localFile).use { output ->
                    input.copyTo(output)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun disconnect() {
        cifsContext = null
        baseUrl = null
    }
}

data class SmbFileInfo(
    val name: String,
    val path: String,
    val isDirectory: Boolean,
    val size: Long
) {
    val isMarkdownFile: Boolean
        get() = name.endsWith(".md", ignoreCase = true) || name.endsWith(".markdown", ignoreCase = true)

    val isZipFile: Boolean
        get() = name.endsWith(".zip", ignoreCase = true)

    val isSupported: Boolean
        get() = isMarkdownFile || isZipFile
}
