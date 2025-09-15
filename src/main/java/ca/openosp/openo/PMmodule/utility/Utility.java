//CHECKSTYLE:OFF
/**
 * Copyright (c) 2005-2012. Centre for Research on Inner City Health, St. Michael's Hospital, Toronto. All Rights Reserved.
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
 * This software was written for
 * Centre for Research on Inner City Health, St. Michael's Hospital,
 * Toronto, Ontario, Canada
 */

package ca.openosp.openo.PMmodule.utility;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.ListIterator;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.logging.log4j.Logger;
import ca.openosp.openo.utility.MiscUtils;

/**
 * Comprehensive utility class providing common string, HTML, SQL, and data processing operations.
 * <p>
 * This class serves as a central repository for utility methods used throughout the PMmodule.
 * It provides functionality for data conversion, string manipulation, HTML/SQL safety,
 * currency formatting, parameter extraction, and age calculations. Many methods were designed
 * to handle legacy data structures and web form processing.
 * </p>
 * <p>
 * Key Feature Areas:
 * <ul>
 *   <li>Data Structure Conversion - ArrayList to arrays, duplicate filtering</li>
 *   <li>String Safety - HTML escaping, SQL injection prevention, URL encoding</li>
 *   <li>Web Form Processing - Parameter extraction from HttpServletRequest</li>
 *   <li>Currency Formatting - Decimal precision handling for financial calculations</li>
 *   <li>Date/Age Calculations - Age computation with localized text output</li>
 *   <li>Null Safety - Defensive programming utilities for null handling</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Security Note:</strong> This class includes basic SQL injection prevention
 * methods, but modern applications should use parameterized queries instead of
 * relying on string escaping methods like escapeSQL() and filterSQLFromHacking().
 * </p>
 * <p>
 * <strong>HTML Safety:</strong> The escapeHTML() method provides basic XSS protection
 * but may not cover all attack vectors. Consider using OWASP Encoder or similar
 * libraries for comprehensive output encoding.
 * </p>
 * <p>
 * <strong>Performance Consideration:</strong> Many methods create new objects and
 * perform string operations that could be optimized. Consider StringBuilder usage
 * and caching for high-frequency operations.
 * </p>
 *
 * @since 2006-12-16
 * @see ca.openosp.openo.PMmodule.utility.DateUtils
 * @see ca.openosp.openo.PMmodule.utility.UtilDateUtilities
 */
public class Utility {
    /** Logger for error reporting and debugging utility operations */
    private static Logger log = MiscUtils.getLogger();

    // ################################################################################
    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private Utility() {
    }

    /*
     * ##########################################################################
     * ##
     */
    /**
     * Converts a nested ArrayList structure to a 2D String array.
     * <p>
     * This method transforms a List of Lists into a two-dimensional String array,
     * commonly used for converting database result sets or form data into arrays
     * suitable for JSP display or further processing.
     * </p>
     * <p>
     * The method assumes all inner lists have the same length as the first list.
     * Objects are converted to strings using toString(), with null objects
     * becoming "_" placeholders for easier debugging.
     * </p>
     * <p>
     * Note: The method silently catches all exceptions and may return partially
     * filled arrays on error, which could lead to unexpected behavior.
     * </p>
     *
     * @param arr_arrayList List<List> nested list structure to convert
     * @return String[][] 2D array representation, or null if input is null/empty
     */
    @SuppressWarnings("unchecked")
    public static String[][] convertArrayListTo2DStringArr(List arr_arrayList) {
        if (arr_arrayList == null || arr_arrayList.size() <= 0) {
            return null;
        }

        String[][] returnStrArr = null;

        try {
            // Process each row in the outer list
            for (int i = 0; i < arr_arrayList.size(); i++) {
                List arr_arrayListCol = (List) arr_arrayList.get(i);
                Object[] obj = arr_arrayListCol.toArray();

                // Initialize return array based on first row dimensions
                if (i == 0) {
                    returnStrArr = new String[arr_arrayList.size()][obj.length];
                }

                // Convert each column value to string
                for (int j = 0; j < obj.length; j++) {
                    if (obj[j] != null) {
                        returnStrArr[i][j] = obj[j].toString();
                        // Use "_" as placeholder for debugging null values after conversion
                        if (returnStrArr[i][j] == null) {
                            returnStrArr[i][j] = "_";
                        }
                    }
                }
            }
        } catch (Exception ex) {
            // Silent exception handling - may result in partially filled array
        }
        return returnStrArr;
    }

