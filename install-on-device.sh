cd /Users/jeffrey/Documents/Git/mazer-android
./setup.sh RELEASE
./gradlew clean
./gradlew bundleRelease
rm app.apks universal.apk
java -jar bundletool.jar build-apks --bundle=app/release/app-release.aab --output=app.apks --mode=universal --ks=/Users/jeffrey/Documents/Git/mazer-android/keystore.jks --ks-key-alias=key0
unzip app.apks universal.apk
adb -s R5CY70QX2MW uninstall com.jmisabella.mazer
adb -s R5CY70QX2MW install universal.apk
adb -s R5CY70QX2MW shell am start -n com.jmisabella.mazer/.MainActivity
#adb uninstall com.jmisabella.mazer
#adb install universal.apk
#adb shell am start -n com.jmisabella.mazer/.MainActivity
