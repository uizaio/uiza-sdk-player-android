
package vn.loitp.restapi.uiza.model.v1.getdetailentity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class ExtendData {

    @SerializedName("published-date")
    @Expose
    private String publishedDate;
    @SerializedName("price")
    @Expose
    private String price;

    public String getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(String publishedDate) {
        this.publishedDate = publishedDate;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

}