    // ###################################################################################
    /**
     * Replaces all occurrences of a substring in HTML content.
     * <p>
     * This method performs string replacement operations specifically designed
     * for HTML content. It uses a loop-based approach to replace all instances
     * of the target substring with the replacement string.
     * </p>
     * <p>
     * Note: Despite the name, this method doesn't perform HTML-specific
     * processing - it's a general string replacement utility. The name suggests
     * it was originally intended for HTML content processing.
     * </p>
     *
     * @param str String the source string to process
     * @param what String the substring to find and replace
     * @param withWhat String the replacement string
     * @return String the processed string with all replacements made, empty string if input is null
     */
    public static String replaceHTML(String str, String what, String withWhat) {
        if (str == null || str.length() <= 0) {
            return "";
        }

        String result = "";
        // Replace all occurrences by repeatedly finding and replacing
        while (str.indexOf(what) != -1) {
            int v1 = str.indexOf(what);
            result += str.substring(0, v1) + withWhat;
            str = str.substring(v1 + what.length());
        }
        return result + str;
    }

    // ###################################################################################
    /**
     * General string replacement method.
     * <p>
     * Replaces all occurrences of a target substring with a replacement string.
     * This is identical in functionality to replaceHTML() but with a more
     * general name, suggesting it's used for non-HTML string processing.
     * </p>
     *
     * @param str String the source string to process
     * @param what String the substring to find and replace
     * @param withWhat String the replacement string
     * @return String the processed string with all replacements made, empty string if input is null
     */
    public static String replaceStrWith(String str, String what, String withWhat) {
        if (str == null || str.length() <= 0) {
            return "";
        }

        String result = "";
        while (str.indexOf(what) != -1) {
            int v1 = str.indexOf(what);
            result += str.substring(0, v1) + withWhat;
            str = str.substring(v1 + what.length());
        }
        return result + str;
    }

    // ###################################################################################
    /**
     * Returns a default string if the input is null or empty.
     * <p>
     * This method provides null-safe string handling by substituting
     * a default value for null or empty strings. Commonly used to
     * ensure non-null values in form processing and data display.
     * </p>
     *
     * @param str String the string to check
     * @param replaceWith String the default value to use if str is null/empty
     * @return String the original string if non-empty, otherwise the replacement
     */
    public static String convertToReplaceStrIfEmptyStr(String str, String replaceWith) {
        if (str == null || str.equals("")) {
            return replaceWith;
        }
        return str;
    }

    // ###################################################################################

    /**
     * Escapes HTML special characters to prevent XSS attacks.
     * <p>
     * Converts potentially dangerous HTML characters to their entity equivalents:
     * <ul>
     *   <li>& becomes &amp;amp;</li>
     *   <li>&lt; becomes &amp;lt;</li>
     *   <li>&gt; becomes &amp;gt;</li>
     *   <li>" becomes &amp;quot;</li>
     *   <li>' becomes &amp;#39</li>
     * </ul>
     * </p>
     * <p>
     * WARNING: This is basic HTML escaping and may not protect against all
     * XSS attack vectors. For comprehensive protection, use OWASP Encoder
     * or similar security libraries.
     * </p>
     *
     * @param str String the string to escape
     * @return String the HTML-escaped string, empty if input is null
     */
    public static String escapeHTML(String str) {
        if (str == null)
            return "";

        // Order matters: & must be escaped first to avoid double-escaping
        str = replaceHTML(str, "&", "&amp;");
        str = replaceHTML(str, "<", "&lt;");
        str = replaceHTML(str, ">", "&gt;");
        str = replaceHTML(str, "\"", "&quot;");
        str = replaceHTML(str, "'", "&#39");
        return str;
    }

