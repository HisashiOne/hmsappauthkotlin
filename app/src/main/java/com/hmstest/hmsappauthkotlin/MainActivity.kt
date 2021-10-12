package com.hmstest.hmsappauthkotlin

import android.content.Intent
import android.net.Uri
import android.nfc.Tag
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import net.openid.appauth.*
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val RC_AUTH = 100

    private var mAuthService: AuthorizationService? = null
    private var mStateManager: AuthStateManager? = null

    private lateinit var button_login: Button
    private lateinit var textUsername : TextView
    private lateinit var imgProfile : ImageView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mStateManager = AuthStateManager.getInstance(this)
        mAuthService = AuthorizationService(this)

        button_login = findViewById(R.id.button_login)
        textUsername = findViewById(R.id.textUsername)
        imgProfile = findViewById(R.id.imgProfile)



        button_login.setOnClickListener {

            Log.d("AppAuth", "Login Button 123445");

            if (mStateManager?.current?.isAuthorized!!) {

            } else {
                val serviceConfig = AuthorizationServiceConfiguration(
                    Uri.parse("https://accounts.google.com/o/oauth2/v2/auth"), // authorization endpoint
                    Uri.parse("https://www.googleapis.com/oauth2/v4/token") // token endpoint
                )

                val clientId = "933843457798-uahc3ss6tjf2uloii3o1lv8t5mfpa1b7.apps.googleusercontent.com"

                val redirectUri = Uri.parse("com.google.codelabs.appauth:/oauth2callback")

                val builder = AuthorizationRequest.Builder(
                    serviceConfig,
                    clientId,
                    ResponseTypeValues.CODE,
                    redirectUri
                )
                builder.setScopes("profile")

                val authRequest = builder.build()
                val authService = AuthorizationService(this)
                val authIntent = authService.getAuthorizationRequestIntent(authRequest)
                startActivityForResult(authIntent, RC_AUTH)
            }
        }
        }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_AUTH) {
            val resp = AuthorizationResponse.fromIntent(data!!)
            val ex = AuthorizationException.fromIntent(data)

            if (resp != null) {
                mAuthService = AuthorizationService(this)
                mStateManager?.updateAfterAuthorization(resp, ex)

                mAuthService?.performTokenRequest(
                    resp.createTokenExchangeRequest()
                ) { resp, ex ->
                    if (resp != null) {

                        mStateManager?.updateAfterTokenResponse(resp, ex)
                        button_login.setText("Logout")
                       Log.d("AppAuth", "Access Token $resp")
                        ProfileTask().execute(resp.accessToken)
                    } else {
                        // authorization failed, check ex for more details
                        Log.d("AppAuth", "Authorizathion Failed")
                    }
                }

                //Log.d("res",resp.accessToken)
                // authorization completed
            } else {
                // authorization failed, check ex for more details
            }
            // ... process the response or exception ...
        } else {
            // ...
        }
        if (mStateManager?.current?.isAuthorized!!) {
            Log.d("Auth", "Done")
            button_login.text = "Logout"
            mStateManager?.current?.performActionWithFreshTokens(
                mAuthService!!
            ) { accessToken, idToken, exception ->
                ProfileTask().execute(accessToken)
                Log.d("AppAuth", "AccessToken $accessToken")
            }

        }

    }

    inner class ProfileTask : AsyncTask<String?, Void, JSONObject>() {
        override fun doInBackground(vararg tokens: String?): JSONObject? {
            val client = OkHttpClient()
            val request = Request.Builder()
                .url("https://www.googleapis.com/oauth2/v3/userinfo")
                .addHeader("Authorization", String.format("Bearer %s", tokens[0]))
                .build()
            try {
                val response = client.newCall(request).execute()
                val jsonBody: String = response.body()!!.string()
                Log.i("AppAuth", String.format("User Info Response %s", jsonBody))
                return JSONObject(jsonBody)
            } catch (exception: Exception) {
                Log.w("AppAuth", exception)
            }
            return null
        }
        override fun onPostExecute(userInfo: JSONObject?) {
            if (userInfo != null) {
                val fullName = userInfo.optString("name", null)
                val imageUrl =
                    userInfo.optString("picture", null)
                if (!TextUtils.isEmpty(imageUrl)) {
                    Glide.with(this@MainActivity).load(imageUrl).into(imgProfile);
                }
                if (!TextUtils.isEmpty(fullName)) {
                    textUsername.setText(fullName)
                    Log.d("AppAuth", "Full Name $fullName")
                }
                val message = if (userInfo.has("error")) {
                    Log.d("AppAuth", "error")
                } else {
                    //getString(R.string.request_complete)
                    Log.d("AppAuth", "Request Complete")
                }
            }
        }
    }

    }



