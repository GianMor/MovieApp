package com.example.popularmovie.fragment;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class AddFavoriteDialogfragment extends DialogFragment {

    String text,title;
    long id;
    IAddFavoriteDialogfragmentListener mListener;

    public AddFavoriteDialogfragment(String text,String title,long id) {
        this.text = text;
        this.title = title;
        this.id = id;
    }



    @Override
    public void onAttach(Activity activity) {
        if (activity instanceof IAddFavoriteDialogfragmentListener) {
            mListener = (IAddFavoriteDialogfragmentListener) activity;
        } else {
            mListener = null;
        }
        super.onAttach(activity);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable final Bundle savedInstanceState) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        dialog.setTitle(title);
        dialog.setMessage(text);
        dialog.setPositiveButton("SI", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onPositiveClick(id);
            }
        });
        dialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mListener.onNegativeClick();

            }
        });
        return dialog.create();
    }
}
