package paige.navic.domain.models.settings

/**
 * Different grid sizes which the user can choose from.
 * Applies to all grids across the app.
 *
 * @property value The grid size
 * @property label The label for this size, to be seen in settings
 */
enum class GridSize(val value: Int, val label: String) {
	TwoByTwo(2, "2x2"),
	ThreeByThree(3, "3x3"),
	FourByFour(4, "4x4")
}
