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

package ca.openosp.openo.entities;

/**
 * Insurance claim information entity.
 *
 * This entity represents insurance claim data for patients, including coverage details,
 * claim numbers, deductibles, and benefits information. It supports various types of
 * insurance including private insurance, Workers' Compensation Board (WCB), and Motor
 * Vehicle Accident (MVA) claims.
 *
 * Key insurance information includes:
 * - Patient record linkage and claim identification
 * - Insurance provider and coverage details
 * - Deductibles, maximums, and percentage coverage
 * - Claim status and payment tracking
 * - Provider and referral information
 * - Injury-related claim details for WCB/MVA cases
 *
 * This entity supports:
 * - Insurance eligibility verification
 * - Claims processing and billing
 * - Benefits calculation and coverage determination
 * - Multi-payer insurance scenarios
 * - Workers' compensation and motor vehicle claims
 *
 * @since November 1, 2004
 */
public class Insclaim {
    private String insclaimno;
    private String insclaimnm;
    private String ptrecid;
    private String insurer;
    private String rank;
    private String rcploc;
    private int pppayee;
    private String idnum;
    private String claimnum;
    private String code;
    private int indefinite;
    private String resetdate;
    private String termStart;
    private String termEnd;
    private double percent;
    private double deductI;
    private double vchargeI;
    private double deductT;
    private double maxDI;
    private double maxDTerm;
    private int maxITerm;
    private int prevInv;
    private int invCount;
    private double dolCount;
    private double dedCount;
    private String mdno;
    private String refmdno;
    private String backupins;
    private String injurydate;
    private String injuryarea;
    private String injurypos;
    private String injurynat;

    /**
     * Default constructor for insurance claim entity.
     * Initializes all fields to their default values.
     */
    public Insclaim() {
    }

    /**
     * Complete constructor for insurance claim entity.
     * Creates a fully initialized insurance claim with all coverage and benefits details.
     *
     * @param insclaimno String insurance claim number (unique identifier)
     * @param insclaimnm String insurance claim name or description
     * @param ptrecid    String patient record identifier
     * @param insurer    String insurance company or provider name
     * @param rank       String insurance rank or priority level
     * @param rcploc     String recipient location code
     * @param pppayee    int payee indicator for payments
     * @param idnum      String insurance identification number
     * @param claimnum   String claim number assigned by insurer
     * @param code       String insurance code or type
     * @param indefinite int indefinite coverage indicator (0=limited, 1=indefinite)
     * @param resetdate  String date when coverage resets
     * @param termStart  String coverage term start date
     * @param termEnd    String coverage term end date
     * @param percent    double coverage percentage (0.0-100.0)
     * @param deductI    double individual deductible amount
     * @param vchargeI   double individual variable charge amount
     * @param deductT    double total deductible amount
     * @param maxDI      double maximum deductible for individual
     * @param maxDTerm   double maximum deductible for term
     * @param maxITerm   int maximum individual term limit
     * @param prevInv    int previous invoice count
     * @param invCount   int current invoice count
     * @param dolCount   double dollar amount count
     * @param dedCount   double deductible count applied
     * @param mdno       String medical doctor number (primary physician)
     * @param refmdno    String referring medical doctor number
     * @param backupins  String backup insurance information
     * @param injurydate String injury date (for WCB/MVA claims)
     * @param injuryarea String injury area or body part affected
     * @param injurypos  String injury position or location
     * @param injurynat  String injury nature or type description
     */
    public Insclaim(String insclaimno, String insclaimnm, String ptrecid,
                    String insurer, String rank, String rcploc, int pppayee,
                    String idnum, String claimnum, String code, int indefinite,
                    String resetdate, String termStart,
                    String termEnd, double percent, double deductI,
                    double vchargeI, double deductT, double maxDI,
                    double maxDTerm,
                    int maxITerm, int prevInv, int invCount, double dolCount,
                    double dedCount, String mdno, String refmdno,
                    String backupins,
                    String injurydate, String injuryarea, String injurypos,
                    String injurynat) {
        this.insclaimno = insclaimno;
        this.insclaimnm = insclaimnm;
        this.ptrecid = ptrecid;
        this.insurer = insurer;
        this.rank = rank;
        this.rcploc = rcploc;
        this.pppayee = pppayee;
        this.idnum = idnum;
        this.claimnum = claimnum;
        this.code = code;
        this.indefinite = indefinite;
        this.resetdate = resetdate;
        this.termStart = termStart;
        this.termEnd = termEnd;
        this.percent = percent;
        this.deductI = deductI;
        this.vchargeI = vchargeI;
        this.deductT = deductT;
        this.maxDI = maxDI;
        this.maxDTerm = maxDTerm;
        this.maxITerm = maxITerm;
        this.prevInv = prevInv;
        this.invCount = invCount;
        this.dolCount = dolCount;
        this.dedCount = dedCount;
        this.mdno = mdno;
        this.refmdno = refmdno;
        this.backupins = backupins;
        this.injurydate = injurydate;
        this.injuryarea = injuryarea;
        this.injurypos = injurypos;
        this.injurynat = injurynat;
    }

