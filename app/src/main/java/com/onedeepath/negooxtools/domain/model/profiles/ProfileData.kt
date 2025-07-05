package com.onedeepath.negooxtools.domain.model.profiles

data class ProfileData(var profileName:String,
                       var webName:String,
                       var webTax: String,
                       var courierName: String,
                       var courierPriceKg: String,
                       var cardName: String,
                       var cardTax: String,
                       var connectWithPaypal: Boolean) {

}
