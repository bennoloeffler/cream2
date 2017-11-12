import lombok.Data;
import lombok.NonNull;

/**
 * Created 06.07.2017.
 */
@Data
public class Person {
    @NonNull String name;
    @NonNull int age;
}
