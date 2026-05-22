package paige.navic.domain.models.settings

enum class MarqueeSpeed(val value: Int) {
	Disabled(0),
	Slow(6000),
	Medium(4000),
	Fast(1000)
}
