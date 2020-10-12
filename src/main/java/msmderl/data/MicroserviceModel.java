package msmderl.data;

public class MicroserviceModel {
    private Microservice[] services;

    public MicroserviceModel(Microservice[] services) {
        this.services = services;
    }

    public Microservice[] getServices() {
        return services;
    }
}
