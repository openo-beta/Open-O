/*

    Copyright (c) 2001-2002. Department of Family Medicine, McMaster University. All Rights Reserved.
    This software is published under the GPL GNU General Public License.
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

    This software was written for the
    Department of Family Medicine
    McMaster University
    Hamilton
    Ontario, Canada

*/

function popupPage(vheight, vwidth, varpage) { //open a new popup window
    var page = "" + varpage;
    windowprops = "height=" + vheight + ",width=" + vwidth + ",location=no,scrollbars=yes,menubars=no,toolbars=no,resizable=yes,screenX=0,screenY=0,top=0,left=0";//360,680
    var popup = window.open(page, "groupno", windowprops);
    if (popup != null) {
        if (popup.opener == null) {
            popup.opener = self;
        }
        popup.focus();
    }
}

function openPatient(demographicNo) {
    // The page sets <base href> to the context root, so every popup URL must start from it
    popupPage(700, 1200, ctx + '/demographic/demographiccontrol.jsp?demographic_no=' + demographicNo + '&displaymode=edit&dboperation=search_detail');

}

function openReport(id) {
    popupPage(700, 1200, ctx + '/hospitalReportManager/Display.do?id=' + id + '&duplicateLabIds=');

}

$(document).ready(function () {

    getHrmStatus();
    setInterval(function () {
        getHrmStatus();
    }, 5000);

    // table sorting
    $('#libraryTable').DataTable({
        serverSide: true,
        ajax: {
            "url": ctx + "/hospitalReportManager/hrm.do",
            "data": function (d) {
                d.providerNo = $("#providerNo").val();
                d.providerUnmatched = $("#providerUnmatched").is(":checked");
                d.noSignOff = $("#noSignOff").is(":checked");
                d.demographicUnmatched = $("#demographicUnmatched").is(":checked");
                d.categoryUnmatched = $("#categoryUnmatched").is(":checked");
                console.log("[HRM-INBOX-DEBUG] sending request: providerNo=" + d.providerNo
                    + " (select exists=" + ($("#providerNo").length > 0) + ", disabled=" + $("#providerNo").prop('disabled') + ")"
                    + " providerUnmatched=" + d.providerUnmatched + " noSignOff=" + d.noSignOff
                    + " demographicUnmatched=" + d.demographicUnmatched + " categoryUnmatched=" + d.categoryUnmatched
                    + " start=" + d.start + " length=" + d.length + " draw=" + d.draw);
            },
            "error": function (xhr, textStatus, errorThrown) {
                console.log("[HRM-INBOX-DEBUG] ajax ERROR status=" + xhr.status + " textStatus=" + textStatus + " errorThrown=" + errorThrown);
                console.log("[HRM-INBOX-DEBUG] ajax ERROR response (first 2000 chars): " + (xhr.responseText || "").substring(0, 2000));
            }
        },
        searching: false,
        "dom": '<"top"i>rt<"bottom"lp><"clear">',
        "columns": [
            {
                render: function (data, type, full, meta) {
                    var r = "<a href='javascript:void(0)' onClick=\"openReport(" + full.id + ");return false;\" ><img src='" + ctx + "/images/details2.gif' border='0'/></a>";
                    return r;
                }
            },
            {
                // Recipient named in the report, then every EMR provider the report is assigned to
                "data": "recipient_name", render: function (data, type, full, meta) {
                    var cell = $('<div>').text(full.recipient_name).html();
                    if (full.assigned_providers != null && full.assigned_providers.length > 0) {
                        cell += "<br/><small title='EMR providers this report is assigned to'>" + $('<div>').text(full.assigned_providers).html() + "</small>";
                    }
                    return cell;
                }
            },
            {
                "data": "patient_name", render: function (data, type, full, meta) {
                    var r = data;
                    if (full.demographic_no != null && full.demographic_no.length > 0) {
                        r = '<a href="javascript:void(0);" onClick="openPatient(' + full.demographic_no + ');return false;">' + data + '</a>';
                    }
                    return r;
                }
            },
            {
                // Which side of the report has no EMR record: the provider, the patient, or both
                "orderable": false, render: function (data, type, full, meta) {
                    var sides = [];
                    if (full.provider_unmatched) {
                        sides.push("Provider");
                    }
                    if (full.demographic_unmatched) {
                        sides.push("Patient");
                    }
                    return sides.join(", ");
                }
            },
            {"data": "patient_dob"},
            {"data": "patient_hcn"},
            {"data": "patient_gender"},
            {"data": "report_date"},
            {"data": "received_date"},
            {"data": "report_status"},
            {"data": "sending_facility"},
            {"data": "report_number"},
            {"data": "class_subclass"},
            {"data": "category", "visible": false},
            {"data": "description"}
        ],
        "order": [[7, "desc"]],
        "language": {
            "url": ctx + "/library/DataTables/i18n/" + lang + ".json"
        }

    });

    $('#libraryTable').on('xhr.dt', function (e, settings, json, xhr) {
        if (!json) {
            console.log("[HRM-INBOX-DEBUG] response received but JSON is null/invalid, http status=" + (xhr ? xhr.status : "?"));
            return;
        }
        console.log("[HRM-INBOX-DEBUG] response: draw=" + json.draw + " recordsTotal=" + json.recordsTotal
            + " recordsFiltered=" + json.recordsFiltered + " rows in data=" + (json.data ? json.data.length : "no data array")
            + (json.error ? " error=" + json.error : ""));
        if (json.data && json.data.length > 0) {
            console.log("[HRM-INBOX-DEBUG] first row: " + JSON.stringify(json.data[0]));
        }
    });
    $('#libraryTable').on('draw.dt', function () {
        console.log("[HRM-INBOX-DEBUG] table drawn, visible rows=" + $('#libraryTable tbody tr').length
            + " page info=" + JSON.stringify($("#libraryTable").DataTable().page.info()));
    });

    $("#uploadHRM").on('click', function () {
        $("#uploadHRMDialog").modal();
    });

    $("#providerUnmatched").on('change', function () {
        console.log("[HRM-INBOX-DEBUG] filter change: providerUnmatched=" + $(this).is(":checked"));
        updateProviderFilter();
    });
    $("#providerNo").on('change', function () {
        console.log("[HRM-INBOX-DEBUG] filter change: providerNo=" + $(this).val());
        updateProviderFilter();
    });
    $("#noSignOff").on('change', function () {
        console.log("[HRM-INBOX-DEBUG] filter change: noSignOff=" + $(this).is(":checked"));
        reloadTable();
    });
    $("#demographicUnmatched").on('change', function () {
        console.log("[HRM-INBOX-DEBUG] filter change: demographicUnmatched=" + $(this).is(":checked"));
        reloadTable();
    });
    $("#categoryUnmatched").on('change', function () {
        console.log("[HRM-INBOX-DEBUG] filter change: categoryUnmatched=" + $(this).is(":checked"));
        reloadTable();
    });

    $("#showAddlPatientInfo").on('change', function () {
        $("#libraryTable").DataTable().column(4).visible($(this).is(":checked"));
        $("#libraryTable").DataTable().column(5).visible($(this).is(":checked"));
        $("#libraryTable").DataTable().column(6).visible($(this).is(":checked"));
    });

    $("#showCategoryInfo").on('change', function () {
        $("#libraryTable").DataTable().column(13).visible($(this).is(":checked"));
    });

    $('#hrm_file').fileupload({
        dataType: 'json',
        done: function (e, data) {
            $("#uploadHRMDialog").modal('hide');
            $("#libraryTable").DataTable().ajax.reload();
        }, fail: function (e, data) {
            alert('Error uploading file. See log for more information');
        }
    });

});