    // ###################################################################################

    /**
     * Escapes single quotes in strings for basic SQL injection prevention.
     * <p>
     * Replaces single quotes with doubled single quotes, which is the SQL
     * standard way to escape quotes within string literals. This provides
     * basic protection against SQL injection attacks.
     * </p>
     * <p>
     * WARNING: This method provides only basic protection. Modern applications
     * should use parameterized queries (PreparedStatement) instead of string
     * escaping for reliable SQL injection prevention.
     * </p>
     *
     * @param str String the string to escape
     * @return String the SQL-escaped string, empty if input is null
     */
    public static String escapeSQL(String str) {
        if (str == null)
            return "";

        // Escape single quotes by doubling them
        str = replaceStrWith(str, "'", "''");
        return str;
    }

    // ###################################################################################

    /**
     * Attempts to filter dangerous SQL keywords from strings.
     * <p>
     * This method tries to prevent SQL injection by replacing common
     * dangerous SQL keywords (delete, update, drop) and semicolons with spaces.
     * However, this approach is fundamentally flawed and easily bypassed.
     * </p>
     * <p>
     * CRITICAL WARNING: This method provides NO real security against SQL
     * injection attacks. Attackers can easily bypass these filters using:
     * <ul>
     *   <li>Case variations (DELETE, Delete, dElEtE)</li>
     *   <li>Unicode encoding</li>
     *   <li>Other dangerous keywords not filtered (INSERT, SELECT)</li>
     *   <li>Comment injection</li>
     * </ul>
     * </p>
     * <p>
     * DO NOT rely on this method for security. Use parameterized queries instead.
     * </p>
     *
     * @param str String the string to filter
     * @return String the filtered string with keywords replaced by spaces
     * @deprecated This method provides false security. Use parameterized queries.
     */
    @Deprecated
    public static String filterSQLFromHacking(String str) {
        if (str == null)
            return "";

        // These replacements provide false security and can be easily bypassed
        str = replaceStrWith(str, "delete", " ");
        str = replaceStrWith(str, "update", " ");
        str = replaceStrWith(str, "drop", " ");
        str = replaceStrWith(str, ";", " ");

        return str;
    }

    // ###################################################################################
    public static String replaceURL(String str, String what, String withWhat) {
        if (str == null || str.length() <= 0) {
            return null;
        }

        String result = "";
        while (str.indexOf(what) != -1) {
            int v1 = str.indexOf(what);
            result += str.substring(0, v1) + withWhat;
            str = str.substring(v1 + what.length());
        }
        return result + str;
    }

    // ###################################################################################

    public static String escapeURL(String str) {
        if (str == null)
            return "";

        str = replaceURL(str, "?", "!");
        str = replaceURL(str, "&", "and");
        str = replaceURL(str, "=", "-");
        str = replaceURL(str, "\"", "_");

        str = replaceURL(str, "\\", "/"); // for mainpic updates in experiences
        // table
        return str;
    }

    // ###################################################################################
    public static String convertToEmptyStrIfNull(String str) {
        if (str != null) {
            return str;
        }

        str = "";
        return str;
    }

    // ###################################################################################
    /**
     * Safely converts an Object to String, handling null values.
     * <p>
     * Returns the string representation of the object, or an empty string
     * if the object is null. This prevents NullPointerExceptions when
     * displaying object values in UI components.
     * </p>
     *
     * @param obj Object the object to convert (can be null)
     * @return String the string representation, or empty string if null
     */
    public static String escapeNull(Object obj) {
        if (obj != null) {
            return obj.toString();
        }
        return "";
    }

