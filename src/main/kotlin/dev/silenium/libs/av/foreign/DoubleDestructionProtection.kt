package dev.silenium.libs.av.foreign

import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicBoolean

abstract class DoubleDestructionProtection<V> : AutoCloseable {
    abstract val value: V

    private val destroyed = AtomicBoolean(false)
    private var destructionPoint: Throwable? = null

    @Synchronized
    fun destroy() {
        if (destroyed.compareAndSet(false, true)) {
            destroyInternal()
            destructionPoint = Exception()
        } else {
            logger.trace(
                "{} {} was already destroyed at: {}",
                javaClass.simpleName,
                value,
                destructionPoint,
                Exception(),
            )
        }
    }

    @Synchronized
    fun abandon() {
        if (destroyed.compareAndSet(false, true)) {
            destructionPoint = Exception("Abandoned")
        } else {
            logger.trace(
                "{} {} was already destroyed at: {}",
                javaClass.simpleName,
                value,
                destructionPoint,
                Exception(),
            )
        }
    }

    protected abstract fun destroyInternal()

    override fun close() = destroy()

    companion object {
        private val logger = LoggerFactory.getLogger(DoubleDestructionProtection::class.java)
    }
}