    /**
     * Gets the insurance claim number.
     * This is the unique identifier for this insurance claim record.
     *
     * @return String the insurance claim number, empty string if null
     */
    public String getInsclaimno() {
        return (insclaimno != null ? insclaimno : "");
    }

    /**
     * Gets the insurance claim name or description.
     * Provides a readable name or description for this insurance claim.
     *
     * @return String the insurance claim name, empty string if null
     */
    public String getInsclaimnm() {
        return (insclaimnm != null ? insclaimnm : "");
    }

    /**
     * Gets the patient record identifier.
     * Links this insurance claim to the specific patient record.
     *
     * @return String the patient record identifier, empty string if null
     */
    public String getPtrecid() {
        return (ptrecid != null ? ptrecid : "");
    }

    /**
     * Gets the insurance company or provider name.
     * Identifies the insurance company providing coverage for this claim.
     *
     * @return String the insurer name, empty string if null
     */
    public String getInsurer() {
        return (insurer != null ? insurer : "");
    }

    /**
     * Gets the rank
     *
     * @return String rank
     */
    public String getRank() {
        return (rank != null ? rank : "");
    }

    /**
     * Gets the rcploc
     *
     * @return String rcploc
     */
    public String getRcploc() {
        return (rcploc != null ? rcploc : "");
    }

    /**
     * Gets the pppayee
     *
     * @return int pppayee
     */
    public int getPppayee() {
        return pppayee;
    }

    /**
     * Gets the idnum
     *
     * @return String idnum
     */
    public String getIdnum() {
        return (idnum != null ? idnum : "");
    }

    /**
     * Gets the claimnum
     *
     * @return String claimnum
     */
    public String getClaimnum() {
        return (claimnum != null ? claimnum : "");
    }

    /**
     * Gets the code
     *
     * @return String code
     */
    public String getCode() {
        return (code != null ? code : "");
    }

    /**
     * Gets the indefinite
     *
     * @return int indefinite
     */
    public int getIndefinite() {
        return indefinite;
    }

    /**
     * Gets the resetdate
     *
     * @return String resetdate
     */
    public String getResetdate() {
        return resetdate;
    }

    /**
     * Gets the termStart
     *
     * @return String termStart
     */
    public String getTermStart() {
        return termStart;
    }

    /**
     * Gets the termEnd
     *
     * @return String termEnd
     */
    public String getTermEnd() {
        return termEnd;
    }

    /**
     * Gets the coverage percentage.
     * The percentage of costs covered by this insurance (0.0-100.0).
     *
     * @return double the coverage percentage
     */
    public double getPercent() {
        return percent;
    }