    // ###################################################################################
    /**
     * Safely handles null strings by converting them to empty strings.
     * <p>
     * This overloaded version specifically handles String objects,
     * providing a null-safe way to ensure non-null string values.
     * </p>
     *
     * @param str String the string to check (can be null)
     * @return String the original string if non-null, otherwise empty string
     */
    public static String escapeNull(String str) {
        if (str != null) {
            return str;
        }
        return "";
    }

    // ###################################################################################
    /**
     * Provides a default date for null Date objects.
     * <p>
     * Returns the original date if non-null, otherwise returns a default
     * date set to January 1, year 1. This prevents NullPointerExceptions
     * in date processing while providing a clearly identifiable default.
     * </p>
     * <p>
     * Note: The year 1 default may cause issues with date calculations
     * or display. Consider using a more recent default date like
     * January 1, 1900 for better compatibility.
     * </p>
     *
     * @param dateObj Date the date to check (can be null)
     * @return Date the original date if non-null, otherwise January 1, year 1
     */
    public static Date escapeNull(Date dateObj) {
        if (dateObj != null) {
            return dateObj;
        }

        Calendar cal = Calendar.getInstance();
        // Set to year 1 - this may cause compatibility issues
        cal.set(Calendar.YEAR, 0001);
        cal.set(Calendar.MONTH, 0); // January (0-based)
        cal.set(Calendar.DAY_OF_MONTH, 1);
        Date defaultDate = cal.getTime();
        return defaultDate;
    }

    // ###################################################################################
    public static String convertToRelacementStrIfNull(String str,
                                                      String replaceWith) {
        if (str != null) {
            return str;
        }

        return replaceWith;
    }

    // ################################################################################

    public static String[] separateStrComponentsIntoStrArray(String str,
                                                             String delimiter) {

        // strTokens[10] =
        // ~MINP4~MAXA4~MAXP6~MAXC2~INFY~ROOMTYPEPHS~OCCUPCODEQAD
        String strComponents = str;

        int count = 0;
        StringTokenizer strTokenizer = new StringTokenizer(strComponents,
                delimiter);
        int tokensCount = strTokenizer.countTokens();
        String[] strTokens = new String[tokensCount];

        while (strTokenizer.hasMoreTokens()) {
            strTokens[count] = strTokenizer.nextToken();
            count++;
        }

        // %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        return strTokens;
    }

    // ############################################################################

    public static String[] getParamsFromPosting(HttpServletRequest request,
                                                String keyPrefix) {
        // this method only filters out the keyNames that has a nonempty
        // keyValue -- others don't work

        if (keyPrefix == null || keyPrefix.length() <= 0 || request == null) {
            return null;
        }

        List arr_keyValues = new ArrayList();
        String keyName, keyValue;
        boolean nothingChecked = true;
        int numChecked = 0;
        String[] paramList = null;
        try {
            Enumeration params = request.getParameterNames(); // receives all
            // the posted
            // params

            while (params.hasMoreElements()) {
                keyName = (String) params.nextElement();

                if (keyName.startsWith(keyPrefix)) {
                    keyValue = request.getParameter(keyName); // 0 or 1

                    if (!keyValue.equals("")) // ie. checked checkboxes
                    {
                        arr_keyValues.add(keyValue); //
                        nothingChecked = false;
                    } else // ie. unchecked checkboxes
                    {
                        // Do nothing for the unchecked msg rows
                    }
                }
            }

            if (nothingChecked == true) {
                // Display an error message saying that at least 1 checkbox
                // should be checked!
                return null;
            }
            paramList = new String[arr_keyValues.size()];

            ListIterator listIterator = arr_keyValues.listIterator();

            while (listIterator.hasNext()) {
                paramList[numChecked] = (String) listIterator.next();

                numChecked++;
            }

            return paramList;
        } catch (Exception e) {
            return null;
        }
    }

