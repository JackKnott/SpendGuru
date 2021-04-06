package ncl.csc3122.spendguru;

import androidx.annotation.NonNull;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Cost {

    private int id;
    private String category;
    private double amount;
    private Date date;
    private String info;

    public Cost (int id, String category, double amount, Date date, String info) {
        this.id = id;
        this.category = category;
        this.amount = amount;
        this.date = date;
        this.info = info;
    }

    public Cost (String category, double amount, Date date) {
        this.category = category;
        this.amount = amount;
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public String getInfo() {
        return info;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getSimpleDate() {
        Format formatter = new SimpleDateFormat("EEE dd/MM/yyyy HH:mm");
        String dateString = formatter.format(date);

        return dateString;
    }

    @NonNull
    @Override
    public String toString() {
        String infoString = "";
        if (info != null) infoString = " (" + info + ")";
        String cost = getSimpleDate() + " - " + category +
                " : £" + amount + infoString;

        return cost;
    }
}
