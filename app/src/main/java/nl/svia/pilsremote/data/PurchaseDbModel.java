package nl.svia.pilsremote.data;

/**
 * Created by Wilco on 26-04-17.
 */

public class PurchaseDbModel {
    public int userId;
    public int productId;
    public int amount;
    public int price;
    public int balance;

    public PurchaseDbModel(int _userId, int _productId,
                           int _amount, double _productPrice, double _balance) {
        this.userId = _userId;
        this.productId = _productId;
        this.amount = _amount;
        this.price = (int) (_productPrice * 100) * this.amount;
        this.balance = (int) (_balance * 100);
    }
}
