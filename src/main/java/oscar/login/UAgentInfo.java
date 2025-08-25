//CHECKSTYLE:OFF
/*
 * Copyright 2010, Anthony Hand
 *
 * LICENSE INFORMATION
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *        http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 *
 */
package oscar.login;

/**
 * The DetectSmartPhone class encapsulates information about
 * a browser's connection to your web site.
 * You can use it to find out whether the browser asking for
 * your site's content is probably running on a mobile device.
 * The methods were written so you can be as granular as you want.
 * For example, enquiring whether it's as specific as an iPod Touch or
 * as general as a smartphone class device.
 * The object's methods return true, or false.
 */
public class UAgentInfo {
    // User-Agent and Accept HTTP request headers

    private String userAgent = "";
    private String httpAccept = "";

    // Optional: store values for quickly accessing same info multiple times.
    // Call InitDeviceScan() to initialize these values.
    public boolean isIphone = false;
    public boolean isTierIphone = false;
    public boolean isTierRichCss = false;
    public boolean isTierGenericMobile = false;

    // Initialize some initial smartphone string variables.
    public static final String engineWebKit = "webkit";
    public static final String deviceAndroid = "android";

    public static final String deviceIphone = "iphone";
    public static final String deviceIpod = "ipod";
    public static final String deviceIpad = "ipad";

    public static final String deviceSymbian = "symbian";
    public static final String deviceS60 = "series60";
    public static final String deviceS70 = "series70";
    public static final String deviceS80 = "series80";
    public static final String deviceS90 = "series90";

    public static final String deviceWinMob = "windows ce";
    public static final String deviceWindows = "windows";
    public static final String deviceIeMob = "iemobile";
    public static final String devicePpc = "ppc"; //Stands for PocketPC
    public static final String enginePie = "wm5 pie"; //An old Windows Mobile

    public static final String deviceBB = "blackberry";
    public static final String vndRIM = "vnd.rim"; //Detectable when BB devices emulate IE or Firefox
    public static final String deviceBBStorm = "blackberry95";  //Storm 1 and 2
    public static final String deviceBBBold = "blackberry97";  //Bold
    public static final String deviceBBTour = "blackberry96";  //Tour
    public static final String deviceBBCurve = "blackberry89";  //Curve 2

    public static final String devicePalm = "palm";
    public static final String deviceWebOS = "webos"; //For Palm's new WebOS devices
    public static final String engineBlazer = "blazer"; //Old Palm
    public static final String engineXiino = "xiino"; //Another old Palm

    public static final String deviceKindle = "kindle";  //Amazon Kindle, eInk one.

    public static final String deviceNuvifone = "nuvifone";  //Garmin Nuvifone

    //Initialize variables for mobile-specific content.
    public static final String vndwap = "vnd.wap";
    public static final String wml = "wml";

    //Initialize variables for other random devices and mobile browsers.
    public static final String deviceBrew = "brew";
    public static final String deviceDanger = "danger";
    public static final String deviceHiptop = "hiptop";
    public static final String devicePlaystation = "playstation";
    public static final String deviceNintendoDs = "nitro";
    public static final String deviceNintendo = "nintendo";
    public static final String deviceWii = "wii";
    public static final String deviceXbox = "xbox";
    public static final String deviceArchos = "archos";

    public static final String engineOpera = "opera"; //Popular browser
    public static final String engineNetfront = "netfront"; //Common embedded OS browser
    public static final String engineUpBrowser = "up.browser"; //common on some phones
    public static final String engineOpenWeb = "openweb"; //Transcoding by OpenWave server
    public static final String deviceMidp = "midp"; //a mobile Java technology
    public static final String uplink = "up.link";
    public static final String engineTelecaQ = "teleca q"; //a modern feature phone browser
    public static final String devicePda = "pda"; //some devices report themselves as PDAs
    public static final String mini = "mini";  //Some mobile browsers put "mini" in their names.
    public static final String mobile = "mobile"; //Some mobile browsers put "mobile" in their user agent strings.
    public static final String mobi = "mobi"; //Some mobile browsers put "mobi" in their user agent strings.

    //Use Maemo, Tablet, and Linux to test for Nokia"s Internet Tablets.
    public static final String maemo = "maemo";
    public static final String maemoTablet = "tablet";
    public static final String linux = "linux";
    public static final String qtembedded = "qt embedded"; //for Sony Mylo
    public static final String mylocom2 = "com2"; //for Sony Mylo also

    //In some UserAgents, the only clue is the manufacturer.
    public static final String manuSonyEricsson = "sonyericsson";
    public static final String manuericsson = "ericsson";
    public static final String manuSamsung1 = "sec-sgh";
    public static final String manuSony = "sony";
    public static final String manuHtc = "htc"; //Popular Android and WinMo manufacturer

