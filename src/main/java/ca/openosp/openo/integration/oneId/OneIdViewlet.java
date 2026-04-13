/**
 * Copyright (c) 2021 WELL EMR Group Inc. This software is made available under the terms of the GNU
 * General Public License, Version 2, 1991 (GPLv2). License details are available via
 * "gnu.org/licenses/gpl-2.0.html".
 */
package ca.openosp.openo.integration.oneId;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.oscarehr.common.model.AbstractModel;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class OneIdViewlet extends AbstractModel<Object> {

  @Expose @Id @GeneratedValue private Integer id;
  @Expose private String name;
  @Expose private String keyValue;
  @Expose private String updatedBy;
  @Expose private Date updateTime;
  @Expose private boolean showInEchart;
  @Expose private boolean deleted;
}
