package com.stg.app.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.socialtravelguide.api.model.SearchRequest;
import com.socialtravelguide.api.model.search.Filter;

/**
 * @author alex
 * @date 2015-06-17
 */
public class HotelListRequest extends SearchRequest implements Parcelable {

    public static final Parcelable.Creator<HotelListRequest> CREATOR = new Parcelable.Creator<HotelListRequest>() {
        public HotelListRequest createFromParcel(Parcel in) {
            return new HotelListRequest(in);
        }

        public HotelListRequest[] newArray(int size) {
            return new HotelListRequest[size];
        }
    };

    public HotelListRequest() {
        super();
    }

    public HotelListRequest(Parcel in) {
        int readType = in.readInt();
        if (readType == 1) {
            setType((CurrentLocation) in.readParcelable(CurrentLocation.class.getClassLoader()));
        } else if (readType == 2) {
            setType((Location) in.readParcelable(Location.class.getClassLoader()));
        } else if (readType == 3) {
            setType((ViewPort) in.readParcelable(ViewPort.class.getClassLoader()));
        } else if (readType == 4) {
            setType((MapSelectedViewPort) in.readParcelable(MapSelectedViewPort.class.getClassLoader()));
        }

        setNumberOfPersons(in.readInt());
        setNumbersOfRooms(in.readInt());
        setCurrency(in.readString());
        setCustomerCountryCode(in.readString());
        setLanguage(in.readString());

    }

    public HotelListRequest(HotelListRequest hotelsRequest) {
        setType(hotelsRequest.getType());
        setLanguage(hotelsRequest.getLanguage());
        setCurrency(hotelsRequest.getCurrency());
        setCustomerCountryCode(hotelsRequest.getCustomerCountryCode());
        setNumberOfPersons(hotelsRequest.getNumberOfPersons());
        setNumbersOfRooms(hotelsRequest.getNumberOfRooms());
    }

    public Location getLocation() {
        return (Location) getType();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (getType() instanceof CurrentLocation) {
            dest.writeInt(1);
            dest.writeParcelable((CurrentLocation) getType(), flags);
        } else if (getType() instanceof Location) {
            dest.writeInt(2);
            dest.writeParcelable((Location) getType(), flags);
        } else if (getType() instanceof ViewPort) {
            dest.writeInt(3);
            dest.writeParcelable((ViewPort) getType(), flags);
        } else if (getType() instanceof MapSelectedViewPort) {
            dest.writeInt(4);
            dest.writeParcelable((MapSelectedViewPort) getType(), flags);
        } else {
            dest.writeInt(0);
        }

        dest.writeInt(getNumberOfPersons());
        dest.writeInt(getNumberOfRooms());
        dest.writeString(getCurrency());
        dest.writeString(getCustomerCountryCode());
        dest.writeString(getLanguage());

    }

    @Override
    protected Filter createNewFilter() {
        return new HotelListFilter();
    }
}
