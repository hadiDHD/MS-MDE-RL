package msmderl.data;

public class Method {
    private String name;
    private String[] nanoentities;

    public Method(String name, String[] nanoentities) {
        this.name = name;
        this.nanoentities = nanoentities;
    }

    public String getName() {
        return name;
    }

    public String[] getNanoentities() {
        return nanoentities;
    }
}
