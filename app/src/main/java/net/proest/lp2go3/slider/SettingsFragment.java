package net.proest.lp2go3.slider;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.proest.lp2go3.R;

public class SettingsFragment extends Fragment {

    public SettingsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_settings, container, false);
    }
}