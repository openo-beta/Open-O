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
package ca.openosp.openo.jobs;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ca.openosp.openo.util.UtilDateUtilities;
import ca.openosp.openo.PMmodule.model.ProgramProvider;
import ca.openosp.openo.commn.dao.OnCallClinicDao;
import ca.openosp.openo.commn.dao.OscarAppointmentDao;
import ca.openosp.openo.commn.dao.PatientLabRoutingDao;
import ca.openosp.openo.commn.dao.ProviderDataDao;
import ca.openosp.openo.commn.dao.ProviderInboxRoutingDao;
import ca.openosp.openo.commn.dao.ScheduleDateDao;
import ca.openosp.openo.commn.jobs.OscarRunnable;
import ca.openosp.openo.commn.model.Appointment;
import ca.openosp.openo.commn.model.Demographic;
import ca.openosp.openo.commn.model.PatientLabRouting;
import ca.openosp.openo.commn.model.Provider;
import ca.openosp.openo.commn.model.ProviderData;
import ca.openosp.openo.commn.model.ScheduleDate;
import ca.openosp.openo.commn.model.Security;
import ca.openosp.openo.managers.ProgramManager2;
import ca.openosp.openo.utility.MiscUtils;
import ca.openosp.openo.utility.SpringUtils;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Font.FontFamily;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfWriter;

import ca.openosp.OscarProperties;
import ca.openosp.openo.documentManager.EDoc;
import ca.openosp.openo.documentManager.EDocUtil;

/**
 * This class implements a runnable job that processes appointments from the
 * "On-Call Clinic" for the previous day. Its primary function is to generate
 * PDF documents summarizing the visit and send them to the patient's primary
 * care provider (PCP).
 *
 * The job performs the following steps:
 * 1. It checks if the previous day was designated as an On-Call Clinic day.
 * 2. If so, it fetches all appointments from that day that occurred in a schedule
 *    template named "P:OnCallClinic".
 * 3. For each valid appointment, it determines if the patient showed up or was a no-show.
 * 4. It generates a PDF document tailored to the appointment outcome (attended or no-show).
 *    This document details which patient was seen (or not seen), by which on-call provider,
 *    and on what date.
 * 5. The generated PDF is then saved as an eDoc (electronic document) within the patient's chart.
 * 6. Finally, the eDoc is routed to the inbox of the patient's regular PCP for their review.
 *
 * This process ensures that PCPs are kept informed about their patients' interactions
 * with the on-call clinic, facilitating continuity of care.
 */
public class OscarOnCallClinic implements OscarRunnable {
    private Provider provider = null;
    private static String SCHEDULE_TEMPLATE = "P:OnCallClinic";
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("EEEE MMMM d, yyyy");
    private static String DOCUMENTDIR = OscarProperties.getInstance().getProperty("DOCUMENT_DIR");

    /**
     * The main execution method for the OscarOnCallClinic job. This method is
     * triggered by the scheduling system. It identifies and processes on-call clinic
     * appointments from the previous day, generating and sending notification
     * documents to the patients' primary care providers.
     */
    @Override
    public void run() {
        MiscUtils.getLogger().info("Starting OSCAR ON CALL CLINIC Job");
        OnCallClinicDao onCallClinicDao = SpringUtils.getBean(OnCallClinicDao.class);
        // Set the calendar to yesterday to process the previous day's appointments.
        Calendar yesterday = Calendar.getInstance();
        MiscUtils.getLogger().info("DATE " + yesterday.getTime());
        yesterday.add(Calendar.DATE, -1);
        yesterday.set(Calendar.HOUR_OF_DAY, 0);
        yesterday.set(Calendar.MINUTE, 0);
        yesterday.set(Calendar.SECOND, 0);
        yesterday.set(Calendar.MILLISECOND, 0);
        MiscUtils.getLogger().info("DATE " + yesterday.getTime());
        Date d = yesterday.getTime();

        // Check if yesterday was an on-call clinic day. If not, the job does nothing.
        if (onCallClinicDao.findByDate(d) != null) {
            MiscUtils.getLogger().info("YESTERDAY WAS ON CALL CLINIC");

            OscarAppointmentDao appointmentDao = SpringUtils.getBean(OscarAppointmentDao.class);
            ProviderDataDao providerDataDao = SpringUtils.getBean(ProviderDataDao.class);
            ScheduleDateDao scheduleDateDao = SpringUtils.getBean(ScheduleDateDao.class);
            // Find all appointments that occurred yesterday.
            List<Object[]> results = appointmentDao.findAppointments(d, d);
            MiscUtils.getLogger().info("FOUND " + results.size() + " appointments");

            for (Object[] result : results) {
                Appointment appointment = (Appointment) result[0];
                // Skip any appointments that were canceled.
                if (appointment.getStatus().matches(".*C.*")) {
                    MiscUtils.getLogger().info("Skipping appointment as it is canceled");
                } else {
                    // Verify that the appointment was part of the official "OnCallClinic" schedule.
                    ScheduleDate scheduleDate = scheduleDateDao.findByProviderNoAndDate(appointment.getProviderNo(), appointment.getAppointmentDate());
                    if (scheduleDate != null && scheduleDate.getHour().equalsIgnoreCase(SCHEDULE_TEMPLATE)) {
                        Demographic demographic = (Demographic) result[1];
                        // Ensure the patient has a primary provider and that it's not the on-call provider.
                        if (demographic.getProviderNo() != null && !demographic.getProviderNo().equals(appointment.getProviderNo())) {
                            ProviderData providerData = providerDataDao.find(appointment.getProviderNo());

                            String filename = "OSCAROnCallClinic" + new Date().getTime() + ".pdf";

                            // Differentiate between patients who did not show up ('N' status) and those who did.
                            if (appointment.getStatus().matches(".*N.*")) {
                                if (makeNoShowApptDocument(filename, appointment, demographic, providerData)) {
                                    SendDocument(filename, demographic);
                                }
                            } else {

                                if (makeGoodApptDocument(filename, appointment, demographic, providerData)) {
                                    SendDocument(filename, demographic);
                                }
                            }
                        }
                    } else {
                        MiscUtils.getLogger().info("Skipping appointment as it does not belong to on call clinic schedule");
                    }
                }
            }

        }
        MiscUtils.getLogger().info("Finished OSCAR ON CALL CLINIC Job");
    }