    //In some UserAgents, the only clue is the operator.
    public static final String svcDocomo = "docomo";
    public static final String svcKddi = "kddi";
    public static final String svcVodafone = "vodafone";

    /**
     * Initialize the userAgent and httpAccept variables
     *
     * @param userAgent  the User-Agent header
     * @param httpAccept the Accept header
     */
    public UAgentInfo(String userAgent, String httpAccept) {
        if (userAgent != null) {
            this.userAgent = userAgent.toLowerCase();
        }
        if (httpAccept != null) {
            this.httpAccept = httpAccept.toLowerCase();
        }

        //Intialize key stored values.
        initDeviceScan();
    }

    /**
     * Return the lower case HTTP_USER_AGENT
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * Return the lower case HTTP_ACCEPT
     */
    public String getHttpAccept() {
        return httpAccept;
    }

    /**
     * Return whether the device is an Iphone or iPod Touch
     */
    public boolean getIsIphone() {
        return isIphone;
    }

    /**
     * Return whether the device is in the Iphone Tier.
     */
    public boolean getIsTierIphone() {
        return isTierIphone;
    }

    /**
     * Return whether the device is an Iphone or iPod Touch
     */
    public boolean getIsTierRichCss() {
        return isTierRichCss;
    }

    /**
     * Return whether the device is an Iphone or iPod Touch
     */
    public boolean getIsTierGenericMobile() {
        return isTierGenericMobile;
    }

    /**
     * Initialize Key Stored Values.
     */
    public void initDeviceScan() {
        this.isIphone = detectIphoneOrIpod();
        this.isTierIphone = detectTierIphone();
        this.isTierRichCss = detectTierRichCss();
        this.isTierGenericMobile = detectTierOtherPhones();
    }


    /**
     * Detects if the current device is an iPhone.
     */
    public boolean detectIphone() {
        // The iPad and iPod touch say they're an iPhone! So let's disambiguate.
        return userAgent.indexOf(deviceIphone) != -1 &&
            !detectIpad() &&
            !detectIpod();
    }

    /**
     * Detects if the current device is an iPod Touch.
     */
    public boolean detectIpod() {
        return userAgent.indexOf(deviceIpod) != -1;
    }

    /**
     * Detects if the current device is an iPad tablet.
     */
    public boolean detectIpad() {
        return userAgent.indexOf(deviceIpad) != -1
            && detectWebkit();
    }

    /**
     * Detects if the current device is an iPhone or iPod Touch.
     */
    public boolean detectIphoneOrIpod() {
        //We repeat the searches here because some iPods may report themselves as an iPhone, which would be okay.
        return userAgent.indexOf(deviceIphone) != -1
            || userAgent.indexOf(deviceIpod) != -1;
    }

    /**
     * Detects if the current device is an Android OS-based device.
     */
    public boolean detectAndroid() {
        return userAgent.indexOf(deviceAndroid) != -1;
    }

    /**
     * Detects if the current device is an Android OS-based device and
     * the browser is based on WebKit.
     */
    public boolean detectAndroidWebKit() {
        return detectAndroid() && detectWebkit();
    }

    /**
     * Detects if the current browser is based on WebKit.
     */
    public boolean detectWebkit() {
        return userAgent.indexOf(engineWebKit) != -1;
    }

    /**
     * Detects if the current browser is the S60 Open Source Browser.
     */
    public boolean detectS60OssBrowser() {
        //First, test for WebKit, then make sure it's either Symbian or S60.
        return detectWebkit()
            && (userAgent.indexOf(deviceSymbian) != -1
            || userAgent.indexOf(deviceS60) != -1);
    }

    /**
     * Detects if the current device is any Symbian OS-based device,
     * including older S60, Series 70, Series 80, Series 90, and UIQ,
     * or other browsers running on these devices.
     */
    public boolean detectSymbianOS() {
        return userAgent.indexOf(deviceSymbian) != -1
            || userAgent.indexOf(deviceS60) != -1
            || userAgent.indexOf(deviceS70) != -1
            || userAgent.indexOf(deviceS80) != -1
            || userAgent.indexOf(deviceS90) != -1;
    }

    /**
     * Detects if the current browser is a Windows Mobile device.
     */
    public boolean detectWindowsMobile() {
        //Most devices use 'Windows CE', but some report 'iemobile'
        //  and some older ones report as 'PIE' for Pocket IE.
        //  We also look for instances of HTC and Windows for many of their WinMo devices.
        return userAgent.indexOf(deviceWinMob) != -1
            || userAgent.indexOf(deviceIeMob) != -1
            || userAgent.indexOf(enginePie) != -1
            || userAgent.indexOf(devicePpc) != -1
            || (userAgent.indexOf(manuHtc) != -1 && userAgent.indexOf(deviceWindows) != -1)
            || (detectWapWml() && userAgent.indexOf(deviceWindows) != -1);
    }

