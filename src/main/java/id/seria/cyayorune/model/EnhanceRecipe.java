package id.seria.cyayorune.model;

import java.util.List;

public class EnhanceRecipe {
    private final String id;
    private final MMOItemData baseItem;
    private final double maxSuccess;
    private final List<MaterialData> materials;

    public EnhanceRecipe(String id, MMOItemData baseItem, double maxSuccess, List<MaterialData> materials) {
        this.id = id;
        this.baseItem = baseItem;
        this.maxSuccess = maxSuccess;
        this.materials = materials;
    }

    public String getId() { return id; }
    public MMOItemData getBaseItem() { return baseItem; }
    public double getMaxSuccess() { return maxSuccess; }
    public List<MaterialData> getMaterials() { return materials; }

    public static class MaterialData {
        private final MMOItemData mmoitem;
        private final int amount;
        private final double successRate;
        private final double successIncrease;
        private final double price;
        private final String currency;

        public MaterialData(MMOItemData mmoitem, int amount, double successRate, double successIncrease, double price, String currency) {
            this.mmoitem = mmoitem;
            this.amount = amount;
            this.successRate = successRate;
            this.successIncrease = successIncrease;
            this.price = price;
            this.currency = currency;
        }

        public MMOItemData getMmoitem() { return mmoitem; }
        public int getAmount() { return amount; }
        public double getSuccessRate() { return successRate; }
        public double getSuccessIncrease() { return successIncrease; }
        public double getPrice() { return price; }
        public String getCurrency() { return currency; }
    }

    public static class MMOItemData {
        private final String type;
        private final String id;

        public MMOItemData(String type, String id) {
            this.type = type;
            this.id = id;
        }

        public String getType() { return type; }
        public String getId() { return id; }
        
        public boolean matches(String type, String id) {
            return this.type.equalsIgnoreCase(type) && this.id.equalsIgnoreCase(id);
        }
    }
}
