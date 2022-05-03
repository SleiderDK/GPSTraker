package com.example.ubicaciontiemporeal;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ubicaciontiemporeal.Entidades.ConexionSQLiteHelper;
import com.example.ubicaciontiemporeal.Utilidades.Utilidades;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText email, password;
    TextView tv_signUp;
    Button btn_signIn;
    SignInButton btn_signInGoogle;
    GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.et_email);
        password = (EditText) findViewById(R.id.et_password);
        btn_signIn = (Button) findViewById(R.id.btn_signIn);
        btn_signInGoogle = (SignInButton) findViewById(R.id.btn_signInGoogle);
        tv_signUp = (TextView) findViewById(R.id.tv_signUp);
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign In
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        //Boton iniciar sesion
        btn_signIn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String Email = email.getText().toString();
                String Password = password.getText().toString();
                if(Email.equals("") || Password.equals("")){
                    Toast.makeText(LoginActivity.this, "Por favor ingrese su email y su contraseña", Toast.LENGTH_SHORT).show();
                }else{
                    login(Email,Password);
                }
            }
        });

        //Boton iniciar sesion con google
        btn_signInGoogle.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                signIn();
            }
        });

        tv_signUp.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent= new Intent (LoginActivity.this, SignUpActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                overridePendingTransition(0,0);
                finish();
            }
        });
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                //Toast.makeText(LoginActivity.this, "firebaseAuthWithGoogle: "+ account.getId(), Toast.LENGTH_SHORT).show();
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(LoginActivity.this, "Google sign in failed "+ e, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            //Toast.makeText(LoginActivity.this, "signInWithCredential:success", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = mAuth.getCurrentUser();
                            IrMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            //Toast.makeText(LoginActivity.this, "signInWithCredential:failure" + task.getException() , Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                        }
                    }
                });
    }


    public void login(String Email,String Password){
        mAuth.signInWithEmailAndPassword(Email, Password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            //Toast.makeText(LoginActivity.this, "Authentication success.", Toast.LENGTH_SHORT).show();
                            IrMainActivity();
                        } else {
                            // If sign in fails, display a message to the user.
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser != null){
            //reload();
            IrMainActivity();
        }
    }

    public void IrMainActivity(){
        int ProtoloUsado = ConsultarProtocolo();
        if (ProtoloUsado == 1){
            redirectActivity(this, HttpActivity.class);
        }else if (ProtoloUsado == 2){
            redirectActivity(this, MainActivity.class);
        }
    }

    public static void redirectActivity(Activity activity, Class aClass) {
        Intent intent = new Intent(activity, aClass);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        activity.startActivity(intent);
        activity.finish();
    }

    public int ConsultarProtocolo(){
        ConexionSQLiteHelper conexion2;
        conexion2 = new ConexionSQLiteHelper(this,"dbConfiguracionFinal",null,1);
        SQLiteDatabase db = conexion2.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM "+Utilidades.TABLA_CONFIGURACION +" WHERE Id = 1",null);
        cursor.moveToFirst();
        if(cursor.getCount() > 0){
            int ProtoculoUsado = Integer.parseInt(cursor.getString(1)) + 1;
            return ProtoculoUsado;
        }else{
            return 1;
        }
    }
}