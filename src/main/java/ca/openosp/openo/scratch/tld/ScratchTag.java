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


package ca.openosp.openo.scratch.tld;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import ca.openosp.openo.utility.MiscUtils;

/**
 * JSP custom tag for displaying clinical scratch pad status indicators in the OpenO EMR system.
 *
 * <p>This custom tag provides visual feedback to healthcare providers about the status of their
 * scratch pad content. It displays different notepad icons based on whether the provider has
 * active scratch pad content, helping with clinical workflow by providing immediate visual
 * cues about temporary note availability.</p>
 *
 * <p>The scratch pad system supports clinical workflow by:</p>
 * <ul>
 *   <li>Providing visual indicators for providers about their temporary note status</li>
 *   <li>Helping providers quickly identify when they have unsaved clinical observations</li>
 *   <li>Supporting multi-provider environments with provider-specific visual cues</li>
 *   <li>Integrating seamlessly with JSP pages in the clinical interface</li>
 * </ul>
 *
 * <p>Usage in JSP pages:</p>
 * <pre>
 * &lt;%@ taglib uri="/WEB-INF/scratch.tld" prefix="scratch" %&gt;
 * &lt;scratch:scratchTag providerNo="${providerNo}" /&gt;
 * </pre>
 *
 * <p>The tag outputs different images based on scratch pad status:</p>
 * <ul>
 *   <li>notepad.gif - Provider has active scratch pad content</li>
 *   <li>notepad_blank.gif - No active scratch pad content</li>
 * </ul>
 *
 * @see Scratch2Action
 * @see ScratchData
 * @see ca.openosp.openo.commn.model.ScratchPad
 * @since July 2003
 */
public class ScratchTag extends TagSupport {

    public ScratchTag() {
        scratchFilled = false;
    }

    public void setProviderNo(String providerNo1) {
        providerNo = providerNo1;
    }

    public String getProviderNo() {
        return providerNo;
    }

    public int doStartTag() throws JspException {

        if (providerNo != null) {
            //isScratchFilled is inefficient, removed until a more efficient method is available. by default show the filled graphic
            scratchFilled = true;//spm.isScratchFilled(providerNo);
        }

        try {
            JspWriter out = super.pageContext.getOut();
            if (scratchFilled)
                out.print("../images/notepad.gif");
            else
                out.print("../images/notepad_blank.gif");
        } catch (Exception p) {
            MiscUtils.getLogger().error("Error", p);
        }
        return (EVAL_BODY_INCLUDE);
    }

    public int doEndTag() throws JspException {
        return EVAL_PAGE;
    }

    /**
     * The unique identifier for the healthcare provider whose scratch pad status is being checked.
     * This is set via the providerNo attribute in the JSP tag.
     */
    private String providerNo;

    /**
     * Flag indicating whether the provider has active scratch pad content.
     * Currently defaults to true for performance reasons in clinical environments.
     */
    private boolean scratchFilled;
}
