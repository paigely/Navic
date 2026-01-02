//
//  AppDelegate.swift
//  iosApp
//
//  Created by paige on 2026.01.01.
//

import ComposeApp
import SwiftUI

struct ComposeView: UIViewControllerRepresentable {
	func makeUIViewController(context: Context) -> some UIViewController {
		MainViewControllerKt.MainViewController()
	}
	func updateUIViewController(
		_ uiViewController: UIViewControllerType,
		context: Context
	) { }
}

@main
struct ComposeApp: App {
	var body: some Scene {
		WindowGroup {
			ComposeView().ignoresSafeArea()
		}
	}
}