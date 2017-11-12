package bel.en.data;

import lombok.Data;

import java.util.List;

/**
 * Created 02.04.2017.
 */
@Data
public class SearchCache {
    List<String> foundInHistory;
    List<CreamAttributeData> foundInFirma;


}
