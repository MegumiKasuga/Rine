package edu.carole.rine.ui.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.CompoundButton
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import edu.carole.rine.MainActivity
import edu.carole.rine.databinding.ActivityRineLoginBinding

import edu.carole.rine.R
import edu.carole.rine.data.RineData
import edu.carole.rine.data.sqlite.DBHelper
import kotlin.math.log

class RineLoginActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel
    private lateinit var binding: ActivityRineLoginBinding

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRineLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val data = (application as RineData)

        val username = binding.username
        val password = binding.password
        val login = binding.login
        val loading = binding.loading
        val rememberMe = binding.rememberMeSwitch
        val autoLogin = binding.autoLoginSwitch
        val avatar = binding.loginAvatar
        val welcomeText = binding.welcomeText
        val sp = getPreferences(MODE_PRIVATE)

        loginViewModel = ViewModelProvider(this, LoginViewModelFactory(data.db, data.networkManager, data))
            .get(LoginViewModel::class.java)

        loginViewModel.loginFormState.observe(this@RineLoginActivity, Observer {
            val loginState = it ?: return@Observer

            // disable login button unless both username / password is valid
            login.isEnabled = loginState.isDataValid

            if (loginState.usernameError != null) {
                username.error = getString(loginState.usernameError)
            }
            if (loginState.passwordError != null) {
                password.error = getString(loginState.passwordError)
            }
        })

        loginViewModel.loginResult.observe(this@RineLoginActivity, Observer {
            val loginResult = it ?: return@Observer

            loading.visibility = View.GONE
            if (loginResult.error != null) {
                showLoginFailed(loginResult.error)
            }
            if (loginResult.success != null) {
                updateUiWithUser(loginResult.success)
                setResult(Activity.RESULT_OK)
                finish()
            }
        })

        loginViewModel.autoLogin()

        if (sp.contains("remember")) {
            val str = sp.getString("remember", "")
            if(str != null)
                username.setText(str)
        }

        username.afterTextChanged {
            loginViewModel.loginDataChanged(
                username.text.toString(),
                password.text.toString()
            )
        }

        val loginFlag = loginViewModel.checkLoginOrRegister()
        avatar?.visibility = if (loginFlag) View.VISIBLE else View.INVISIBLE
        welcomeText?.visibility = if (loginFlag) View.INVISIBLE else View.VISIBLE
        if (!loginFlag) {
            login.setText(R.string.action_register)
        }

        autoLogin?.setOnCheckedChangeListener (
            {btn: CompoundButton, checked: Boolean ->
                if (checked) rememberMe?.isChecked = true
            }
        )

        password.apply {
            afterTextChanged {
                loginViewModel.loginDataChanged(
                    username.text.toString(),
                    password.text.toString()
                )
            }

            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        loginViewModel.login(
                            username.text.toString(),
                            password.text.toString(),
                            autoLogin != null && autoLogin.isChecked,
                            rememberMe != null && rememberMe.isChecked,
                            sp
                        )
                }
                false
            }

            login.setOnClickListener {
                loading.visibility = View.VISIBLE
                if (!loginFlag) {
                    loginViewModel.register(
                        username.text.toString(),
                        password.text.toString(),
                        autoLogin != null && autoLogin.isChecked,
                        rememberMe != null && rememberMe.isChecked,
                        sp
                    )
                } else {
                    loginViewModel.login(
                        username.text.toString(),
                        password.text.toString(),
                        autoLogin != null && autoLogin.isChecked,
                        rememberMe != null && rememberMe.isChecked,
                        sp
                    )
                }
            }
        }
    }

    private fun updateUiWithUser(model: LoggedInUserView) {
        val welcome = getString(R.string.welcome)
        val displayName = model.displayName
        // TODO : initiate successful logged in experience
        Toast.makeText(
            applicationContext,
            "$welcome $displayName",
            Toast.LENGTH_LONG
        ).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user", model.displayName)
        startActivity(intent)
        finish()
    }

    private fun showLoginFailed(@StringRes errorString: Int) {
        Toast.makeText(applicationContext, errorString, Toast.LENGTH_SHORT).show()
    }
}

/**
 * Extension function to simplify setting an afterTextChanged action to EditText components.
 */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
    })
}