/*
 * Copyright (c) 2015-2019. The Pharmacists Clinic, Faculty of Pharmaceutical Sciences, University of British Columbia. All Rights Reserved.
 * This software is published under the GPL GNU General Public License.
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * This software was written for the
 * The Pharmacists Clinic
 * Faculty of Pharmaceutical Sciences
 * University of British Columbia
 * Vancouver, British Columbia, Canada
 */

/**
 * Item Style editor: edits one item's description, colour or icon in a modal dialog.
 *
 * Vanilla rewrite of UBC's lookupListItemEditor.js (jQuery UI dialog, colorPicker plugin and a
 * stylesheet-scanning icon picker). The dialog markup is static, in
 * appointment/itemStyleEditorDialog.jspf, so every save fills the same three fields instead of
 * appending new ones.
 *
 * Page contract:
 * - Include itemStyleEditorDialog.jspf, css/itemStyleEditor.css and this script; add
 *   css/glyphicons-standalone.css when an icon set is of kind 'glyphicon', and Coloris
 *   (library/coloris/0.25.0/coloris.min.css and .js, before this script) when the page edits colours.
 * - Each edit trigger carries data-item-style-edit (description, colour or icon),
 *   data-item-id and data-current (the value being edited). WEB-INF/tags/itemStyleEditButton.tag
 *   renders one.
 * - Call ItemStyleEditor.init() with one entry per kind the page edits. A trigger for a
 *   kind the page did not configure does nothing.
 * - A colour's swatch is an element with class item-style-swatch and data-colour (the colour);
 *   ItemStyleEditor.paintSwatches() fills every one on the page, and the colour picker offers
 *   their colours as the ones in use.
 *
 * The form posts ID, dispatch (updateDescription, updateColour or updateIcon) and value.
 * Clear posts a blank value. Saving an unchanged value closes the dialog without posting.
 * A colour posts as the picker reports it, #rrggbb or #rrggbbaa when see-through; a fully
 * see-through colour keeps its hue, so raising its opacity later brings it back.
 *
 * @since 2026-09-15
 */
