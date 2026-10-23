/**
 * Location List dropdown for the booking screens (WEB-INF/tags/locationSelect.tag): the locationmenu
 * widget that draws it, and the helpers that choose a location.
 *
 * @since 2026-09-21
 */

/**
 * A jQuery UI selectmenu that draws each option's Location Chip (its data-chip) before its name, in the
 * menu and on the button. It redraws on the select's change event, so code that sets the value fires one.
 * Its menu carries the class location-menu, which css/locationSelect.css sizes its rows by.
 */
jQuery.widget('custom.locationmenu', jQuery.ui.selectmenu, {
    options: {
        classes: {'ui-selectmenu-menu': 'location-menu'}
    },

    _create: function () {
        this.tabIndex = this.element.attr('tabindex');
        this._super();
        this._on({change: 'refresh'});
    },

    _drawButton: function () {
        this._super();
        // selectmenu hides the select, and the browser then refuses a required blank one without saying
        // why. Keep it laid out, unseen, at the button, so the browser's message points there; the button
        // takes its place in the tab order.
        this.element.attr('tabindex', -1)
            .css({display: 'block', position: 'absolute', width: '1px', opacity: 0, pointerEvents: 'none'});
        this.button.attr('tabindex', this.tabIndex);
    },

    // selectmenu sets the button's tabindex back to 0 each time it draws the menu.
    _setOptionDisabled: function (value) {
        this._super(value);
        if (!value) {
            this.button.attr('tabindex', this.tabIndex);
        }
    },

    _renderItem: function (ul, item) {
        const li = this._super(ul, item);
        this._prependChip(li.children('div'), item);
        return li;
    },

    _renderButtonItem: function (item) {
        return this._prependChip(this._super(item), item);
    },

    // The chip is hidden from screen readers, which read the name beside it.
    _prependChip: function (element, item) {
        const chip = item.element.attr('data-chip');
        if (chip) {
            element.prepend(jQuery('<span aria-hidden="true">').html(chip), ' ');
        }
        return element;
    }
});

/**
 * Chooses the active location whose name matches, ignoring case and surrounding spaces, as when an
 * appointment type names its location. The blank option, a Legacy Location and an inactive location
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
        return option.value && !option.hasAttribute('data-inactive') && !option.hasAttribute('data-legacy')
            && option.text.trim().toLowerCase() === wanted;
    });
    if (match) {
        select.value = match.value;
        select.dispatchEvent(new Event('change'));
    }
}

/**
 * Chooses a location by its code, as pasting a copied appointment does. A code the dropdown doesn't
 * offer, such as a location disabled since the copy, or a copied Legacy Location, falls back to the
 * blank option. Leaving no option chosen would look the same but post no locationCode, and the save
 * would then keep the target's own Legacy Location.
 *
 * @param {HTMLSelectElement} select the locationCode dropdown
 * @param {string} code the location code, or blank
 */
function selectLocationCode(select, code) {
    const offered = Array.from(select.options).some(function (option) {
        return option.value === code && !option.hasAttribute('data-legacy');
    });
    select.value = offered ? code : '';
    select.dispatchEvent(new Event('change'));
}

/**
 * Pastes a copied appointment's location into a booking form: its code into the Location List
 * dropdown when the form offers one (see selectLocationCode), otherwise its text into the location
 * field, which sites, programs and free text all post.
 *
 * @param {HTMLFormElement} form the booking form
 * @param {string} code the copied location code, or blank
 * @param {string} text the copied location text, or blank
 */
function pasteLocation(form, code, text) {
    if (form.locationCode) {
        selectLocationCode(form.locationCode, code);
    } else {
        form.location.value = text;
    }
}
