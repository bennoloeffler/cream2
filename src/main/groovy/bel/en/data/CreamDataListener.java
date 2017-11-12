package bel.en.data;

/**
 * used to notify all the views, whenever
 * - data in the model changes (by a sync or another view)
 * - the CreamSelection changes (by another view)
 *
 * The origin of the notification is provided in order to prevent
 * endless notification cycles.
 */
public class CreamDataListener {

    public void dataChanged(Object origin) {

    }

    public void selectionChanged(Object origin, CreamFirmaData creamFirmaData) {

    }

    public void noteChanged(Object origin, CreamFirmaData creamFirmaData) {

    }
}
