package msmderl.data;

public class Microservice {
    private String id;
    private String name;
    private String[] nanoentities;

    public Microservice(String id, String name, String[] nanoentities) {
        this.id = id;
        this.name = name;
        this.nanoentities = nanoentities;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String[] getNanoentities() {
        return nanoentities;
    }
}