    // ############################################################################

    public static String[][] getKeyNamesAndKeyValuesFromPosting(
            HttpServletRequest request, String keyPrefix) {
        // this method only filters out the keyNames that has a nonempty
        // keyValue -- others don't work

        if (keyPrefix == null || keyPrefix.length() <= 0 || request == null) {
            return null;
        }

        List arr_keyNames = new ArrayList();
        List arr_keyValues = new ArrayList();
        String keyName, keyValue;
        boolean nothingChecked = true;
        int numChecked = 0;
        String[][] paramList = null;
        try {
            Enumeration params = request.getParameterNames(); // receives all
            // the posted
            // params

            while (params.hasMoreElements()) {
                keyName = (String) params.nextElement();

                if (keyName.startsWith(keyPrefix)) {
                    keyValue = request.getParameter(keyName); // 0 or 1

                    if (!keyValue.equals("")) // ie. checked checkboxes
                    {
                        arr_keyNames.add(keyName);
                        arr_keyValues.add(keyValue); //
                        nothingChecked = false;
                    } else // ie. unchecked checkboxes
                    {
                        // Do nothing for the unchecked msg rows
                    }
                }
            }

            if (nothingChecked == true) {
                // Display an error message saying that at least 1 checkbox
                // should be checked!
                return null;
            }
            paramList = new String[arr_keyNames.size()][2];

            ListIterator listIterator1 = arr_keyNames.listIterator();
            ListIterator listIterator2 = arr_keyValues.listIterator();

            while (listIterator1.hasNext()) {
                paramList[numChecked][0] = (String) listIterator1.next();
                paramList[numChecked][1] = (String) listIterator2.next();
                numChecked++;
            }

            return paramList;
        } catch (Exception e) {
            return null;
        }
    }

    // ############################################################################

    public static String[] getKeyNameSuffixFromPosting(
            HttpServletRequest request, String keyPrefix) {
        // this method only filters out the keyNames that has a nonempty
        // keyValue -- others don't work

        if (keyPrefix == null || keyPrefix.length() <= 0 || request == null) {
            return null;
        }

        List arr_keyNameSuffix = new ArrayList();
        String keyName = "";
        String keyValue = "";
        String keyNameSuffix = "";
        boolean nothingChecked = true;
        int numChecked = 0;
        String[] paramList = null;
        try {
            Enumeration params = request.getParameterNames(); // receives all
            // the posted
            // params

            while (params.hasMoreElements()) {
                keyNameSuffix = "";
                keyName = (String) params.nextElement();

                if (keyName.startsWith(keyPrefix)) {
                    if (keyName.length() > keyPrefix.length()) {
                        keyNameSuffix = keyName.substring(keyPrefix.length(),
                                keyName.length());
                        keyValue = request.getParameter(keyName);
                    }
                    if (!keyValue.equals("") && keyNameSuffix.length() > 0) // ie.
                    // checked
                    // checkboxes
                    {
                        arr_keyNameSuffix.add(keyNameSuffix);
                        nothingChecked = false;
                    } else // ie. unchecked checkboxes
                    {
                        // Do nothing for the unchecked msg rows
                    }
                }
            }

            if (nothingChecked == true) {
                // Display an error message saying that at least 1 checkbox
                // should be checked!
                return null;
            }
            paramList = new String[arr_keyNameSuffix.size()];

            ListIterator listIterator = arr_keyNameSuffix.listIterator();

            while (listIterator.hasNext()) {
                paramList[numChecked] = (String) listIterator.next();
                numChecked++;
            }

            return paramList;
        } catch (Exception e) {
            return null;
        }
    }

