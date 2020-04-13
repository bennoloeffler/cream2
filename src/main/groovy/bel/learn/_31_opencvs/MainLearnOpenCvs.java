package bel.learn._31_opencvs;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.Cleanup;
import lombok.Data;
import lombok.val;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * https://sourceforge.net/p/opencsv/source/ci/master/tree/
 */
public class MainLearnOpenCvs {

    @Data
    public static class Visitor {

        @CsvBindByName
        private String firstName;

        @CsvBindByName
        private String lastName;

        @CsvBindByName
        private int visitsToWebsite;

    }

    public static void main(String[] args) {


        try {
            val fileName = "yourfile.csv";
            //val fileWriter = new FileWriter(fileName);
            //fileWriter.write("aha...");
            @Cleanup val file = new FileReader(fileName);
            System.out.println(fileName);


            /* Wrong version of lang3.jar Apache Commons FieldUtils?
            java.lang.NoSuchMethodError: org.apache.commons.lang3.reflect.FieldUtils.getAllFields(Ljava/lang/Class;)[Ljava/lang/reflect/Field;
	        at com.opencsv.bean.MappingUtils.determineMappingStrategy(MappingUtils.java:57)
	        at com.opencsv.bean.CsvToBeanBuilder.build(CsvToBeanBuilder.java:141)
	        at bel.learn._31_opencvs.MainLearnOpenCvs.main(MainLearnOpenCvs.java:45)
             */
            List<Visitor> beans = new CsvToBeanBuilder(file)
                    .withType(Visitor.class).build().parse();

            System.out.println(beans);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
