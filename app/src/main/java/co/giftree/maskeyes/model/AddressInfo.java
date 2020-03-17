package co.giftree.maskeyes.model;

public class AddressInfo {

    String address;
    // 위도
    double x;
    // 경도
    double y;


    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setX(double x) {
        this.x = x;
    }

    public void setY(double y) {
        this.y = y;
    }
}
