package com.parrot.sdksample.models.common;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by mbodis on 6/4/17.
 */

public class MyPoint implements Parcelable {
    public int x;
    public int y;

    public MyPoint(int x, int y){
        this.x = x;
        this.y = y;
    }

    protected MyPoint(Parcel in) {
        x = in.readInt();
        y = in.readInt();
    }

    public static final Creator<MyPoint> CREATOR = new Creator<MyPoint>() {
        @Override
        public MyPoint createFromParcel(Parcel in) {
            return new MyPoint(in);
        }

        @Override
        public MyPoint[] newArray(int size) {
            return new MyPoint[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(x);
        parcel.writeInt(y);
    }
}
