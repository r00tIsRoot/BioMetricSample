package com.example.biometricsample

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.biometricsample.ui.theme.BioMetricSampleTheme

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        setContent {
            BioMetricSampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    MainScreen(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding),
                        clickEvent = {
                            checkAvailableAuth()
                        }
                    )
                }
            }
        }
    }


    private fun checkAvailableAuth() {
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                //  생체 인증 가능
                Log.d("MainActivity", "Biometric facility is available")


                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("내 앱을 위해 biometric을 통해 로그인")
                    .setSubtitle("biometric credential을 통해 로그인 하세요")
                    .setDescription("인증 실패 시 다시 시도하세요.") // 설명 추가 (선택 사항)
                    .setNegativeButtonText("취소") // Negative 버튼 텍스트 설정
                    .setConfirmationRequired(true) // 생체 인증 확인을 요구
                    .build()
                createBiometricPrompt().authenticate(promptInfo)
//            createBiometricPrompt().authenticate(promptInfo, cryptoObject) //cryptoObject 사용
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                //  기기에서 생체 인증을 지원하지 않는 경우
                Log.d("MainActivity", "No biometric features available on this device")
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Log.d("MainActivity", "Biometric facility is currently not available")
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                //  생체 인식 정보가 등록되지 않은 경우
                Log.d("MainActivity", "No biometric credentials configured on this device")

                promptUserToEnrollBiometric()
            }
            else -> {
                // 기타 오류
                Log.d("MainActivity", "Unknown error occurred")
            }
        }
    }

    private fun promptUserToEnrollBiometric() {
        // 생체 인증 등록 화면으로 이동
        val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Intent(Settings.ACTION_BIOMETRIC_ENROLL)
        } else {
            TODO("VERSION.SDK_INT < R")
        }
        startActivity(intent)
    }

    private fun createBiometricPrompt(): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(this)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Log.d("MainActivity", "$errorCode :: $errString")
                if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                    //TODO - 생체 인식이 안될 경우 비밀번호 입력할 수 있도록 기능 추가
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Log.d("MainActivity", "Authentication failed for an unknown reason")
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                // 인증 성공 로그 출력
                Log.d("MainActivity", "Authentication was successful")

                // 결과 객체 출력
                Log.d("MainActivity", "Result: $result")

                // CryptoObject 확인
                val cryptoObject = result.cryptoObject
                if (cryptoObject != null) {
                    Log.d("MainActivity", "CryptoObject: $cryptoObject")

                    // Cipher, Signature, MAC 정보 출력
                    val cipher = cryptoObject.cipher
                    if (cipher != null) {
                        Log.d("MainActivity", "Cipher: $cipher")
                    } else {
                        Log.d("MainActivity", "Cipher is null")
                    }

                    val signature = cryptoObject.signature
                    if (signature != null) {
                        Log.d("MainActivity", "Signature: $signature")
                    } else {
                        Log.d("MainActivity", "Signature is null")
                    }

                    val mac = cryptoObject.mac
                    if (mac != null) {
                        Log.d("MainActivity", "MAC: $mac")
                    } else {
                        Log.d("MainActivity", "MAC is null")
                    }
                } else {
                    Log.d("MainActivity", "CryptoObject is null")
                }
            }
        }

        return BiometricPrompt(this, executor, callback)
    }
}

@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
    name: String,
    clickEvent: () -> Unit = {},
) {
    Column {
        Text(
            text = "Hello $name!",
            modifier = modifier,
        )
        Button(onClick = clickEvent) { // 클릭 이벤트를 인자로 받아 실행하는 버튼 추가
            Text("Click Me")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    BioMetricSampleTheme {
        MainScreen(name = "Android")
    }
}