    /**
     * Creates a PDF document for a patient who did NOT show up for their on-call clinic appointment.
     * The document notifies the patient's primary care provider of the missed appointment.
     *
     * @param filename The name of the PDF file to be created.
     * @param appointment The appointment object containing details of the missed appointment.
     * @param demographic The demographic object for the patient.
     * @param providerData The provider object for the on-call physician.
     * @return {@code true} if the document was created successfully, {@code false} otherwise.
     */
    private Boolean makeNoShowApptDocument(String filename, Appointment appointment, Demographic demographic, ProviderData providerData) {
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(DOCUMENTDIR + filename));
            Rectangle pageSize = new Rectangle(PageSize.A5.getWidth(), PageSize.A5.getHeight());
            pageSize.setBackgroundColor(new BaseColor(0xCC, 0xCC, 0xFF));
            document.setPageSize(pageSize);
            document.setMargins(36, 72, 108, 180);
            document.setMarginMirroringTopBottom(true);
            document.open();
            Font headerFont = new Font(FontFamily.HELVETICA, 14);
            Chunk chunkHeader = new Chunk("OSCAR ON CALL CLINIC", headerFont);
            chunkHeader.setUnderline(2f, -2f);
            Paragraph header = new Paragraph(chunkHeader);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setExtraParagraphSpace(24f);
            document.add(header);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            Font bodyFont = new Font(FontFamily.TIMES_ROMAN, 12);
            Chunk chunkAttn = new Chunk("ATTN: " + demographic.getProvider().getFormattedName(), bodyFont);
            Paragraph attnParagraph = new Paragraph(chunkAttn);
            attnParagraph.setAlignment(Element.ALIGN_LEFT);
            attnParagraph.setExtraParagraphSpace(24f);
            document.add(attnParagraph);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            Font patientFont = new Font(FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.BLUE);
            Chunk patientChunk = new Chunk(demographic.getFormattedName(), patientFont);
            Paragraph body = new Paragraph();
            Chunk body1 = new Chunk("Your patient ", bodyFont);
            Chunk body2 = new Chunk(" did NOT show for an appointment on " + simpleDateFormat.format(appointment.getAppointmentDate()) +
                    ".  The appointment was with " + providerData.getFirstName() + " " + providerData.getLastName());

