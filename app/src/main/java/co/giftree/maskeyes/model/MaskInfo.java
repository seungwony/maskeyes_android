package co.giftree.maskeyes.model;

import android.os.Parcel;
import android.os.Parcelable;

import co.giftree.maskeyes.util.JsonUtil;

public class MaskInfo implements Parcelable {

    String name;
    String code; // 식별 코드
    String addr; // 주 소

    float lat; // 위 도
    float lng; // 경 도


    String type;

    String remain_stat;



    String stock_at; // 입고시간

    String created_at; // 데이터 생성 일자

    double distance;


    public MaskInfo(){

    }
    protected MaskInfo(Parcel in) {
        name = in.readString();
        code = in.readString();
        addr = in.readString();
        lat = in.readFloat();
        lng = in.readFloat();


        type = in.readString();

        remain_stat = in.readString();
        stock_at = in.readString();

        created_at = in.readString();
        distance = in.readDouble();
    }

    public static final Creator<MaskInfo> CREATOR = new Creator<MaskInfo>() {
        @Override
        public MaskInfo createFromParcel(Parcel in) {
            return new MaskInfo(in);
        }

        @Override
        public MaskInfo[] newArray(int size) {
            return new MaskInfo[size];
        }
    };

    public String getName() {
        return name;
    }

    public float getLat() {
        return lat;
    }

    public float getLng() {
        return lng;
    }


    public String getAddr() {
        return addr;
    }

    public String getCode() {
        return code;
    }

    public String getCreated_at() {
        return created_at;
    }

    public String getRemain_stat() {
        return remain_stat;
    }

    public String getStock_at() {
        return stock_at;
    }

    public String getType() {
        return type;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public void setLat(float lat) {
        this.lat = lat;
    }

    public void setLng(float lng) {
        this.lng = lng;
    }


    public void setName(String name) {
        this.name = name;
    }


    public void setRemain_stat(String remain_stat) {
        this.remain_stat = remain_stat;
    }

    public void setStock_at(String stock_at) {
        this.stock_at = stock_at;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public double getDistance() {
        return distance;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(code);
        dest.writeString(addr);
        dest.writeFloat(lat);
        dest.writeFloat(lng);

        dest.writeString(type);
        dest.writeString(remain_stat);
        dest.writeString(stock_at);

        dest.writeString(created_at);
        dest.writeDouble(distance);
    }
}