    // ################################################################################
    public static List filterOutDuplicateStrTokens(List duplStrList) {

        if (duplStrList == null || duplStrList.size() <= 0) {
            return null;
        }

        String compareToken = "";

        List filteredStrList = new ArrayList();

        for (int i = 0; i < duplStrList.size(); i++) {
            compareToken = (String) duplStrList.get(i);

            for (int j = i + 1; j < duplStrList.size(); j++) {
                if (compareToken.length() > 0
                        && compareToken.equalsIgnoreCase((String) duplStrList
                        .get(j))) {
                    duplStrList.set(j, "");
                }
            }
        }// end of for(int i=0; i < duplStrList.size(); i++)

        for (int i = 0; duplStrList != null && i < duplStrList.size(); i++) {
            if (!(duplStrList.get(i).toString()).equals("")) {
                filteredStrList.add(duplStrList.get(i).toString());

            }
        }

        if (filteredStrList == null) {
            log
                    .debug("Utility/filterOutDuplicateStrTokens():  filteredStrList == null");

        } else {
            log
                    .debug("Utility/filterOutDuplicateStrTokens():  filteredStrList.size() = "
                            + filteredStrList.size());
        }

        return filteredStrList;
    }

    // ################################################################################

    // ###################################################################################
    /**
     * Formats a double value as a currency string with two decimal places.
     * <p>
     * Rounds the input value to the nearest cent and formats it with exactly
     * two decimal places. This ensures consistent currency display formatting
     * throughout the application.
     * </p>
     * <p>
     * Examples:
     * <ul>
     *   <li>123.1 becomes "123.10"</li>
     *   <li>123 becomes "123.00"</li>
     *   <li>123.456 becomes "123.46"</li>
     * </ul>
     * </p>
     *
     * @param money double the monetary amount to format
     * @return String the formatted currency string with two decimal places
     */
    public static String toCurrency(double money) {
        // Round to nearest cent
        double rtn = (Math.round(money * 100)) / 100.00;
        String rtnStr = "" + rtn;

        // Calculate decimal places present
        int pos = rtnStr.length() - rtnStr.indexOf(".");

        // Ensure exactly two decimal places
        if (pos == 3) {
            // Already has two decimal places
        } else if (pos == 2) {
            rtnStr += "0"; // Add one zero
        } else if (pos == 1) {
            rtnStr += "00"; // Add two zeros
        } else {
            rtnStr += ".00"; // Add decimal point and two zeros
        }

        return rtnStr;
    }

    // ###################################################################################
    public static String toCurrency(String money) {
        // 649; 649.; 649.0; 649.23; 649.9000000000001

        if (money == null || money.equals("")) {
            return "0.00"; // ???<-- may be should leave it "" or null
        }

        String rtnStr = money;
        int index = 0;

        index = rtnStr.indexOf(".");

        int pos = rtnStr.length() - index;

        if (pos == 3)
            ; // in xxx.xx format already
        else if (pos == 2)
            rtnStr += "0";
        else if (pos == 1)
            rtnStr += "00";
        else if (pos <= 0) {
            rtnStr += ".00";
        } else if (pos > 3) {
            rtnStr = rtnStr.substring(0, index + 3);
        }

        return rtnStr;
    }

    // ###################################################################################
    // ###################################################################################
    public static String to3DecimalDigits(String decimal) {
        // 649; 649.; 649.0; 649.23; 649.9000000000001
        double decimalDouble = 0.00;
        String rtnStr = decimal;

        try {
            decimalDouble = Double.parseDouble(decimal);
            decimalDouble = (Math.round(decimalDouble * 1000)) / 1000.00;
            rtnStr = String.valueOf(decimalDouble);
        } catch (Exception ex) {
            rtnStr = decimal;
        }

        if (decimal == null) {
            return "0.000"; // ???<-- may be should leave it "" or null
        }

        int index = 0;

        index = rtnStr.indexOf(".");

        int pos = rtnStr.length() - index;

        if (pos == 4)
            ; // in xxx.xx format already
        else if (pos == 3)
            rtnStr += "00";
        else if (pos == 2)
            rtnStr += "000";
        else if (pos == 1)
            rtnStr += "000";
        else if (pos <= 0) {
            rtnStr += ".000";
        } else if (pos > 5) {
            rtnStr = rtnStr.substring(0, index + 4);
        }

        return rtnStr;
    }

