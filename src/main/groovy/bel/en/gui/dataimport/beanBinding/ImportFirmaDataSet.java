package bel.en.gui.dataimport.beanBinding;

import com.opencsv.bean.AbstractBeanField;
import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvCustomBindByName;
import com.opencsv.exceptions.CsvConstraintViolationException;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import lombok.Data;

/**
 * Created 18.04.2017.
 */
@Data
public class ImportFirmaDataSet {

    //     @CsvBindByName @CsvBindByPosition @CsvCustomBindByName @CsvCustomBindByPosition

    @CsvBindByName(required = true, column="uid")
    String uid;

    @CsvCustomBindByName(converter = FirmenNamenConverter.class, column = "name")
    String firmenName;


    class FirmenNamenConverter<String> extends AbstractBeanField<String> {
        @Override
        protected String convert(java.lang.String s) throws CsvDataTypeMismatchException, CsvRequiredFieldEmptyException, CsvConstraintViolationException {
            String result = (String) s.toUpperCase();
            return  result;
        }
    }
}
