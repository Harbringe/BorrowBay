package com.example.borrowbay.util

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import org.json.JSONObject

class RazorpayHelper(
    private val activity: Activity,
    private val onSuccess: (String) -> Unit = {},
    private val onError: (Int, String) -> Unit = { _, _ -> }
) : PaymentResultListener {

    init {
        Checkout.preload(activity.applicationContext)
    }

    /**
     * @param totalAmountInPaisa Total amount to be charged (Rent + Deposit + Fee)
     * @param itemName Name of the item for display
     * @param userEmail Customer email
     * @param merchantId The Razorpay Account ID of the Lender (for split payment)
     * @param depositAmountInPaisa The portion of the payment to be kept by the platform (Deposit + Platform Fee)
     */
    fun startPayment(
        totalAmountInPaisa: Int,
        itemName: String,
        userEmail: String,
        userContact: String,
        merchantId: String?,
        depositAmountInPaisa: Int
    ) {
        val checkout = Checkout()
        // checkout.setKeyID("rzp_test_YOUR_ACTUAL_KEY") 
        checkout.setKeyID("rzp_test_SQWikLUEBMFnuk")

        try {
            val options = JSONObject()
            options.put("name", "BorrowBay")
            options.put("description", "Rent: $itemName")
            options.put("currency", "INR")
            options.put("amount", totalAmountInPaisa)
            
            val prefill = JSONObject()
            prefill.put("email", userEmail)
            prefill.put("contact", userContact)
            options.put("prefill", prefill)

            // NOTE: Split payments (transfers) require Razorpay Route to be enabled on your account.
            // If it's not enabled, this part might cause the checkout to fail in test mode.
            // For simple testing, you can comment out the 'transfers' block.
            
            if (!merchantId.isNullOrBlank() && merchantId.startsWith("acc_") && merchantId != "acc_TEST_123") {
                val transfers = org.json.JSONArray()
                val merchantTransfer = JSONObject()
                merchantTransfer.put("account", merchantId)
                merchantTransfer.put("amount", totalAmountInPaisa - depositAmountInPaisa)
                merchantTransfer.put("currency", "INR")
                transfers.put(merchantTransfer)
                
                options.put("transfers", transfers)
            }

            checkout.open(activity, options)
        } catch (e: Exception) {
            Log.e("RazorpayHelper", "Error in starting Razorpay Checkout", e)
            Toast.makeText(activity, "Checkout error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onPaymentSuccess(razorpayPaymentId: String?) {
        onSuccess(razorpayPaymentId ?: "")
    }

    override fun onPaymentError(code: Int, response: String?) {
        // Detailed logging for debugging
        Log.e("RazorpayHelper", "Payment Error ($code): $response")
        onError(code, response ?: "Unknown Error")
    }
}