    // ###################################################################################
    public static String to4DecimalDigits(String decimal) {
        // 649; 649.; 649.0; 649.23; 649.9000000000001
        double decimalDouble = 0.00;
        String rtnStr = decimal;

        try {
            decimalDouble = Double.parseDouble(decimal);
            decimalDouble = (Math.round(decimalDouble * 10000)) / 10000.00;
            rtnStr = String.valueOf(decimalDouble);
        } catch (Exception ex) {
            rtnStr = decimal;
        }

        if (decimal == null) {
            return "0.0000"; // ???<-- may be should leave it "" or null
        }

        int index = 0;

        index = rtnStr.indexOf(".");

        int pos = rtnStr.length() - index;

        if (pos == 5)
            ; // in xxx.xx format already
        else if (pos == 4)
            rtnStr += "0";
        else if (pos == 3)
            rtnStr += "00";
        else if (pos == 2)
            rtnStr += "000";
        else if (pos == 1)
            rtnStr += "0000";
        else if (pos <= 0) {
            rtnStr += ".0000";
        } else if (pos > 5) {
            rtnStr = rtnStr.substring(0, index + 5);
        }

        return rtnStr;
    }

    // ###################################################################################
    public static String toInteger(String decimalValue) {
        // 649; 649.; 649.0; 649.23; 649.9000000000001

        if (decimalValue == null) {
            return "0"; // ???<-- may be should leave it "" or null
        }

        String rtnStr = decimalValue;
        int index = 0;

        index = rtnStr.indexOf(".");

        if (index >= 0) {
            rtnStr = rtnStr.substring(0, index);
        }

        return rtnStr;
    }

    // ############################################################################
    public static Date calcDate(String s, String s1, String s2) {
        if (s == null || s1 == null || s2 == null) return (null);

        int i = Integer.parseInt(s);
        int j = Integer.parseInt(s1) - 1;
        int k = Integer.parseInt(s2);
        GregorianCalendar gregoriancalendar = new GregorianCalendar(i, j, k);
        return gregoriancalendar.getTime();
    }

    public static String calcAge(Date DOB) {
        return calcAgeAtDate(DOB, new GregorianCalendar().getTime());
    }

    /**
     * This returns the Patients Age string at a point in time. IE. How old the
     * patient will be right now or how old will they be on march.31 of this
     * year.
     *
     * @param DOB         Demographics Date of birth
     * @param pointInTime The date you would like to calculate there age at.
     * @return age string ( ie 2 months, 4 years .etc )
     */
    public static String calcAgeAtDate(Date DOB, Date pointInTime) {
        if (DOB == null) return (null);

        GregorianCalendar now = new GregorianCalendar();
        now.setTime(pointInTime);
        int curYear = now.get(Calendar.YEAR);
        int curMonth = now.get(Calendar.MONTH) + 1;
        int curDay = now.get(Calendar.DAY_OF_MONTH);

        GregorianCalendar birthDate = new GregorianCalendar();
        birthDate.setTime(DOB);
        int birthYear = birthDate.get(Calendar.YEAR);
        int birthMonth = birthDate.get(Calendar.MONTH) + 1;
        int birthDay = birthDate.get(5);

        int ageInYears = curYear - birthYear;
        String result = ageInYears
                + " "
                + ResourceBundle.getBundle("oscarResources").getString(
                "global.years");

        if (curMonth > birthMonth || curMonth == birthMonth
                && curDay >= birthDay) {
            ageInYears = curYear - birthYear;
            result = ageInYears
                    + " "
                    + ResourceBundle.getBundle("oscarResources").getString(
                    "global.years");
        } else {
            ageInYears = curYear - birthYear - 1;
            result = ageInYears
                    + " "
                    + ResourceBundle.getBundle("oscarResources").getString(
                    "global.years");
        }
        if (ageInYears < 2) {
            int yearDiff = curYear - birthYear;
            int ageInDays;
            if (yearDiff == 2) {
                ageInDays = (birthDate.getActualMaximum(Calendar.DAY_OF_YEAR) - birthDate
                        .get(Calendar.DAY_OF_YEAR))
                        + now.get(Calendar.DAY_OF_YEAR) + 365;
            } else if (yearDiff == 1) {
                ageInDays = (birthDate.getActualMaximum(Calendar.DAY_OF_YEAR) - birthDate
                        .get(Calendar.DAY_OF_YEAR))
                        + now.get(Calendar.DAY_OF_YEAR);
            } else {
                ageInDays = now.get(Calendar.DAY_OF_YEAR)
                        - birthDate.get(Calendar.DAY_OF_YEAR);
            }
            if (ageInDays / 7 > 9) {
                result = ageInDays
                        / 30
                        + " "
                        + ResourceBundle.getBundle("oscarResources").getString(
                        "global.months");
            } else if (ageInDays >= 14) {
                result = ageInDays
                        / 7
                        + " "
                        + ResourceBundle.getBundle("oscarResources").getString(
                        "global.weeks");
            } else {
                result = ageInDays
                        + " "
                        + ResourceBundle.getBundle("oscarResources").getString(
                        "global.days");
            }
        }
        return result;
    }

