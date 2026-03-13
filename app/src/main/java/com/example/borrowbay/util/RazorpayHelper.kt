package com.example.borrowbay.util

import android.app.Activity
import android.util.Log
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class RazorpayHelper(private val activity: Activity) : PaymentResultListener {

    init {
        Checkout.preload(activity.applicationContext)
    }

    fun startPayment(
        amountInPaisa: Int,
        itemName: String,
        userEmail: String,
        userContact: String,
        merchantId: String? = null // For direct transfer to lender
    ) {
        val checkout = Checkout()
        // Replace with your actual key from Razorpay Dashboard
        checkout.setKeyID("rzp_test_YourKeyHere") 

        try {
            val options = JSONObject()
            options.put("name", "BorrowBay")
            options.put("description", "Rent: $itemName")
            options.put("image", "https://s3.amazonaws.com/rzp-mobile/images/rzp.png")
            options.put("theme.color", "#007AFF")
            options.put("currency", "INR")
            options.put("amount", amountInPaisa) // Amount in paisa (e.g., 50000 = Rs 500)
            
            val retryObj = JSONObject()
            retryObj.put("enabled", true)
            retryObj.put("max_count", 4)
            options.put("retry", retryObj)

            val prefill = JSONObject()
            prefill.put("email", userEmail)
            prefill.put("contact", userContact)
            options.put("prefill", prefill)

            // If we're paying a specific lender, we can pass their account ID here
            // Note: This usually requires Razorpay Route setup on the backend
            if (merchantId != null) {
                // options.put("account_id", merchantId)
            }

            checkout.open(activity, options)
        } catch (e: Exception) {
            Log.e("RazorpayHelper", "Error in starting Razorpay Checkout", e)
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        Log.d("RazorpayHelper", "Payment Success: $razorpayPaymentId")
        // Handle success (update database, show confirmation)
    }

    override fun onPaymentError(code: Int, response: String?) {
        Log.e("RazorpayHelper", "Payment Error ($code): $response")
        // Handle failure
    }
}
