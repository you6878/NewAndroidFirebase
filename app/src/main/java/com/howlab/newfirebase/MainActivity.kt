package com.howlab.newfirebase

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Base64
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginBehavior
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.ads.nativetemplates.NativeTemplateStyle
import com.google.android.gms.ads.*
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdCallback
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.android.synthetic.main.activity_main.*
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*


class MainActivity : AppCompatActivity() {
    var googleSignInClient : GoogleSignInClient? = null
    val RC_SIGN_IN = 1000
    var callbackManager = CallbackManager.Factory.create()
    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var rewardedAd: RewardedAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        MobileAds.initialize(this, "ca-app-pub-3940256099942544~3347511713")
        adView.loadAd(AdRequest.Builder().build())

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-3940256099942544/1033173712"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        show_ads.setOnClickListener {
            mInterstitialAd.show()
        }
        mInterstitialAd.adListener = object : AdListener(){
            override fun onAdLoaded() {
                super.onAdLoaded()
                //mInterstitialAd.show()
            }
        }
        //Native ads
        var adLoader = AdLoader.Builder(this,"ca-app-pub-3940256099942544/2247696110").forUnifiedNativeAd { unifiedNativeAd ->
            var styles = NativeTemplateStyle.Builder().withMainBackgroundColor(ColorDrawable(resources.getColor(R.color.gnt_red))).build()
            my_template.setStyles(styles)
            my_template.setNativeAd(unifiedNativeAd)
        }.build()
        adLoader.loadAd(AdRequest.Builder().build())
        //Video ads
        rewardedAd = RewardedAd(this, "ca-app-pub-3940256099942544/5224354917")
        val adLoadCallback = object: RewardedAdLoadCallback() {
            override fun onRewardedAdLoaded() {
                // Ad successfully loaded.
                rewardedAd.show(this@MainActivity,object : RewardedAdCallback(){
                    override fun onRewardedAdOpened() {
                        //When open video
                    }

                    override fun onRewardedAdClosed() {
                        //When close video
                    }

                    override fun onUserEarnedReward(p0: RewardItem) {
                        //When click ads from user
                    }

                })

            }
            override fun onRewardedAdFailedToLoad(errorCode: Int) {
                // Ad failed to load.
            }
        }
        rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)

        signup_button.setOnClickListener {
            createEmailId()
        }

        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this,gso)

        google_login_button.setOnClickListener {
            var signInIntent = googleSignInClient?.signInIntent
            startActivityForResult(signInIntent,RC_SIGN_IN)
        }
        printHashKey(this)
        facebook_login_button.setOnClickListener {
            facebookLogin()
        }
        email_login_button.setOnClickListener {
            loginEmail()
        }
        val firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseRemoteConfig.fetch(0).addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                firebaseRemoteConfig.fetchAndActivate()
//                dialogDisplay(firebaseRemoteConfig)
            }
        }
    }
    fun dialogDisplay(firebaseRemoteConfig : FirebaseRemoteConfig){
        var showDialog = firebaseRemoteConfig.getBoolean("message_caps")
        var message = firebaseRemoteConfig.getString("message_content")

        if(showDialog){
            AlertDialog.Builder(this)
                .setTitle("Update")
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("Ok", DialogInterface.OnClickListener { dialogInterface, i ->
                    this.finish()
                }).show()
        }
    }

    override fun onResume() {
        super.onResume()
        moveNextPage()
    }
    fun moveNextPage(){
        var currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser != null){
            startActivity(Intent(this,NextActivity::class.java))
        }
    }
    fun facebookLogin(){
        LoginManager.getInstance().loginBehavior = LoginBehavior.WEB_VIEW_ONLY
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("public_profile","email"))
        LoginManager.getInstance().registerCallback(callbackManager,object : FacebookCallback<LoginResult>{
            override fun onSuccess(result: LoginResult?) {
                firebaseAuthWithFacebook(result)
            }

            override fun onCancel() {

            }

            override fun onError(error: FacebookException?) {

            }

        })
    }
    fun firebaseAuthWithFacebook(result: LoginResult?){
        var credential = FacebookAuthProvider.getCredential(result?.accessToken?.token!!)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener { task ->
            if(task.isSuccessful){
                moveNextPage()
            }
        }
    }
    fun printHashKey(pContext: Context) {
        try {
            val info: PackageInfo = pContext.getPackageManager()
                .getPackageInfo(pContext.getPackageName(), PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                println( "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {

        } catch (e: Exception) {

        }
    }
    fun createEmailId(){
        var email = email_edittext.text.toString()
        var password = password_edittext.text.toString()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                moveNextPage()
            }
        }
    }
    fun loginEmail(){
        var email = email_edittext.text.toString()
        var password = password_edittext.text.toString()

        FirebaseAuth.getInstance().signInWithEmailAndPassword(email,password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                moveNextPage()
            }
        }
    }
    fun firebaseAuthWithGoogle(acct : GoogleSignInAccount?){
        var credential = GoogleAuthProvider.getCredential(acct?.idToken,null)
        FirebaseAuth.getInstance().signInWithCredential(credential).addOnCompleteListener {
            task ->
            if(task.isSuccessful){
                moveNextPage()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager.onActivityResult(requestCode,resultCode,data)
        if(requestCode == RC_SIGN_IN){
            var task = GoogleSignIn.getSignedInAccountFromIntent(data)
            var account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        }
    }
}
