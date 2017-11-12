package bel.learn._04_lomlok;


import lombok.*;
import lombok.experimental.FieldDefaults;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;

@AllArgsConstructor
@NoArgsConstructor // (force = true) // even if final or NonNull - force it e.g. for hibernate...
@Data
@RequiredArgsConstructor // only really required final und NonNull fields
//@FieldDefaults(makeFinal=true, level= AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(exclude = "mTestGrades")
public class PersonLombok {

    @Getter(lazy = true)
    private final double[] cached = expensive();

    boolean male;
    @NonNull
    String fullName;
    @Setter(AccessLevel.PROTECTED)

    int age;
    int[] mTestGrades;
    @Setter
    @Getter
    double money;

    public double mary(@NonNull PersonLombok other) {
        return money + other.money;
    }

    @SneakyThrows(FileNotFoundException.class)
    public String calcSomthingStupid() {
        val example = Arrays.asList("a", "bed", "sadsafdsfd", "dfsdffs");
        val result = example.stream().filter(e -> hashCode() > 10000).max((a, b) -> a.length() - b.length()).get();
        File f = new File("abc");
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(f)); // this throws...
        return result;
    }


    private double[] expensive() {
        double[] result = new double[1000000];
        for (int i = 0; i < result.length; i++) {
            result[i] = Math.asin(i);
        }
        return result;
    }


}
