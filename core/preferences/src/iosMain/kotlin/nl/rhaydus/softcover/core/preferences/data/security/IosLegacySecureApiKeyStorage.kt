package nl.rhaydus.softcover.core.preferences.data.security

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.MemScope
import kotlinx.cinterop.alloc
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.convert
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.value
import kotlinx.coroutines.withContext
import platform.CoreFoundation.CFDictionaryCreate
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringRef
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import nl.rhaydus.common.AppDispatchers

/**
 * Reads the API key from Softcover's **pre-foundation** iOS location: the generic-password Keychain item
 * under service `nl.rhaydus.softcover`, account `softcover_api_key`. The foundation store addresses a
 * different `(service, account)` pair, so an upgrading install would otherwise find nothing.
 *
 * See [LegacySecureApiKeyStorage] — migration only, delete after one release.
 */
@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal class IosLegacySecureApiKeyStorage(
    private val dispatchers: AppDispatchers,
) : LegacySecureApiKeyStorage {
    override suspend fun read(): String? = withContext(dispatchers.io) {
        val cfService = CFBridgingRetain(SERVICE as NSString)
        val cfAccount = CFBridgingRetain(ACCOUNT as NSString)

        try {
            memScoped {
                val query = cfDictionaryOf(
                    kSecClass to kSecClassGenericPassword,
                    kSecAttrService to cfService,
                    kSecAttrAccount to cfAccount,
                    kSecReturnData to kCFBooleanTrue,
                    kSecMatchLimit to kSecMatchLimitOne,
                )

                val result = alloc<CFTypeRefVar>()
                val status = SecItemCopyMatching(
                    query,
                    result.ptr,
                )
                CFRelease(query)

                if (status != errSecSuccess) return@memScoped null

                val data = CFBridgingRelease(result.value) as? NSData
                    ?: return@memScoped null

                NSString.create(
                    data,
                    NSUTF8StringEncoding,
                ) as String?
            }
        } finally {
            CFRelease(cfService)
            CFRelease(cfAccount)
        }
    }

    override suspend fun delete() {
        withContext(dispatchers.io) {
            val cfService = CFBridgingRetain(SERVICE as NSString)
            val cfAccount = CFBridgingRetain(ACCOUNT as NSString)

            try {
                memScoped {
                    val query = cfDictionaryOf(
                        kSecClass to kSecClassGenericPassword,
                        kSecAttrService to cfService,
                        kSecAttrAccount to cfAccount,
                    )
                    SecItemDelete(query)
                    CFRelease(query)
                }
            } finally {
                CFRelease(cfService)
                CFRelease(cfAccount)
            }
        }
    }

    private fun MemScope.cfDictionaryOf(vararg pairs: Pair<CFStringRef?, CFTypeRef?>): CFDictionaryRef {
        val keys: CArrayPointer<*> = allocArrayOf(pairs.map { it.first })
        val values: CArrayPointer<*> = allocArrayOf(pairs.map { it.second })

        return CFDictionaryCreate(
            allocator = kCFAllocatorDefault,
            keys = keys.reinterpret(),
            values = values.reinterpret(),
            numValues = pairs.size.convert(),
            keyCallBacks = null,
            valueCallBacks = null,
        ) ?: error("Unable to build keychain query dictionary")
    }

    private companion object {
        const val SERVICE = "nl.rhaydus.softcover"
        const val ACCOUNT = "softcover_api_key"
    }
}