(function (window, document) {
    'use strict';

    /** A colour as the pages store it, #rrggbb or #rrggbbaa, and the only form this script applies as CSS. */
    const COLOUR = /^#[0-9a-fA-F]{6}([0-9a-fA-F]{2})?$/;

    /** Where the colour picker starts for an item with no colour: fully see-through. */
    const NO_COLOUR = '#ffffff00';

    /**
     * The colour picker's presets: the GNOME palette GTK's colour chooser offers, light to dark,
     * listed row by row so that css/itemStyleEditor.css's 9 columns give one hue per column.
     */
    const PRESETS = Object.freeze([
        '#99c1f1', '#8ff0a4', '#f9f06b', '#ffbe6f', '#f66151', '#dc8add', '#cdab8f', '#ffffff', '#77767b',
        '#62a0ea', '#57e389', '#f8e45c', '#ffa348', '#ed333b', '#c061cb', '#b5835a', '#f6f5f4', '#5e5c64',
        '#3584e4', '#33d17a', '#f6d32d', '#ff7800', '#e01b24', '#9141ac', '#986a44', '#deddda', '#3d3846',
        '#1c71d8', '#2ec27e', '#f5c211', '#e66100', '#c01c28', '#813d9c', '#865e3c', '#c0bfbc', '#241f31',
        '#1a5fb4', '#26a269', '#e5a50a', '#c64600', '#a51d2d', '#613583', '#63452c', '#9a9996', '#000000'
    ]);

    /**
     * Every Glyphicons Halflings class in css/glyphicons-standalone.css, in stylesheet order.
     */
    const GLYPHICONS = Object.freeze([
        'glyphicon-asterisk', 'glyphicon-plus', 'glyphicon-euro', 'glyphicon-minus', 'glyphicon-cloud',
        'glyphicon-envelope', 'glyphicon-pencil', 'glyphicon-glass', 'glyphicon-music', 'glyphicon-search',
        'glyphicon-heart', 'glyphicon-star', 'glyphicon-star-empty', 'glyphicon-user', 'glyphicon-film',
        'glyphicon-th-large', 'glyphicon-th', 'glyphicon-th-list', 'glyphicon-ok', 'glyphicon-remove',
        'glyphicon-zoom-in', 'glyphicon-zoom-out', 'glyphicon-off', 'glyphicon-signal', 'glyphicon-cog',
        'glyphicon-trash', 'glyphicon-home', 'glyphicon-file', 'glyphicon-time', 'glyphicon-road',
        'glyphicon-download-alt', 'glyphicon-download', 'glyphicon-upload', 'glyphicon-inbox',
        'glyphicon-play-circle', 'glyphicon-repeat', 'glyphicon-refresh', 'glyphicon-list-alt',
        'glyphicon-flag', 'glyphicon-headphones', 'glyphicon-volume-off', 'glyphicon-volume-down',
        'glyphicon-volume-up', 'glyphicon-qrcode', 'glyphicon-barcode', 'glyphicon-tag', 'glyphicon-tags',
        'glyphicon-book', 'glyphicon-print', 'glyphicon-font', 'glyphicon-bold', 'glyphicon-italic',
        'glyphicon-text-height', 'glyphicon-text-width', 'glyphicon-align-left', 'glyphicon-align-center',
        'glyphicon-align-right', 'glyphicon-align-justify', 'glyphicon-list', 'glyphicon-indent-left',
        'glyphicon-indent-right', 'glyphicon-facetime-video', 'glyphicon-picture', 'glyphicon-map-marker',
        'glyphicon-adjust', 'glyphicon-tint', 'glyphicon-edit', 'glyphicon-share', 'glyphicon-check',
        'glyphicon-move', 'glyphicon-step-backward', 'glyphicon-fast-backward', 'glyphicon-backward',
        'glyphicon-play', 'glyphicon-pause', 'glyphicon-stop', 'glyphicon-forward',
        'glyphicon-fast-forward', 'glyphicon-step-forward', 'glyphicon-eject', 'glyphicon-chevron-left',
        'glyphicon-chevron-right', 'glyphicon-plus-sign', 'glyphicon-minus-sign', 'glyphicon-remove-sign',
        'glyphicon-ok-sign', 'glyphicon-question-sign', 'glyphicon-info-sign', 'glyphicon-screenshot',
        'glyphicon-remove-circle', 'glyphicon-ok-circle', 'glyphicon-ban-circle', 'glyphicon-arrow-left',
        'glyphicon-arrow-right', 'glyphicon-arrow-up', 'glyphicon-arrow-down', 'glyphicon-share-alt',
        'glyphicon-resize-full', 'glyphicon-resize-small', 'glyphicon-exclamation-sign', 'glyphicon-gift',
        'glyphicon-leaf', 'glyphicon-eye-open', 'glyphicon-eye-close', 'glyphicon-warning-sign',
        'glyphicon-plane', 'glyphicon-random', 'glyphicon-comment', 'glyphicon-magnet',
        'glyphicon-chevron-up', 'glyphicon-chevron-down', 'glyphicon-retweet', 'glyphicon-shopping-cart',
        'glyphicon-folder-close', 'glyphicon-folder-open', 'glyphicon-resize-vertical',
        'glyphicon-resize-horizontal', 'glyphicon-hdd', 'glyphicon-bullhorn', 'glyphicon-certificate',
        'glyphicon-thumbs-up', 'glyphicon-thumbs-down', 'glyphicon-hand-right', 'glyphicon-hand-left',
        'glyphicon-hand-up', 'glyphicon-hand-down', 'glyphicon-circle-arrow-right',
        'glyphicon-circle-arrow-left', 'glyphicon-circle-arrow-up', 'glyphicon-circle-arrow-down',
        'glyphicon-globe', 'glyphicon-tasks', 'glyphicon-filter', 'glyphicon-fullscreen',
        'glyphicon-dashboard', 'glyphicon-heart-empty', 'glyphicon-link', 'glyphicon-phone',
        'glyphicon-usd', 'glyphicon-gbp', 'glyphicon-sort', 'glyphicon-sort-by-alphabet',
        'glyphicon-sort-by-alphabet-alt', 'glyphicon-sort-by-order', 'glyphicon-sort-by-order-alt',
        'glyphicon-sort-by-attributes', 'glyphicon-sort-by-attributes-alt', 'glyphicon-unchecked',
        'glyphicon-expand', 'glyphicon-collapse-down', 'glyphicon-collapse-up', 'glyphicon-log-in',
        'glyphicon-flash', 'glyphicon-log-out', 'glyphicon-new-window', 'glyphicon-record',
        'glyphicon-save', 'glyphicon-open', 'glyphicon-saved', 'glyphicon-import', 'glyphicon-export',
        'glyphicon-send', 'glyphicon-floppy-disk', 'glyphicon-floppy-saved', 'glyphicon-floppy-remove',
        'glyphicon-floppy-save', 'glyphicon-floppy-open', 'glyphicon-credit-card', 'glyphicon-transfer',
        'glyphicon-cutlery', 'glyphicon-header', 'glyphicon-compressed', 'glyphicon-earphone',
        'glyphicon-phone-alt', 'glyphicon-tower', 'glyphicon-stats', 'glyphicon-sd-video',
        'glyphicon-hd-video', 'glyphicon-subtitles', 'glyphicon-sound-stereo', 'glyphicon-sound-dolby',
        'glyphicon-sound-5-1', 'glyphicon-sound-6-1', 'glyphicon-sound-7-1', 'glyphicon-copyright-mark',
        'glyphicon-registration-mark', 'glyphicon-cloud-download', 'glyphicon-cloud-upload',
        'glyphicon-tree-conifer', 'glyphicon-tree-deciduous', 'glyphicon-briefcase', 'glyphicon-calendar',
        'glyphicon-pushpin', 'glyphicon-paperclip', 'glyphicon-camera', 'glyphicon-lock', 'glyphicon-bell',
        'glyphicon-bookmark', 'glyphicon-fire', 'glyphicon-wrench'
    ]);

    let dialog;
    let form;
    let config = {};
    let editing = null;
    /** The colour kind's value to save, kept up to date from the picker's coloris:pick events. */
    let picked = '';

    /**
     * Turns an icon value into words, for the choice's tooltip and for screen readers:
     * glyphicon-map-marker becomes "Map marker", empty.gif becomes "Empty".
     *
     * @param {string} name the icon value
     * @returns {string} the icon's name in words
     */
    function readableIconName(name) {
        const words = name.replace(/^glyphicon-/, '').replace(/\.[a-z]+$/, '').replace(/-/g, ' ').trim();
        return words.charAt(0).toUpperCase() + words.slice(1);
    }

    /**
     * Fills each colour swatch on the page with its data-colour. Only #rrggbb and #rrggbbaa are
     * applied, so a stored value cannot inject other CSS; a swatch with any other value shows only
     * its checks.
     */
    function paintSwatches() {
        document.querySelectorAll('.item-style-swatch').forEach(function (swatch) {
            if (COLOUR.test(swatch.dataset.colour)) {
                swatch.style.setProperty('--item-style-swatch-colour', swatch.dataset.colour);
            }
        });
    }

    /**
     * Lists the colours the page already uses (its swatches' data-colour), once each.
     *
     * @returns {string[]} the colours in use, in page order, in lower case
     */
    function coloursInUse() {
        const colours = [];
        document.querySelectorAll('.item-style-swatch').forEach(function (swatch) {
            const colour = (swatch.dataset.colour || '').toLowerCase();
            if (COLOUR.test(colour) && colours.indexOf(colour) === -1) {
                colours.push(colour);
            }
        });
        return colours;
    }

    /**
     * Builds the colour picker (Coloris) into its host in the dialog: inline, so it stays inside the
     * modal, with transparency, and the presets followed by the colours in use under their own
     * heading. Coloris keeps one picker per page and is configured again on each open.
     */
    function setUpPicker() {
        const host = form.querySelector('.item-style-editor-picker');
        const inUse = coloursInUse();
        Coloris({
            parent: host,
            inline: true,
            alpha: true,
            format: 'hex',
            swatches: PRESETS.concat(inUse),
            a11y: {
                input: host.dataset.valueLabel,
                hueSlider: host.dataset.hueLabel,
                alphaSlider: host.dataset.opacityLabel,
                instruction: host.dataset.areaLabel,
                marker: host.dataset.markerLabel
            }
        });
        const picker = host.querySelector('.clr-picker');
        const opacity = picker.querySelector('#clr-alpha-slider');

        // Its fields would otherwise post with the form; the value posts through the value field.
        picker.querySelectorAll('[name]').forEach(function (field) {
            field.removeAttribute('name');
        });
        // Coloris writes this label as HTML, so it is set here, as text.
        picker.querySelector('#clr-swatch-label').textContent = host.dataset.swatchLabel;
        // Its preview doubles as a close button for a pop-up picker; inline there is nothing to close.
        const close = picker.querySelector('#clr-close');
        close.tabIndex = -1;
        close.setAttribute('aria-hidden', 'true');
        // The presets fill whole rows, so the colours in use start a row of their own.
        if (inUse.length > 0) {
            const heading = document.createElement('p');
            heading.className = 'item-style-editor-in-use';
            heading.textContent = host.dataset.inUseLabel;
            picker.querySelector('#clr-swatch-' + PRESETS.length).before(heading);
        }

        document.addEventListener('coloris:pick', function (event) {
            // An emptied value box picks blank, which only a clearable colour may save.
            if (event.detail.color || config.colour.clearable) {
                picked = event.detail.color;
            }
        });

        host.addEventListener('pointerdown', function (event) {
            // Coloris measures the colour area only when told to, and the dialog moves and scrolls.
            Coloris.updatePosition();
            // Choosing a shade or hue of a fully see-through colour would show nothing, so it turns solid.
            if (opacity.value === '0' && event.target.closest('.clr-gradient, .clr-hue')) {
                opacity.value = '100';
                opacity.dispatchEvent(new Event('input', {bubbles: true}));
            }
        }, true);

        // Coloris holds Tab inside the picker, which would keep the keyboard from the dialog's
        // buttons; Tab moves on as usual here, with Coloris's focus rings still shown.
        host.addEventListener('keydown', function (event) {
            if (event.key === 'Tab') {
                event.stopPropagation();
                picker.classList.add('clr-keyboard-nav');
            }
        });
    }

    /**
     * Builds one icon choice button.
     *
     * @param {Object} iconSet the icon set descriptor the choice belongs to
     * @param {string} name the icon value to save
     * @param {string} [notInSetLabel] caption marking the current icon when it is outside the set
     * @returns {HTMLButtonElement} the choice button
     */
    function buildChoice(iconSet, name, notInSetLabel) {
        const button = document.createElement('button');
        button.type = 'button';
        button.className = 'btn btn-light item-style-editor-choice';
        button.dataset.icon = name;
        button.title = readableIconName(name);
        button.setAttribute('aria-label', button.title);
        button.setAttribute('aria-pressed', 'false');

        if (iconSet.kind === 'image') {
            const img = document.createElement('img');
            img.src = iconSet.base + name;
            img.alt = '';
            button.appendChild(img);
        } else {
            const glyph = document.createElement('span');
            glyph.className = 'glyphicon ' + name;
            glyph.setAttribute('aria-hidden', 'true');
            button.appendChild(glyph);
        }

        if (notInSetLabel) {
            const caption = document.createElement('small');
            caption.className = 'd-block text-muted';
            caption.textContent = notInSetLabel;
            button.classList.add('item-style-editor-choice-current');
            button.appendChild(caption);
        }
        return button;
    }

    /**
     * Marks one icon choice as the selected one.
     *
     * @param {string} name the icon to select, or blank for none
     */
    function selectIcon(name) {
        form.querySelectorAll('.item-style-editor-choice').forEach(function (choice) {
            choice.setAttribute('aria-pressed', String(choice.dataset.icon === name));
        });
    }

    /**
     * How each kind fills its control from the current value and reads the value to save back.
     * The pressed icon choice is the icon kind's state, and picked the colour kind's; neither has
     * a field of its own. An optional opened runs once the dialog shows.
     */
    const KINDS = Object.freeze({
        description: {
            dispatch: 'updateDescription',
            load: function (current, kindConfig) {
                const input = form.querySelector('.item-style-editor-description');
                input.maxLength = kindConfig.maxLength || 255;
                input.value = current;
            },
            read: function () {
                return form.querySelector('.item-style-editor-description').value.trim();
            }
        },
        colour: {
            dispatch: 'updateColour',
            load: function (current) {
                picked = COLOUR.test(current) ? current : '';
            },
            // Coloris measures the picker as it is configured, so it is set once the dialog shows.
            opened: function () {
                Coloris({inline: true, defaultColor: picked || NO_COLOUR});
            },
            read: function () {
                return picked;
            }
        },
        icon: {
            dispatch: 'updateIcon',
            load: function (current, kindConfig) {
                const grid = form.querySelector('.item-style-editor-icons');
                const names = kindConfig.iconSet.names;
                const choices = names.map(function (name) {
                    return buildChoice(kindConfig.iconSet, name);
                });
                if (current && names.indexOf(current) === -1) {
                    choices.unshift(buildChoice(kindConfig.iconSet, current, grid.dataset.notInSetLabel));
                }
                grid.replaceChildren(...choices);
                selectIcon(current);
            },
            read: function () {
                const pressed = form.querySelector('.item-style-editor-choice[aria-pressed="true"]');
                return pressed ? pressed.dataset.icon : '';
            }
        }
    });

    /**
     * Opens the dialog for one item and kind. Kinds the page did not configure are ignored.
     *
     * @param {string} kind description, colour or icon
     * @param {string} id the item id to save against
     * @param {string} current the item's current value for this kind
     */
    function openEditor(kind, id, current) {
        if (!Object.hasOwn(KINDS, kind) || !Object.hasOwn(config, kind)) {
            return;
        }
        // Disabled as well as hidden, or the hidden description's required check blocks every save.
        form.querySelectorAll('fieldset[data-kind]').forEach(function (fieldset) {
            fieldset.hidden = fieldset.disabled = fieldset.dataset.kind !== kind;
        });
        // The visible field's label names the dialog for screen readers.
        dialog.setAttribute('aria-labelledby', 'itemStyleEditorTitle-' + kind);
        dialog.dataset.kind = kind;
        form.querySelector('[data-action="clear"]').hidden = !config[kind].clearable;
        KINDS[kind].load(current, config[kind]);

        editing = {kind: kind, id: id, initial: KINDS[kind].read()};
        dialog.showModal();
        if (window.frameElement) {
            fitToVisibleArea();
            window.parent.addEventListener('scroll', fitToVisibleArea);
            window.parent.addEventListener('resize', fitToVisibleArea);
        }
        if (KINDS[kind].opened) {
            KINDS[kind].opened();
        }
    }

    /**
     * Centres the dialog in the part of its iframe the browser window shows, and caps its height to
     * that part. A modal dialog centres itself in its own window, but the admin page loads settings
     * pages in an iframe that can be taller than the browser window, which left the buttons below it.
     */
    function fitToVisibleArea() {
        const GAP = 16;
        const box = window.frameElement.getBoundingClientRect();
        const visibleTop = Math.max(0, -box.top);
        const visibleBottom = Math.min(window.innerHeight, window.parent.innerHeight - box.top);
        dialog.style.top = (visibleTop + GAP) + 'px';
        dialog.style.bottom = Math.max(0, window.innerHeight - visibleBottom + GAP) + 'px';
        dialog.style.setProperty('--item-style-editor-room', Math.max(0, visibleBottom - visibleTop - 2 * GAP) + 'px');
    }

    /**
     * Stops following the parent page's scrolling and resizing once the dialog closes.
     */
    function stopFitting() {
        if (window.frameElement) {
            window.parent.removeEventListener('scroll', fitToVisibleArea);
            window.parent.removeEventListener('resize', fitToVisibleArea);
        }
    }

    /**
     * Posts the value for the item being edited.
     *
     * @param {string} value the value to save; blank clears it
     */
    function save(value) {
        form.elements.namedItem('ID').value = editing.id;
        form.elements.namedItem('dispatch').value = KINDS[editing.kind].dispatch;
        form.elements.namedItem('value').value = value;
        form.submit();
    }

    /**
     * Saves the edited value once the browser has validated it, or just closes when nothing changed.
     *
     * @param {SubmitEvent} event the form's submit event
     */
    function onSubmit(event) {
        event.preventDefault();
        const value = KINDS[editing.kind].read();
        if (value === editing.initial) {
            dialog.close();
        } else {
            save(value);
        }
    }

    /**
     * Wires the dialog and the page's edit triggers. Call once, after the dialog markup.
     *
     * @param {Object} options one entry per kind the page edits: description, colour, icon
     * @param {boolean} [options.<kind>.clearable] whether to offer Clear, which saves a blank value
     * @param {number} [options.description.maxLength] the description column's width
     * @param {Object} options.icon.iconSet the icon set descriptor: {kind: 'image', base, names} or
     *     {kind: 'glyphicon', names}
     */
    function init(options) {
        dialog = document.getElementById('itemStyleEditor');
        form = dialog.querySelector('form');
        config = options;

        if (Object.hasOwn(config, 'colour')) {
            // Coloris builds its picker once the page has loaded.
            Coloris.ready(setUpPicker);
        }

        form.addEventListener('submit', onSubmit);
        dialog.addEventListener('close', stopFitting);
        form.querySelector('[data-action="clear"]').addEventListener('click', function () {
            save('');
        });
        form.querySelector('[data-action="cancel"]').addEventListener('click', function () {
            dialog.close();
        });
        form.querySelector('.item-style-editor-icons').addEventListener('click', function (event) {
            const choice = event.target.closest('.item-style-editor-choice');
            if (choice) {
                selectIcon(choice.dataset.icon);
            }
        });
        // A drag from a field that ends on the backdrop clicks the dialog in some browsers, which
        // would lose what was typed, so only a click that also started on the backdrop closes it.
        let pressedOnBackdrop = false;
        dialog.addEventListener('pointerdown', function (event) {
            pressedOnBackdrop = event.target === dialog;
        });
        dialog.addEventListener('click', function (event) {
            if (event.target === dialog && pressedOnBackdrop) {
                dialog.close();
            }
        });
        document.addEventListener('click', function (event) {
            const trigger = event.target.closest('[data-item-style-edit]');
            if (trigger) {
                event.preventDefault();
                openEditor(trigger.dataset.itemStyleEdit, trigger.dataset.itemId, trigger.dataset.current || '');
            }
        });
    }

    window.ItemStyleEditor = Object.freeze({
        init: init,
        readableIconName: readableIconName,
        paintSwatches: paintSwatches,
        GLYPHICONS: GLYPHICONS
    });
})(window, document);
