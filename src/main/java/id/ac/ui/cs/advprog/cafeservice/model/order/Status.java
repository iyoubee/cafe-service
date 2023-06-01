package id.ac.ui.cs.advprog.cafeservice.model.order;

public enum Status {
    CONFIRM("Menunggu konfirmasi"),
    PREPARE("Sedang disiapkan"),
    DELIVER("Sedang diantar"),
    DONE("Selesai"),
    CANCEL("Dibatalkan");

    private final String value;
    Status(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