    public static int calcAge(String year_of_birth, String month_of_birth, String date_of_birth) {
        GregorianCalendar now = new GregorianCalendar();
        int curYear = now.get(Calendar.YEAR);
        int curMonth = (now.get(Calendar.MONTH) + 1);
        int curDay = now.get(Calendar.DAY_OF_MONTH);
        int age = 0;

        try {
            if (curMonth > Integer.parseInt(month_of_birth)) {
                age = curYear - Integer.parseInt(year_of_birth);
            } else {
                if (curMonth == Integer.parseInt(month_of_birth) && curDay > Integer.parseInt(date_of_birth)) {
                    age = curYear - Integer.parseInt(year_of_birth);
                } else {
                    age = curYear - Integer.parseInt(year_of_birth) - 1;
                }
            }
        } catch (NumberFormatException nfe) {//return -1 for unparsable dates
            log.warn("Invalid date :" + year_of_birth + ":" + month_of_birth + ":" + date_of_birth);
            return -1;
        }
        return age;
    }

    public static int getNumYears(Date dStart, Date dEnd) {
        GregorianCalendar now = new GregorianCalendar();
        now.setTime(dEnd);
        int curYear = now.get(Calendar.YEAR);
        int curMonth = now.get(Calendar.MONTH) + 1;
        int curDay = now.get(Calendar.DAY_OF_MONTH);

        GregorianCalendar birthDate = new GregorianCalendar();
        birthDate.setTime(dStart);
        int birthYear = birthDate.get(Calendar.YEAR);
        int birthMonth = birthDate.get(Calendar.MONTH) + 1;
        int birthDay = birthDate.get(5);

        int ageInYears = curYear - birthYear;

        if (curMonth > birthMonth || curMonth == birthMonth
                && curDay >= birthDay) {
            ageInYears = curYear - birthYear;
        } else {
            ageInYears = curYear - birthYear - 1;
        }
        return ageInYears;
    }

    // ############################################################################
    /**
     * Checks if a string is neither null nor empty.
     * <p>
     * This utility method provides a convenient way to validate string
     * values in a single call, commonly used in form validation and
     * data processing where empty strings should be treated as invalid.
     * </p>
     *
     * @param str String the string to validate
     * @return boolean true if string is not null and not empty, false otherwise
     */
    public static boolean isNotNullOrEmptyStr(String str) {
        return str != null && !str.equals("");
    }

    // ############################################################################
}
