package se.example.monkeydogpsalarm.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    private val phoneNumberMutable: MutableLiveData<String> = MutableLiveData()

    var phoneNumber: String
        get() = phoneNumberMutable.value ?: ""
        set(value) {
            phoneNumberMutable.value = value
        }

    fun getPhoneNumberLiveData() = phoneNumberMutable as LiveData<String>
}