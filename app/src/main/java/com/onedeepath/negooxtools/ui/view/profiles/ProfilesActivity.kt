package com.onedeepath.negooxtools.ui.view.profiles

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.onedeepath.negooxtools.ui.view.aproxCalculate.AproxCalculateActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.onedeepath.negooxtools.data.local.dataStore
import com.onedeepath.negooxtools.R
import com.onedeepath.negooxtools.domain.model.profiles.ProfileData

class ProfilesActivity : AppCompatActivity() {

    // KEYS
    companion object{
        // Shared preferences Keys
            // SaveProfile and LoadProfile keys
        const val PROFILES_SHARED_KEY = "profile_shared_key"
        const val PROFILE_GSONLIST_KEY = "profile_gsonlist_key"

            // SaveProfileUI and LoadProfileUI keys
        const val PROFILE_SHARED_UI_KEY = "profile_shared_ui_key"
        const val PROFILE_SELECTED_UI_KEY = "profile_selected_ui_key"
        const val PROFILE_ACTIVE_UI_STATE = "profile_active_ui_key"

        // Send data to Aprox Calculate keys
        const val PROFILE_NAME = "profile_name"
        const val WEB_NAME = "web_name"
        const val WEB_TAX = "web_tax"
        const val CARD_NAME = "card_name"
        const val CARD_TAX = "card_tax"
        const val COURIER_NAME = "courier_name"
        const val COURIER_TAX = "courier_tax"
        const val WITH_PAYPAL = "with_paypal"

        // Send Real Data to Aprox Calculate
        const val PROFILE_NAME_DS = "profile_name_ds"
        const val WEB_NAME_DS = "web_name_ds"
        const val WEB_TAX_DS = "web_tax_ds"
        const val CARD_TAX_DS = "card_tax_ds"
        const val CARD_NAME_DS = "card_name_ds"
        const val COURIER_NAME_DS = "courier_name_ds"
        const val COURIER_TAX_DS = "courier_tax_ds"
        const val WITH_PAYPAL_DS = "with_paypal_ds"
    }

    // PROFILES UI
    private lateinit var fabProfilesAddProfile: FloatingActionButton
    private lateinit var tvProfilesTitleProfile: TextView
    private lateinit var actvProfileDropDownMenu: AutoCompleteTextView

    // PROFILES INFO
    private lateinit var tvProfilesInfoWebName: TextView
    private lateinit var tvProfilesInfoWebTax: TextView
    private lateinit var tvProfilesInfoCardName: TextView
    private lateinit var tvProfilesInfoCardTax: TextView
    private lateinit var tvProfilesInfoCourierName: TextView
    private lateinit var tvProfilesInfoCourierPriceKg: TextView
    private lateinit var ivProfilesInfoWithPaypalIcon: ImageView
    private lateinit var tvProfilesInfoWithPaypalTax: TextView
    private lateinit var btnProfilesInfoUpdate: AppCompatButton
    private lateinit var btnProfilesInfoDelete: AppCompatButton

