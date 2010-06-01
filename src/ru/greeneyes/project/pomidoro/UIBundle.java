package ru.greeneyes.project.pomidoro;

import com.intellij.CommonBundle;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.PropertyKey;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.ResourceBundle;

/**
 * Copied from {@link com.intellij.ui.UIBundle}.
 *
 * User: dima
 * Date: Jun 1, 2010
 */
public class UIBundle {
	private static Reference<ResourceBundle> ourBundle;
	@NonNls
	protected static final String PATH_TO_BUNDLE = "resources.messages";

	public static String message(@PropertyKey(resourceBundle = "resources.messages") String key, Object... params) {
		return CommonBundle.message(getBundle(), key, params);
	}

	private static ResourceBundle getBundle() {
		ResourceBundle bundle = null;
		if (ourBundle != null) bundle = ourBundle.get();
		if (bundle == null) {
			bundle = ResourceBundle.getBundle(PATH_TO_BUNDLE);
			ourBundle = new SoftReference<ResourceBundle>(bundle);
		}
		return bundle;
	}

}
