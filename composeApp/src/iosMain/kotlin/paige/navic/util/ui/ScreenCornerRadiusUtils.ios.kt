package paige.navic.util.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import platform.CoreGraphics.CGFloat
import platform.Foundation.valueForKey
import platform.UIKit.UIScreen
import kotlin.io.encoding.Base64

// https://github.com/palera1n/loader/blob/main/NimbleKit/Sources/NimbleExtensions/UIScreen/UIScreen%2BdisplayCornerRadius.swift
@Composable
actual fun rememberScreenCornerRadius(defaultRadius: Dp): Dp {
	val key = Base64.decode("X2Rpc3BsYXlDb3JuZXJSYWRpdXM=".encodeToByteArray()).decodeToString()
	// probably doesn't need runCatching but im too lazy to test this
	return runCatching {
		UIScreen.mainScreen.valueForKey(key) as? CGFloat
	}.getOrNull()?.dp ?: 0.dp
}
