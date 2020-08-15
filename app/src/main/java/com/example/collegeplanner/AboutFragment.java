package com.example.collegeplanner;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


/**
 * A simple {@link Fragment} subclass.
 */
public class AboutFragment extends Fragment {

    TextView linkedin, github;

    public AboutFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_about, container, false);
        //init views
        linkedin = view.findViewById(R.id.linkedin);
        linkedin.setMovementMethod(LinkMovementMethod.getInstance());
        github = view.findViewById(R.id.git);
        github.setMovementMethod(LinkMovementMethod.getInstance());

        return view;
    }
}