    /**
     * Gets the individual deductible amount.
     * The deductible amount that must be met by the individual before coverage applies.
     *
     * @return double the individual deductible amount
     */
    public double getDeductI() {
        return deductI;
    }

    /**
     * Gets the vchargeI
     *
     * @return double vchargeI
     */
    public double getVchargeI() {
        return vchargeI;
    }

    /**
     * Gets the deductT
     *
     * @return double deductT
     */
    public double getDeductT() {
        return deductT;
    }

    /**
     * Gets the maxDI
     *
     * @return double maxDI
     */
    public double getMaxDI() {
        return maxDI;
    }

    /**
     * Gets the maxDTerm
     *
     * @return double maxDTerm
     */
    public double getMaxDTerm() {
        return maxDTerm;
    }

    /**
     * Gets the maxITerm
     *
     * @return int maxITerm
     */
    public int getMaxITerm() {
        return maxITerm;
    }

    /**
     * Gets the prevInv
     *
     * @return int prevInv
     */
    public int getPrevInv() {
        return prevInv;
    }

    /**
     * Gets the invCount
     *
     * @return int invCount
     */
    public int getInvCount() {
        return invCount;
    }

    /**
     * Gets the dolCount
     *
     * @return double dolCount
     */
    public double getDolCount() {
        return dolCount;
    }

    /**
     * Gets the dedCount
     *
     * @return double dedCount
     */
    public double getDedCount() {
        return dedCount;
    }

    /**
     * Gets the mdno
     *
     * @return String mdno
     */
    public String getMdno() {
        return (mdno != null ? mdno : "");
    }

    /**
     * Gets the refmdno
     *
     * @return String refmdno
     */
    public String getRefmdno() {
        return (refmdno != null ? refmdno : "");
    }

    /**
     * Gets the backupins
     *
     * @return String backupins
     */
    public String getBackupins() {
        return (backupins != null ? backupins : "");
    }

    /**
     * Gets the injury date (for WCB/MVA claims).
     * The date when the injury occurred that is covered by this insurance claim.
     *
     * @return String the injury date, may be null
     */
    public String getInjurydate() {
        return injurydate;
    }

    /**
     * Gets the injuryarea
     *
     * @return String injuryarea
     */
    public String getInjuryarea() {
        return (injuryarea != null ? injuryarea : "");
    }

    /**
     * Gets the injurypos
     *
     * @return String injurypos
     */
    public String getInjurypos() {
        return (injurypos != null ? injurypos : "");
    }

    /**
     * Gets the injurynat
     *
     * @return String injurynat
     */
    public String getInjurynat() {
        return (injurynat != null ? injurynat : "");
    }

    /**
     * Sets the insclaimno
     *
     * @param insclaimno String
     */
    public void setInsclaimno(String insclaimno) {
        this.insclaimno = insclaimno;
    }

    /**
     * Sets the insclaimnm
     *
     * @param insclaimnm String
     */
    public void setInsclaimnm(String insclaimnm) {
        this.insclaimnm = insclaimnm;
    }

    /**
     * Sets the ptrecid
     *
     * @param ptrecid String
     */
    public void setPtrecid(String ptrecid) {
        this.ptrecid = ptrecid;
    }

    /**
     * Sets the insurer
     *
     * @param insurer String
     */
    public void setInsurer(String insurer) {
        this.insurer = insurer;
    }

    /**
     * Sets the rank
     *
     * @param rank String
     */
    public void setRank(String rank) {
        this.rank = rank;
    }

    /**
     * Sets the rcploc
     *
     * @param rcploc String
     */
    public void setRcploc(String rcploc) {
        this.rcploc = rcploc;
    }

    /**
     * Sets the pppayee
     *
     * @param pppayee int
     */
    public void setPppayee(int pppayee) {
        this.pppayee = pppayee;
    }

    /**
     * Sets the idnum
     *
     * @param idnum String
     */
    public void setIdnum(String idnum) {
        this.idnum = idnum;
    }

