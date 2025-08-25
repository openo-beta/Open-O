//CHECKSTYLE:OFF
/**
 * Copyright (c) 2024. Magenta Health. All Rights Reserved.
 * Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * <p>
 * This software was written for the
 * Department of Family Medicine
 * McMaster University
 * Hamilton
 * Ontario, Canada
 * <p>
 * Modifications made by Magenta Health in 2024.
 */
package org.oscarehr.managers;

import java.util.ArrayList;
import java.util.List;

import org.oscarehr.common.dao.DrugProductDao;
import org.oscarehr.common.dao.DrugProductTemplateDao;
import org.oscarehr.common.dao.ProductLocationDao;
import org.oscarehr.common.model.DrugProduct;
import org.oscarehr.common.model.DrugProductTemplate;
import org.oscarehr.common.model.ProductLocation;
import org.oscarehr.util.LoggedInInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import oscar.log.LogAction;

public interface DrugProductManager {

    void saveDrugProduct(LoggedInInfo loggedInInfo, DrugProduct drugProduct);

    void updateDrugProduct(LoggedInInfo loggedInInfo, DrugProduct drugProduct);

    DrugProduct getDrugProduct(LoggedInInfo loggedInInfo, Integer id);

    List<DrugProduct> getAllDrugProducts(LoggedInInfo loggedInInfo, Integer offset, Integer limit);

    List<DrugProduct> getAllDrugProductsByName(LoggedInInfo loggedInInfo, Integer offset, Integer limit,
                                               String productName);

    List<DrugProduct> getAllDrugProductsByNameAndLot(LoggedInInfo loggedInInfo, Integer offset, Integer limit,
                                                     String productName, String lotNumber, Integer location, boolean availableOnly);

    Integer getAllDrugProductsByNameAndLotCount(LoggedInInfo loggedInInfo, String productName, String lotNumber,
                                                Integer location, boolean availableOnly);

    List<DrugProduct> getAllDrugProductsGroupedByCode(LoggedInInfo loggedInInfo, Integer offset, Integer limit);

    List<String> findUniqueDrugProductNames(LoggedInInfo loggedInInfo);

    List<String> findUniqueDrugProductLotsByName(LoggedInInfo loggedInInfo, String productName);

    void deleteDrugProduct(LoggedInInfo loggedInInfo, Integer drugProductId);

    List<ProductLocation> getProductLocations();

    List<DrugProductTemplate> getDrugProductTemplates();
}
