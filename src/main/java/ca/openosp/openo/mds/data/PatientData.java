//CHECKSTYLE:OFF
/**
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
 */

package ca.openosp.openo.mds.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import ca.openosp.openo.commn.dao.MdsPIDDao;
import ca.openosp.openo.commn.model.MdsPID;
import ca.openosp.openo.utility.SpringUtils;

import ca.openosp.openo.util.ConversionUtils;

public class PatientData {

    public Patient getPatient(String demographicNo) {
        MdsPIDDao dao = SpringUtils.getBean(MdsPIDDao.class);
        MdsPID pid = dao.find(ConversionUtils.fromIntString(demographicNo));

        if (pid == null) {
            return null;
        }
        return new Patient(pid.getPatientName(), pid.getDob(), pid.getHealthNumber(), pid.getSex(), pid.getHomePhone(), pid.getAlternatePatientId());
    }

    public class Patient {
        String patientName;
        String dOB;
        String sex;
        String age;
        String healthNumber;
        String homePhone;
        String workPhone;
        String patientLocation;

        public Patient(String patientName, String dOB, String healthNumber, String sex, String homePhone, String patientLocation) {
            int patientAge;
            GregorianCalendar cal = new GregorianCalendar(Locale.ENGLISH);
            GregorianCalendar now = new GregorianCalendar(Locale.ENGLISH);
            java.util.Date curTime = new java.util.Date();
            now.setTime(curTime);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy", Locale.ENGLISH);

            if (dOB.length() >= 8) {
                // boneheaded calendar numbers months from 0
                cal.set(Integer.parseInt(dOB.substring(0, 4)), Integer.parseInt(dOB.substring(4, 6)) - 1, Integer.parseInt(dOB.substring(6, 8)));

                this.dOB = dateFormat.format(cal.getTime());
            } else {
                this.dOB = "";
            }

            try {
                this.patientName = patientName.substring(0, patientName.indexOf("^")) + ", " + patientName.substring(patientName.indexOf("^") + 1).replace('^', ' ');
            } catch (IndexOutOfBoundsException e) {
                this.patientName = patientName.replace('^', ' ');
            }
            this.sex = sex;
            this.homePhone = homePhone;
            this.healthNumber = healthNumber;
            // this.patientLocation = patientLocation;
            this.patientLocation = "";
            if (dOB.length() > 0) {
                patientAge = now.get(Calendar.YEAR) - cal.get(Calendar.YEAR);
                if (now.get(Calendar.MONTH) < cal.get(Calendar.MONTH)) {
                    patientAge--;
                }
                if (now.get(Calendar.MONTH) == cal.get(Calendar.MONTH) && now.get(Calendar.DAY_OF_MONTH) < cal.get(Calendar.DAY_OF_MONTH)) {
                    patientAge--;
                }
                this.age = String.valueOf(patientAge);
            } else {
                this.age = "N/A";
            }
            this.workPhone = "";
        }

        /**
         * Gets the formatted patient name for clinical display.
         *
         * @return String patient name in "Last, First Middle" format
         */
        public String getPatientName() {
            return this.patientName;
        }

        /**
         * Gets the patient's sex/gender identifier.
         *
         * @return String patient sex (typically "M" or "F")
         */
        public String getSex() {
            return this.sex;
        }

        /**
         * Gets the formatted date of birth for clinical display.
         *
         * @return String date of birth in "dd-MMM-yyyy" format, or empty string if unavailable
         */
        public String getDOB() {
            return this.dOB;
        }

        /**
         * Gets the calculated current age of the patient.
         *
         * @return String current age in years, or "N/A" if date of birth unavailable
         */
        public String getAge() {
            return this.age;
        }

        /**
         * Gets the patient's home phone number.
         *
         * @return String home phone number for patient contact
         */
        public String getHomePhone() {
            return this.homePhone;
        }

        /**
         * Gets the patient's work phone number.
         * <p>
         * Note: Work phone is not currently supported in MDS integration.
         *
         * @return String work phone number (always empty in current implementation)
         */
        public String getWorkPhone() {
            return this.workPhone;
        }

        /**
         * Gets the patient's location information.
         * <p>
         * Note: Patient location is not currently used in MDS integration.
         *
         * @return String patient location (always empty in current implementation)
         */
        public String getPatientLocation() {
            return this.patientLocation;
        }

        /**
         * Gets the patient's Canadian health insurance number.
         *
         * @return String health insurance number for patient identification
         */
        public String getHealthNumber() {
            return this.healthNumber;
        }

    }

}
