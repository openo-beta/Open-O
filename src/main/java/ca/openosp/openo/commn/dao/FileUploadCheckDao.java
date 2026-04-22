//CHECKSTYLE:OFF


package ca.openosp.openo.commn.dao;

import java.util.List;

import ca.openosp.openo.commn.model.FileUploadCheck;

public interface FileUploadCheckDao extends AbstractDao<FileUploadCheck> {
    List<FileUploadCheck> findByMd5Sum(String md5sum);
}
