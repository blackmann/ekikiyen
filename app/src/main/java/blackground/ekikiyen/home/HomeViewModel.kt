package blackground.ekikiyen.home

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.databinding.ObservableField
import blackground.ekikiyen.data.Ekikime
import blackground.pidgin.arch.SingleLiveEvent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

class HomeViewModel : ViewModel() {

    val ekikimeList = MutableLiveData<ArrayList<Ekikime>>()
    val showLoading = SingleLiveEvent<Void>()
    val hideLoading = SingleLiveEvent<Void>()
    val requestRefresh = SingleLiveEvent<Void>()
    val incompleteCard = SingleLiveEvent<Void>()
    val processInfo = MutableLiveData<String>()
    val saveShared = SingleLiveEvent<String>()

    // contains the recharge code to be dialed
    val dialNumber = SingleLiveEvent<String>()

    val cardNumber = ObservableField<String>()

    // this holds the array of strings
    private val numberArr = ArrayList<String>()

    private fun inform(message: String) {
        processInfo.value = message
        showLoading.call()
    }

    fun getAll() {
        inform("Fetching shared cards...")

        getRetrofit()
                .create(Endpoint::class.java)
                .getAll()
                .enqueue(object : Callback<ArrayList<Ekikime>> {
                    override fun onResponse(call: Call<ArrayList<Ekikime>>?,
                                            response: Response<ArrayList<Ekikime>>) {
                        ekikimeList.value = response.body()
                        hideLoading.call()
                    }

                    override fun onFailure(call: Call<ArrayList<Ekikime>>?, t: Throwable?) {
                        hideLoading.call()
                    }

                })
    }

    private fun submit(cardNumber: String) {
        val form = mapOf(
                "card_number" to cardNumber
        )

        inform("Submitting your card...")

        getRetrofit()
                .create(Endpoint::class.java)
                .submit(form)
                .enqueue(object : Callback<Void> {
                    override fun onResponse(call: Call<Void>?, response: Response<Void>) {
                        hideLoading.call()

                        if (response.isSuccessful) {
                            saveShared.value = cardNumber
                            requestRefresh.call()
                        }
                    }

                    override fun onFailure(call: Call<Void>?, t: Throwable?) {
                        hideLoading.call()
                    }

                })

    }

    fun use(cardNumber: String) {
        val form = mapOf("card_number" to cardNumber)

        inform("Updating...")

        getRetrofit()
                .create(Endpoint::class.java)
                .use(form)
                .enqueue(object : Callback<Void> {
                    override fun onFailure(call: Call<Void>?, t: Throwable?) {
                        hideLoading.call()
                    }

                    override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                        hideLoading.call()
                        requestRefresh.call()
                    }

                })
    }

    fun invalid(cardNumber: String) {
        val form = mapOf("card_number" to cardNumber)

        inform("Updating...")

        getRetrofit()
                .create(Endpoint::class.java)
                .invalid(form)
                .enqueue(object : Callback<Void> {
                    override fun onFailure(call: Call<Void>?, t: Throwable?) {
                        hideLoading.call()
                    }

                    override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                        hideLoading.call()
                        requestRefresh.call()
                    }

                })
    }

    private fun getRetrofit(): Retrofit {

        return Retrofit.Builder()
                .baseUrl("https://ekikime.herokuapp.com/ekikime/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }


    // VIEW METHODS
    fun delete() {
        if (!numberArr.isEmpty()) {
            numberArr.removeAt(numberArr.size - 1)
            updateCardNumber()
        }
    }

    fun appendNumber(number: Int) {
        numberArr.add(number.toString())

        updateCardNumber()
    }


    fun dial() {
        dialNumber.value = cardNumber.get()
    }

    fun publish() {
        if (cardNumber.get() == null || cardNumber.get().length < 14) {
            incompleteCard.call()
            return
        }
        submit(cardNumber.get())
    }

    fun clearDialer(): Boolean {
        numberArr.clear()
        updateCardNumber()
        return true
    }

    private fun updateCardNumber() {
        cardNumber.set(numberArr.joinToString(separator = ""))
    }


    interface Endpoint {
        @GET("get_all/")
        fun getAll(): Call<ArrayList<Ekikime>>

        @POST("submit/")
        fun submit(@Body form: Map<String, String>): Call<Void>

        @PUT("use/")
        fun use(@Body form: Map<String, String>): Call<Void>

        @PUT("invalid/")
        fun invalid(@Body form: Map<String, String>): Call<Void>
    }
}