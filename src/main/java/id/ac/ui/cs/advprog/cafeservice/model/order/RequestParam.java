package id.ac.ui.cs.advprog.cafeservice.model.order;

public enum RequestParam {

    PREPARE("prepare"),
    DELIVER("deliver"),
    DONE("done"),
    CANCEL("cancel"),
    NULL("");

    private final String value;
    RequestParam(String value) {
        this.value = value;
    }

    public static RequestParam fromString(String value) {
        for (RequestParam param : RequestParam.values()) {
            if (param.value.equalsIgnoreCase(value)) {
                return param;
            }
        }
        return NULL;
    }
}
