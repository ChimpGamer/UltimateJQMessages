package nl.chimpgamer.ultimatejqmessages.paper.utils

object ReflectionUtils {

    fun hasMethod(
        clazz: Class<*>,
        methodName: String,
        vararg parameterTypes: Class<*>
    ): Boolean {
        return try {
            clazz.getMethod(methodName, *parameterTypes)
            true
        } catch (e: NoSuchMethodException) {
            false
        }
    }

    fun classExists(className: String) = try {
        Class.forName(className)
        true
    } catch (ex: ClassNotFoundException) {
        false
    }
}
