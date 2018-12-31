package com.tran.elaine.polyscapeapp;

import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Elaine on 9/10/2017.
 */

public interface GetShapeListCompleted {
    void GetShapeListCompleted(ArrayList<Bitmap> response, ArrayList<String> names);

}