function updateProviderFilter() {
    var providerNoVal = $("#providerNo").val();
    var providerUnmatchedVal = $("#providerUnmatched").is(':checked');
    console.log("[HRM-INBOX-DEBUG] updateProviderFilter: providerNo=" + providerNoVal + " providerUnmatched=" + providerUnmatchedVal);

    if (providerUnmatchedVal) {
        $("#providerNo").prop('disabled', true);
        $("#noSignOff").prop('checked', false);
        $("#noSignOff").prop('disabled', true);
    } else {
        $("#providerNo").prop('disabled', false);
        $("#noSignOff").prop('disabled', false);
        $("#noSignOff").prop('checked', true);
    }

    $("#libraryTable").DataTable().ajax.reload();

}

function reloadTable() {
    console.log("[HRM-INBOX-DEBUG] reloadTable()");
    $("#libraryTable").DataTable().ajax.reload();
}

function fetchNewData() {
    $.ajax({
        type: "GET",
        url: ctx + '/hospitalReportManager/hrm.do?method=fetch',
        dataType: 'json',
        async: true,
        success: function (data) {
            if (data && data.error) {
                alert('An error occured. Please check the HRM log for more information\n' + data.error);
            }
            $("#libraryTable").DataTable().ajax.reload();
        }
    });

}

function getHrmStatus() {
    $.ajax({
        type: "GET",
        url: ctx + '/hospitalReportManager/hrm.do?method=getHrmStatus',
        dataType: 'json',
        async: true,
        success: function (data) {
            if (data.running != null && data.running == true) {
                $("#hrm_status").html("Fetching data from HRM");
            } else {
                $("#hrm_status").html("Idle");
            }

        }
    });

}
