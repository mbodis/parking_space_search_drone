package com.parrot.sdksample.models.qr_code_landing.logic;

import android.os.Parcel;
import android.os.Parcelable;

import com.parrot.sdksample.models.common.MyPoint;
import com.parrot.sdksample.models.landing.iface.LandingPatternIface;

/**
 * Created by mbodis on 5/15/17.
 */
public class LandingPatternQrCode extends LandingPatternIface implements Parcelable{

    public static final String TAG = LandingPatternQrCode.class.getName();

    private String qrCodeMessage;

    public LandingPatternQrCode(long tsDetection, MyPoint center, MyPoint[] pointsBB, String qrCodeMessage) {
        super(tsDetection, center, pointsBB, LandingPatternIface.PATTERN_TYPE_QR_CODE);
        this.qrCodeMessage = qrCodeMessage;
    }

    public String getQrCodeMessage() {
        return qrCodeMessage;
    }

    public void setQrCodeMessage(String qrCodeMessage) {
        this.qrCodeMessage = qrCodeMessage;
    }

    public LandingPatternQrCode(Parcel source) {
        setType(source.readInt());
        setTimestampDetected(source.readLong());
        setCenter((MyPoint) source.readParcelable(MyPoint.class.getClassLoader()));
        setLandingBB(source.createTypedArray(MyPoint.CREATOR));
        setQrCodeMessage(source.readString());
    }

    public static final Creator<LandingPatternQrCode> CREATOR = new Creator<LandingPatternQrCode>() {
        @Override
        public LandingPatternQrCode createFromParcel(Parcel in) {
            return new LandingPatternQrCode(in);
        }

        @Override
        public LandingPatternQrCode[] newArray(int size) {
            return new LandingPatternQrCode[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeInt(getType());
        parcel.writeLong(getTimestampDetected());
        parcel.writeParcelable(getCenter(), flags);
        parcel.writeParcelableArray(getLandingBB(), flags);
        parcel.writeString(getQrCodeMessage());
    }


}
