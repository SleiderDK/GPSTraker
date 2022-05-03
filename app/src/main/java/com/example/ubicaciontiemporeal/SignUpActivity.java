package com.example.ubicaciontiemporeal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignUpActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText email, password;
    TextView tv_signIn;
    Button btn_signUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        email = (EditText) findViewById(R.id.et_email);
        password = (EditText) findViewById(R.id.et_password);
        btn_signUp = (Button) findViewById(R.id.btn_signUp);
        tv_signIn = (TextView) findViewById(R.id.tv_signIn);
        mAuth = FirebaseAuth.getInstance();

        //Boton registrar
        btn_signUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String Email = email.getText().toString();
                String Password = password.getText().toString();
                if(Email.equals("") || Password.equals("")){
                    Toast.makeText(SignUpActivity.this, "Por favor ingrese un email y contraseña", Toast.LENGTH_SHORT).show();
                }else{
                    signUp(Email,Password);
                }
            }
        });
        //Boton registrar
        tv_signIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent= new Intent (SignUpActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0,0);
                finish();
            }
        });
    }
    public void signUp(String Email,String Password){
        mAuth.createUserWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Toast.makeText(SignUpActivity.this, "createUserWithEmail:success", Toast.LENGTH_SHORT).show();
                            Toast.makeText(SignUpActivity.this, "Su cuenta fue creada exitosamente", Toast.LENGTH_SHORT).show();
                            Toast.makeText(SignUpActivity.this, "Ahora puedes iniciar sesión", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent intent= new Intent (SignUpActivity.this, LoginActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(SignUpActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}