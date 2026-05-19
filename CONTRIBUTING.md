## Rules

### What's allowed

* LLM-assisted contributions are **not allowed**
* Typo contributions are **allowed**
* Code cleaning contributions are **allowed**

### Conventions

* Format your code
* Test your changes, ensure UI is correct on different themes and form factors
* Include a screenshot for UI changes
* Keep PRs focused, create separate PRs for unrelated changes
* Use [conventional commits](https://conventionalcommits.org/)
* Use [conventional branch names](https://conventional-branch.github.io/)

## Contributing

### Structure overview

This project is structured like most Compose Multiplatform apps are.

#### Modules

| Module       | Description                                                                                       |
|--------------|---------------------------------------------------------------------------------------------------|
| `composeApp` | Almost everything is here. This might change in the future and be split up into multiple modules. |
| `iosApp`     | The Xcode project for iOS. There is usually no reason to modify this at all.                      |

#### Packages

| Package              | Description                                                    |
|----------------------|----------------------------------------------------------------|
| `paige.navic.data`   | General models, types and repositories                         |
| `paige.navic.shared` | Classes and functions which specific platforms must implement. |
| `paige.navic.ui`     | All of the UI code and components.                             |
| `paige.navic.utils`  | Random helper functions or modifiers.                          |

#### Resources

Strings, fonts and other things are in `composeApp/src/commonMain/composeResources`

SVG icons are in `composeApp/src/commonMain/valkyrieResources`. Run
`./gradlew :generateValkyrieImageVector` to regenerate code for these
icons. Access them in code using `Icons.<Category>.<Icon>`

Most icons are sourced from [Material Symbols](https://fonts.google.com/icons)
**with the rounded variant.**

### Environment

You will need:

* Android Studio
	* You can use [JetBrains Toolbox](https://www.jetbrains.com/toolbox-app/) to get this
* High-end development box
	* You should ideally have MORE than 16GB of RAM
	* Ensure you have 50GB or so of free storage

On macOS, you will also need:

* [Xcode](https://developer.apple.com/xcode/) if developing for iOS
	* **Highly recommended to use [Xcodes](https://www.xcodes.app/)** (`brew install xcodes`)
* Even more storage and compute. Kotlin Native is very heavy and slow.
	* Because of this, you are recommended to test mainly on Android, and only iOS for iOS specific
	  changes

### Questions or assistance

Ask in the [Discord](https://discord.gg/TBcnNX66PH)
or [Matrix](https://matrix.to/#/#navic:maize.moe) server.
