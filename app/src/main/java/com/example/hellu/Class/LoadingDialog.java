package com.example.hellu.Class;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.TextView;

import com.example.hellu.R;

public class LoadingDialog extends AlertDialog{
    Context context;
    TextView txtLoadingText;
    String loadingText;
    public LoadingDialog(Context context, String loadingText) {
        super(context);
        this.context=context;
        this.loadingText=loadingText;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loading_dialog);
        txtLoadingText=findViewById(R.id.loadingText);
        txtLoadingText.setText(loadingText);
        setCancelable(false);
    }
    public void startDialog(){
        show();
    }
    public void dismissDialog(){
        dismiss();
    }
}
