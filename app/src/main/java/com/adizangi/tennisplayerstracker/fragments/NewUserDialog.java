/*
   A dialog that explains how to use the app
 */

package com.adizangi.tennisplayerstracker.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import com.adizangi.tennisplayerstracker.R;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class NewUserDialog extends DialogFragment {

    @NonNull
    @Override
    /*
       Creates the dialog and returns it
     */
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_new_user_title)
                .setView(View.inflate(getContext(), R.layout.dialog_new_user, null))
                .setPositiveButton(R.string.button_close, null)
                .setCancelable(false);
        return dialogBuilder.create();
    }
}
