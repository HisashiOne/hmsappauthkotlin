# hmsappauthkotlin
Basic example of Google OAuth2 integration in HMS devices with the AppAuth framework, based in Velmurugan Murugesan example


MainActivity.kt
//Configure or create your clientId in https://console.cloud.google.com/apis/credentials/oauthclient/
val clientId = ""
//Create your Redirect URI
val redirectUri

Manifest.xml
//Set Your Redirect URI
<data android:scheme="com.google.codelabs.appauth"/>

Build.gradle
//Set your Manifest Placeholder with the same Redirect Uri
manifestPlaceholders = [
                'appAuthRedirectScheme': 'com.example.myappauth'
        ]
