//CHECKSTYLE:OFF

package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.AbstractCodeSystemModel;

public interface AbstractCodeSystemDao<T extends AbstractCodeSystemModel<?>> extends AbstractDao<T> {

    //public static enum codingSystem {icd9,icd10,ichppccode,msp,SnomedCore}

    // public static String getDaoName(codingSystem codeSystem);

    public List<T> searchCode(String term);

    public T findByCode(String code);

    public AbstractCodeSystemModel<?> findByCodingSystem(String codingSystem);

    public static enum codingSystem {icd9, icd10, ichppccode, msp, SnomedCore}

    public static Class<?> getDaoName(codingSystem codeSystem) {
        Class<?> object;
        switch(codeSystem) {
            case SnomedCore:
                object = SnomedCoreDao.class;
                break;
            case icd10:
                object = Icd10Dao.class;
                break;
            case icd9:
                object = Icd9Dao.class;
                break;
            case ichppccode:
                object = IchppccodeDao.class;
                break;
            case msp:
                object = DiagnosticCodeDao.class;
                break;
            default:
                throw new IllegalArgumentException("Unsupported code system: " + codeSystem + ". Please use one of icd9, ichppccode, snomedcore");
        }
        return object;
    }
}
