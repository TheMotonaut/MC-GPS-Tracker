package se.example.monkeydogpsalarm

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.viewModels
import se.example.monkeydogpsalarm.viewmodels.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private lateinit var model: LoginViewModel
    private lateinit var phoneNumber: EditText
    private lateinit var loginButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        phoneNumber = findViewById<EditText>(R.id.phone_number_input_view)
        loginButton = findViewById<Button>(R.id.login_button)

        val loginViewModel: LoginViewModel by viewModels()
        model = loginViewModel

        model.getPhoneNumberLiveData().observe(this, {
            phoneNumber.setText(it, TextView.BufferType.EDITABLE)
        })
        phoneNumber.setOnKeyListener {
            _, _, _ ->
                model.phoneNumber = phoneNumber.text.toString()
                false
        }
        loginButton.setOnClickListener {
            Log.d("BOOT", "SHOOY")
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
