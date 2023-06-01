package id.ac.ui.cs.advprog.cafeservice.model.order;

public enum Status {
    CONFIRM("Menunggu Konfirmasi"),
    PREPARE("Sedang Disiapkan"),
    DELIVER("Sedang Diantar"),
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
