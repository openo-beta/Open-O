/**
 * Location List dropdown helpers for the booking screens (WEB-INF/tags/locationSelect.tag).
 *
 * @since 2026-09-21
 */

/**
 * Chooses the active location whose name matches, ignoring case and surrounding spaces, as when an
 * appointment type names its location. "Not specified", a Legacy Location and an inactive location
 * are never chosen, and the choice is left as it is when nothing matches.
 *
 * @param {HTMLSelectElement} select the locationCode dropdown
 * @param {string} name the location's name, or blank
 */
function selectLocationByName(select, name) {
    const wanted = (name || '').trim().toLowerCase();
    if (!wanted) {
        return;
    }
    const match = Array.from(select.options).find(function (option) {
        return option.value && !option.hasAttribute('data-inactive')
            && option.text.trim().toLowerCase() === wanted;
    });
    if (match) {
        select.value = match.value;
    }
}

/**
 * Chooses a location by its code, as pasting a copied appointment does. A code the dropdown doesn't
 * offer, such as a location disabled since the copy, falls back to the first choice ("Not specified",
 * or the booking's Legacy Location), so a paste never leaves the dropdown blank.
 *
 * @param {HTMLSelectElement} select the locationCode dropdown
 * @param {string} code the location code, or blank
 */
function selectLocationCode(select, code) {
    const offered = Array.from(select.options).some(function (option) {
        return option.value === code;
    });
    select.value = offered ? code : '';
}