    // PROFILES DATA
    private val profiles = mutableListOf<String>()
    private var profileData = mutableMapOf<String, ProfileData>()



    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profiles)

        // initComponents
        initComponents()

        // Cargamos los perfiles guardados

        Log.i("actvProfile", "onCreate profiles: $profiles ProfileData: $profileData")

        profileData.putAll(loadProfiles())

        profiles.addAll(profileData.keys)


        // Drop Down Menu config
        val adapter = ArrayAdapter(this, R.layout.item_actv_profile_list, profiles)
        actvProfileDropDownMenu.setAdapter(adapter)

        // Cargamos los perfiles en la UI
        loadPreferences()

        // Actualizamos UI
        actvProfileDropDownMenu.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->

                val selectedProfile = profiles[position]
                val infoProfile = profileData[selectedProfile]

                if (infoProfile != null) {

                    // Pintamos la UI
                    updateUIWithProfile(infoProfile)

                    sendDataToAproxCalcutate(infoProfile)

                    CoroutineScope(Dispatchers.IO).launch {

                        saveDataToDataStore(infoProfile)
                    }
                    Toast.makeText(this, "$selectedProfile ON", Toast.LENGTH_SHORT).show()

                    // Guardamos el perfil y datos seleccionados en las sharedprefs de la UI
                    savePreferencesUI(selectedProfile)

                }

            }

        // Listeners
        initListeners(adapter)


    }

    private suspend fun saveDataToDataStore(profileDS: ProfileData) {


        dataStore.edit { prefs ->

            prefs[stringPreferencesKey(PROFILE_NAME_DS)] = profileDS.profileName
            prefs[stringPreferencesKey(WEB_NAME_DS)] = profileDS.webName
            prefs[stringPreferencesKey(WEB_TAX_DS)] = profileDS.webTax
            prefs[stringPreferencesKey(CARD_NAME_DS)] = profileDS.cardName
            prefs[stringPreferencesKey(CARD_TAX_DS)] = profileDS.cardTax
            prefs[stringPreferencesKey(COURIER_NAME_DS)] = profileDS.courierName
            prefs[stringPreferencesKey(COURIER_TAX_DS)] = profileDS.courierPriceKg
            prefs[booleanPreferencesKey(WITH_PAYPAL_DS)] = profileDS.connectWithPaypal


        }

    }

    private fun sendDataToAproxCalcutate(profile: ProfileData) {


        val intent = Intent(this, AproxCalculateActivity::class.java)
        intent.putExtra(PROFILE_NAME, profile.profileName)
        intent.putExtra(WEB_NAME, profile.webName)
        intent.putExtra(WEB_TAX, profile.webTax.toDouble())
        intent.putExtra(CARD_NAME, profile.cardName)
        intent.putExtra(CARD_TAX, profile.cardTax.toDouble())
        intent.putExtra(COURIER_NAME, profile.courierName)
        intent.putExtra(COURIER_TAX, profile.courierPriceKg.toDouble())
        intent.putExtra(WITH_PAYPAL, profile.connectWithPaypal)


    }

    private suspend fun clearProfileFromDataStore() {
        dataStore.edit { prefs ->
            prefs.remove(stringPreferencesKey(PROFILE_NAME_DS))
            prefs.remove(stringPreferencesKey(WEB_NAME_DS))
            prefs.remove(stringPreferencesKey(WEB_TAX_DS))
            prefs.remove(stringPreferencesKey(CARD_NAME_DS))
            prefs.remove(stringPreferencesKey(CARD_TAX_DS))
            prefs.remove(stringPreferencesKey(COURIER_NAME_DS))
            prefs.remove(stringPreferencesKey(COURIER_TAX_DS))
            prefs.remove(booleanPreferencesKey(WITH_PAYPAL_DS))
        }
    }

    // Guardamos los perfiles en la UI
    private fun savePreferencesUI(profileName: String) {

        val sharedPreferences = getSharedPreferences(PROFILE_SHARED_UI_KEY, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(PROFILE_SELECTED_UI_KEY, profileName)
        editor.apply()

    }

    // Cargamos los perfiles en la UI
    private fun loadPreferences() {

        val sharedPreferences = getSharedPreferences(PROFILE_SHARED_UI_KEY, MODE_PRIVATE)
        val savedProfile = sharedPreferences.getString(PROFILE_SELECTED_UI_KEY, null)

        savedProfile?.let {
            actvProfileDropDownMenu.setText(it, false)
            profileData[it]?.let { profile ->
                updateUIWithProfile(profile)
            }
        }

    }

    // Guardamos los perfiles
    private fun saveProfiles(profiles: Map<String, ProfileData>){
        // Usamos sharedPrefences
        val sharedPreferences = getSharedPreferences(PROFILES_SHARED_KEY, MODE_PRIVATE)
        // configuramos su editor
        val editor = sharedPreferences.edit()
        // Usamos gson para convertir nuestro map en json
        val jsonProfiles = Gson().toJson(profiles)
        // colocamos el json con su key
        editor.putString(PROFILE_GSONLIST_KEY, jsonProfiles)
        // aplicamos cambios
        editor.apply()

    }

    // Cargamos los perfiles
    private fun loadProfiles() : Map<String, ProfileData> {

        // Abrimos nuestro sharedprefrence
        val sharedPreferences = getSharedPreferences(PROFILES_SHARED_KEY, MODE_PRIVATE)
        // Sacamos nuestro json
        val gson = Gson()

        val jsonProfiles = sharedPreferences.getString(PROFILE_GSONLIST_KEY, null)


        // retornamos json deserializado a tipo Map
        return if (jsonProfiles != null) {

            Gson().fromJson(jsonProfiles, object : TypeToken<Map<String, ProfileData>>() {}.type)

        } else {

            emptyMap()
        }

    }

    // Actualizamos el UI
    private fun updateUIWithProfile(profile: ProfileData) {
        tvProfilesTitleProfile.text = profile.profileName
        tvProfilesInfoWebName.text = profile.webName
        tvProfilesInfoWebTax.text = "${profile.webTax}%"
        tvProfilesInfoCardName.text = profile.cardName
        tvProfilesInfoCardTax.text = "${profile.cardTax}%"
        tvProfilesInfoCourierName.text = profile.courierName
        tvProfilesInfoCourierPriceKg.text = "${profile.courierPriceKg}/kg"

        if (profile.connectWithPaypal) {
            ivProfilesInfoWithPaypalIcon.setImageResource(R.drawable.ic_profiles_withpaypal)
            tvProfilesInfoWithPaypalTax.text = "4.5%"
        } else {
            ivProfilesInfoWithPaypalIcon.setImageResource(R.drawable.ic_profiles_withoutpaypal)
            tvProfilesInfoWithPaypalTax.text = "0%"
        }
    }

    // Limpiamos el UI
    private fun clearUIProfile() {

        tvProfilesTitleProfile.text = ""
        tvProfilesInfoWebName.text = ""
        tvProfilesInfoWebTax.text = "0"
        tvProfilesInfoCardName.text = ""
        tvProfilesInfoCardTax.text = "0"
        tvProfilesInfoCourierName.text = ""
        tvProfilesInfoCourierPriceKg.text = "0"
        tvProfilesInfoWithPaypalTax.text = "0"


    }

    // Dialog Edit
    private fun showEditProfileDialog(profileName: String, adapter: ArrayAdapter<String>) {

        // Obtener el perfil seleccionado
        val profile = profileData[profileName]

        if (profile != null) {
            Log.i("ProfilesActivity", "profile no es nulo")

            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_profiles_addprofile)

            val tvTitleDialogProfile : TextView = dialog.findViewById(R.id.tvTilteDialogProfile)
            val etAddDialogProfileName: EditText = dialog.findViewById(R.id.etAddDialogProfileName)
            val etAddDialogWebName: EditText = dialog.findViewById(R.id.etAddDialogWebName)
            val etAddDialogWebTax: EditText = dialog.findViewById(R.id.etAddDialogWebTax)
            val etAddDialogCardName: EditText = dialog.findViewById(R.id.etAddDialogCardName)
            val etAddDialogCardTax: EditText = dialog.findViewById(R.id.etAddDialogCardTax)
            val etAddDialogCourierName: EditText = dialog.findViewById(R.id.etAddDialogCourierName)
            val etAddDialogCourierPriceKg: EditText =
                dialog.findViewById(R.id.etAddDialogCourierPriceKg)
            val cbAddDialogConnectWithPaypal: CheckBox =
                dialog.findViewById(R.id.cbAddDialogConnectWithPaypal)

            val btnAddDialogAddProfile: AppCompatButton =
                dialog.findViewById(R.id.btnAddDialogAddProfile)

            // Cargar los datos del perfil en los EditTexts
            tvTitleDialogProfile.setText("EDIT PROFILE")
            etAddDialogProfileName.setText(profile.profileName)
            etAddDialogWebName.setText(profile.webName)
            etAddDialogWebTax.setText(profile.webTax.toString())
            etAddDialogCardName.setText(profile.cardName)
            etAddDialogCardTax.setText(profile.cardTax.toString())
            etAddDialogCourierName.setText(profile.courierName)
            etAddDialogCourierPriceKg.setText(profile.courierPriceKg.toString())
            cbAddDialogConnectWithPaypal.isChecked = profile.connectWithPaypal

            // Cambiar el texto del botón para que sea "Guardar" en lugar de "Agregar"
            btnAddDialogAddProfile.text = "Guardar"

            btnAddDialogAddProfile.setOnClickListener {
                // Obtener los valores modificados del usuario
                val updatedProfileName = etAddDialogProfileName.text.toString()
                val updatedWebName = etAddDialogWebName.text.toString()
                val updatedWebTax = etAddDialogWebTax.text.toString()
                val updatedCardName = etAddDialogCardName.text.toString()
                val updatedCardTax = etAddDialogCardTax.text.toString()
                val updatedCourierName = etAddDialogCourierName.text.toString()
                val updatedCourierPriceKg = etAddDialogCourierPriceKg.text.toString()
                val updatedWithPaypal = cbAddDialogConnectWithPaypal.isChecked

                if (updatedProfileName.isNotEmpty()) {
                    // Eliminar el perfil anterior si el nombre ha cambiado
                    if (updatedProfileName != profileName) {
                        profileData.remove(profileName)
                        profiles.remove(profileName)
                    }


                    // Actualizamos el perfil en el mapa profileData
                    val updatedProfile = ProfileData(
                        profileName = updatedProfileName,
                        webName = updatedWebName,
                        webTax = updatedWebTax,
                        cardName = updatedCardName,
                        cardTax = updatedCardTax,
                        courierName = updatedCourierName,
                        courierPriceKg = updatedCourierPriceKg,
                        connectWithPaypal = updatedWithPaypal
                    )

                    profileData[updatedProfileName] = updatedProfile

                    // Actualizar la lista de perfiles
                    if (!profiles.contains(updatedProfileName)) {

                        profiles.add(updatedProfileName)

                    }

                    // Actualizar el adaptador
                    adapter.notifyDataSetChanged()

                    // Actualizamos el drop down menu
                    actvProfileDropDownMenu.setText(updatedProfileName, false)

                    // Guardar los cambios en SharedPreferences
                    saveProfiles(profileData)

                    // Actualizamos la ui
                    updateUIWithProfile(updatedProfile)
                    // Cerrar el diálogo
                    dialog.dismiss()

                    Toast.makeText(this, "Perfil actualizado", Toast.LENGTH_SHORT).show()

                }else{

                    Toast.makeText(this, "Perfil requerido", Toast.LENGTH_SHORT).show()
                }


            }

            dialog.show()

        }else {
            Log.i("ProfilesActivity", "profile es nulo")
            Toast.makeText(this, "El profile es null :c", Toast.LENGTH_SHORT).show()
        }
    }

    // Dialog Add
    private fun showAddProfileDialog(adapter: ArrayAdapter<String>) {

        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_profiles_addprofile)

        val tvTitleDialogProfile : TextView = dialog.findViewById(R.id.tvTilteDialogProfile)
        val etAddDialogProfileName: EditText = dialog.findViewById(R.id.etAddDialogProfileName)
        val etAddDialogWebName: EditText = dialog.findViewById(R.id.etAddDialogWebName)
        val etAddDialogWebTax: EditText = dialog.findViewById(R.id.etAddDialogWebTax)
        val etAddDialogCardName: EditText = dialog.findViewById(R.id.etAddDialogCardName)
        val etAddDialogCardTax: EditText = dialog.findViewById(R.id.etAddDialogCardTax)
        val etAddDialogCourierName: EditText = dialog.findViewById(R.id.etAddDialogCourierName)
        val etAddDialogCourierPriceKg: EditText =
            dialog.findViewById(R.id.etAddDialogCourierPriceKg)

        val cbAddDialogConnectWithPaypal: CheckBox =
            dialog.findViewById(R.id.cbAddDialogConnectWithPaypal)

        val btnAddDialogAddProfile: AppCompatButton =
            dialog.findViewById(R.id.btnAddDialogAddProfile)

        tvTitleDialogProfile.setText("ADD PROFILE")

        btnAddDialogAddProfile.setOnClickListener {

            val profileName = etAddDialogProfileName.text.toString()
            val webName = etAddDialogWebName.text.toString()
            val webTax = etAddDialogWebTax.text.toString()
            val cardName = etAddDialogCardName.text.toString()
            val cardTax = etAddDialogCardTax.text.toString()
            val courierName = etAddDialogCourierName.text.toString()
            val courierPriceKg = etAddDialogCourierPriceKg.text.toString()
            val withPaypal = cbAddDialogConnectWithPaypal.isChecked

            if (profileName.isNotEmpty()) {

                // Creamos nuevo perfil
                val newProfile = ProfileData(

                    profileName = profileName,
                    webName = webName,
                    webTax = webTax,
                    cardName = cardName,
                    cardTax = cardTax,
                    courierName = courierName,
                    courierPriceKg = courierPriceKg,
                    connectWithPaypal = withPaypal

                )

                // Guardamos perfil en el MAP
                profileData[profileName] = newProfile
                profiles.add(profileName)

                // Actualizamos sharedPreferences
                saveProfiles(profileData)

                //Actualizamos spinner o drop down menu
                adapter.notifyDataSetChanged()


                Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show()

                dialog.dismiss()
            } else {

                Toast.makeText(this, "Profile Name is required", Toast.LENGTH_SHORT).show()

            }

        }

        dialog.show()

    }

    // Listeners
    private fun initListeners(adapter: ArrayAdapter<String>) {

        // Add profile data fab
        fabProfilesAddProfile.setOnClickListener {
            showAddProfileDialog(adapter)
        }

        // Edit profile data button
        btnProfilesInfoUpdate.setOnClickListener {
            // Obtener perfil seleccionado
            val selectedProfile = actvProfileDropDownMenu.text.toString()
            if (selectedProfile.isNotEmpty()) {

                showEditProfileDialog(selectedProfile, adapter)

            } else {
                Toast.makeText(this, "Select profile to edit", Toast.LENGTH_SHORT).show()

            }
        }

        // Delete profile data button
        btnProfilesInfoDelete.setOnClickListener {

            MaterialAlertDialogBuilder(this)
                .setTitle("Delete Product")
                .setMessage("Are you sure to delete this product?")
                .setNeutralButton("Cancel"){_,_ ->}
                .setPositiveButton("Yes"){_,_ ->

                    // Obtener perfil seleccionado
                    val selectedProfile = actvProfileDropDownMenu.text.toString()

                    if (selectedProfile.isNotEmpty() && profileData.containsKey(selectedProfile)) {

                        // 1. Eliminar el perfil de profileData y profiles
                        profileData.remove(selectedProfile)
                        profiles.remove(selectedProfile)

                        // Eliminamos el perfil de la data store
                        CoroutineScope(Dispatchers.IO).launch {
                            clearProfileFromDataStore()
                        }

                        // 2. Guardar los perfiles actualizados en SharedPreferences
                        saveProfiles(profileData)

                        // Eliminar el perfil seleccionado en la UI si es el que se eliminó
                        val sharedPreferences = getSharedPreferences(PROFILE_SHARED_UI_KEY, MODE_PRIVATE)
                        val editor = sharedPreferences.edit()
                        val savedProfile = sharedPreferences.getString(PROFILE_SELECTED_UI_KEY, null)

                        if (savedProfile == selectedProfile) {
                            editor.remove(PROFILE_SELECTED_UI_KEY) // Eliminar el perfil seleccionado de SharedPreferences
                            actvProfileDropDownMenu.setText("", false) // Limpiar el AutoCompleteTextView
                            clearUIProfile()
                        }

                        editor.apply()

                        // Notificar al adaptador del cambio
                        adapter.notifyDataSetChanged()

                    }

                }.setNegativeButton("No"){_,_ ->}.show()

        }
    }


    // Componentes
    private fun initComponents() {

        fabProfilesAddProfile = findViewById(R.id.fabProfilesAddProfile)
        tvProfilesTitleProfile = findViewById(R.id.tvProfilesTitleProfile)
        actvProfileDropDownMenu = findViewById(R.id.actvProfiles)

        tvProfilesInfoWebName = findViewById(R.id.tvProfilesInfoWebName)
        tvProfilesInfoWebTax = findViewById(R.id.tvProfilesInfoWebTax)
        tvProfilesInfoCardName = findViewById(R.id.tvProfilesInfoCardName)
        tvProfilesInfoCardTax = findViewById(R.id.tvProfilesInfoCardTax)
        tvProfilesInfoCourierName = findViewById(R.id.tvProfilesInfoCourierName)
        tvProfilesInfoCourierPriceKg = findViewById(R.id.tvProfilesInfoCourierPriceKg)
        ivProfilesInfoWithPaypalIcon = findViewById(R.id.ivProfilesInfoWithPaypalYesOrNot)
        tvProfilesInfoWithPaypalTax = findViewById(R.id.tvProfilesInfoWithPaypalTax)
        btnProfilesInfoUpdate = findViewById(R.id.btnProfilesInfoUpdate)
        btnProfilesInfoDelete = findViewById(R.id.btnProfilesInfoDelete)

    }


}







