/**
 * Adds a quick pick date picker to the page.
 */

function addQuickPick() {
  const quickPickDiv = document.getElementById('quickPickDateOptions');
  for (let i = 0; i < 40; i++) {
    const linkButton = document.createElement('a');
    linkButton.href = 'javascript:void(0)';
    switch (Math.floor(i / 10)) {
      case 0:
        linkButton.innerText = (i % 10) + 1 + "d";
        linkButton.onclick = function () {
          addTime((i % 10) + 1, "days");
        };
        break;//1 through 10 days
      case 1:
        linkButton.innerText = (i % 10) + 1 + "w";
        linkButton.onclick = function () {
          addTime(((i % 10) + 1) * 7, "days");
        };
        break;//1 through 10 weeks
      case 2:
        linkButton.innerText = (i % 10) + 1 + "m";
        linkButton.onclick = function () {
          addTime((i % 10) + 1, "months");
        };
        break;//1 through 10 months
      case 3:
        linkButton.innerText = (i % 10) + 1 + "y";
        linkButton.onclick = function () {
          addTime(((i % 10) + 1) * 12, "months");
        };
        break;//1 through 10 years
    }
    quickPickDiv.appendChild(linkButton);
  }
}

/**
 * Adds a number of days or months to the current date.
 */
function addTime(num, type) {
  let currentDate = new Date();
  if (type === "months") {
    currentDate.setMonth(currentDate.getMonth() + num);
  } else {
    currentDate.setDate(currentDate.getDate() + num);
  }
  const year = currentDate.getFullYear();
  const month = String(currentDate.getMonth() + 1).padStart(2, '0');
  const day = String(currentDate.getDate()).padStart(2, '0');
  document.serviceform.xml_appointment_date.value = year + "-" + month + "-" + day;
}
