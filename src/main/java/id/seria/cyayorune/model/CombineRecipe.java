package id.seria.cyayorune.model;

public class CombineRecipe {
    private final String id;
    private final EnhanceRecipe.MMOItemData input;
    private final EnhanceRecipe.MMOItemData result;
    private final double successRate;
    private final double price;
    private final String currency;

    public CombineRecipe(String id, EnhanceRecipe.MMOItemData input, EnhanceRecipe.MMOItemData result, double successRate, double price, String currency) {
        this.id = id;
        this.input = input;
        this.result = result;
        this.successRate = successRate;
        this.price = price;
        this.currency = currency;
    }

    public String getId() { return id; }
    public EnhanceRecipe.MMOItemData getInput() { return input; }
    public EnhanceRecipe.MMOItemData getResult() { return result; }
    public double getSuccessRate() { return successRate; }
    public double getPrice() { return price; }
    public String getCurrency() { return currency; }
}
