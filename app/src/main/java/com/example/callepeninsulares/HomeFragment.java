package com.example.callepeninsulares;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_home_fragment, container, false);

        Button MedinaBtn = view.findViewById(R.id.medinaBtn);
        Button BacommBtn = view.findViewById(R.id.bacommBtn);
        Button ChapelBtn = view.findViewById(R.id.chapelBtn);
        Button AutomotiveBtn = view.findViewById(R.id.autoBtn);
        Button MedinaGroundsBtn = view.findViewById(R.id.medinagroundsBtn);

        if (MedinaBtn != null) {
            MedinaBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), activity_medina.class);
                startActivity(intent);
            });
        } else {
            Log.e("HomeFragment", "Medina Button is null");
        }
        if (BacommBtn != null) {
            BacommBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), bacomm.class);
                startActivity(intent);
            });
        } else {
            Log.e("HomeFragment", "Medina Button is null");
        }
        if (ChapelBtn != null) {
            ChapelBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), chapel.class);
                startActivity(intent);
            });
        } else {
            Log.e("HomeFragment", "Medina Button is null");
        }
        if (AutomotiveBtn != null) {
            AutomotiveBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), automotive.class);
                startActivity(intent);
            });

        }
        if (MedinaGroundsBtn != null) {
            MedinaGroundsBtn.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), medinagrounds.class);
                startActivity(intent);
            });
        }
        return view;
    }
}
