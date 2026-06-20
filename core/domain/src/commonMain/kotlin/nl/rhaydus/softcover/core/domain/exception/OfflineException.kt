package nl.rhaydus.softcover.core.domain.exception

class OfflineException : RetryableSyncException("No internet connection")
