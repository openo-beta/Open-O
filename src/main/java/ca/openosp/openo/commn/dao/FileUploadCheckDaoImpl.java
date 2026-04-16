//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;
import javax.persistence.Query;

import ca.openosp.openo.commn.model.FileUploadCheck;
import org.springframework.stereotype.Repository;

@Repository
public class FileUploadCheckDaoImpl extends AbstractDaoImpl<FileUploadCheck> implements FileUploadCheckDao {

    public FileUploadCheckDaoImpl() {
        super(FileUploadCheck.class);
    }

    @SuppressWarnings("unchecked")
    public List<FileUploadCheck> findByMd5Sum(String md5sum) {
        Query query = createQuery("c", "c.md5sum = ?1");
        query.setParameter(1, md5sum);
        return query.getResultList();
    }
}
