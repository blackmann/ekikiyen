package blackground.ekikiyen.home

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import android.content.Intent
import android.databinding.ObservableField
import android.net.Uri
import android.util.Log
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

    // contains the recharge code to be dialed
    val dialNumber = SingleLiveEvent<String>()

    val cardNumber = ObservableField<String>()

    // this holds the array of strings
    private val numberArr = ArrayList<String>()

    fun getAll() {
        getRetrofit()
                .create(Endpoint::class.java)
                .getAll()
                .enqueue(object : Callback<ArrayList<Ekikime>> {
                    override fun onResponse(call: Call<ArrayList<Ekikime>>?,
                                            response: Response<ArrayList<Ekikime>>) {
                        ekikimeList.value = response.body()
                    }

                    override fun onFailure(call: Call<ArrayList<Ekikime>>?, t: Throwable?) {

                    }

                })
    }

    fun submit(cardNumber: String) {

        val form = mapOf(
                "card_number" to cardNumber
        )

        getRetrofit()
                .create(Endpoint::class.java)
                .submit(form)
                .enqueue(object: Callback<Void> {
                    override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                        TODO("not implemented")
                    }

                    override fun onFailure(call: Call<Void>?, t: Throwable?) {
                        TODO("not implemented")
                    }

                })

    }

    fun use(cardNumber: String) {

        val form = mapOf("card_number" to cardNumber)

        getRetrofit()
                .create(Endpoint::class.java)
                .use(form)
                .enqueue(object: Callback<Void> {
                    override fun onFailure(call: Call<Void>?, t: Throwable?) {
                        TODO("not implemented")
                    }

                    override fun onResponse(call: Call<Void>?, response: Response<Void>?) {
                        TODO("not implemented")
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
            numberArr.removeAt(numberArr.size -1)
            updateCardNumber()
        }
    }

    fun appendNumber(number: Int) {
        numberArr.add(number.toString())

        updateCardNumber()
    }


    fun dial() {
        // %23 represents hash #
        dialNumber.value = "*134*${cardNumber.get()}%23"
    }

    fun publish() {
        submit(cardNumber.get())
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
    }
}