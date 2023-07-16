package com.example.plotmaster;

import androidx.appcompat.app.AppCompatDelegate;

import com.orm.SugarApp;

import es.dmoral.toasty.Toasty;

public class GrblController extends SugarApp {

    private static GrblController grblController;

    @Override
    public void onCreate() {
        super.onCreate();

        grblController = this;

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        //Iconify.with(new FontAwesomeModule());

        Toasty.Config.getInstance()
                .tintIcon(true)
                .allowQueue(true)
                .apply();
    }

    public static synchronized GrblController getInstance(){
        return grblController;
    }
}
