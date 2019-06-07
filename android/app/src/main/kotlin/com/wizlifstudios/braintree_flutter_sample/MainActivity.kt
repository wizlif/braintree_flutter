package com.wizlifstudios.braintree_flutter_sample

import android.app.Activity
import android.os.Bundle

import io.flutter.app.FlutterActivity
import io.flutter.plugins.GeneratedPluginRegistrant
import io.flutter.plugin.common.MethodChannel
import android.content.Intent.getIntent
import com.braintreepayments.api.dropin.DropInActivity
import com.braintreepayments.api.dropin.DropInRequest
import com.braintreepayments.api.models.GooglePaymentRequest
import com.google.android.gms.wallet.TransactionInfo
import com.google.android.gms.wallet.WalletConstants
import com.loopj.android.http.TextHttpResponseHandler
import com.loopj.android.http.AsyncHttpClient
import cz.msebera.android.httpclient.Header
import com.braintreepayments.api.dropin.DropInResult
import android.content.Intent
import com.loopj.android.http.AsyncHttpResponseHandler
import com.loopj.android.http.RequestParams






class MainActivity: FlutterActivity() {
  private val CHANNEL = "BRAINTREE"
  private lateinit var result: MethodChannel.Result
  private val REQUEST_CODE = 2008

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    GeneratedPluginRegistrant.registerWith(this)

    MethodChannel(flutterView, CHANNEL).setMethodCallHandler { call, r ->
      result = r
      when (call.method) {
        "getPlatformVersion" -> result.success("Android " + android.os.Build.VERSION.RELEASE)
        "generateClientToken" -> {
          val client = AsyncHttpClient()
          val server = call.argument<String>("server")
          client.get(server, object : TextHttpResponseHandler() {
            override fun onFailure(statusCode: Int, headers: Array<Header>, responseString: String, throwable: Throwable) {
              result.error("ERROR", responseString, null)
            }

            override fun onSuccess(statusCode: Int, headers: Array<Header>, clientToken: String) {
              result.success(clientToken)
            }
          })
        }
        "dropInRequest" -> {
          val token = call.argument<String>("token")
          val dropInRequest = DropInRequest()
                  .clientToken(token)
          startActivityForResult(dropInRequest.getIntent(this), REQUEST_CODE)
          result.success(true)
        }
        "postNounceToServer" -> {
          val s = call.argument<String>("server")
          val nounce = call.argument<String>("nounce")
          postNonceToServer(s, nounce)
        }
        else -> result.notImplemented()
      }
    }

  }

  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
    super.onActivityResult(requestCode, resultCode, data)

    if (requestCode == REQUEST_CODE) {
      when (resultCode) {
        Activity.RESULT_OK ->
          result.success(data.getParcelableExtra<DropInResult>(DropInResult.EXTRA_DROP_IN_RESULT))
        // use the result to update your UI and send the payment method nonce to your server
        Activity.RESULT_CANCELED -> // the user canceled
          result.success(false)
        else -> {
          // handle errors here, an exception may be available in
          val error = data.getSerializableExtra(DropInActivity.EXTRA_ERROR) as Exception
          result.error("ERROR", error.message, null)
        }
      }
    }
  }

  private fun postNonceToServer(server: String?, nonce: String?) {
    val client = AsyncHttpClient()
    val params = RequestParams()
    params.put("payment_method_nonce", nonce)
    client.post(server, params,
            object : AsyncHttpResponseHandler() {
              override fun onSuccess(statusCode: Int, headers: Array<Header>, responseBody: ByteArray) {

              }

              override fun onFailure(statusCode: Int, headers: Array<Header>, responseBody: ByteArray, error: Throwable) {

              }
              // Your implementation here
            }
    )
  }

  private fun enableGooglePay(dropInRequest:DropInRequest) {
    val googlePaymentRequest:GooglePaymentRequest = GooglePaymentRequest()
            .transactionInfo(TransactionInfo.newBuilder()
                    .setTotalPrice("1.00")
                    .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                    .setCurrencyCode("USD")
                    .build())
                    // We recommend collecting and passing billing address information
                    // with all Google Pay transactions as a best practice.
                    .billingAddressRequired(true)


    // Optional in sandbox; if set in sandbox, this value must be
    // a valid production Google Merchant ID.
//    googlePaymentRequest.

    dropInRequest.googlePaymentRequest(googlePaymentRequest)
  }



}
