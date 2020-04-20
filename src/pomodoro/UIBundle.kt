package pomodoro

import com.intellij.AbstractBundle
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.lang.ref.Reference
import java.lang.ref.SoftReference
import java.util.*

/**
 * Copied from [com.intellij.ui.UIBundle].
 */
object UIBundle {
    private var ourBundle: Reference<ResourceBundle>? = null
    @NonNls private const val PATH_TO_BUNDLE = "messages"

    @JvmStatic fun message(@PropertyKey(resourceBundle = "messages") key: String, vararg params: Any): String =
        AbstractBundle.message(bundle, key, *params)

    private val bundle: ResourceBundle
        get() {
            var bundle: ResourceBundle? = null
            if (ourBundle != null) bundle = ourBundle!!.get()
            if (bundle == null) {
                bundle = ResourceBundle.getBundle(PATH_TO_BUNDLE)
                ourBundle = SoftReference(bundle)
            }
            return bundle!!
        }
}
