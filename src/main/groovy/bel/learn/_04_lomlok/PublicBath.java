package bel.learn._04_lomlok;

import lombok.Builder;
import lombok.Singular;

import java.util.ArrayList;
import java.util.List;

/**
 * Created 09.04.2017.
 */
@Builder
public class PublicBath {
    String nameOfBath;
    @Singular("swimMaster")
    private List<String> swimMaster = new ArrayList<>();
}
