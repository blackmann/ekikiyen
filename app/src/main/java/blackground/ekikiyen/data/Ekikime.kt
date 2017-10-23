package blackground.ekikiyen.data

import com.google.gson.annotations.SerializedName
import java.util.*


class Ekikime(@SerializedName("card_number") val cardNumber: String,
              @SerializedName("created_at") val createdAt: Date,
              @SerializedName("usage") val usage: Int)