            body.add(body1);
            body.add(patientChunk);
            body.add(body2);
            body.setAlignment(Element.ALIGN_LEFT);
            document.add(body);
            document.close();


        } catch (FileNotFoundException e) {
            MiscUtils.getLogger().error("ERROR", e);
            return false;

        } catch (DocumentException e) {
            MiscUtils.getLogger().error("ERROR", e);
            return false;
        }

        return true;

    }

    /**
     * Creates a PDF document for a patient who attended their on-call clinic appointment.
     * The document informs the patient's primary care provider that the visit occurred.
     *
     * @param filename The name of the PDF file to be created.
     * @param appointment The appointment object containing details of the visit.
     * @param demographic The demographic object for the patient.
     * @param providerData The provider object for the on-call physician.
     * @return {@code true} if the document was created successfully, {@code false} otherwise.
     */
    private Boolean makeGoodApptDocument(String filename, Appointment appointment, Demographic demographic, ProviderData providerData) {
        Document document = new Document();

        try {
            PdfWriter.getInstance(document, new FileOutputStream(DOCUMENTDIR + filename));
            Rectangle pageSize = new Rectangle(PageSize.A5.getWidth(), PageSize.A5.getHeight());
            pageSize.setBackgroundColor(new BaseColor(0xCC, 0xCC, 0xFF));
            document.setPageSize(pageSize);
            document.setMargins(36, 72, 108, 180);
            document.setMarginMirroringTopBottom(true);
            document.open();
            Font headerFont = new Font(FontFamily.HELVETICA, 14);
            Chunk chunkHeader = new Chunk("OSCAR ON CALL CLINIC", headerFont);
            chunkHeader.setUnderline(2f, -2f);
            Paragraph header = new Paragraph(chunkHeader);
            header.setAlignment(Element.ALIGN_CENTER);
            header.setExtraParagraphSpace(24f);
            document.add(header);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            String reason = appointment.getReason() == null || "".equals(appointment.getReason()) ? "" : " for \"" + appointment.getReason() + "\"";
            Font bodyFont = new Font(FontFamily.TIMES_ROMAN, 12);
            Chunk chunkAttn = new Chunk("ATTN: " + demographic.getProvider().getFormattedName(), bodyFont);
            Paragraph attnParagraph = new Paragraph(chunkAttn);
            attnParagraph.setAlignment(Element.ALIGN_LEFT);
            attnParagraph.setExtraParagraphSpace(24f);
            document.add(attnParagraph);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            Font patientFont = new Font(FontFamily.HELVETICA, 12, Font.ITALIC, BaseColor.BLUE);
            Chunk patientChunk = new Chunk(demographic.getFormattedName(), patientFont);
            Paragraph body = new Paragraph();
            Chunk body1 = new Chunk("Your patient ", bodyFont);
            Chunk body2 = new Chunk(" was seen on " + simpleDateFormat.format(appointment.getAppointmentDate()) + " by " +
                    providerData.getFirstName() + " " + providerData.getLastName() + reason, bodyFont);

            body.add(body1);
            body.add(patientChunk);
            body.add(body2);
            body.setAlignment(Element.ALIGN_LEFT);
            document.add(body);
            document.close();


        } catch (FileNotFoundException e) {
            MiscUtils.getLogger().error("ERROR", e);
            return false;

        } catch (DocumentException e) {
            MiscUtils.getLogger().error("ERROR", e);
            return false;
        }

        return true;
    }

    /**
     * Saves the generated PDF as an eDoc in the patient's chart and routes it to the
     * primary care provider's inbox.
     *
     * @param fileName The filename of the PDF document to be sent.
     * @param demographic The demographic object of the patient to whom the document belongs.
     */
    private void SendDocument(String fileName, Demographic demographic) {
        String user = "System";
        String mrp = demographic.getProviderNo();
        EDoc newDoc = new EDoc("", "", fileName, "", user, user, "", 'A',
                UtilDateUtilities.getToday("yyyy-MM-dd"), "", "", "demographic", demographic.getDemographicNo().toString(), 0);
        newDoc.setDocPublic("0");

        // if the document was added in the context of a program
        ProgramManager2 programManager = SpringUtils.getBean(ProgramManager2.class);

        ProgramProvider pp = programManager.getCurrentProgramInDomain(null, provider.getProviderNo());
        if (pp != null && pp.getProgramId() != null) {
            newDoc.setProgramId(pp.getProgramId().intValue());
        }

        newDoc.setFileName(fileName);
        newDoc.setContentType("application/pdf");
        newDoc.setType("on-call clinic");
        newDoc.setDescription("On-Call Clinic");

        newDoc.setNumberOfPages(1);
        String doc_no = EDocUtil.addDocumentSQL(newDoc);

        ProviderInboxRoutingDao providerInboxRoutingDao = SpringUtils.getBean(ProviderInboxRoutingDao.class);
        providerInboxRoutingDao.addToProviderInbox(mrp, Integer.parseInt(doc_no), "DOC");


        PatientLabRoutingDao patientLabRoutingDao = SpringUtils.getBean(PatientLabRoutingDao.class);

        PatientLabRouting patientLabRouting = new PatientLabRouting();
        patientLabRouting.setDemographicNo(demographic.getDemographicNo());
        patientLabRouting.setLabNo(Integer.parseInt(doc_no));
        patientLabRouting.setLabType("DOC");
        patientLabRoutingDao.persist(patientLabRouting);


        MiscUtils.getLogger().info("Sent Document");
    }


    /**
     * Sets the provider context for this job. This is typically the provider
     * under whose authority the job is executed (e.g., a system or admin provider).
     *
     * @param provider The provider object representing the user running the job.
     */
    @Override
    public void setLoggedInProvider(Provider provider) {
        this.provider = provider;

    }

    /**
     * Sets the security context for this job. This method is required by the
     * OscarRunnable interface but is not utilized in this implementation, as the
     * necessary security context is derived from the logged-in provider.
     *
     * @param security The security object, which is not used in this job.
     */
    @Override
    public void setLoggedInSecurity(Security security) {
        // TODO Auto-generated method stub
        // This method is intentionally left empty. The security context for this job
        // is managed via the Provider object, and no separate security object is needed.
    }

    /**
     * A configuration method required by the OscarRunnable interface. This method
     * is not used in this implementation as the job has no external configuration parameters.
     *
     * @param string Configuration string (not used).
     */
    @Override
    public void setConfig(String string) {
    }

}