    /**
     * Sets the claimnum
     *
     * @param claimnum String
     */
    public void setClaimnum(String claimnum) {
        this.claimnum = claimnum;
    }

    /**
     * Sets the code
     *
     * @param code String
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * Sets the indefinite
     *
     * @param indefinite int
     */
    public void setIndefinite(int indefinite) {
        this.indefinite = indefinite;
    }

    /**
     * Sets the resetdate
     *
     * @param resetdate String
     */
    public void setResetdate(String resetdate) {
        this.resetdate = resetdate;
    }

    /**
     * Sets the termStart
     *
     * @param termStart String
     */
    public void setTermStart(String termStart) {
        this.termStart = termStart;
    }

    /**
     * Sets the termEnd
     *
     * @param termEnd String
     */
    public void setTermEnd(String termEnd) {
        this.termEnd = termEnd;
    }

    /**
     * Sets the percent
     *
     * @param percent double
     */
    public void setPercent(double percent) {
        this.percent = percent;
    }

    /**
     * Sets the deductI
     *
     * @param deductI double
     */
    public void setDeductI(double deductI) {
        this.deductI = deductI;
    }

    /**
     * Sets the vchargeI
     *
     * @param vchargeI double
     */
    public void setVchargeI(double vchargeI) {
        this.vchargeI = vchargeI;
    }

    /**
     * Sets the deductT
     *
     * @param deductT double
     */
    public void setDeductT(double deductT) {
        this.deductT = deductT;
    }

    /**
     * Sets the maxDI
     *
     * @param maxDI double
     */
    public void setMaxDI(double maxDI) {
        this.maxDI = maxDI;
    }

    /**
     * Sets the maxDTerm
     *
     * @param maxDTerm double
     */
    public void setMaxDTerm(double maxDTerm) {
        this.maxDTerm = maxDTerm;
    }

    /**
     * Sets the maxITerm
     *
     * @param maxITerm int
     */
    public void setMaxITerm(int maxITerm) {
        this.maxITerm = maxITerm;
    }

    /**
     * Sets the prevInv
     *
     * @param prevInv int
     */
    public void setPrevInv(int prevInv) {
        this.prevInv = prevInv;
    }

    /**
     * Sets the invCount
     *
     * @param invCount int
     */
    public void setInvCount(int invCount) {
        this.invCount = invCount;
    }

    /**
     * Sets the dolCount
     *
     * @param dolCount double
     */
    public void setDolCount(double dolCount) {
        this.dolCount = dolCount;
    }

    /**
     * Sets the dedCount
     *
     * @param dedCount double
     */
    public void setDedCount(double dedCount) {
        this.dedCount = dedCount;
    }

    /**
     * Sets the mdno
     *
     * @param mdno String
     */
    public void setMdno(String mdno) {
        this.mdno = mdno;
    }

    /**
     * Sets the refmdno
     *
     * @param refmdno String
     */
    public void setRefmdno(String refmdno) {
        this.refmdno = refmdno;
    }

    /**
     * Sets the backupins
     *
     * @param backupins String
     */
    public void setBackupins(String backupins) {
        this.backupins = backupins;
    }

    /**
     * Sets the injurydate
     *
     * @param injurydate String
     */
    public void setInjurydate(String injurydate) {
        this.injurydate = injurydate;
    }

    /**
     * Sets the injuryarea
     *
     * @param injuryarea String
     */
    public void setInjuryarea(String injuryarea) {
        this.injuryarea = injuryarea;
    }

    /**
     * Sets the injurypos
     *
     * @param injurypos String
     */
    public void setInjurypos(String injurypos) {
        this.injurypos = injurypos;
    }

    /**
     * Sets the injurynat
     *
     * @param injurynat String
     */
    public void setInjurynat(String injurynat) {
        this.injurynat = injurynat;
    }
}
