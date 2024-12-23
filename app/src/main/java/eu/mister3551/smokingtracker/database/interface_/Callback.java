package eu.mister3551.smokingtracker.database.interface_;

public interface Callback {
    void onSuccess(Object object);
    void onError(String errorMessage);
}