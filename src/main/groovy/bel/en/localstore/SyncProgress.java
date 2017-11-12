package bel.en.localstore;

/**
 * Created by VundS02 on 20.09.2016.
 */
public interface SyncProgress {
    // progress on a scale of 0 to 100
    public void count(int progress);
    public void clear();
    public void message(String message);
}
