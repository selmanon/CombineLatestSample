package com.example.abdellah.combinelatestsample;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;


import com.jakewharton.rxbinding2.widget.RxTextView;


import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.subscribers.DisposableSubscriber;
import timber.log.Timber;

import static android.text.TextUtils.isEmpty;
import static android.util.Patterns.EMAIL_ADDRESS;

public class MainActivity extends AppCompatActivity {

    TextView btnValidIndicator;
    EditText email;
    EditText password;
    EditText number;

    private DisposableSubscriber<Boolean> disposableObserver = null;
    private Flowable<CharSequence> emailChangeObservable;
    private Flowable<CharSequence> numberChangeObservable;
    private Flowable<CharSequence> passwordChangeObservable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initViews();

        emailChangeObservable =
                RxTextView.textChanges(email).skip(1).toFlowable(BackpressureStrategy.LATEST);
        passwordChangeObservable =
                RxTextView.textChanges(password).skip(1).toFlowable(BackpressureStrategy.LATEST);
        numberChangeObservable =
                RxTextView.textChanges(number).skip(1).toFlowable(BackpressureStrategy.LATEST);

        combineLatestEvents();
    }

    private void initViews() {
        btnValidIndicator = this.findViewById(R.id.btn_demo_form_valid);
        email = this.findViewById(R.id.demo_combl_email);
        password = this.findViewById(R.id.demo_combl_password);
        number = this.findViewById(R.id.demo_combl_num);
    }

    private void combineLatestEvents() {
        disposableObserver =
                new DisposableSubscriber<Boolean>() {
                    @Override
                    public void onNext(Boolean formValid) {
                        if (formValid) {
                            btnValidIndicator.setBackgroundColor(
                                    ContextCompat.getColor(getApplicationContext(), R.color.blue));
                        } else {
                            btnValidIndicator.setBackgroundColor(
                                    ContextCompat.getColor(getApplicationContext(), R.color.gray));
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "there was an error");
                    }

                    @Override
                    public void onComplete() {
                        Timber.d("completed");
                    }
                };

        Flowable.combineLatest(
                emailChangeObservable,
                passwordChangeObservable,
                numberChangeObservable,
                (newEmail, newPassword, newNumber) -> {
                    boolean emailValid = !isEmpty(newEmail) && EMAIL_ADDRESS.matcher(newEmail).matches();
                    if (!emailValid) {
                        email.setError("Invalid Email!");
                    }

                    boolean passValid = !isEmpty(newPassword) && newPassword.length() > 8;
                    if (!passValid) {
                        password.setError("Invalid Password!");
                    }

                    boolean numValid = !isEmpty(newNumber);
                    if (numValid) {
                        int num = Integer.parseInt(newNumber.toString());
                        numValid = num > 0 && num <= 100;
                    }
                    if (!numValid) {
                        number.setError("Invalid Number!");
                    }

                    return emailValid && passValid && numValid;
                })
                .subscribe(disposableObserver);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposableObserver.dispose();
    }
}
