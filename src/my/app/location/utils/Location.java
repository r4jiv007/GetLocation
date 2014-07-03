package my.app.location.utils;

import android.os.Parcel;
import android.os.Parcelable;

public class Location implements Parcelable {

	private double mLatitude;
	private double mLongitude;
	private String mAddress = null;

	public Location(double mLatitude, double mLongitude) {
		super();
		this.mLatitude = mLatitude;
		this.mLongitude = mLongitude;
	}

	public Location(double mLatitude, double mLongitude, String mAddress) {
		super();
		this.mLatitude = mLatitude;
		this.mLongitude = mLongitude;
		this.mAddress = mAddress;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public String getAddress() {
		return mAddress;
	}

	public void setmAddress(String mAddress) {
		this.mAddress = mAddress;
	}

	public void setmLatitude(double mLatitude) {
		this.mLatitude = mLatitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setmLongitude(double mLongitude) {
		this.mLongitude = mLongitude;
	}

	private Location(Parcel in) {
		mLatitude = in.readDouble();
		mLongitude = in.readDouble();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(mLatitude);
		dest.writeDouble(mLongitude);
	}

	public static final Parcelable.Creator<Location> CREATOR = new Parcelable.Creator<Location>() {
		public Location createFromParcel(Parcel in) {
			return new Location(in);
		}

		public Location[] newArray(int size) {
			return new Location[size];
		}
	};

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		String value = mLatitude + "," + mLongitude;
		return value;
	}
}
