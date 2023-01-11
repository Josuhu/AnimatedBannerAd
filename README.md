# AnimatedBannerAd
Animated Jetpack Compose Banner ad with software based mediation. Choose programmatically which supplier ads are active (Admob, Meta and Unity). Use local selector values or Google Firebase RemoteConfig for Ad configuration.

In respect with Better Ads coalition requiremements.

NOTE! The test setup will not work without your google-service.Json file for Firebase remote config and Google Admob integration to show ads. Among any other add supplier such as Meta & Unity which are options in the setup.

If you wish to simplify the setup then please check the most relevant classes as below. Other enviromental functions support this in larger scale.

class MainActivity & MainViewModel

composables -> MyComposables.kt

ads -> class AdsProvider

Youtube walkthrough:
https://www.youtube.com/watch?v=0ctBmI-gwlw
