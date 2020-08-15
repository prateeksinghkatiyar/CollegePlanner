package com.example.collegeplanner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StudentLogin extends AppCompatActivity {

    EditText email, password;
    Button signin, forget;
    // firebase Authentication
    private FirebaseAuth firebaseAuth;
    FirebaseUser user;
    // Progress bar
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student_login);

        email = findViewById(R.id.txt_email);
        password = findViewById(R.id.txt_password);
        signin = findViewById(R.id.btn_signin);
        forget = findViewById(R.id.btn_forget);

        //init progress bar
        progressDialog = new ProgressDialog(this);

        // user sign in
        firebaseAuth = FirebaseAuth.getInstance();
        user = firebaseAuth.getCurrentUser();

        //signin user
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email1 = email.getText().toString().trim();
                String password1 = password.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email1).matches()) {
                    Toast.makeText(StudentLogin.this, "Invalid mail", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(password1)) {
                    Toast.makeText(StudentLogin.this, "please enter password", Toast.LENGTH_SHORT).show();
                } else {
                    SigninUser(email1, password1);
                }
            }
        });

        //forget password class
        forget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgetPasswordDialog();
            }
        });
    }

    private void showForgetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Enter your email");

        LinearLayout linearLayout = new LinearLayout(this);

        final EditText emailEt = new EditText(this);
        emailEt.setHint("Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(20);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10, 20, 10, 20);

        builder.setView(linearLayout);

        builder.setPositiveButton("Submit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String email = emailEt.getText().toString().trim();
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Toast.makeText(StudentLogin.this, "Invalid Mail Id", Toast.LENGTH_SHORT).show();
                } else {
                    beginRecovery(email);
                }
            }
        }).setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private void beginRecovery(String email) {
        progressDialog.setMessage("Sending....");
        progressDialog.show();
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    Toast.makeText(StudentLogin.this, "Email sent", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StudentLogin.this, "Failed", Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(StudentLogin.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void SigninUser(String email1, String password1) {
        //sign in firebase code
        progressDialog.setMessage("Signing in....");
        progressDialog.show();
        firebaseAuth.signInWithEmailAndPassword(email1, password1)
                .addOnCompleteListener(StudentLogin.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            progressDialog.dismiss();
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            startActivity(new Intent(getApplicationContext(), StudentHome.class));
                            Toast.makeText(StudentLogin.this, "Welcome", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            // If sign in fails, display a message to the user.
                            progressDialog.dismiss();
                            Toast.makeText(StudentLogin.this, "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(StudentLogin.this, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void Register(View view) {
        startActivity(new Intent(getApplicationContext(), StudentRegister.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if(currentUser != null) {
            startActivity(new Intent(this, StudentHome.class));
            finish();
        }
    }
}