    /**
     * Detects if the current browser is a BlackBerry of some sort.
     */
    public boolean detectBlackBerry() {
        return userAgent.indexOf(deviceBB) != -1 || httpAccept.indexOf(vndRIM) != -1;
    }

    /**
     * Detects if the current browser is a BlackBerry Touch
     * device, such as the Storm
     */
    public boolean detectBlackBerryTouch() {
        return userAgent.indexOf(deviceBBStorm) != -1;
    }

    /**
     * Detects if the current browser is a BlackBerry device AND
     * has a more capable recent browser.
     * Examples, Storm, Bold, Tour, Curve2
     */
    public boolean detectBlackBerryHigh() {
        if (detectBlackBerry()) {
            return detectBlackBerryTouch()
                || userAgent.indexOf(deviceBBBold) != -1
                || userAgent.indexOf(deviceBBTour) != -1
                || userAgent.indexOf(deviceBBCurve) != -1;
        } else {
            return false;
        }
    }

    /**
     * Detects if the current browser is a BlackBerry device AND
     * has an older, less capable browser.
     * Examples: Pearl, 8800, Curve1
     */
    public boolean detectBlackBerryLow() {
        if (detectBlackBerry()) {
            //Assume that if it's not in the High tier, then it's Low
            return !detectBlackBerryHigh();
        } else {
            return false;
        }
    }

    /**
     * Detects if the current browser is on a PalmOS device.
     */
    public boolean detectPalmOS() {
        //Most devices nowadays report as 'Palm', but some older ones reported as Blazer or Xiino.
        if (userAgent.indexOf(devicePalm) != -1
                || userAgent.indexOf(engineBlazer) != -1
                || userAgent.indexOf(engineXiino) != -1) {
            //Make sure it's not WebOS first
            return !detectPalmWebOS();
        }
        return false;
    }


    /**
     * Detects if the current browser is on a Palm device
     * running the new WebOS.
     */
    public boolean detectPalmWebOS() {
        return userAgent.indexOf(deviceWebOS) != -1;
    }

    /**
     * Detects if the current browser is a
     * Garmin Nuvifone.
     */
    public boolean detectGarminNuvifone() {
        return userAgent.indexOf(deviceNuvifone) != -1;
    }

    /**
     * Check to see whether the device is any device
     * in the 'smartphone' category.
     */
    public boolean detectSmartphone() {
        return (detectIphoneOrIpod()
                || detectS60OssBrowser()
                || detectSymbianOS()
                || detectAndroid()
                || detectWindowsMobile()
                || detectBlackBerry()
                || detectPalmWebOS()
                || detectPalmOS()
                || detectGarminNuvifone());
    }

    /**
     * Detects whether the device is a Brew-powered device.
     */
    public boolean detectBrewDevice() {
        return userAgent.indexOf(deviceBrew) != -1;
    }

    /**
     * Detects the Danger Hiptop device.
     */
    public boolean detectDangerHiptop() {
        return userAgent.indexOf(deviceDanger) != -1
            || userAgent.indexOf(deviceHiptop) != -1;
    }

    /**
     * Detects Opera Mobile or Opera Mini.
     */
    public boolean detectOperaMobile() {
        return userAgent.indexOf(engineOpera) != -1
            && (userAgent.indexOf(mini) != -1
            || userAgent.indexOf(mobi) != -1);
    }

    /**
     * Detects whether the device supports WAP or WML.
     */
    public boolean detectWapWml() {
        return httpAccept.indexOf(vndwap) != -1
            || httpAccept.indexOf(wml) != -1;
    }

    /**
     * Detects if the current device is an Amazon Kindle.
     */
    public boolean detectKindle() {
        return userAgent.indexOf(deviceKindle) != -1;
    }

    /**
     * The quick way to detect for a mobile device.
     * Will probably detect most recent/current mid-tier Feature Phones
     * as well as smartphone-class devices. Excludes Apple iPads.
     */
    public boolean detectMobileQuick() {
        //Let's say no if it's an iPad, which contains 'mobile' in its user agent
        if (detectIpad()) {
            return false;
        }
        //Most mobile browsing is done on smartphones
        if (detectSmartphone()) {
            return true;
        }

        if (detectWapWml()) {
            return true;
        }
        if (detectBrewDevice()) {
            return true;
        }
        if (detectOperaMobile()) {
            return true;
        }

        if (userAgent.indexOf(engineNetfront) != -1) {
            return true;
        }
        if (userAgent.indexOf(engineUpBrowser) != -1) {
            return true;
        }
        if (userAgent.indexOf(engineOpenWeb) != -1) {
            return true;
        }

        if (detectDangerHiptop()) {
            return true;
        }

        if (detectMidpCapable()) {
            return true;
        }

        if (detectMaemoTablet()) {
            return true;
        }
        if (detectArchos()) {
            return true;
        }

        if (userAgent.indexOf(devicePda) != -1) {
            return true;
        }
        return userAgent.indexOf(mobile) != -1;
    }

