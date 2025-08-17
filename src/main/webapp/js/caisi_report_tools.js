function getGeneralFormsReport() {
    popupPage2('${request.contextPath}/PMmodule/ClientManager.do?method=getGeneralFormsReport', "generalFormsReport");
}

function getIntakeReport(type) {
    alert("Intake report functionality has been removed");
}


function createStreetHealthReport() {
    var startDate = "";

    while (startDate.length != 10 || startDate.substring(4, 5) != "-" || startDate.substring(7, 8) != "-") {
        startDate = prompt("Please enter start date (e.g. 2006-01-01)", "<%=dateStr%>");
        if (startDate == null) {
            return false;
        }
    }

    alert('Generating report for date ' + startDate);

    popupPage2("../PMmodule/StreetHealthIntakeReportAction.do?startDate=" + startDate, "StreetHealthReport");
}


function popupPage2(varpage, windowname) {
    var page = "" + varpage;
    var windowprops = "height=700,width=1000,top=10,left=0,location=yes,scrollbars=yes,menubars=no,toolbars=no,resizable=yes";
    var popup = window.open(page, windowname, windowprops);
    if (popup != null) {
        if (popup.opener == null) {
            popup.opener = self;
        }
        popup.focus();
    }
}