    /**
     * Detects if the current device is a Sony Playstation.
     */
    public boolean detectSonyPlaystation() {
        return userAgent.indexOf(devicePlaystation) != -1;
    }

    /**
     * Detects if the current device is a Nintendo game device.
     */
    public boolean detectNintendo() {
        return userAgent.indexOf(deviceNintendo) != -1
            || userAgent.indexOf(deviceWii) != -1
            || userAgent.indexOf(deviceNintendoDs) != -1;
    }

    /**
     * Detects if the current device is a Microsoft Xbox.
     */
    public boolean detectXbox() {
        return userAgent.indexOf(deviceXbox) != -1;
    }

    /**
     * Detects if the current device is an Internet-capable game console.
     */
    public boolean detectGameConsole() {
        return detectSonyPlaystation()
            || detectNintendo()
            || detectXbox();
    }

    /**
     * Detects if the current device supports MIDP, a mobile Java technology.
     */
    public boolean detectMidpCapable() {
        return userAgent.indexOf(deviceMidp) != -1
            || httpAccept.indexOf(deviceMidp) != -1;
    }


    /**
     * Detects if the current device is on one of the Maemo-based Nokia Internet Tablets.
     */
    public boolean detectMaemoTablet() {
        if (userAgent.indexOf(maemo) != -1) {
            return true;
        } else return userAgent.indexOf(maemoTablet) != -1
            && userAgent.indexOf(linux) != -1;
    }

    /**
     * Detects if the current device is an Archos media player/Internet tablet.
     */
    public boolean detectArchos() {
        return userAgent.indexOf(deviceArchos) != -1;
    }

    /**
     * Detects if the current browser is a Sony Mylo device.
     */
    public boolean detectSonyMylo() {
        return userAgent.indexOf(manuSony) != -1
            && (userAgent.indexOf(qtembedded) != -1
            || userAgent.indexOf(mylocom2) != -1);
    }

    /**
     * The longer and more thorough way to detect for a mobile device.
     * Will probably detect most feature phones,
     * smartphone-class devices, Internet Tablets,
     * Internet-enabled game consoles, etc.
     * This ought to catch a lot of the more obscure and older devices, also --
     * but no promises on thoroughness!
     */
    public boolean detectMobileLong() {
        if (detectMobileQuick()
                || detectGameConsole()
                || detectSonyMylo()) {
            return true;
        }

        //detect older phones from certain manufacturers and operators.
        if (userAgent.indexOf(uplink) != -1) {
            return true;
        }
        if (userAgent.indexOf(manuSonyEricsson) != -1) {
            return true;
        }
        if (userAgent.indexOf(manuericsson) != -1) {
            return true;
        }
        if (userAgent.indexOf(manuSamsung1) != -1) {
            return true;
        }

        if (userAgent.indexOf(svcDocomo) != -1) {
            return true;
        }
        if (userAgent.indexOf(svcKddi) != -1) {
            return true;
        }
        return userAgent.indexOf(svcVodafone) != -1;
    }

    //*****************************
    // For Mobile Web Site Design
    //*****************************

    /**
     * The quick way to detect for a tier of devices.
     * This method detects for devices which can
     * display iPhone-optimized web content.
     * Includes iPhone, iPod Touch, Android, Palm WebOS, etc.
     */
    public boolean detectTierIphone() {
        return detectIphoneOrIpod()
            || detectAndroid()
            || detectAndroidWebKit()
            || detectPalmWebOS()
            || detectGarminNuvifone()
            || detectMaemoTablet();
    }

    /**
     * The quick way to detect for a tier of devices.
     * This method detects for devices which are likely to be capable
     * of viewing CSS content optimized for the iPhone,
     * but may not necessarily support JavaScript.
     * Excludes all iPhone Tier devices.
     */
    public boolean detectTierRichCss() {
        //The following devices are explicitly ok.
        //Note: 'High' BlackBerry devices ONLY
        return detectMobileQuick()
            && (detectWebkit()
            || detectS60OssBrowser()
            || detectBlackBerryHigh()
            || detectWindowsMobile()
            || userAgent.indexOf(engineTelecaQ) != -1);
    }

    /**
     * The quick way to detect for a tier of devices.
     * This method detects for all other types of phones,
     * but excludes the iPhone and RichCSS Tier devices.
     */
    public boolean detectTierOtherPhones() {
        return detectMobileQuick() && (!detectTierIphone()) && (!detectTierRichCss());
    }
}
